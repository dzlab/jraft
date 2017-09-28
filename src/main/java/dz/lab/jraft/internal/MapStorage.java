package dz.lab.jraft.internal;

import dz.lab.jraft.Storage;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple implementation of {@link Storage} that relies on Ma.
 */
public class MapStorage implements Storage {

  private Map<Object, Object> db;

  public MapStorage()
  {
  }

  public boolean init() {
    db = new HashMap<Object, Object>();
    return true;
  }

  public boolean start() {
    return true;
  }

  public boolean stop() {
    return true;
  }

  public boolean store(Object key, Object value) {
    db.put(key, value);
    return true;
  }

  public Object retrieve(Object key) {
    return db.get(key);
  }
}
