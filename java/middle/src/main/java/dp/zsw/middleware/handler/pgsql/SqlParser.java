package dp.zsw.middleware.handler.pgsql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zsw on 2017/11/14.
 *
 */
public class SqlParser {
    private static Logger LOG = LoggerFactory.getLogger(SqlParser.class);
    private static boolean flag;

    static {
        try {
            System.loadLibrary("sql_parser");
            flag = true;
        } catch (Exception e) {
            LOG.warn("Failed to load library sql_parser");
            flag = false;
        }
    }

    public static native String parse(String sql);

    public static String parseSql(String sql){
        if (flag){
            return parse(sql);
        }
        return null;
    }
}
