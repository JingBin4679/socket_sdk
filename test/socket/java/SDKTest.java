package socket.java;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jingbin on 2016/12/19.
 */
public class SDKTest {

    public static void testCore() throws InterruptedException {
        final Core instance = Core.getInstance();
        instance.setConnectPara("app.test.11deyi.com", 10002, new ConnectCallback() {
            @Override
            protected void onSuccess() {
                Map<String, String> header = new HashMap<>();
                header.put("api", "/api/php/Info");
                instance.addRequest("{\"title\":\"test\"}".getBytes(), header, new RequestCallback() {

                    @Override
                    protected void onSuccess(byte[] data) {
                        String s = new String(data);
                        System.out.print(s);
                    }

                    @Override
                    public void onFailed(String err) {
                        System.out.print(err);
                    }
                });
            }

            @Override
            protected void onError(String err) {

            }
        });
        instance.start();

        Thread.sleep(100000);
    }

    public static void main(String[] args) {
        try {
            testCore();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}