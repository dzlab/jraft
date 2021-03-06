package dz.lab.jraft;

import dz.lab.jraft.model.LogEntry;
import dz.lab.jraft.model.RequestVoteResult;
import dz.lab.jraft.model.AppendEntriesResult;

import java.util.List;

/**
 *
 */
public interface RaftService extends Lifecycle {

  RequestVoteResult requestVote(String candidateId, long term, long lastLogIndex, long lastLogTerm);

  void receiveVote(String id, long term, boolean voteGranted);

  AppendEntriesResult appendEntries(long term, String leaderId, long prevLogIndex, long prevLogTerm, List<LogEntry> entries, long leaderCommit);
}
