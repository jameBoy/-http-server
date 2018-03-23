package com.ccut.wp.framework.server.http.core;

import com.ccut.wp.framework.server.http.filter.Filter;
import com.ccut.wp.framework.server.http.dto.HttpRequestKit;
import com.ccut.wp.framework.server.http.dto.HttpResponseKit;
import io.netty.buffer.Unpooled;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by lixiaoqing on 2018/3/22.
 */
public class URLDispatcher {

    private final static Logger LOG = LoggerFactory.getLogger(URLDispatcher.class);

    private Map<String, Action> actionMap;


    public void doDispatcher(HttpRequestKit requestKit, HttpResponseKit responseKit){
        String uri = requestKit.getUri();
        long start = System.currentTimeMillis();
        try {
            if (StringUtils.isBlank(uri)) {
                throw new RuntimeException("Dispatcher doDispatcher url is null");
            }

            if ("/ping".equals(uri)) {
                responseKit.getHttpResponse().content().writeBytes(Unpooled.copiedBuffer("OK", Charset.forName("utf-8")));
                return;
            }

            if ("/favicon.ico".equals(uri)) {

                return;
            }

            Action action = actionMap.get(uri);

            if (action == null) {
                throw new RuntimeException("Dispatcher doDispatcher action is not found");
            }

            if (CollectionUtils.isNotEmpty(action.getFilterSet())) {
                for (Filter filter : action.getFilterSet()) {
                    filter.before(requestKit);
                }
            }

            String result = action.action(requestKit, responseKit);
            if (StringUtils.isNotBlank(result)) {
                responseKit.getHttpResponse().content().writeBytes(Unpooled.copiedBuffer(result, Charset.forName("utf-8")));
            }

            if (CollectionUtils.isNotEmpty(action.getFilterSet())) {
                for (Filter filter : action.getFilterSet()) {
                    filter.after(requestKit, responseKit);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            responseKit.getHttpResponse().content().writeBytes(Unpooled.copiedBuffer(e.getMessage(), Charset.forName("utf-8")));
        }catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }finally {
            long end = System.currentTimeMillis();
            long cost = end - start;
            if (cost > 200) {
                LOG.info("slow execute ,url= {} ,cost= {}", requestKit.getHttpRequest().uri(), cost);
            }
        }
    }

    public void setActionMap(Map<String, Action> actionMap) {
        this.actionMap = actionMap;
    }

}
