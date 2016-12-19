package socket.java;

/**
 * Created by jingbin on 2016/12/19.
 */
public abstract class ConnectCallback {

    static ConnectCallback DEFAULT_CONNECT_CALLBACK = new ConnectCallback() {
        @Override
        protected void onSuccess() {

        }

        @Override
        protected void onError(String err) {

        }
    };

    protected abstract void onSuccess();

    protected abstract void onError(String err);

}
