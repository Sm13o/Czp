package com.itheima.reggie.common;

/**
 * 基于ThreadLocal封装工具类，用户保存和获取当前登录用户id
 */
public class BaseContext {
    public static final ThreadLocal<Long> tl=new ThreadLocal();

    public static void setById(long id){
        tl.set(id);
    }

    public static Long getByID(){
       return tl.get();
    }
}
