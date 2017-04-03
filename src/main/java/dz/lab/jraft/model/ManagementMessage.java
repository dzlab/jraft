package dz.lab.jraft.model;

import dz.lab.jraft.Message;

/**
 * A message representing an requests or responses for cluster management (i.e. modifying configuration by adding/removing servers).
 */
public class ManagementMessage extends Message {

  public ManagementMessage(Types.MessageType type, String sourceId, String destinationId, Object body)
  {
    super(type, sourceId, destinationId, body);
  }
}
