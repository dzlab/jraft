package dz.lab.jraft;

/**
 * .
 */
public interface Storage extends Lifecycle {

  /**
   * Store a key value tuple.
   * @param key
   * @param value
   * @return <code>true</code> if operation succeeded, <code>false</code> otherwise.
   */
  boolean store(Object key, Object value);

  /**
   * Retrieve the value for a given key.
   * @param key
   * @return the found value, <code>null</code> otherwise.
   */
  Object retrieve(Object key);
}
