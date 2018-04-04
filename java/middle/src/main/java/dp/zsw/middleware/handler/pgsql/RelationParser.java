package dp.zsw.middleware.handler.pgsql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import dp.zsw.middleware.handler.utils.Regex;

import java.util.*;

public class RelationParser {
    public static Set<String> regexs = new TreeSet<>();
    public static Set<String> sqlRegexs = new TreeSet<>();

    static {
        //表名
        regexs.add("(TASK_[^\\s\\.\"\'\\(\\;]+)");
        regexs.add("(VIEW_SOURCE_SAMPLE_[^\\s\\.\"\'\\(\\;]+)");
        regexs.add("(VIEW_SOURCE_[^\\s\\.\"\'\\(\\;]+)");
        regexs.add("(DATA_ID_[^\\s\\.\"\'\\(\\;]+)");
        regexs.add("(TASK_[^\\s\\.\"\'\\(\\;]+)");

        sqlRegexs.add("(SELECT.*FROM.*)");
        sqlRegexs.add("(CREATE.*)");
        sqlRegexs.add("(TRUNCATE.*)");
        sqlRegexs.add("(INSERT.*INTO.*)");
        sqlRegexs.add("(UPDATE.*)");
        sqlRegexs.add("(DROP.*)");
        sqlRegexs.add("(ALTER.*)");
    }
    /**
     * DFS查找relname
     * @param sql sql
     */
    public static String parseTable(String sql){
        String tree = SqlParser.parseSql(sql);
        if (tree == null || tree.equalsIgnoreCase(""))
            return null;
        try {
            JSONArray array = JSON.parseArray(tree);
            Map<Object, Object> data = (Map<Object,Object>) array.get(0);
            StringBuffer tables = new StringBuffer();
            deepSearch(data, tables);
            return tables.toString();
        } catch (Exception e){
            return null;
        }
    }

    private static void deepSearch(Map<Object, Object> node, StringBuffer tables){
        if (node.size() == 1){
            String res = (String)node.get("relname");
            if (res!=null && !res.equalsIgnoreCase("")){
                tables.append(res).append(",");
                return ;
            }
            else {
                res = (String)node.get("idxname");
                if (res!=null && !res.equalsIgnoreCase("")){
                    tables.append(res).append(",");
                    return ;
                }
            }
        }
        for (Map.Entry<Object, Object> entry : node.entrySet()) {
            if (((String)entry.getKey()).equalsIgnoreCase("relname")){
                tables.append(entry.getValue()).append(",");
            }
            else if (((String)entry.getKey()).equalsIgnoreCase("idxname")){
                tables.append(entry.getValue()).append(",");
            }
            else if (((String)entry.getKey()).equalsIgnoreCase("FuncCall")){
                Map<Object, Object> leaf = (Map<Object, Object>) entry.getValue();
                deepSearchFunCall(leaf, tables);
            }
            else if (node.get(entry.getKey()) instanceof Map) {
                Map<Object, Object> leaf = (Map<Object, Object>) entry.getValue();
                deepSearch(leaf, tables);
            }
            else if (node.get(entry.getKey()) instanceof List){
                List<Object> arrList = (List<Object>) entry.getValue();
                for (Object o: arrList) {
                    if (o instanceof  List){
                        for (Object ob : (List)o){
                            if (ob instanceof  Map){
                                deepSearch((Map<Object, Object>)ob, tables);
                            }
                        }
                    }
                    else {
                        deepSearch((Map<Object, Object>) o, tables);
                    }
                }
            }
        }
    }

    private static void deepSearchFunCall(Map<Object, Object> node, StringBuffer tables){
        for (Map.Entry<Object, Object> entry : node.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map){
                Map<Object, Object> leaf = (Map<Object, Object>) value;
                deepSearchFunCall(leaf, tables);
            }
            else if (value instanceof  List){
                List<Map<Object, Object>> arrList = (List<Map<Object, Object>>) value;
                for (Map<Object, Object> objectObjectMap : arrList) {
                    deepSearchFunCall(objectObjectMap, tables);
                }
            }
            else if (value instanceof String){
                String res = "";
                for (String regex : regexs) {
                    res = Regex.group1(((String) value).toUpperCase(), regex);
                    if (res != null && !res.equalsIgnoreCase("")){
                        break;
                    }
                }
                if(res != null && !res.equalsIgnoreCase("")){
                    tables.append(res).append(",");
                }
            }
        }
    }
}
