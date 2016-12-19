package socket.java;

/**
 * Created by jingbin on 2016/12/19.
 */
public abstract class NotifyCallback {

    public static final NotifyCallback DEFAULT_NOTIFY_CALLBACK = new NotifyCallback() {
        @Override
        protected void onNotfiy(byte[] notify) {

        }
    };


    protected abstract void onNotfiy(byte[] notify);



}

