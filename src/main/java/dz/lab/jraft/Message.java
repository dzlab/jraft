package dz.lab.jraft;

import dz.lab.jraft.common.StringUtils;
import dz.lab.jraft.model.Types;

/**
 * An object representing a message.
 */
public class Message {

  /**
   * Type of this message.
   */
  protected final Types.MessageType type;
  /**
   * ID of source server.
   */
  protected final String srcId;
  /**
   * ID of destination server.
   */
  protected final String dstId;
  /**
   * Body of the message.
   */
  protected final Object body;

  public Message(Types.MessageType type, String sourceId, String destinationId, Object body)
  {
    this.type = type;
    this.srcId = sourceId;
    this.dstId = destinationId;
    this.body = body;
  }

  public Types.MessageType getType() {
    return type;
  }

  /**
   * Get the destination of this message.
   * @return
   */
  public String getDestination() {
    return this.dstId;
  }

  /**
   * Get the source of this message.
   * @return
   */
  public String getSource() {
    return this.srcId;
  }

  /**
   * Indicate whether this message is destinated to all servers.
   * @return boolean
   */
  public boolean isBroadcast() {
    return StringUtils.isEmpty(this.dstId);
  }

  /**
   * Get the body of this message.
   * @return Object
   */
  public Object getBody() {
    return body;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Message that = (Message) o;

    if (type != that.type) return false;
    if (srcId != null ? !srcId.equals(that.srcId) : that.srcId != null) return false;
    if (dstId != null ? !dstId.equals(that.dstId) : that.dstId != null) return false;
    return body != null ? body.equals(that.body) : that.body == null;
  }

  @Override
  public int hashCode() {
    int result = type != null ? type.hashCode() : 0;
    result = 31 * result + (srcId != null ? srcId.hashCode() : 0);
    result = 31 * result + (dstId != null ? dstId.hashCode() : 0);
    result = 31 * result + (body != null ? body.hashCode() : 0);
    return result;
  }
}