package socket.java;

/**
 * Created by jingbin on 2016/12/20.
 */
public abstract class RequestCallback {
    protected abstract void onSuccess(byte[] data);

    public abstract void onFailed(String err);


    public void onComplete() {

    }
}
