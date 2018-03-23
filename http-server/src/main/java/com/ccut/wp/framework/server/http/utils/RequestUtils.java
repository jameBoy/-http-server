package com.ccut.wp.framework.server.http.utils;

import com.ccut.wp.framework.server.http.dto.HttpRequestKit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by lixiaoqing on 2018/3/23.
 */
public class RequestUtils {
    private static Logger LOG = LoggerFactory.getLogger(RequestUtils.class);

    private static String deReqQuery(HttpRequestKit request, String name){
        Map<String, List<String>> params = request.getParams();
        if(params!=null && params.containsKey(name)){
            return params.get(name).get(0);
        }
        return null;
    }



    public static String getRequestString(HttpRequestKit request, final String name) {
        return getRequestString(request, name, null);
    }

    public static String getRequestString(HttpRequestKit request, final String name, final String defaultValue) {
        String value = deReqQuery(request, name);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public static int getRequestInt(HttpRequestKit request, final String name) {
        return getRequestInt(request, name, 0);
    }



    public static int getRequestInt(HttpRequestKit request, final String name, int defaultValue) {
        String value = getRequestString(request, name, null);
        if (value == null)
            return defaultValue;

        try {
            return NumberUtils.toInt(value.trim(), defaultValue);
        } catch (Exception e) {
            LOG.error("[RequestUtils] [getRequestInt] error name is {}, value is {}", name, value);
            return defaultValue;
        }
    }

    public static long getRequestLong(HttpRequestKit request, final String name) {
        return getRequestLong(request, name, 0);
    }

    public static long getRequestLong(HttpRequestKit request, final String name, long defaultValue) {
        String value = getRequestString(request, name, "0");
        return paseLong(value, defaultValue);
    }
    public static long paseLong(String s,long defaultV){
        long r = defaultV;
        try {
            if (StringUtils.isNotBlank(s)) {
                r = Long.parseLong(s);
            }
        } catch (Exception e) {
            LOG.error("[RequestUtils] [getRequestLong] error name is {}", s);
        }
        return r;
    }
    public static String getRemoteIpAddress(HttpRequestKit request) {

        String rip = request.getRemoteAddress();
        String xff = request.getHeaders().get("X-Forwarded-For");
        String ip = "";
        if (xff != null && xff.length() != 0) {
            int px = xff.indexOf(',');
            if (px != -1) {
                ip = xff.substring(0, px);
            } else {
                ip = xff;
            }
        } else if(StringUtils.isNotBlank(rip)){
            ip = rip;
        }
        return ip.trim();
    }

    /**
     * 获取用户地区编码（基于国标）
     *
     * @param request
     * @return
     */
    public static String getIploc(HttpRequestKit request) {
        final String DEFAULT_LOCAL = "CN0000";
        String iploc = request.getHeaders().get("cmccip");
        if (iploc == null || iploc.length() == 0 || "unknown".equalsIgnoreCase(iploc)) {
            iploc = DEFAULT_LOCAL;
        }
        return iploc;
    }

    public static String getGbgodeFromIploc(HttpRequestKit request){
        String iploc = getIploc(request);
        if (StringUtils.indexOf(iploc, "CN") != -1) {
            iploc = StringUtils.substring(iploc, 2);
            if (iploc.length() > 6) {
                iploc = StringUtils.substring(iploc, 0, 6);
            }
        }
        if("000000".equalsIgnoreCase(iploc)||"0000".equalsIgnoreCase(iploc)){
            return null;
        }
        return iploc;
    }

    /**
     *  获取客户端ip地址
     *
     * @param request
     * @return
     */
    public static String getIpAddr(HttpRequestKit request) {
        String ip = request.getHeaders().get("x-source-id");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().get("x-forwarded-for");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().get("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().get("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().get("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress();
        }

        return ip;
    }





    public static long getReqHeaderLong(HttpRequestKit request,
                                        String name, long defaultValue) {
        String value = request.getHeaders().get(name);
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            LOG.error("[RequestUtils] [getReqHeaderLong] error name is {}", name);
        }
        return defaultValue;
    }

    public static int getReqHeaderInt(HttpRequestKit request, String name,
                                      int defaultValue) {
        String value = request.getHeaders().get(name);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            LOG.error("[RequestUtils] [getReqHeaderInt] error name is {}", name);
        }
        return defaultValue;
    }

    public static String getReqHeader(HttpRequestKit request, String name,
                                      String defaultValue) {
        String value = request.getHeaders().get(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public static String getReqHeader(HttpRequestKit request, String name1,
                                      String name2, String defaultValue) {
        String value = request.getHeaders().get(name1);
        if (value == null) {
            value = request.getHeaders().get(name2);
        }
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
