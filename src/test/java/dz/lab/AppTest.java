package dz.lab;


import com.sun.net.httpserver.HttpServer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
@Ignore
public class AppTest {
    public static boolean fail = false;

    @Before
    public void setup() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.start();
    }
    /**
     * Rigourous Test :-)
     */
    @Test
    public void testApp()
    {
        assertTrue(!fail);
        fail = true;
    }
}
