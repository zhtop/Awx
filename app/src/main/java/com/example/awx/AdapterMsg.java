package com.example.awx;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;


public class AdapterMsg extends CursorAdapter {
    private LayoutInflater mInflater;

    public AdapterMsg(Context context, Cursor c) {
        super(context, c);
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.ac_msg_list_item, parent, false);

    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        TextView ids, tags;
        ids = (TextView) view.findViewById(R.id.msg_item_id);
        tags = (TextView) view.findViewById(R.id.msg_item_tag);

        ids.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex("_id"))));
        tags.setText(cursor.getString(cursor.getColumnIndex("desc")));
    }

}
