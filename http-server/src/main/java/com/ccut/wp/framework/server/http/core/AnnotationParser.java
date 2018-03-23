package com.ccut.wp.framework.server.http.core;

import com.ccut.wp.framework.server.http.annotation.CloudController;
import com.ccut.wp.framework.server.http.annotation.CloudRequestMapping;
import com.ccut.wp.framework.server.http.dto.HttpRequestKit;
import com.ccut.wp.framework.server.http.dto.HttpResponseKit;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lixiaoqing on 2018/3/23.
 */
public class AnnotationParser extends ClassLoader {
    private static Logger LOG = LoggerFactory.getLogger(AnnotationParser.class);


    private AtomicInteger counter = new AtomicInteger();

    public Map<String,Action> parseAction(List<String> packageList) throws Exception {

        Map<String,Action> result = new HashMap<String,Action>();

        Set<Class<?>> classSet = PackageScanner.getClasses(CloudController.class, packageList);
        Set<String> path = new HashSet<String>();
        if(CollectionUtils.isNotEmpty(classSet)) {
            for(Class<?> cls:classSet) {
                CloudController actionType = (CloudController) cls.getAnnotation(CloudController.class);
                String folderurl = formatClassUrl(actionType.value()[0]);  //formtter

                Method[] methods = cls.getDeclaredMethods();
                for(Method method:methods) {
                    if(method.isAnnotationPresent(CloudRequestMapping.class)) {
                        if (method.getModifiers() != Modifier.PUBLIC) {
                            throw new IllegalArgumentException(cls.getName() + "." + method.getName() + " is not public.");
                        }

                        Class[] parameterClassList = method.getParameterTypes();
                        for (Class parameter : parameterClassList) {
                            if (parameter != HttpRequestKit.class && parameter != HttpResponseKit.class) {    // 如果参数的类型不是request resp的话，也认为是错误的
                                LOG.error("paramerter must be SimpleHttpRequest  or SimpleHttpResponse");
                                throw new IllegalArgumentException(cls.getName() + "." + method.getName() + " param type error. paramtType=" + parameter);
                            }
                        }
                        CloudRequestMapping requestMapping = method.getAnnotation(CloudRequestMapping.class);
                        String url = folderurl + formatMethodUrl(requestMapping.method(), method);   //生成最终的url

                        //目标类的全路径，只要不重复即可，此处是用被代理类的包 +proxy +method+className 的方式
                        String proxyName = cls.getPackage().getName() + ".proxy." + cls.getSimpleName() + "_" + method.getName() + "_" + (counter.incrementAndGet());

                        //动态生成action & 字节码
                        byte[] code = BytesBuilder.dump(proxyName, method);

                        Class<?> exampleClass = this.defineClass(proxyName, code, 0, code.length);


                        Constructor c1 = exampleClass.getDeclaredConstructor(new Class[]{cls});
                        Object obj = cls.newInstance();
                        Action action = (Action) c1.newInstance(new Object[]{obj});    //new
                        action.setName(url);
                        if (path.contains(url)) {
                            throw new IllegalArgumentException(cls.getName() + "." + method.getName() + " url is repeat. url="+url);
                        } else {
                            path.add(url);
                            LOG.info("found:   "+url);
                            result.put(url,action);
                        }

                    }
                }
            }
        }
        return result;
    }



    private static String formatClassUrl(String url) {
        url = StringUtils.trimToEmpty(url);
        if (StringUtils.isBlank(url)) {
            return url;
        }
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }


    private static String formatMethodUrl(String[] urlArray, Method method) {
        if (urlArray == null || urlArray.length < 1) {
            return "/" + method.getName();
        }
        String url = StringUtils.trimToEmpty(urlArray[0]);
        if (StringUtils.isBlank(url)) {
            return "/" + method.getName().toLowerCase();
        }
        if (!url.startsWith("/")) {
            url = "/" + url;
        }

        return url;
    }

}
