package com.ccut.wp.framework.server.http.core;

import com.ccut.wp.framework.server.http.filter.Filter;
import com.ccut.wp.framework.server.http.dto.HttpRequestKit;
import com.ccut.wp.framework.server.http.dto.HttpResponseKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Created by lixiaoqing on 2018/3/22.
 */
public abstract class Action {

    private final static Logger LOG = LoggerFactory.getLogger(Action.class);

    private String name;
    private Class originalClass;


    private Object object;
    private Method method;
    private Set<Filter> filterSet;

    public void execute(HttpRequestKit httpRequestKit, HttpResponseKit httpResponseKit) {
        try {
            this.method.setAccessible(true);
            this.method.invoke(this.object, httpRequestKit, httpResponseKit);
        } catch (InvocationTargetException e) {
            LOG.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOG.error(e.getMessage(), e);
        }
    }


    public abstract String action(HttpRequestKit httpRequestKit, HttpResponseKit httpResponseKit)throws Exception;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getOriginalClass() {
        return originalClass;
    }

    public void setOriginalClass(Class originalClass) {
        this.originalClass = originalClass;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Set<Filter> getFilterSet() {
        return filterSet;
    }

    public void setFilterSet(Set<Filter> filterSet) {
        this.filterSet = filterSet;
    }
}
