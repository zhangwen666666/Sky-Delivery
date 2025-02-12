package com.sky.utils;

/**
 * 处理Integer类型和Double类型的数据
 * 为空则返回0，否则返回原值
 */
public class NumberUtil {
    private NumberUtil(){}

    public static Integer check(Integer num){
        return num == null ? 0 : num;
    }

    public static Double check(Double num){
        return num == null ? 0.0 : num;
    }
}
