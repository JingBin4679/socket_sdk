package socket.java;

import socket.java.utils.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Core {

    //output thread
    private static final ExecutorService OUTPUT_SERVICE = Executors.newFixedThreadPool(1);

    //input thread
    public static final ExecutorService INPUT_SERVCIE = Executors.newFixedThreadPool(1);


    public static final ExecutorService SOCKET_HEARTBEAT_SERVICE = Executors.newCachedThreadPool();

    private String _host;
    private int _port;


    private static Core instance;
    private NotifyCallback _notifyCallback = NotifyCallback.DEFAULT_NOTIFY_CALLBACK;
    private ConnectCallback _connectCallback = ConnectCallback.DEFAULT_CONNECT_CALLBACK;

    private Core() {
    }

    public static synchronized Core getInstance() {    //对获取实例的方法进行同步
        if (instance == null) {
            synchronized (Core.class) {
                if (instance == null)
                    instance = new Core();
            }
        }
        return instance;
    }

    public void setConnectPara(String host, int port, ConnectCallback connectCallback) {
        this._host = host;
        this._port = port;
        if (connectCallback == null) {
            this._connectCallback = ConnectCallback.DEFAULT_CONNECT_CALLBACK;
            return;
        }
        this._connectCallback = connectCallback;

    }

    public void setNotifyCallback(NotifyCallback callback) {
        if (callback == null) {
            this._notifyCallback = NotifyCallback.DEFAULT_NOTIFY_CALLBACK;
            return;
        }
        this._notifyCallback = callback;
    }

    public void start() {
        if (StringUtils.isEmpty(_host) || _port <= 0) {
            throw new IllegalArgumentException("Set host and post first !!!");
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(_host, _port), Config.connection_timeout);
                    socket.setKeepAlive(true);
                } catch (IOException e) {
                    Log.e(e.getLocalizedMessage());
                    _connectCallback.onError(e.getLocalizedMessage());
                    return;
                }
                //TODO 链接成功
                Log.e("connect success !!!");
            }
        };
        SOCKET_HEARTBEAT_SERVICE.execute(runnable);
    }
}