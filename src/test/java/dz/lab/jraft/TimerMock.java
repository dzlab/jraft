package dz.lab.jraft;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

/**
 * A mock for {@link Timer} to have more control on task scheduling in tests.
 */
public class TimerMock extends Timer
{

  protected static final Logger LOG = Logger.getLogger(TimerMock.class.getSimpleName());

  private final Collection<TimerTaskWrapper> tasks;

  public TimerMock() {
        this.tasks = new ArrayBlockingQueue<TimerTaskWrapper>(100);
    }

  @Override
  public void schedule(TimerTask task, long delay) {
        tasks.add(new TimerTaskWrapper(task, delay, 0));
    }

  @Override
  public void schedule(TimerTask task, long delay, long period) {
    tasks.add(new TimerTaskWrapper(task, delay, period));
  }

  /**
   * Increment time by X. Note that incrementing by multiples of delay or period time will NOT execute multiple times.
   * <p>
   * You must call incrementTime multiple times each increment being larger than 'period' on subsequent calls to cause multiple executions.
   * <p>
   * This is because executing multiple times in a tight-loop would not achieve the correct behavior, such as batching, since it will all execute "now" not after intervals of time.
   *
   * @param timeInMilliseconds
   */
  public void incrementTime(int timeInMilliseconds)
  {
    LOG.info("number of tasks: "+tasks.size());
    for (TimerTaskWrapper t : tasks)
    {
      if(t.isCanceled())
      {
        tasks.remove(t);
      }
      else
      {
        t.incrementTime(timeInMilliseconds);
      }
    }
  }

  /**
   * A wrapper around {@link TimerTask}.
   */
  private static class TimerTaskWrapper
  {
    // the wrapped task
    private final TimerTask task;
    // delay before first execution
    private final long delay;
    // period of time between two subsequentive executions
    private final long period;

    // relative time that we'll use
    volatile int time = 0;
    // task execution count
    volatile int executionCount = 0;

    public TimerTaskWrapper(TimerTask task, long delay, long period)
    {
      this.task = task;
      this.delay = delay;
      this.period = period;
    }

    /**
     * Check of the wrapped task is cancelled.
     * @return <code>true</code> if underlying {@link TimerTask} has state CANCELLED, <code>false</code> otherwise.
     */
    public boolean isCanceled()
    {
      try
      {
        Field f = TimerTask.class.getDeclaredField("state");
        f.setAccessible(true);
        int state = f.getInt(task);
        return state == 3;
      }
      catch (Exception e)
      {
        e.printStackTrace();
        return false;
      }
    }

    /**
     * Advance the current time and check if OK to executed the wrapped task.
     * @param timeInMilliseconds
     */
    public synchronized void incrementTime(int timeInMilliseconds)
    {
      time += timeInMilliseconds;
      if (task != null) {
        if (executionCount == 0)
        {
          LOG.info("ExecutionCount 0 => Time: " + time + " Delay: " + delay);
          if (time >= delay)
          {
            // first execution, we're past the delay time
            executeTask();
          }
        }
        else
        {
          LOG.info("ExecutionCount 1+ => Time: " + time + " Period: " + period);
          if (period > 0 && time >= period)
          {
            // subsequent executions, we're past the interval time
            executeTask();
          }
        }
      }
    }

    private synchronized void executeTask()
    {
      LOG.info("Executing task ...");
      task.run();
      this.time = 0; // we reset time after each execution
      this.executionCount++;
      LOG.info("executionCount: " + executionCount);
    }
  }

}
