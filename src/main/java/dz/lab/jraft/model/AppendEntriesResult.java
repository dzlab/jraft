package dz.lab.jraft.model;

/**
 * The result to sender's AppendEntries RPC.
 */
public class AppendEntriesResult {

  /**
   * currentTerm, for leader to update itself
   */
  public final long term;

  /**
   * <code>true</code> if follower contained entry matching prevLogIndex and prevLogTerm
   */
  public final boolean success;

  public AppendEntriesResult(long term, boolean success) {
    this.term = term;
    this.success = success;
  }

  public boolean isSuccess() {
    return success;
  }
}
