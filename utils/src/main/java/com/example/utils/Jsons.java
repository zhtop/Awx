package com.example.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by john on 2016/7/24.
 */
public class Jsons {
    public static boolean isPrintException = true;
    public static List<Map<String, String>> parseList(String jsonStr) {
        if (jsonStr == null || "".equals(jsonStr)) {
            return null;
        }
        List<Map<String, String>> retList = new ArrayList<>();
        try {
            JSONArray data = new JSONArray(jsonStr);
            for (int i = 0; i < data.length(); i++) {
                JSONObject json = data.getJSONObject(i);
                Map<String, String> tmpMap = new HashMap<>();
                for (Iterator iter = json.keys(); iter.hasNext(); ) {
                    String key = (String) iter.next();
                    if (key == null ? false : !key.trim().equals("")) {
                        tmpMap.put(key, json.getString(key));
                    }
                }
                retList.add(tmpMap);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retList;
    }

    public static String getString(String jsonData, String key, String defaultValue) {
        if (Strs.isEmpty(jsonData)) {
            return defaultValue;
        }

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            return getString(jsonObject, key, defaultValue);
        } catch (JSONException e) {
            if (isPrintException) {
                e.printStackTrace();
            }
            return defaultValue;
        }
    }

    public static String getString(JSONObject jsonObject, String key, String defaultValue) {
        if (jsonObject == null || Strs.isEmpty(key)) {
            return defaultValue;
        }

        try {
            return jsonObject.getString(key);
        } catch (JSONException e) {
            if (isPrintException) {
                e.printStackTrace();
            }
            return defaultValue;
        }
    }


}
