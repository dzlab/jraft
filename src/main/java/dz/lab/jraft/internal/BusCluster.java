package dz.lab.jraft.internal;

import dz.lab.jraft.RaftServer;
import dz.lab.jraft.Storage;
import dz.lab.jraft.common.Configuration;
import dz.lab.jraft.common.Constants;
import dz.lab.jraft.common.Metrics;
import dz.lab.jraft.impl.LeveldbStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.UUID;

import static java.lang.String.format;

/**
 * A mini Raft cluster that uses an event bus as message transport layer between servers.
 */
public class BusCluster {

  private final Configuration config;
  private final List<RaftServer> servers;
  private final List<Timer> timers;
  private final List<Storage> storages;

  public BusCluster()
  {
    this.config = new Configuration();
    this.servers = new ArrayList<RaftServer>();
    this.timers = new ArrayList<Timer>();
    this.storages = new ArrayList<Storage>();
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
      this.timers.add(timer);
      String filename = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID() + ".leveldb";
      Storage storage = new LeveldbStorage(filename);
      storage.init();
      storages.add(storage);
      RaftServer server = BusServer.create(bus, config, format("server-%3d", i), storage, timer);
      server.init();
      this.servers.add(server);
    }
  }

  /**
   * Start this cluster and all its associated resources.
   */
  public void start()
  {
    Metrics.getInstance().start();
    for(int i=0; i<servers.size(); i++)
    {
      Storage storage = this.storages.get(i);
      storage.start();
      RaftServer server = this.servers.get(i);
      server.start();
    }
  }

  /**
   * Stop this cluster and all its associated resources.
   */
  public void stop()
  {
    for(int i=0; i<servers.size(); i++)
    {
      RaftServer server = this.servers.get(i);
      server.stop();
      Storage storage = this.storages.get(i);
      storage.stop();
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
    this.storages.clear();
    config.unset(Constants.CLUSTER_SIZE);
  }

}
