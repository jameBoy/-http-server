package com.ccut.wp.server.test;

import com.ccut.wp.framework.server.http.HttpServer;

import java.util.Arrays;

/**
 * Created by lixiaoqing on 2018/3/23.
 */
public class TestHttpServer {

    public static void main(String[] args) {
        HttpServer server = HttpServer.create().scanPackageList(Arrays.asList("com.ccut.wp.server.test.action")).port(8080).build();
        server.start();
    }
}
