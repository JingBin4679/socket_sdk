package socket.java;

/**
 * Created by jingbin on 2016/12/19.
 */
public class CoreTest {

    public static void testCore() throws InterruptedException {
        Core instance = Core.getInstance();
        instance.setConnectPara("app.test.11deyi.com", 10002, new ConnectCallback() {
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

    public static void main(String[] args) {
        try {
            testCore();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}