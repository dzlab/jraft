package dz.lab.jraft.internal;

import dz.lab.jraft.RaftServer;
import dz.lab.jraft.RaftService;
import dz.lab.jraft.common.Configuration;
import dz.lab.jraft.common.Constants;
import dz.lab.jraft.common.Metrics;
import dz.lab.jraft.impl.RaftServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.String.format;

/**
 * A mini Raft cluster that uses an event bus as message transport layer between servers.
 */
public class BusCluster {

  private final Configuration config;
  private final List<RaftServer> servers;
  private final List<Timer> timers;

  public BusCluster()
  {
    this.config = new Configuration();
    this.servers = new ArrayList<RaftServer>();
    this.timers = new ArrayList<Timer>();
  }

  /**
   *
   * @param size size of the cluster.
   */
  public void init(int size)
  {
    Metrics.getInstance().init();
    Bus bus = Bus.getInstance();
    config.set(Constants.CLUSTER_SIZE, Integer.valueOf(size));

    for(int i=0; i<size; i++) {
      Timer timer = new Timer();
      RaftServer server = BusServer.create(bus, config, format("server-%3d", i), timer);
      server.init();
      this.servers.add(server);
      this.timers.add(timer);
    }
  }

  /**
   * Start this cluster and all its associated resources.
   */
  public void start()
  {
    Metrics.getInstance().start();
    for(RaftServer server: this.servers)
    {
      server.start();
    }
  }

  /**
   * Stop this cluster and all its associated resources.
   */
  public void stop()
  {
    for(RaftServer server: this.servers)
    {
      server.stop();
    }
    Metrics.getInstance().stop();
  }

  /**
   * Destroy this cluster and all its associated resources.
   */
  public void destroy()
  {
    for(Timer timer: this.timers) {
      timer.cancel();
    }
    this.timers.clear();
    this.servers.clear();
    config.unset(Constants.CLUSTER_SIZE);
  }

}
