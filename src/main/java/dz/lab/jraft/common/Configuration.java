package dz.lab.jraft.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static dz.lab.jraft.common.Constants.*;

/**
 *
 */
public class Configuration {

  /**
   * The default timeout to apply.
   */
  private final long defaultTimeout;
  private final Random rand;
  private final Map<String, Object> props;

  public Configuration() {
    this(-1);
  }

  public Configuration(long defaultTimeout)
  {
    this.defaultTimeout = defaultTimeout;
    this.rand = new Random();
    this.props = new HashMap<String, Object>();
  }

  /**
   * Set a configuration property.
   * @param key the property key.
   * @param value the property value.
   */
  public void set(String key, Object value)
  {
    this.props.put(key, value);
  }

  /**
   * Unset a configuration property.
   * @param key the property key.
   */
  public void unset(String key)
  {
    this.props.remove(key);
  }

  /**
   * Get a configuration property by its key.
   * @param key the key of the property.
   * @return the value if found, otherwise <code>null</code>.
   */
  public Object get(String key)
  {
    return this.props.get(key);
  }

  /**
   * Get a randomly chosen election timeout if a default timeout is not set.
   * The timeout amount should be higher than broadcast time.
   * @return a random timeout amount.
   */
  public long getRandomTimemout() {
    if(defaultTimeout > 0)
      return defaultTimeout;
    return getRandomTimemout(MIN_TIMEOUT, MAX_TIMEOUT);
  }

  /**
   * Get a random value between two bounds.
   * @param min the lower bound.
   * @param max the upper bound.
   * @return the random value.
   */
  public long getRandomTimemout(long min, long max)
  {
    return min + (long)this.rand.nextInt((int)(max - min));
  }

}
