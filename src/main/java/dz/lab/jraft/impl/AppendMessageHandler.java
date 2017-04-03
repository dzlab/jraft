package dz.lab.jraft.impl;

import dz.lab.jraft.Message;
import dz.lab.jraft.MessageHandler;
import dz.lab.jraft.RaftServer;
import dz.lab.jraft.RaftService;
import dz.lab.jraft.model.AppendMessage;

/**
 * An implementation of {@link MessageHandler} to handle {@link AppendMessage} requests and responses.
 */
public class AppendMessageHandler implements MessageHandler<AppendMessage> {

  private final RaftServer server;
  private final RaftService service;

  public AppendMessageHandler(RaftServer server, RaftService service)
  {
    this.server = server;
    this.service = service;
  }

  public boolean canHandle(Message msg) {
    return (msg instanceof AppendMessage);
  }

  public void handle(AppendMessage msg) {

  }
}
