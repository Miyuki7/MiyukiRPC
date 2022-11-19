package com.miyuki.mrpc.core.common.utils;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: miyuki
 * @Date: 2022/11
 * @Description:
 */
public class StringUtil {
    public static boolean isBlank(String s){
        if (s == null || s.length() == 0){
            return true;
        }

        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))){
                return false;
            }
        }
        return true;

    }

    public static boolean isEmpty(String s){
        if (s == null || s.length() == 0){
            return true;
        }
        return false;

    }
}
