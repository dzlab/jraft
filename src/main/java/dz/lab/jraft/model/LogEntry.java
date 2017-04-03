package dz.lab.jraft.model;


/**
 * Entry in RaftServiceImpl's logs.
 * Log entry = index, term, command
 */
public class LogEntry {

  /**
   * term when entry was received by leader
   */
  final int term;
  /**
   * position of entry in the log (first index is 1)
   */
  final int index;
  /**
   * command for state machine
   */
  final String command;

  public LogEntry(int term, int index, String command) {
    this.term = term;
    this.index = index;
    this.command = command;
  }

  public int getTerm() {
    return this.term;
  }

  public int getIndex() {
    return this.index;
  }
}
