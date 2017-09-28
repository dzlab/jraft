package dz.lab.jraft.impl;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dz.lab.jraft.Message;
import dz.lab.jraft.RaftServer;
import dz.lab.jraft.Storage;
import dz.lab.jraft.TimerMock;
import dz.lab.jraft.common.Configuration;
import dz.lab.jraft.model.AppendEntriesResult;
import dz.lab.jraft.model.LogEntry;
import dz.lab.jraft.model.Types.*;
import dz.lab.jraft.model.RequestVoteResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static dz.lab.jraft.TestUtils.*;

/**
 *  Unit test for {@link RaftServiceImpl}.
 */
public class ServiceTest
{
  // timer mock for controlling stop/resume
  private TimerMock t;

  private final List<LogEntry> emptyLog = Collections.emptyList();
 
  @Before
  public void setup() 
  {
    t = new TimerMock();
  }
  @After
  public void teardown() 
  {
    t.cancel();
  }

  /**
   * Test inital state of servers.
   */
  @Test
  public void testInitialisation()
  {
    Storage storage = mock(Storage.class);
    Configuration conf = new Configuration(10);
    RaftServiceImpl srv = new RaftServiceImpl(new BasicServer("s1"), storage, t, conf);
    State s = srv.getState();
    assertEquals("s1", srv.getId());
    assertEquals(ServerRole.Follower, s.getRole());
    assertEquals(0, s.getTerm());
    assertNull(s.getVotedFor());
    assertEquals(0, s.getLogs().size());
  }

  /**
   * Test server transition from Follower to Candidate after timeout.
   */
  @Test
  public void testFollowerCandidate()
  {
    Storage storage = mock(Storage.class);
    Configuration conf = new Configuration(10);
    RaftServiceImpl srv = new RaftServiceImpl(new BasicServer("s1"), storage, t, conf);
    State s = srv.getState();
    srv.reset();
    // after timeout the server should become candidate
    t.incrementTime(11);
    assertEquals(ServerRole.Candidate, s.getRole());
    assertEquals(1, s.getTerm());
    assertTrue(s.getVoterIds().contains("s1"));
    // another election timeout
    t.incrementTime(11);
    assertEquals(ServerRole.Candidate, s.getRole());
    assertEquals(2, s.getTerm());
    assertEquals(1, s.getVoterIds().size());
    assertTrue(s.getVoterIds().contains("s1"));
  }

  /**
   * Test server transition from Candidate to Follower.
   */
  @Test
  public void testCandidateFollower()
  {
    Storage storage = mock(Storage.class);
    Configuration conf = new Configuration(10);
    RaftServiceImpl srv = new RaftServiceImpl(new BasicServer("s1"), storage, t, conf);
    State s = srv.getState();
    srv.reset();
    // after timeout the server should become candidate
    t.incrementTime(11);
    assertEquals(ServerRole.Candidate, s.getRole());
    assertEquals(1, s.getTerm());

    // transit to follower: after discovering current leader (receive heartbeat)
    srv.appendEntries(1,"s2", 0, 0, emptyLog, 0);
    assertEquals(ServerRole.Follower, s.getRole());
    assertEquals(1, s.getTerm());

    // after timeout the server should become candidate
    t.incrementTime(10);
    assertEquals(ServerRole.Candidate, s.getRole());
    assertEquals(2, s.getTerm());

    //transit to follower: after discovering new term (in response to sent heartbeat)
    srv.receiveVote("s3", 2, false);
    assertEquals(ServerRole.Follower, s.getRole());
  }

  /**
   * Test server transition to Leader after winning election.
   */
  @Test
  public void testCandidateLeader()
  {
    // TODO implement

  }

  /**
   * Test server transition from Leader to Follower.
   */
  @Test
  public void testLeaderFollower()
  {
    // TODO implement
    //s.setRole(ServerRole.Leader);
  }

  /**
   * Test server handling RequestVote RPC calls.
   */
  @Test
  public void testRequestVote() 
  {
    Storage storage = mock(Storage.class);
    Configuration conf = new Configuration(10);
    RaftServiceImpl srv = new RaftServiceImpl(new BasicServer("s1"), storage, t, conf);
    State s = srv.getState();
    srv.reset();
    // check term updates
    RequestVoteResult result = srv.requestVote("s2", 1, 0, 0);
    assertEquals(1, s.getTerm());
    assertEquals("s2", s.getVotedFor());
    assertTrue(result.voteGranted);

    // check stepping down from Candidate role
    t.incrementTime(13);
    assertState(new State("s1", ServerRole.Candidate, asSet("s1"), 2, "s2"), srv.getState());
    result = srv.requestVote("s3", 3, 0, 0);
    assertTrue(result.voteGranted);
    assertEquals(3, s.getTerm());
    assertEquals("s3", s.getVotedFor());
    assertEquals(ServerRole.Follower, s.getRole());

    // check candidate term is same as current
    assertState(new State("s1", ServerRole.Follower, 3, "s3"), srv.getState());
    // req from different candidate than the one voted for
    result = srv.requestVote("s4", 3, 0, 0);
    assertFalse(result.voteGranted);
    // req from same candidate as voted for
    result = srv.requestVote("s3", 3, 0, 0);
    assertTrue(result.voteGranted);

    // TODO check stepping down from Leader role
    // TODO check refusing to vote
  }

  /**
   * Test server handling AppendEntries RPC calls.
   */
  @Test
  public void testAppendEntries() 
  {
    Storage storage = mock(Storage.class);
    Configuration conf = new Configuration(10);
    RaftServiceImpl srv = new RaftServiceImpl(new BasicServer("s1"), storage, t, conf);
    State s = srv.getState();
    srv.reset();
    // check fail if term less than current
    s.currentTerm = 3;
    AppendEntriesResult result = srv.appendEntries(2, "s2", 0, 0, new ArrayList<LogEntry>(), 0);
    assertFalse(result.success);
    assertEquals(3, result.term);
    // check fail if log doesn’t contain an entry at prevLogIndex whose term matches prevLogTerm 
    s.logs = Arrays.asList(new LogEntry(1, 1, "SET A 1"), new LogEntry(1, 2, "SET B 22"));
    result = srv.appendEntries(4, "s2", 2, 2, new ArrayList<LogEntry>(), 0);
    assertFalse(result.success);
    assertEquals(3, result.term);
    // check fail when there is no entry at given index
    result = srv.appendEntries(4, "s2", 3, 2, new ArrayList<LogEntry>(), 0);
    assertFalse(result.success);
    assertEquals(3, result.term);
 
    // check removing conflicting entries
    s.logs = new ArrayList();
    s.logs.add(new LogEntry(1, 1, "SET A 1"));
    s.logs.add(new LogEntry(1, 2, "SET B 22"));
    result = srv.appendEntries(4, "s2", 2, 1, Arrays.asList(new LogEntry(2, 2, "SET B 22")), 0);
    assertTrue(result.success);
    assertEquals(3, result.term);
    assertEquals(2, s.logs.size());

    // check adding new entry
    result = srv.appendEntries(4, "s2", 2, 2, Arrays.asList(new LogEntry(3, 3, "SET C 2")), 3);
    assertTrue(result.success);
    assertEquals(3, result.term);
    assertEquals(3, s.logs.size());
    assertEquals(3, s.commitIndex);  // check update commit index
  }

  /**
   * Test server processing client requests when it has leader role.
   */
  @Test
  public void testProcess()
  {
    Storage storage = mock(Storage.class);
    Configuration conf = new Configuration(10);
    RaftServiceImpl srv = new RaftServiceImpl(new BasicServer("s1"), storage, t, conf);
    State s = srv.getState();
    srv.reset();
    boolean result;
    // check followers cannot process
    result = srv.process("dummy command");
    assertFalse("Followers cannot process client requests.", result);

    // check leader able to process command
    s.setRole(ServerRole.Leader);
    result = srv.process("dummy command");
    assertTrue("Leaders can process client requests.", result);
  }

  /**
   * A basic implementation of a RaftServer that interact with nothing.
   */
  private static class BasicServer implements RaftServer {
    final String id;
    public BasicServer(String id) {
      this.id = id;
    }
    public String getId() {
      return this.id;
    }
    public void send(Message msg) {}
    public void receive(Message msg) {}

    public boolean init() {
      return true;
    }

    public boolean start() {
      return true;
    }

    public boolean stop() {
      return true;
    }
  }
}
