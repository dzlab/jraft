package dz.lab;

import dz.lab.jraft.internal.BusCluster;

/**
 * RAFT Hello world!
 *
 */
public class App {
  public static void main(String[] args) throws Exception {
    BusCluster cluster = new BusCluster();
    cluster.init(5);
    cluster.start();
    Thread.sleep(10000);
    cluster.stop();
    cluster.destroy();
  }
}
