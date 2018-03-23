package com.ccut.wp.framework.server.http.utils;

import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Created by lixiaoqing on 2018/3/23.
 */
public class NetWorkUtil {

    private static String LOCAL_IP = "";

    static {
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();

            while(e.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface)e.nextElement();
                Iterator var3 = ni.getInterfaceAddresses().iterator();

                while(var3.hasNext()) {
                    InterfaceAddress inetAdd = (InterfaceAddress)var3.next();
                    if(inetAdd.getAddress() instanceof Inet4Address) {
                        Inet4Address inet4Add = (Inet4Address)inetAdd.getAddress();
                        if(inet4Add.isSiteLocalAddress() && !inet4Add.isLoopbackAddress()) {
                            LOCAL_IP = inet4Add.getHostAddress();
                            break;
                        }
                    }
                }
            }
        } catch (Exception var5) {
            System.out.println("cloud store get local ip exception:" + var5.getMessage());
        }

    }

    public static String getLocalIp() {
        return LOCAL_IP;
    }
}
