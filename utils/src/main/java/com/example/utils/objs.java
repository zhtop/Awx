package com.example.utils;

/**
 * Created by john on 2016/7/24.
 */
public class Objs {

    public static boolean isEquals(Object actual, Object expected) {
        return actual == expected || (actual == null ? expected == null : actual.equals(expected));
    }

}
