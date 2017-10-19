package json;

import com.alibaba.fastjson.JSON;
import model.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jay on 2017/7/6.
 */
public class JsonUtils {

    public static String mapToJson(Map m){
        return JSON.toJSONString(m);
    }

    public static String toJsonString(Result m){
        return JSON.toJSONString(m);
    }

    public static void main(String[] args) {
        Map map = new HashMap<String,Object>();
        List l = new ArrayList<String>();
        l.add("1");
        l.add("2");
        map.put("1","a");
        map.put("2",l);
        System.out.println(JSON.toJSONString(map));
    }

}
