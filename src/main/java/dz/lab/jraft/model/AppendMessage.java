package dz.lab.jraft.model;

import dz.lab.jraft.Message;

import java.util.Collections;
import java.util.List;

/**
 * A message representing an append entries request or response.
 */
public class AppendMessage extends Message {

  public AppendMessage(Types.MessageType type, String sourceId, String destinationId, Object body)
  {
    super(type, sourceId, destinationId, body);
  }

  public static AppendMessage createRequest(long term, String leaderId, long prevLogIndex, long prevLogTerm, List<LogEntry> entries, long leaderCommit)
  {
    RequestBody body = new RequestBody(term, leaderId, prevLogIndex, prevLogTerm, entries, leaderCommit);
    return new AppendMessage(Types.MessageType.REQUEST, leaderId, null, body);
  }

  public static AppendMessage createResponse(String srcId, String dstId, long term, boolean success)
  {
    ResponseBody body = new ResponseBody(term, success);
    return new AppendMessage(Types.MessageType.RESPONSE, srcId, dstId, body);
  }

  public static class RequestBody
  {
    long term;
    String leaderId;
    long prevLogIndex;
    long prevLogTerm;
    List<LogEntry> entries;
    long leaderCommit;

    public RequestBody(long term, String leaderId, long prevLogIndex, long prevLogTerm, List<LogEntry> entries, long leaderCommit)
    {
      this.term = term;
      this.leaderId = leaderId;
      this.prevLogIndex = prevLogIndex;
      this.prevLogTerm = prevLogTerm;
      this.entries = (List<LogEntry>) Collections.unmodifiableCollection(entries);
      this.leaderCommit = leaderCommit;
    }
  }

  public static class ResponseBody
  {
    long term;
    boolean success;

    public ResponseBody(long term, boolean success)
    {
      this.term = term;
      this.success = success;
    }
  }
}
