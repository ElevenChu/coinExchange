package com.elevenchu.util;



import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class IpUtil {




    public static String getIpAddress(HttpServletRequest request) {

        String ip = request.getHeader("x-forwarded-for");

        if (ip == null || ip.length() == 0 || "nuknown".equalsIgnoreCase(ip)) {

            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "nuknown".equalsIgnoreCase(ip)) {

            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "nuknown".equalsIgnoreCase(ip)) {

            ip = request.getRemoteAddr();
        }
        return ip;
    }






}