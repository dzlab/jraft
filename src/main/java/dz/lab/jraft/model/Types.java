package dz.lab.jraft.model;


public class Types 
{
  /**
   * Possible RaftServiceImpl role.
   */
  public enum ServerRole {
    Follower, Candidate, Leader
  }

  /**
   * Type of a message.
   */
  public enum MessageType {
    REQUEST, RESPONSE
  }
}
