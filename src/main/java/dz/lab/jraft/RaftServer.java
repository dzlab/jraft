package dz.lab.jraft;

/**
 * Manage communication with the external world (i.e. clients and other servers).
 */
public interface RaftServer extends Lifecycle
{
  /**
   * Get the ID of this server.
   * @return String
   */
  String getId();
  /**
   * Send a message to all remote servers.
   */
  void send(Message msg);

  /**
   * Receive a message.
   */
  void receive(Message msg);

}