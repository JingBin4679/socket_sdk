package socket.java;

import socket.java.utils.StringUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Core {

    //output thread
    private static final ExecutorService OUTPUT_SERVICE = Executors.newFixedThreadPool(1);

    //input thread
    public static final ExecutorService INPUT_SERVCIE = Executors.newFixedThreadPool(1);


    public static final ExecutorService SOCKET_HEARTBEAT_SERVICE = Executors.newCachedThreadPool();

    //默认心跳时长
    private static final long TIME_LENGTH_FOR_HEARTBEAT = TimeUnit.MINUTES.toMillis(1);

    private String _host;
    private int _port;


    private static Core instance;
    private NotifyCallback _notifyCallback = NotifyCallback.DEFAULT_NOTIFY_CALLBACK;
    private ConnectCallback _connectCallback = ConnectCallback.DEFAULT_CONNECT_CALLBACK;
    private boolean exitFlag = false;
    private Socket _socket;

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
                    _socket = new Socket();
                    _socket.connect(new InetSocketAddress(_host, _port), Config.connection_timeout);
                    _socket.setKeepAlive(true);
                } catch (IOException e) {
                    Log.e(e.getLocalizedMessage());
                    _connectCallback.onError(e.getLocalizedMessage());
                    return;
                }
                //TODO 连接成功
                Log.e("connect success !!!");
                INPUT_SERVCIE.execute(inputRunnable);
            }
        };
        SOCKET_HEARTBEAT_SERVICE.execute(runnable);
        startHeartbeat();
    }


    private void startHeartbeat() {
        do {
            addHeartbeatMessage();
            try {
                Thread.sleep(TIME_LENGTH_FOR_HEARTBEAT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!exitFlag);
    }

    private void addHeartbeatMessage() {
        SOCKET_HEARTBEAT_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                Log.i("addHeartbeatMessage()");
            }
        });
    }

    //接收数据的Runnable
    private Runnable inputRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                InputStream inputStream = _socket.getInputStream();
                DataInputStream dis = new DataInputStream(inputStream);
                while (!exitFlag) {
                    Log.i("start wait for inputStream ");
                    String s = dis.readUTF();
                    Log.i(s);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}