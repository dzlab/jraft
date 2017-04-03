package dz.lab.jraft.internal;

import dz.lab.jraft.Message;
import dz.lab.jraft.MessageHandler;
import dz.lab.jraft.RaftServer;
import dz.lab.jraft.RaftService;
import dz.lab.jraft.common.Configuration;
import dz.lab.jraft.impl.AppendMessageHandler;
import dz.lab.jraft.impl.RaftServiceImpl;
import dz.lab.jraft.impl.VoteMessageHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import static java.lang.String.format;

/**
 * An event bus based implementation of {@link RaftServer}.
 */
public class BusServer implements RaftServer {
  private final Bus bus;
  private RaftService service;
  private final String id;
  private volatile boolean started;

  private final List<MessageHandler> handlers;

  public BusServer(String id, Bus bus) {
    this.id = id;
    this.bus = bus;
    this.started = false;
    this.handlers = new ArrayList<MessageHandler>();
  }

  public void setService(RaftService service)
  {
    this.service = service;
  }

  public void addHandler(MessageHandler handler)
  {
    this.handlers.add(handler);
  }

  /**
   * Initialise this server and its underlying components.
   * @return <code>true</code> if successfully initialized, <code>false</code> otherwise.
   */
  public synchronized boolean init()
  {
    return this.service.init();
  }

  /**
   * Start this server and subscribe to the event bus.
   */
  public synchronized boolean start()
  {
    if(this.bus==null || this.started) return false;
    this.started = false;
    boolean started = this.bus.subscribe(this);
    if(started) {
      started = this.service.start();
    }
    return started;
  }

  /**
   * Stop this server and un-subscribe from the event bus.
   */
  public synchronized boolean stop()
  {
    if(!this.started) return false;
    this.started = false;
    boolean stopped = this.service.stop();
    if(stopped) {
      stopped = this.bus.unsubscribe(this);
    }
    return stopped;
  }

  public String getId() {
        return this.id;
    }

  public void send(Message msg) {
        bus.publish(msg);
    }

  /**
   * Handle a message.
   * @param msg the received message
   */
  public void receive(Message msg)
  {
    for(MessageHandler handler: this.handlers)
    {
      if(!handler.canHandle(msg)) continue;
      handler.handle(msg);
    }
  }

  /**
   * Factory method for creating and configuring an instance of {@link BusServer}.
   *
   * @return
   */
  public static BusServer create(Bus bus, Configuration config, String name, Timer timer)
  {
    BusServer server = new BusServer(name, bus);
    RaftService service = new RaftServiceImpl(server, timer, config);
    server.setService(service);
    // configure message handlers
    server.addHandler(new VoteMessageHandler(server, service));
    server.addHandler(new AppendMessageHandler(server, service));
    return server;
  }
}
