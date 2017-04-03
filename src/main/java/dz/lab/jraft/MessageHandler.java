package dz.lab.jraft;

/**
 * An interface for the message handler.
 */
public interface MessageHandler<T extends Message> {

  /**
   * Check if this handler can handle the given message type.
   * @param msg the message to check
   * @return <code>true</code> if this handler is able to process this message, <code>false</code> otherwise.
   */
  boolean canHandle(Message msg);

  /**
   * Handle the given message.
   * @param msg
   */
  void handle(T msg);
}
