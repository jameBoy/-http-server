package com.ccut.wp.framework.server.http.filter;

import com.ccut.wp.framework.server.http.dto.HttpRequestKit;
import com.ccut.wp.framework.server.http.dto.HttpResponseKit;

/**
 * Created by lixiaoqing on 2018/3/22.
 */
public interface Filter {

    /**
     * before action
     * @param httpRequestKit
     */
    void before(HttpRequestKit httpRequestKit);

    /**
     * after action
     * @param httpRequestKit
     * @param httpResponse
     */
    void after(HttpRequestKit httpRequestKit, HttpResponseKit httpResponse);
}
