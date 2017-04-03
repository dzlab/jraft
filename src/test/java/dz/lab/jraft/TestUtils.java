package dz.lab.jraft;

import dz.lab.jraft.impl.State;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class TestUtils {

  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    }catch (Exception e) {
    } 
  }

  public static void println(Object o) {
    System.out.println(o);
  }

  /**
   * @param e expected state
   * @param c current state
   */
  public static void assertState(State e, State c) {
    assertEquals(e.getRole(), c.getRole());
    assertEquals(e.getVoterIds(), c.getVoterIds());
    assertEquals(e.getTerm(), c.getTerm());
    assertEquals(e.getVotedFor(), c.getVotedFor());
    assertEquals(e.getLogs(), c.getLogs());
  }

  public static <T> Set<T> asSet(T... s) {
    return new HashSet<T>(Arrays.asList(s));
  }
}
