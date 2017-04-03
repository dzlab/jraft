package dz.lab.jraft.internal;

import dz.lab.jraft.Message;
import dz.lab.jraft.common.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * An event bus to handle communication between servers.
 */
public class Bus {

  private static Bus instance;

  /**
   * Get a singleton instance of {@link Bus}.
   * @return Bus
   */
  public synchronized static Bus getInstance()
  {
    if(instance == null)
    {
      instance = new Bus();
    }
    return instance;
  }

  /**
   * List of registered servers.
   */
  List<BusServer> servers;

  private Bus()
    {
        this.servers = new ArrayList<BusServer>();
    }

  /**
   * Subscribe a server to receive messages.
   * @param server the {@link BusServer} to register for receiving events.
   */
  public boolean subscribe(BusServer server)
  {
    this.servers.add(server);
    return true;
  }

  /**
   * Un-subscribe a server to receive messages.
   * @param server the {@link BusServer} to deregister from receiving events.
   */
  public boolean unsubscribe(BusServer server)
  {
    this.servers.remove(server);
    return true;
  }

  /**
   * Publish a message
   * @param message
   * @return <code>true</code> if the message was successfully sent, <code>false</code> otherwise.
   */
  public boolean publish(Message message)
  {
    List<BusServer> targets = new ArrayList<BusServer>();
    // broadcast the message to all servers except source
    if(message.isBroadcast())
    {
      // filter out the source of the message from the targets
      for (BusServer server : servers)
      {
        if(StringUtils.equals(server.getId(), message.getSource())) continue;
        targets.add(server);
      }
    }
    else
    {
      // do nothing if addresses are empty
      if(StringUtils.isEmpty(message.getDestination()) || StringUtils.isEmpty(message.getSource())
              || StringUtils.equals(message.getSource(), message.getDestination())) {
        return false;
      }
      // find the destination server by ID
      for(BusServer server: servers)
      {
        if(StringUtils.equals(server.getId(), message.getDestination()))
        {
          targets.add(server);
          break;
        }
      }
    }
    // send the message to actual targets
    for (BusServer server : targets)
    {
      server.receive(message);
    }
    return true;
  }

}
