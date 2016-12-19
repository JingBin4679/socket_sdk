package socket.java;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jingbin on 2016/12/19.
 */
public class CoreTest {
    @org.junit.Before
    public void setUp() throws Exception {

    }


    @Test
    public void testCore() throws InterruptedException {
        Core instance = Core.getInstance();
        instance.setConnectPara("", 0, new ConnectCallback() {
            @Override
            protected void onSuccess() {
            }

            @Override
            protected void onError(String err) {

            }
        });
        instance.start();
        Thread.sleep(10000);
    }




    @org.junit.After
    public void tearDown() throws Exception {

    }

}