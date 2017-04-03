package dz.lab.jraft.model;

/**
 * The result to sender's RequestVote RPC.
 */
public class RequestVoteResult {

  public final long term;
  public final boolean voteGranted;

  public RequestVoteResult(long term, boolean voteGranted) {
    this.term = term;
    this.voteGranted = voteGranted;
  }

  public long getTerm() {
    return term;
  }

  public boolean isVoteGranted() {
        return voteGranted;
    }
}
