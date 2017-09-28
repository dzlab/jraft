package dz.lab.jraft.impl;

import dz.lab.jraft.*;
import dz.lab.jraft.common.Configuration;
import dz.lab.jraft.model.AppendMessage;
import dz.lab.jraft.model.RequestVoteResult;
import dz.lab.jraft.model.Types;
import dz.lab.jraft.model.VoteMessage;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Timer;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link dz.lab.jraft.MessageHandler} implementations.
 */
public class MessageHandlerTest {

  /**
   * Test for {@link dz.lab.jraft.MessageHandler#canHandle(Message)}
   */
  @Test
  public void testCanHandle()
  {
    // mocks
    RaftService mockService = mock(RaftService.class);
    RaftServer mockServer = mock(RaftServer.class);
    // input
    VoteMessage msg1 = new VoteMessage(Types.MessageType.REQUEST, "s1", "s2", null);
    AppendMessage msg2 = new AppendMessage(Types.MessageType.REQUEST, "s1", "s2", null);
    // vote message handler
    VoteMessageHandler vHandler = new VoteMessageHandler(mockServer, mockService);
    assertTrue("VoteMessageHandler should be able to handle VoteMessages", vHandler.canHandle(msg1));
    assertFalse("VoteMessageHandler should not be able to handle AppendMessages", vHandler.canHandle(msg2));
    // append message handler
    AppendMessageHandler aHandler = new AppendMessageHandler(mockServer, mockService);
    assertFalse("AppendMessageHandler should not be able to handle VoteMessages", aHandler.canHandle(msg1));
    assertTrue("AppendMessageHandler should be able to handle AppendMessages", aHandler.canHandle(msg2));
  }

  /**
   * Test for {@link VoteMessageHandler#handle(Message)} with messages of type request.
   */
  @Test
  public void testHandleVoteRequest()
  {
    // mocks
    RaftServer mockServer2 = mock(RaftServer.class);
    when(mockServer2.getId()).thenReturn("s2");
    RaftService mockService2 = mock(RaftService.class);
    when(mockService2.requestVote("s1", 8, 15, 7))
            .thenReturn(new RequestVoteResult(9, false));
    // input
    VoteMessage req = VoteMessage.createRequest("s1", 8, 15, 7);
    VoteMessage res = VoteMessage.createResponse("s2", "s1", 9, false);
    // action
    VoteMessageHandler vHandler2 = new VoteMessageHandler(mockServer2, mockService2);
    vHandler2.handle(req);
    // check calls
    verify(mockServer2, times(1)).send(any(Message.class));
    verify(mockServer2, times(1)).send(res);
    verify(mockService2, times(1)).requestVote(anyString(), anyLong(), anyLong(), anyLong());
  }

  /**
   * Test for {@link VoteMessageHandler#handle(Message)} with messages of type response.
   */
  @Test
  public void testHandleVoteResponse()
  {
    // mocks
    RaftServer mockServer1 = mock(RaftServer.class);
    when(mockServer1.getId()).thenReturn("s1");
    RaftService mockService1 = mock(RaftService.class);
    // input
    VoteMessage req = VoteMessage.createRequest("s1", 8, 15, 7);
    VoteMessage res = VoteMessage.createResponse("s2", "s1", 9, false);
    // action
    VoteMessageHandler vHandler2 = new VoteMessageHandler(mockServer1, mockService1);
    vHandler2.handle(res);
    // check calls
    verify(mockServer1, times(0)).send(any(Message.class));
    verify(mockService1, times(1)).receiveVote(anyString(), anyLong(), anyBoolean());
    verify(mockService1, times(1)).receiveVote("s2", 9, false);
  }

  /**
   * Test for {@link VoteMessageHandler#handle(Message)} with messages sent and reply received.
   */
  @Test
  public void testHandleVoteRequestResponse()
  {
    Storage storage = mock(Storage.class);
    Timer mockTimer = mock(Timer.class);
    // mocks RaftServer
    final RaftServer mockServer1 = mock(RaftServer.class);
    when(mockServer1.getId()).thenReturn("s1");
    final RaftServer mockServer2 = mock(RaftServer.class);
    when(mockServer2.getId()).thenReturn("s2");
    doAnswer(new RedirectMessage(mockServer2)).when(mockServer1).send(any(Message.class));
    doAnswer(new RedirectMessage(mockServer1)).when(mockServer2).send(any(Message.class));
    // mocks RaftServiceImpl
    RaftServiceImpl mockService1 = spy(new RaftServiceImpl(mockServer1, storage, mockTimer, new Configuration()));
    RaftServiceImpl mockService2 = spy(new RaftServiceImpl(mockServer2, storage, mockTimer, new Configuration()));

    // setup handlers
    final VoteMessageHandler handler1 = new VoteMessageHandler(mockServer1, mockService1);
    final VoteMessageHandler handler2 = new VoteMessageHandler(mockServer2, mockService2);
    doAnswer(new HandleMessage(handler1)).when(mockServer1).receive(any(Message.class));
    doAnswer(new HandleMessage(handler2)).when(mockServer2).receive(any(Message.class));

    // action
    RaftServiceImpl.ElectionTask electionTask = new RaftServiceImpl.ElectionTask(mockService1);
    electionTask.run();

    // verify
    verify(mockServer1, times(1)).send(any(Message.class));     // s1 send -> s2
    verify(mockServer1, times(1)).receive(any(Message.class));  // s2 -> s1 receive
    verify(mockServer2, times(1)).send(any(Message.class));     // s2 send -> s1
    verify(mockServer2, times(1)).receive(any(Message.class));  // s1 -> s2 receive
  }

  private static class RedirectMessage implements Answer<Void> {

    private final RaftServer otherServer;

    public RedirectMessage(RaftServer otherServer) {
      this.otherServer = otherServer;
    }

    public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
      Object[] args = invocationOnMock.getArguments();
      otherServer.receive((Message)args[0]);
      return null;
    }
  }
  private static class HandleMessage implements Answer<Void> {

    private final MessageHandler handler;

    public HandleMessage(MessageHandler handler) {
      this.handler = handler;
    }

    public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
      Object[] args = invocationOnMock.getArguments();
      handler.handle((Message)args[0]);
      return null;
    }
  }

}
