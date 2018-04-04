package dp.zsw.middleware.handler.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by zsw on 2017/11/18.
 *
 */
public class Regex {
    public static String group1(String search, String regex){
        Pattern patternSQL = Pattern.compile(regex);
        Matcher matcher = patternSQL.matcher(search);
        String find = null;
        while(matcher.find()){
            find = matcher.group(1);
        }
        return find;
    }
}
