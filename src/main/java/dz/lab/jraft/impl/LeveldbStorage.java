package dz.lab.jraft.impl;

import dz.lab.jraft.Storage;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;

import static org.fusesource.leveldbjni.JniDBFactory.*;

/**
 * An implementation of {@link Storage} based on LevelDB.
 */
public class LeveldbStorage implements Storage {

  private static final Logger LOG = LoggerFactory.getLogger(LeveldbStorage.class);

  private DB db;
  private final String filename;

  public LeveldbStorage()
  {
    this(System.getProperty("java.io.tmpdir") + File.separator + "logs.leveldb");
  }

  public LeveldbStorage(String filename)
  {
    this.filename = filename;
  }

  public boolean init() {
    return true;
  }

  public boolean start() {
    Options options = new Options();
    options.createIfMissing(true);
    try {
      this.db = factory.open(new File(filename), options);
    } catch (IOException ioe) {
      LOG.warn("Failed to open database", ioe);
      return false;
    }
    return true;
  }

  public boolean stop() {
    try
    {
      db.close();
      db = null;
    }
    catch (IOException ioe)
    {
      LOG.warn("Failed to close database", ioe);
      return false;
    }
    return true;
  }

  /**
   * TODO serialize the key and value
   * @param key
   * @param value
   * @return
   */
  public boolean store(Object key, Object value) {
    db.put(bytes("Tampa"), bytes("rocks"));
    return true;
  }

  /**
   * TODO serialize the key
   * @param key
   * @return
   */
  public Object retrieve(Object key) {
    return db.get(bytes("Tampa"));
  }
}
