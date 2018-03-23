package com.ccut.wp.framework.server.http.dto;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by lixiaoqing on 2018/3/22.
 */
public class HttpRequestKit {
    private final static Logger LOG = LoggerFactory.getLogger(HttpRequestKit.class);
    private final FullHttpRequest httpRequest;
    private final Channel channel;
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(1024 * 1024 * 20); //

    private String uri;
    private final Map<String, List<String>> params = new HashMap<String, List<String>>();
    private final Map<String, List<FileItem>> files = Maps.newHashMap();
    private final Map<String, String> cookies = new HashMap<String, String>();
    private final Map<String, String> headers = new HashMap<String, String>();
    private final Map<String, Object> attributes = new HashMap<String, Object>();
    private byte[] body;
    private static final Charset defaultChareSet = Charset.forName("utf-8");

    public HttpRequestKit(Channel channel, FullHttpRequest msg) {
        this.httpRequest = msg;

        this.channel = channel;
        HttpHeaders httpHeaders = httpRequest.headers();
        if (!httpHeaders.isEmpty()) {
            for (Map.Entry<String, String> h : httpHeaders) {
                String key = h.getKey();
                String value = h.getValue();
                headers.put(key, value);
            }
        }

        if (headers.containsKey(HttpHeaders.Names.COOKIE)) {
            String cookieString = headers.get(HttpHeaders.Names.COOKIE);
            if (cookieString != null) {
                Set<Cookie> cookieSet = CookieDecoder.decode(cookieString);
                for (Cookie cookie : cookieSet) {
                    cookies.put(cookie.getName(), cookie.getValue());
                }
            }
        }
        String uriFull = httpRequest.uri();
        boolean no_decode = "true".equals(headers.get("X-No-Decode"));
        try {
            if (!no_decode) {
                uriFull = java.net.URLDecoder.decode(uriFull, "utf-8");
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error("url decode error! " + uriFull);
        }
        QueryStringDecoder decoderQuery = new QueryStringDecoder(uriFull);
        if (decoderQuery != null) {
            this.uri = decoderQuery.path();
        }
        Map<String, List<String>> uriAttributes = decoderQuery.parameters();
        for (Map.Entry<String, List<String>> attr : uriAttributes.entrySet()) {
            params.put(attr.getKey(), attr.getValue());
        }
        if (httpRequest.method().name().equals(HttpMethod.GET.name())) {


        } else if (httpRequest.method().name().equals(HttpMethod.POST.name())) {
            //   HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
            boolean isBinary = "true".equals(headers.get("X-Binary"));
            HttpPostRequestDecoder decoder = null;
            if (!isBinary) {
                decoder = new HttpPostRequestDecoder(factory, httpRequest, defaultChareSet);
            }
            try {
                if (decoder != null && decoder.isMultipart()) {//如果文件上传
                    while (decoder.hasNext()) {
                        InterfaceHttpData data = decoder.next();
                        if (data != null) {
                            try {
                                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                                    FileUpload fileUpload = (FileUpload) data;
                                    if (fileUpload.isCompleted()) {
                                        List<FileItem> fileItemList = files.get(fileUpload.getName());
                                        if (fileItemList == null) {
                                            fileItemList = new ArrayList<>();
                                        }
                                        fileItemList.add(new FileItem(fileUpload.get(), fileUpload.getContentType(), fileUpload.getFilename()));
                                        files.put(fileUpload.getName(), fileItemList);
                                    }
                                } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                                    Attribute attribute = (Attribute) data;
                                    List<String> p = params.get(attribute.getName());
                                    if (p == null) {
                                        p = new ArrayList<>();
                                    }
                                    p.add(attribute.getValue());
                                    params.put(attribute.getName(), p);
                                }
                            } catch (Exception e) {
                                LOG.error(e.getMessage(), e);
                            } finally {
                                data.release();
                            }
                        }
                    }
                } else {//普通的post表单
                    byte[] bb = new byte[httpRequest.content().readableBytes()];
                    httpRequest.content().readBytes(bb);
                    body = bb;//把原始的body存下来
                    if (!isBinary) {
                        QueryStringDecoder decoderQuery1 = new QueryStringDecoder("?" + new String(bb));

                        Map<String, List<String>> uriAttributes1 = decoderQuery1.parameters();
                        for (Map.Entry<String, List<String>> attr : uriAttributes1.entrySet()) {
                            params.put(attr.getKey(), attr.getValue());
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            } finally {
                if (decoder != null) {
                    decoder.destroy();
                }
            }
        }
    }


    public final byte[] getBody() {
        return body;
    }

    public final void setAttribute(String key, String value) {
        this.attributes.put(key, value);

    }

    public final Object getAttribute(String key) {
        return attributes.get(key);
    }

    public String getCookieValue(String name) {
        if (cookies.containsKey(name)) {
            return cookies.get(name);
        }
        return null;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    /**
     * 返回最原始的netty请求对象
     *
     * @return
     */
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    /**
     * 得到参数的字符串值
     *
     * @param name
     * @return
     */
    public String getString(String name) {
        return getString(name, null);
    }

    /**
     * 取参数值，可以指定默认值
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public String getString(String name, String defaultValue) {
        if (params.containsKey(name)) {
            List<String> list = params.get(name);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return defaultValue;
    }

    /**
     * 返回请求中所有的参数的Map集合
     *
     * @return
     */
    public Map<String, List<String>> getParams() {
        return params;
    }

    /**
     * 返回请求客户端地址
     *
     * @return
     */
    public final String getRemoteAddress() {
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        if (address == null) {
            return null;
        }
        InetAddress inetAddress = address.getAddress();
        if (inetAddress == null) {
            return null;
        }
        return inetAddress.getHostAddress();
    }

    /**
     * 返回客户端请求连接的端口
     *
     * @return
     */
    public final int getRemotePort() {
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        if (address == null) {
            return -1;
        }

        return address.getPort();
    }

    /**
     * 返回整形参数,默认值是-1
     *
     * @param name
     * @return
     */
    public int getInt(String name) {
        return NumberUtils.toInt(getString(name), -1);

    }

    /**
     * 返回请求中整形的参数，可以指定默认值
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public int getInt(String name, int defaultValue) {
        return NumberUtils.toInt(getString(name), defaultValue);

    }

    /**
     * 返回长整形参数,默认值是-1
     *
     * @param name
     * @return
     */
    public long getLong(String name) {
        return NumberUtils.toLong(getString(name), -1);
    }

    /**
     * 返回请求中长整形的参数，可以指定默认值
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public long getLong(String name, long defaultValue) {
        return NumberUtils.toLong(getString(name), defaultValue);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Map<String, List<FileItem>> getFiles() {
        return files;
    }

}
