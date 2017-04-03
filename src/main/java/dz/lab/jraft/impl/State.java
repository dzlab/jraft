package dz.lab.jraft.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import dz.lab.jraft.model.LogEntry;
import dz.lab.jraft.model.Types.*;

/**
 * A placeholder for information about RaftServiceImpl's state.
 */
public class State {

  /**
   * server id
   */
  final String id;
 
  /**
   * server current role
   */
  ServerRole currentRole;
  /**
   * list of server ids from which a vote was received.
   */
  Set<String> voterIds;

  ///// Persistent state /////
  /**
   * latest term server has seen
   */
  long currentTerm;
  /**
   * candidateId that received vote in current term (or null if none)
   */
  String votedFor;
  /**
   * log entries
   */
  List<LogEntry> logs;
  /** Persistent state */

  ///// Volatile state /////
  /**
   * index of highest log entry known to be committed (initialized to 0, increases monotonically)
   */
  long commitIndex;
  /**
   * index of highest log entry applied to state machine (initialized to 0, increases monotonically).
   * Entry committed if known to be stored on majority of servers.
   */
  long lastApplied;
  ///// Volatile state /////

  ///// Volatile state (LEADER) /////
  /**
   * for each server, index of the next log entry to send to that server (initialized to leader last log index + 1)
   */
  int[] nextIndex;
  /**
   * for each server, index of highest log entry known to be replicated on server (initialized to 0, increases monotonically)
   */
  int[] matchIndex;
  ///// Volatile state (LEADER) /////


  public State(String id) {
    this(id, ServerRole.Follower, 0, null);
  }

  public State(String id, ServerRole role, int term, String voted) {
    this(id, role, new HashSet<String>(), term, voted, new ArrayList<LogEntry>());
  }

  public State(String id, ServerRole role, Set<String> voters, int term, String voted) 
  {
    this(id, role, voters, term, voted, new ArrayList<LogEntry>());
  }

  public State(String id, ServerRole role, Set<String> voters, int term, String voted, List<LogEntry> logs) {
    this.id = id;
    this.currentRole = role;
    this.voterIds = voters;
    this.currentTerm = term;
    this.votedFor = voted;
    this.logs = logs;
  }

  /**
   * Become a {@link ServerRole#Candidate} and change the state accordingly.
   */
  public synchronized void becomeCandidate()
  {
    // update role
    this.currentRole = ServerRole.Candidate;
    // increment current term
    this.currentTerm++;
    // vote for self
    this.voterIds.clear();
    this.voterIds.add(id);
  }

  /**
   * @return current server state.
   */
  public ServerRole getRole() {
    return this.currentRole;
  }

  /**
   * Set current server state.
   */
  public synchronized void setRole(ServerRole newRole) 
  {
    this.currentRole = newRole;
  }

  /**
   * @return current server term.
   */
  public long getTerm() {
    return this.currentTerm;
  }

  /**
   * @return current candidateId for which this server has voted.
   */
  public String getVotedFor() {
    return this.votedFor;
  }

  /**
   * @return current server logs.
   */
  public List<LogEntry> getLogs() {
    return new CopyOnWriteArrayList<LogEntry>(this.logs);
  }

  /**
   * @param index entry's indexi (first index at 1).
   * @return the {@link LogEntry} at the given index, or <code>null</code> if entry does not exist.
   */ 
  public LogEntry getEntryAt(long index) {
    if(index > this.logs.size()) {
      return null;
    }
    return this.logs.get((int)index - 1);
  }

  public Set<String> getVoterIds() {
    return new CopyOnWriteArraySet<String>(this.voterIds);
  }

  public void addVoter(String id) {this.voterIds.add(id);}

  /**
   * Append any new entries not already in the log.
   * If an existing entry conflicts with a new one (same index
   * but different terms), delete the existing entry and all that
   * follow it.
   */
  public void append(List<LogEntry> entries) {
    for(LogEntry newEntry: entries) {
      LogEntry oldEntry = getEntryAt(newEntry.getIndex());
      if(oldEntry == null) 
      {
        logs.add(newEntry);
      }
      else if(oldEntry.getTerm() != newEntry.getTerm())
      {
        removeAllFrom(newEntry.getIndex());
        logs.add(newEntry);
      }
      lastApplied = newEntry.getIndex();
    }
  }

  /**
   * Remove all entry with index at least equal or superior to the provided index.
   * @param index the minimum index
   */
  private void removeAllFrom(int index) {
    Set<Integer> indexes = new HashSet<Integer>();
    // look for entries
    for(int i=0; i<this.logs.size(); i++) {
      if(logs.get(i).getIndex() >= index) {
        indexes.add(Integer.valueOf(i));
      }
    }
    // remove the entries
    for(Integer i: indexes) {
      this.logs.remove((int) i);
    }
  }
}
