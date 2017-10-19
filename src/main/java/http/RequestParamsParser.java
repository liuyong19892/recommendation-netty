package http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;

/**
 * Created by Jay on 2017/7/12.
 */
public class RequestParamsParser {

    public static Map<String, String> getRequestParams(ChannelHandlerContext ctx, HttpRequest req){

        Map<String, String>requestParams=new HashMap<>();
        // 处理get请求
        if (req.method() == GET) {
            QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
            Map<String, List<String>> parame = decoder.parameters();
            Iterator<Map.Entry<String, List<String>>> iterator = parame.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<String, List<String>> next = iterator.next();
                requestParams.put(next.getKey(), next.getValue().get(0));
            }
        }
        // 处理POST请求
        if (req.method() == POST) {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(
                    new DefaultHttpDataFactory(false), req);
            List<InterfaceHttpData> postData = decoder.getBodyHttpDatas(); //
            for(InterfaceHttpData data:postData){
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    MemoryAttribute attribute = (MemoryAttribute) data;
                    requestParams.put(attribute.getName(), attribute.getValue());
                }
            }
        }
        return requestParams;
    }
}
