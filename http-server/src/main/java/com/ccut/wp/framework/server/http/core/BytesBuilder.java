package com.ccut.wp.framework.server.http.core;

import com.ccut.wp.framework.server.http.dto.HttpRequestKit;
import com.ccut.wp.framework.server.http.dto.HttpResponseKit;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lixiaoqing on 2018/3/23.
 */
public class BytesBuilder implements Opcodes {


    private static final Logger log = LoggerFactory.getLogger(BytesBuilder.class);
    private static final char DOT = '.';
    private static final char SPLITER = '/';
    private final AtomicInteger count = new AtomicInteger(0);

    /**
     * @param destClass 目标类 即要生成的类
     * @param method    被代理的方法
     * @return
     * @throws Exception
     */
    public static final byte[] dump(String destClass, Method method) {
        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        destClass = destClass.replace(DOT, SPLITER);          //目标类
        String srcPath = method.getDeclaringClass().getName().replace(DOT, SPLITER);//被代理的类对象

        cw.visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, destClass, null, "com/ccut/wp/framework/server/http/core/Action", null);
        String basePath = Action.class.getName().replace(DOT, SPLITER);  //目标类的父类


        {
            fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "action", "L" + srcPath + ";", null, null);
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(L" + srcPath + ";)V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, basePath, "<init>", "()V");
            Label l1 = new Label();
            mv.visitLabel(l1);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, destClass, "action", "L" + srcPath + ";");
            Label l2 = new Label();
            mv.visitLabel(l2);

            mv.visitInsn(RETURN);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitLocalVariable("this", "L" + destClass + ";", null, l0, l3, 0);
            mv.visitLocalVariable("action", "L" + srcPath + ";", null, l0, l3, 1);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }

        mv = cw.visitMethod(ACC_PUBLIC, "action",
                "(Lcom/ccut/wp/framework/server/http/dto/HttpRequestKit;Lcom/ccut/wp/framework/server/http/dto/HttpResponseKit;)Ljava/lang/String;", null, new String[]{"java/lang/Exception"});
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, destClass, "action", "L" + srcPath + ";");


        Class cls[] = method.getParameterTypes();
        String paramString = "";
        for (Class c : cls) {
            paramString += "L" + c.getName().replace(DOT, SPLITER) + ";";
            if (c == HttpRequestKit.class) {
                mv.visitVarInsn(ALOAD, 1);
            } else if (c == HttpResponseKit.class) {
                mv.visitVarInsn(ALOAD, 2);
            }

        }

        String returnType = method.getReturnType().getName();

        if (returnType.equals("void")) {


            mv.visitMethodInsn(INVOKEVIRTUAL, srcPath, method.getName(), "(" + paramString + ")V");
            Label l1 = new Label();
            mv.visitLabel(l1);

            mv.visitLdcInsn("");
            mv.visitInsn(ARETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", "L" + destClass + ";", null, l0, l2, 0);
            mv.visitLocalVariable("request", "Lcom/ccut/wp/framework/server/http/dto/HttpRequestKit;", null, l0, l2, 1);
            mv.visitLocalVariable("response", "Lcom/ccut/wp/framework/server/http/dto/HttpResponseKit;", null, l0, l2, 2);

            mv.visitMaxs(3, 3);
            mv.visitEnd();
        } else {

            returnType = returnType.replace(DOT, SPLITER);
            mv.visitMethodInsn(INVOKEVIRTUAL, srcPath, method.getName(), "(" + paramString + ")L" + returnType + ";");

            mv.visitInsn(ARETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", "L" + destClass + ";", null, l0, l2, 0);
            mv.visitLocalVariable("request", "Lcom/ccut/wp/framework/server/http/dto/HttpRequestKit;", null, l0, l2, 1);
            mv.visitLocalVariable("response", "Lcom/ccut/wp/framework/server/http/dto/HttpResponseKit;", null, l0, l2, 2);


            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }

        cw.visitEnd();
//
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream("r:/"+destClass.substring(destClass.lastIndexOf("/"))+".class");
//            fos.write(cw.toByteArray());
//            fos.close();
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);  //log exception
//            throw new RuntimeException(e);
//        }

        return cw.toByteArray();
    }
}
