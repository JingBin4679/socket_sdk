package socket.java.callback;

/**
 * Created by jingbin on 2016/12/20.
 */
public abstract class RequestCallback {

    public abstract void onSuccess(byte[] data);

    public abstract void onFailed(String err);


    public void onComplete() {}
}
