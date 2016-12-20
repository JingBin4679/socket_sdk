package socket.java;

import socket.java.callback.ConnectCallback;
import socket.java.callback.NotifyCallback;
import socket.java.callback.RequestCallback;
import socket.java.utils.RequestBuilder;
import socket.java.utils.StringUtils;
import utils.Log;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Core {

    //output thread
    private static final ExecutorService OUTPUT_SERVICE = Executors.newFixedThreadPool(1);

    //input thread
    public static final ExecutorService INPUT_SERVICE = Executors.newFixedThreadPool(1);


    public static final ExecutorService SOCKET_SERVICE = Executors.newFixedThreadPool(2);

    //默认心跳时长
    private static final long TIME_LENGTH_FOR_HEARTBEAT = TimeUnit.MINUTES.toMillis(1);

    static ConnectCallback DEFAULT_CONNECT_CALLBACK = new ConnectCallback() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onError(String err) {

        }
    };

    static final NotifyCallback DEFAULT_NOTIFY_CALLBACK = new NotifyCallback() {
        @Override
        protected void onNotify(byte[] notify) {

        }
    };

    private List<byte[]> data = new ArrayList<byte[]>();
    private String _host;
    private int _port;


    private static Core instance;
    private NotifyCallback _notifyCallback = DEFAULT_NOTIFY_CALLBACK;
    private ConnectCallback _connectCallback = DEFAULT_CONNECT_CALLBACK;
    private boolean exitFlag = false;
    private Socket _socket;
    private long reqID_;
    private long reqIDstart = 200;

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
            this._connectCallback = DEFAULT_CONNECT_CALLBACK;
            return;
        }
        this._connectCallback = connectCallback;

    }

    public void setNotifyCallback(NotifyCallback callback) {
        if (callback == null) {
            this._notifyCallback = DEFAULT_NOTIFY_CALLBACK;
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
                onOpen();
                _connectCallback.onSuccess();
            }
        };
        SOCKET_SERVICE.execute(runnable);
        startHeartbeat();
    }

    private void onOpen() {
        sendHandShake();
        OUTPUT_SERVICE.execute(outputRunnable);
        INPUT_SERVICE.execute(inputRunnable);
    }

    private void sendHandShake() {
        byte[] handshake = new byte[6];
        handshake[5] = (byte) 0xff;
        for (int i = 0; i < 5; ++i) {
            handshake[5] ^= (byte)handshake[i];
        }

        synchronized (data) {
            data.add(handshake);
            data.notify();
        }
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
        SOCKET_SERVICE.execute(new Runnable() {
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
                while (!exitFlag) {
                    Log.i("start wait for inputStream ");
                    long length = parseContentLength(inputStream);
                    if (length < 0) {
                        System.out.println("illegal response");
                        continue;
                    }
                    if (length == 0) { // heartbeat
                        System.out.println("heartbeat");
                        continue;
                    }
                    length -= 4;
                    int pos = 0;
                    final byte[] data = new byte[(int) length];
                    while (length - pos != 0) {
                        int n = inputStream.read(data, pos, (int) length - pos);
                        if (n < 0) {
                            continue;
                        }
                        if (n == 0) {
                            continue;
                        }
                        pos += n;
                    }
                    Response response = parseResponse(data);
                    performCallback(response);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void performCallback(Response response) {
        Request request = tasks.get(response.reqID);
        if (request == null) {
            return;
        }
        if (response.status == Response.Status.Success) {
            request.requestCallback.onSuccess(response.data);
        } else {
            request.requestCallback.onFailed("请求失败");
        }
        request.requestCallback.onComplete();
        tasks.remove(response.reqID);
    }

    private Response parseResponse(byte[] data) {
        Response res = new Response();
        res.reqID = 0;
        for (int i = 0; i < 4; ++i) {
            res.reqID = (res.reqID << 8) + (data[i] & 0xff);
        }
        res.status = data[4] == 0 ? Response.Status.Success : Response.Status.Failed;
        if (data.length <= 5) {
            return res;
        }
        try {
            res.data = Arrays.copyOfRange(data, 5, data.length);
        } catch (Exception e) {
            res.data = null;
        }
        return res;
    }

    private long parseContentLength(InputStream inputStream) throws IOException {
        byte[] lengthB = new byte[4];
        int pos = 0;
        while (4 - pos != 0) {
            int n = inputStream.read(lengthB, pos, 4 - pos);
            if (n <= 0) {
                continue;
            }
            pos += n;
        }
        long length = ((0xff & lengthB[0]) << 24)
                + ((0xff & lengthB[1]) << 16)
                + ((0xff & lengthB[2]) << 8)
                + ((0xff & lengthB[3]));
        return length;
    }

    //接收数据的Runnable
    private Runnable outputRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                OutputStream outputStream = _socket.getOutputStream();
                DataOutputStream dis = new DataOutputStream(outputStream);
                while (!exitFlag) {
                    Log.i("start wait for output ");
                    byte[] writeData = getWriteData();
                    Log.i("start write output ");
                    dis.write(writeData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private byte[] getWriteData() {
        synchronized (data) {
            while (data.isEmpty()) {
                try {
                    data.wait();
                } catch (InterruptedException e) {
                    return null;
                }
            }
            return data.remove(0);
        }
    }


    private long reqID() {
        reqID_++;
        if (reqID_ < reqIDstart || reqID_ > Integer.MAX_VALUE) {
            reqID_ = reqIDstart;
        }
        return reqID_;
    }


    private ConcurrentMap<Long, Request> tasks = new ConcurrentHashMap<>();

    public void addRequest(final byte[] body, final Map<String, String> header, final RequestCallback callback) {
        final long reqID = reqID();
        //------添加任务---------
        Request request = new Request();
        request.reqID = reqID;
        request.body = body;
        request.header = header;
        request.requestCallback = callback;
        tasks.put(reqID, request);
        //------添加任务---------
        SOCKET_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                byte[] content = RequestBuilder.build(body, header, reqID);
                //------构建任务长度信息先发送给服务器 start--------
                byte[] len = new byte[4];
                int length= content.length + 4;
                len[0] = (byte) ((length & 0xff000000) >> 24);
                len[1] = (byte) ((length & 0xff0000) >> 16);
                len[2] = (byte) ((length & 0xff00) >> 8);
                len[3] = (byte) (length & 0xff);
                //------构建任务长度信息先发送给服务器 end--------

                synchronized (data) {
                    data.add(len);
                    data.add(content);
                    data.notify();
                }

            }
        });
    }


    private class Request {
        long reqID;
        byte[] body;
        Map<String, String> header;
        RequestCallback requestCallback;
    }
}