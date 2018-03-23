package com.ccut.wp.framework.server.http.utils;

import com.alibaba.fastjson.JSONObject;
import com.ccut.wp.framework.server.http.dto.HttpResponseKit;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by lixiaoqing on 2018/3/23.
 */
public class ResponseUtils {

    private static Logger LOG = LoggerFactory.getLogger(ResponseUtils.class);

    // header 常量定义
    private static final String ENCODING_PREFIX = "encoding";
    public static final String NOCACHE_PREFIX = "no-cache";
    private static final String ENCODING_DEFAULT = "UTF-8";
    private static final boolean NOCACHE_DEFAULT = true;
    private static int Expires = 100;
    private static final String SUCCESS = "S"; // 成功
    private static final String FAIL = "F"; // 失败



    /**
     * 直接输出Txt
     *
     */
    public static void renderText(HttpResponseKit response, final String text, final String... headers) {
        render(response, "text/plain", text, headers);
    }

    public static void renderText(HttpResponseKit response,int status, final String text, final String... headers) {
        render(response,status, "text/plain", text, headers);
    }


    /**
     * 调用成功，输出JSON
     *
     * @param response
     */
    public static void renderSuccessJson(HttpResponseKit response, final String strResponse) {
        JSONObject jso = new JSONObject();
        jso.put("isSuccess", SUCCESS);
        if (StringUtils.isNotBlank(strResponse)) {
            jso.put("response", strResponse);
        }
        render(response, "application/json", jso.toString(), "no-cache:false");
    }

    /**
     * 调用失败，输出JSON
     *
     * @param response
     * @param
     */
    public static void renderFailJson(HttpResponseKit response, final String errCode) {
        JSONObject jso = new JSONObject();
        jso.put("isSuccess", FAIL);
        jso.put("error", errCode);
        render(response, "application/json", jso.toString(), "no-cache:false");
    }



    /**
     * 直接输出JSON.
     *
     * @param string
     *            json字符串.
     */
    public static void renderJson(HttpResponseKit response, final String string, final String... headers) {
        renderJson(response, string,null, headers);
    }

    /**
     * 直接输出JSON. 增加了可以构造result通信结果节点的返回值
     *
     * @param string
     *            json字符串.
     */
    public static void renderJson(HttpResponseKit response,String string,JSONObject result, final String... headers) {
        // 需要做标准化结果的
        if (result!=null){
            JSONObject jsonObject = JSONObject.parseObject(string);
            // 增加标准返回结果节点result

            string = jsonObject.toString();
        }
        render(response, "application/json", string, headers);
    }

    /**
     * 直接输出JSON. 增加了可以构造result通信结果节点的返回值
     *
     * json字符串.
     *
     */
    public static void renderJson(HttpResponseKit response, JSONObject jsonObject, JSONObject result, final String... headers) {
        // 需要做标准化结果的
        if (result!=null){
            if (jsonObject!=null){
                // 增加标准返回结果节点result
                jsonObject.put("result",result);
            }
            else {
                jsonObject=new JSONObject();
                jsonObject.put("result",result);
            }
        }
        render(response, "application/json", jsonObject.toString(), headers);
    }


    /**
     * 直接输出内容的简便函数.
     *
     * eg. render("text/plain", "hello", "encoding:GBK"); render("text/plain",
     * "hello", "no-cache:false"); render("text/plain", "hello", "encoding:GBK",
     * "no-cache:false");
     *
     * @param headers
     *            可变的header数组，目前接受的值为"encoding:"或"no-cache:",默认值分别为UTF-8和true .
     */
    public static void render(HttpResponseKit response,final String contentType, final String content,
                              final String... headers) {
        render(response,200,contentType,content,headers);
    }

    public static void render(HttpResponseKit response, int status, final String contentType, final String content,
                              final String... headers) {
        try {
            // 分析headers参数
            String encoding = ENCODING_DEFAULT;
            boolean noCache = NOCACHE_DEFAULT;
            for (String header : headers) {
                String headerName = StringUtils.substringBefore(header, ":");
                String headerValue = StringUtils.substringAfter(header, ":");

                if (StringUtils.equalsIgnoreCase(headerName, ENCODING_PREFIX)) {
                    encoding = headerValue;
                } else if (StringUtils.equalsIgnoreCase(headerName, NOCACHE_PREFIX)) {
                    noCache = Boolean.parseBoolean(headerValue);
                } else
                    throw new IllegalArgumentException(headerName
                            + "不是一个合法的header类型");
            }

            // 设置headers参数
            String fullContentType = contentType + ";charset=" + encoding;
            response.setContentType(fullContentType);
            if (noCache) {
                response.setHeader("Pragma", "No-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setHeader("Expires", "0");
            }
            response.getHttpResponse().setStatus(HttpResponseStatus.valueOf(status));
            response.getHttpResponse().content().writeBytes(content.getBytes(ENCODING_DEFAULT));

        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
