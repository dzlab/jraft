package dz.lab.jraft.common;

public class Constants
{
  /**
   * default min election timeout amount.
   */
  public static final long MIN_TIMEOUT = 100;
  /**
   * Default max election timeout amount.
   */
  public static final long MAX_TIMEOUT = 500;

  /**
   * Name of key for the number of servers in the cluster.
   */
  public static final String CLUSTER_SIZE = "cluster_size";
}
