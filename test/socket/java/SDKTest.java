package socket.java;

import jdk.nashorn.api.scripting.JSObject;
import socket.java.callback.ConnectCallback;
import socket.java.callback.RequestCallback;

import java.io.*;
import java.nio.charset.Charset;
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
            public void onSuccess() {
                sendRequest(instance, "000000");
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    String send = br.readLine();
                    sendRequest(instance, send);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String err) {

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

    private static void sendRequest(Core client, String content) {
        Map<String, String> headers = new HashMap<>();
        headers.put("api", "/api/php/info");
        client.addRequest((content).getBytes(Charset.forName("UTF-8")), headers, new RequestCallback() {
            @Override
            public void onSuccess(byte[] data) {
                System.out.println("onSuccess---" + new String(data));
            }

            public void onFailed(String error) {
                System.out.println("request failed---" + error);
            }

            public void onComplete() {
                System.out.println("request <null, long header> complete");
            }
        });
    }

}