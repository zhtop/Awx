package com.example.awx;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by john on 2016/7/21.
 */
public class NewsAdapter extends BaseAdapter {
    List<Map<String, String>> data;
    Context context;

    public NewsAdapter(Context context, List<Map<String, String>> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(context, R.layout.news_item, null);
        TextView ids, titles, times;
        ids = (TextView) view.findViewById(R.id.news_id);
        titles = (TextView) view.findViewById(R.id.news_title);
        times = (TextView) view.findViewById(R.id.news_times);
        ids.setText(data.get(position).get("ids"));
        titles.setText(data.get(position).get("titles"));
        times.setText(data.get(position).get("times"));
        return view;
    }
}
