package socket.java;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by jingbin on 2016/12/20.
 */
public class RequestBuilder {

    public static byte[] build(byte[] body, Map<String, String> headers, long reqID) {
        int length = 4 + 1;
        if (body != null) {
            length += body.length;
        }
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                byte[] key = null;
                byte[] value = null;
                try {
                    key = entry.getKey().getBytes("UTF-8");
                    value = entry.getValue().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
                if (key.length > 255 || value.length > 255) {
                    return null;
                }
                length += 1 + key.length + 1 + value.length;
            }
        }

        byte[] request = new byte[length];
        request[0] = (byte) ((reqID & 0xff000000) >> 24);
        request[1] = (byte) ((reqID & 0xff0000) >> 16);
        request[2] = (byte) ((reqID & 0xff00) >> 8);
        request[3] = (byte) (reqID & 0xff);

        int pos = 4;
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                byte[] key = null;
                byte[] value = null;
                try {
                    key = entry.getKey().getBytes("UTF-8");
                    value = entry.getValue().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
                if (key.length > 255 || value.length > 255) {
                    return null;
                }
                request[pos] = (byte) key.length;
                pos++;
                System.arraycopy(key, 0, request, pos, key.length);
                pos += key.length;
                request[pos] = (byte) value.length;
                pos++;
                System.arraycopy(value, 0, request, pos, value.length);
                pos += value.length;
            }
        }
        request[pos] = 0; // header-end
        pos++;

        if (body != null) {
            System.arraycopy(body, 0, request, pos, body.length);
        }

        return request;
    }
}
