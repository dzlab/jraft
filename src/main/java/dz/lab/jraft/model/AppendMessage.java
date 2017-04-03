package dz.lab.jraft.model;

import dz.lab.jraft.Message;

/**
 * A message representing an append entries request or response.
 */
public class AppendMessage extends Message {

  public AppendMessage(Types.MessageType type, String sourceId, String destinationId, Object body)
  {
    super(type, sourceId, destinationId, body);
  }
}
