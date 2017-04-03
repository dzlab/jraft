package dz.lab.jraft.common;

import com.codahale.metrics.*;
import dz.lab.jraft.Lifecycle;

import java.util.concurrent.TimeUnit;

/**
 * Metrics collector.
 */
public class Metrics implements Lifecycle {
  private static Metrics ourInstance = new Metrics();

  public static Metrics getInstance() {
    return ourInstance;
  }

  private final MetricRegistry metrics;
  private final ScheduledReporter reporter;

  private Metrics() {
    this.metrics = new MetricRegistry();
    this.reporter = ConsoleReporter.forRegistry(metrics)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build();
  }

  public boolean init() {
    return true;
  }

  /**
   * Start reporting metrics to STDOUT (or whatever).
   */
  public boolean start() {
    reporter.start(1, TimeUnit.SECONDS);
    return true;
  }

  public boolean stop() {
    reporter.stop();
    return true;
  }


  /**
   * Get a new counter.
   * @param clazz
   * @param names
   * @return
   */
  public Counter getCounter(Class clazz, String... names)
  {
    return metrics.counter(MetricRegistry.name(clazz, names));
  }

  /**
   * Get a new Meter.
   * @param clazz
   * @param names
   * @return
   */
  public Meter getMeter(Class clazz, String... names)
  {
    return metrics.meter(MetricRegistry.name(clazz, names));
  }

}
