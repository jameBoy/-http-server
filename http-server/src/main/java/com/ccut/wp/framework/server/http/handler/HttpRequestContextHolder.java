package com.ccut.wp.framework.server.http.handler;

import java.util.Map;

/**
 * Created by lixiaoqing on 2018/3/22.
 */
public class HttpRequestContextHolder {

    private static final ThreadLocal<Map<String,String>> requestHeaderHolder =
            new ThreadLocal<Map<String,String>>();



    public static void setRequestHeader(Map<String,String> headers) {
        requestHeaderHolder.set(headers);
    }

    public static Map<String, String> getRequestHeader() {
        return requestHeaderHolder.get();
    }

    public static void resetRequestHeader() {
        requestHeaderHolder.remove();
    }

}
