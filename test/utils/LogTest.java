package utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kevin on 2016/12/19.
 */
public class LogTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void setSystemConsole() throws Exception {
        Log.setSystemConsole();
        Log.e("1234567890");
        Thread.sleep(1000);
        Log.i("TAG", "info -------- ");
        Thread.sleep(1000);

//        Log.closeConsole();

        Log.i("0987654321");
        Thread.sleep(1000);

        Log.e("TAG", "info -------- ");

    }

}