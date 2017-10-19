package com.xmtj.netty.log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by sky-wind on 2016/3/21.
 */
public class LoggerUtils {
    public static String getTrace(Throwable t) {
        StringWriter stringWriter= new StringWriter();
        PrintWriter writer= new PrintWriter(stringWriter);
        t.printStackTrace(writer);
        StringBuffer buffer= stringWriter.getBuffer();
        return buffer.toString();
    }
}
