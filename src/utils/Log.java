package utils;

/**
 * java日志工具
 * Created by jingbin on 2016/12/19.
 */
public class Log {

    public static final String TAG = Log.class.getSimpleName();
    private static Loggable _console;

    public interface Loggable {
        void info(String tag, String msg);

        void err(String tag, String msg);
    }

    /**
     * 关闭输出
     */
    public static void closeConsole() {
        setConsole(null);
    }

    /**
     * 设置系统默认的System输出
     */
    public static void setSystemConsole() {
        setConsole(new SystemLogImpl());
    }

    public static void setConsole(Loggable log) {
        Log._console = log;
    }

    public static void i(String tag, String info) {
        if (_console != null) {
            _console.info(tag, info);
        }
    }

    public static void e(String tag, String err) {
        if (_console != null) {
            _console.err(tag, err);
        }
    }

    public static void i(String info) {
        if (_console != null) {
            _console.info(TAG, info);
        }
    }

    public static void e(String err) {
        if (_console != null) {
            _console.err(TAG, err);
        }
    }

    private static class SystemLogImpl implements Loggable {

        @Override
        public void info(String tag, String msg) {
            System.out.println(tag + "\t" + msg);
        }

        @Override
        public void err(String tag, String msg) {
            System.err.println(tag + "\t" + msg);
        }
    }
}