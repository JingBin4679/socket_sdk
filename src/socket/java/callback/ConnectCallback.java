package socket.java.callback;

import java.io.IOException;

/**
 * Created by jingbin on 2016/12/19.
 */
public abstract class ConnectCallback {

    public abstract void onSuccess();

    public abstract void onError(String err);

}
