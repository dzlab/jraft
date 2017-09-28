package dz.lab.jraft.impl;

import dz.lab.jraft.Message;
import dz.lab.jraft.MessageHandler;

/**
 *
 */
public abstract class AbsractMessageHandler<T extends Message> implements MessageHandler<T> {

  public void handle(T msg)
  {
    switch (msg.getType())
    {
      case REQUEST:
        handleRequest(msg);
        break;
      case RESPONSE:
        handleResponse(msg);
        break;
    }
  }

  /**
   * Handle a received request message.
   * @param requestMessage the vote request.
   */
  protected abstract void handleRequest(T requestMessage);

  /**
   * Handle a received response message.
   * @param responseMessage the vote response.
   */
  protected abstract void handleResponse(T responseMessage);

}
