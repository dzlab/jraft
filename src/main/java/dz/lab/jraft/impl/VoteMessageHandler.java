package dz.lab.jraft.impl;

import dz.lab.jraft.Message;
import dz.lab.jraft.MessageHandler;
import dz.lab.jraft.RaftServer;
import dz.lab.jraft.RaftService;
import dz.lab.jraft.model.RequestVoteResult;
import dz.lab.jraft.model.VoteMessage;

/**
 * An implementation of {@link MessageHandler} to handle {@link VoteMessage} requests and responses.
 */
public class VoteMessageHandler extends AbsractMessageHandler<VoteMessage> {

  private final RaftServer server;
  private final RaftService service;

  public VoteMessageHandler(RaftServer server, RaftService service)
  {
    this.server = server;
    this.service = service;
  }

  public boolean canHandle(Message msg)
  {
    return (msg instanceof VoteMessage);
  }

  /**
   * Handle a received vote request message.
   * @param requestMessage the vote request.
   */
  @Override protected void handleRequest(VoteMessage requestMessage)
  {
    VoteMessage.RequestBody body = (VoteMessage.RequestBody) requestMessage.getBody();
    RequestVoteResult result = this.service.requestVote(body.getCandidateId(), body.getTerm(), body.getLastLogIndex(), body.getLastLogTerm());
    // send back a response
    VoteMessage responseMessage = VoteMessage.createResponse(this.server.getId(), requestMessage.getSource(), result.getTerm(), result.isVoteGranted());
    this.server.send(responseMessage);
  }

  /**
   * Handle a received vote response message.
   * @param responseMessage the vote response.
   */
  @Override protected void handleResponse(VoteMessage responseMessage)
  {
    VoteMessage.ResponseBody body = (VoteMessage.ResponseBody) responseMessage.getBody();
    this.service.receiveVote(responseMessage.getSource(), body.getTerm(), body.isVoteGranted());
  }

}
