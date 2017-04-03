package dz.lab.jraft;

/**
 * Interface representing a lifecycle of a component.
 */
public interface Lifecycle
{

  /**
   * Initialize this component.
   */
  boolean init();

  /**
   * Start this component.
   */
  boolean start();

  /**
   * Stop this component.
   */
  boolean stop();

}
