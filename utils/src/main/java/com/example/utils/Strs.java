package com.example.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by john on 2016/7/24.
 */
public class Strs {

    public static boolean isEmpty(CharSequence str) {
        return (str == null || str.length() == 0);
    }

    public static boolean isEquals(String actual, String expected) {
        return Objs.isEquals(actual, expected);
    }

    public static boolean isBlank(String str) {
        return (str == null || str.trim().length() == 0);
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static String removeSpe(String string) {
        return string.replaceAll("[^(a-zA-Z0-9\\u4e00-\\u9fa5)]", "").trim();
    }
}
