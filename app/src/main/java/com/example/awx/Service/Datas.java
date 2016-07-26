package com.example.awx.Service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.utils.Dates;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 2016/7/25.
 */
public class Datas {
    public static String Q_tableGroup = "groupdata";
    public static String Q_tableFriend = "friendata";
    public static String Q_tableGroupAdded = "groupadds";
    public static String Q_tableFriendAdded = "friendaddGroup";
    public static String Q_quareFriendAddwill = "friendAddwill";

    public static List<String> getExistName(SQLiteDatabase sqLiteDatabase, String table) {
        List<String> res = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.query(table, null, "dates='" + Dates.getNowDate() + "'", null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                res.add(cursor.getString(cursor.getColumnIndex("names")));
            } while (cursor.moveToNext());
        }
        return res;
    }

    public static void addItemsToData(SQLiteDatabase sqLiteDatabase, List<String> data,String table) {
        if (data.size() > 0) {
            for (String name : data) {
                addItemToData(sqLiteDatabase, name, table);
            }
        }
    }

    public static long addItemToData(SQLiteDatabase sqLiteDatabase, String name, String table) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("names", name);
        contentValues.put("dates", Dates.getNowDate());
        return sqLiteDatabase.insert(table, null, contentValues);
    }

}
