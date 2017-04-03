package dz.lab.jraft.model;

import dz.lab.jraft.Message;
import dz.lab.jraft.MessageHandler;
import dz.lab.jraft.common.StringUtils;

/**
 * A message representing a vote request or response.
 */
public class VoteMessage extends Message {

  public VoteMessage(Types.MessageType type, String sourceId, String destinationId, Object body)
  {
    super(type, sourceId, destinationId, body);
  }

  /**
   * A factory method for creating a request {@link VoteMessage}.
   * @param candidateId
   * @param term
   * @param lastLogIndex
   * @param lastLogTerm
   * @return VoteMessage
   */
  public static VoteMessage createRequest(String candidateId, long term, long lastLogIndex, long lastLogTerm)
  {
    RequestBody body = new RequestBody(candidateId, term, lastLogIndex, lastLogTerm);
    return new VoteMessage(Types.MessageType.REQUEST, candidateId, null, body);
  }

  /**
   * A factory method for creating a response {@link VoteMessage}.
   * @param srcId
   * @param dstId
   * @param term
   * @param voteGranted
   * @return VoteMessage
   */
  public static VoteMessage createResponse(String srcId, String dstId, long term, boolean voteGranted)
  {
    ResponseBody body = new ResponseBody(term, voteGranted);
    return new VoteMessage(Types.MessageType.RESPONSE, srcId, dstId, body);
  }

  /**
   * Body of a RequestVote request to be sent by candidate to all servers.
   */
  public static class RequestBody
  {
    String candidateId;
    long term;
    long lastLogIndex;
    long lastLogTerm;

    public RequestBody(String candidateId, long term, long lastLogIndex, long lastLogTerm)
    {
      this.candidateId = candidateId;
      this.term = term;
      this.lastLogIndex = lastLogIndex;
      this.lastLogTerm = lastLogTerm;
    }

    public String getCandidateId() {
            return candidateId;
        }

    public long getTerm() {
            return term;
        }

    public long getLastLogIndex() {
            return lastLogIndex;
        }

    public long getLastLogTerm() {
            return lastLogTerm;
        }
  }

  /**
   * Body of a RequestVote response to candidate' request.
   */
  public static class ResponseBody
  {
    long term;
    boolean voteGranted;

    public ResponseBody(long term, boolean voteGranted)
    {
      this.term = term;
      this.voteGranted = voteGranted;
    }

    public long getTerm() {
            return term;
        }

    public boolean isVoteGranted() {
            return voteGranted;
        }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ResponseBody that = (ResponseBody) o;

      if (term != that.term) return false;
      return voteGranted == that.voteGranted;
    }

    @Override
    public int hashCode() {
      int result = (int) (term ^ (term >>> 32));
      result = 31 * result + (voteGranted ? 1 : 0);
      return result;
    }
  }
}