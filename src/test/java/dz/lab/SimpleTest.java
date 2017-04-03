package dz.lab;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by dzlab on 08/02/2017.
 */
@Ignore
public class SimpleTest {

    @Test
    public void testApp()
    {
        try {
            Thread.sleep(1000);
        }catch (Exception e) {
            fail();
        }
        assertTrue(true);
    }
}
