package dz.lab.jraft.impl;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dz.lab.jraft.Message;
import dz.lab.jraft.RaftServer;
import dz.lab.jraft.RaftService;
import dz.lab.jraft.common.Configuration;
import dz.lab.jraft.common.Metrics;
import dz.lab.jraft.model.LogEntry;
import dz.lab.jraft.model.AppendEntriesResult;
import dz.lab.jraft.model.Types.*;
import dz.lab.jraft.model.VoteMessage;
import dz.lab.jraft.model.RequestVoteResult;

/**
 * An implementation of {@link RaftService}.
 */
public class RaftServiceImpl implements RaftService
{
  // server state
  private State state;
  // lock for synchronising access to state.
  private final Object lock;

  // raft server
  private final RaftServer server;
  // timer for scheduling tasks
  private final Timer timer;
  // election timeout: time amount to wait for messages from Leader
  private final long timeout;
  // task to convert current server to Candidate
  private ElectionTask task;

  /**
   * Default constructor.
   */
  public RaftServiceImpl(RaftServer server, Timer timer, Configuration config)
  {
    assert server != null;
    assert timer != null;
    this.lock = new Object();
    this.server = server;
    this.state = new State(server.getId());
    this.timer = timer;
    this.timeout = config.getRandomTimemout();
  }

  /**
   * Initialize this service.
   */
  public boolean reset() {
    resetTimeout();
    getState().setRole(ServerRole.Follower);
    return true;
  }

  /**
   * Reset election timeout.
   */
  private void resetTimeout() 
  {
    // cancel previous task
    if(this.task != null) 
    {
      this.task.cancel();
    }
    // schedule task for becoming candidate
    this.task = new ElectionTask(this);
    if(this.timer != null)
    {
      // log warn cannot schedule task as no scheduler is defined
      this.timer.schedule(this.task, this.timeout);
    }
  }

  /**
   * @return server id;
   */
  public String getId() {
    return this.state.id;
  }

  /**
   * @return server state;
   */
  public State getState() {
    return this.state;
  }

  /**
   * Persists state to stable storage synchronously before responding to RPCs.
   */
  public void persist() {
    // TODO implement this
  }

  /**
   * Process client requests.
   * @return <code>true</code> if command processed, <code>false</code> otherwise. 
   */
  public boolean process(String command)
  {
    // TODO implement this
    if(getState().getRole() != ServerRole.Leader)
    {
      return false;
    }
    return true;
  }

  /**
   * Invoked by candidates to gather votes.
   * @param candidateId candidate requesting vote
   * @param term candidate's term
   * @param lastLogIndex index of candidate's last log entry
   * @param lastLogTerm term of candidate's last log entry
   */
  public RequestVoteResult requestVote(String candidateId, long term, long lastLogIndex, long lastLogTerm)
  {
    RequestVoteResult result;
    synchronized(this.lock) 
    {
      State s = this.state;
      result = new RequestVoteResult(s.currentTerm, false);
      if(term > s.currentTerm) 
      {
        // step down if leader or candidate
        stepDown(term);
        s.votedFor = candidateId;
        result = new RequestVoteResult(s.currentTerm, true);
      }
      else if(term == s.currentTerm) 
      {
        if((s.votedFor== null || s.votedFor=="" || s.votedFor.equals(candidateId)) && (lastLogIndex >= s.logs.size())) {
          result = new RequestVoteResult(s.currentTerm, true);
          resetTimeout();
        }
      }
    }
    Metrics.getInstance().getMeter(RaftServiceImpl.class, "votes", (result.isVoteGranted()?"granted":"ungranted"), "rate").mark();
    return result;
  }

  /**
   * Step down to the Follower role.
   * @param term the term for this server update to update itself
   */
  private void stepDown(long term) {
    this.state.currentTerm = term;
    if(this.state.currentRole != ServerRole.Follower)
    {
      this.state.currentRole = ServerRole.Follower;
      this.state.voterIds.clear();
    }
  }

  /**
   * Send vote requests to all reachable servers in the cluster.
   */
  protected void sendVoteRequests() {
    State s = this.getState();
    long lastLogIndex = s.getLogs().size();
    long lastLogTerm = lastLogIndex > 0 ? s.getEntryAt(lastLogIndex).getTerm(): 0;
    Message req = VoteMessage.createRequest(this.getId(), s.getTerm(), lastLogIndex, lastLogTerm);
    this.server.send(req);
  }

  /**
   * Invoked after receiving a response to an earlier vote request.
   * @param id the identifier of the remote server, source of this response.
   * @param term current for this server to update itself
   * @param voteGranted <code>true</code> if this server received a vote, <code>false</code> otherwise.
   */
  public void receiveVote(String id, long term, boolean voteGranted)
  {
    synchronized (this.lock) {
      State s = this.state;
      if (s.currentTerm <= term)
      {
        // step down from candidate
        stepDown(term);
        // reset election timeout
        resetTimeout();
      }
      else if(voteGranted)
      {
        // add
        s.addVoter(id);
        // check if we have majority

      }
    }
  }

  /**
   * Invoked by leader to replicate log entries, also used as heartbeat.
   * @param term leader’s term
   * @param leaderId so follower can redirect clients
   * @param prevLogIndex index of log entry immediately preceding new ones (first index is 1)
   * @param prevLogTerm term of prevLogIndex entry
   * @param entries log entries to store (empty for heartbeat; may send more than one for efficiency)
   * @param leaderCommit leader’s commitIndex
   */
  public AppendEntriesResult appendEntries(int term, String leaderId, int prevLogIndex, int prevLogTerm, List<LogEntry> entries, int leaderCommit)
  {
    AppendEntriesResult result = null;
    reset();
    synchronized(this.lock) 
    {
      State s = this.state;
      try
      {
        if(term < s.currentTerm)
        {
          result = new AppendEntriesResult(s.currentTerm, false);
          return result;
        }
        // if has at least one entry logged
        if(prevLogIndex > 0)
        {
          LogEntry entry = s.getEntryAt(prevLogIndex);
          if (entry == null || entry.getTerm() != prevLogTerm)
          {
            result = new AppendEntriesResult(s.currentTerm, false);
            return result;
          }
        }
        // follower contained entry matching prevLogIndex and prevLogTerm
        // remove any conflicting entries from the log
        s.append(entries);
        if(leaderCommit > s.commitIndex)
        {
          int lastEntryIndex = entries.get(entries.size()-1).getIndex();
          s.commitIndex = Math.min(leaderCommit, lastEntryIndex);
        }
        result = new AppendEntriesResult(s.currentTerm, true);
        return result;
      }
      finally
      {
        Metrics.getInstance().getMeter(RaftServiceImpl.class, "entries", (result.isSuccess()?"appended":"nonappended"), "rate").mark();
      }
    }
  }

  public boolean init() {
    return true;
  }

  public boolean start() {
    return reset();
  }

  public boolean stop() {
    boolean stopped = false;
    if(this.task != null)
    {
      stopped = this.task.cancel();
      this.task = null;
    }
    return stopped;
  }

  /**
   * A {@link TimerTask} implementation that moves this server state into Candidate.
   */
  public static class ElectionTask extends TimerTask
  {
    private RaftServiceImpl srv;
    public ElectionTask(RaftServiceImpl srv) {
      this.srv = srv;
    }
    @Override
    public void run() 
    {
      Metrics.getInstance().getCounter(ElectionTask.class, "elections", "count").inc();
      Metrics.getInstance().getMeter(ElectionTask.class, "elections", "rate").mark();
      /*if(this.srv == null)
      {
        cancel();
        return;
      }*/
      // become candidate
      srv.getState().becomeCandidate();
      // send parallel vote requests
      srv.sendVoteRequests();
      // reset election timeout
      srv.resetTimeout();
    }
    /*@Override
    public boolean cancel()
    {
      this.srv = null;
      return super.cancel();
    }*/
  }
}
