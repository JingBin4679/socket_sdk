package socket.java.utils;

/**
 * Created by jingbin on 2016/12/19.
 */
public class StringUtils {

    public static boolean isEmpty(CharSequence cs) {
        if (cs == null) {
            return true;
        }
        if (cs.length() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

}
