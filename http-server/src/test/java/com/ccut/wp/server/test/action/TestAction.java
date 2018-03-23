package com.ccut.wp.server.test.action;

import com.alibaba.fastjson.JSONObject;
import com.ccut.wp.framework.server.http.annotation.CloudController;
import com.ccut.wp.framework.server.http.annotation.CloudRequestMapping;
import com.ccut.wp.framework.server.http.dto.HttpRequestKit;
import com.ccut.wp.framework.server.http.dto.HttpResponseKit;
import com.ccut.wp.framework.server.http.utils.RequestUtils;
import com.ccut.wp.framework.server.http.utils.ResponseUtils;
import com.ccut.wp.server.test.model.RetObject;

/**
 * Created by lixiaoqing on 2018/3/23.
 */
@CloudController(value = "api/test")
public class TestAction {

    @CloudRequestMapping(method = "/sayHello.action")
    public String get(HttpRequestKit request, HttpResponseKit response) {
        String name = RequestUtils.getRequestString(request, "name","default_name");
        return "hello:"+name;
    }



    @CloudRequestMapping(method = "/getJson.go")
    public void getJson(HttpRequestKit request, HttpResponseKit response) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", 200);
        JSONObject data = new JSONObject();
        data.put("v1", "v1");
        data.put("v2", "v2");
        data.put("v3", "v3");
        jsonObject.put("value", data);

        ResponseUtils.renderJson(response, jsonObject.toJSONString());
    }


    @CloudRequestMapping(method = "/getJson2.go")
    public void getJson2(HttpRequestKit request, HttpResponseKit response) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", 200);
        RetObject data = new RetObject();
        data.setId(100);
        data.setName("testname");
        data.setAddress("test address");
        jsonObject.put("value", JSONObject.toJSON(data));

        ResponseUtils.renderJson(response, jsonObject.toJSONString());
    }
}
