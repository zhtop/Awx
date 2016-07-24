package com.example.awx;


import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.example.awx.Service.Utils;
import com.example.uis.spinner.MaterialSpinner;
import com.example.utils.DataManager;

import java.util.ArrayList;
import java.util.List;


public class MsgFragmen extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {
    private ListView listView;
    private EditText msgs;
    private Button edit, add;
    private AdapterMsg adMsgList;
    private SQLiteDatabase sqLiteDatabase;
    private Cursor cursor;
    private Handler handler;
    private View select;
    private LinearLayout listfill, leftfill;
    private ImageView msgclean;
    private InputMethodManager imm;
    private MaterialSpinner spinner;
    private List<String> spinnerData = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private TextView ids;

    public MsgFragmen() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sqLiteDatabase = DataManager.getManager(getActivity()).getDatabase("db.db");
        cursor = sqLiteDatabase.query("msgtype", null, null, null, null, null, "types ASC");
        if (spinnerData.size() <= 0 && cursor.moveToFirst()) {
            do {
                spinnerData.add(cursor.getString(cursor.getColumnIndex("desc")));
            } while (cursor.moveToNext());
        }
        imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                cursor = sqLiteDatabase.query("msg_list", null, null, null, null, null, null);
                adMsgList = new AdapterMsg(getActivity().getApplicationContext(), cursor);
                listView.setAdapter(adMsgList);
            }
        };


        View view = inflater.inflate(R.layout.fragment_msg, container, false);

        spinner = (MaterialSpinner) view.findViewById(R.id.msg_t);
        spinner.setItems(spinnerData);

        listView = (ListView) view.findViewById(R.id.msg_list);
        msgs = (EditText) view.findViewById(R.id.msg_strs);
        ids = (TextView) view.findViewById(R.id.msg_ids);

        edit = (Button) view.findViewById(R.id.msg_edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put("texts", msgs.getText().toString());
                values.put("types", spinner.getSelectedIndex());
                String whereClause = "_id=?";
                String[] whereArgs = {ids.getText().toString().trim()};
                if (sqLiteDatabase.update("sendstrs", values, whereClause, whereArgs) > 0) {//
                    Utils.makeText(getActivity(), "修改成功!");
                }
                handler.sendEmptyMessage(0);
            }
        });
        add = (Button) view.findViewById(R.id.msg_add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("types", spinner.getSelectedIndex());
                contentValues.put("texts", msgs.getText().toString());
                sqLiteDatabase.insert("sendstrs", null, contentValues);
                handler.sendEmptyMessage(0);
            }
        });

        msgclean = (ImageView) view.findViewById(R.id.msg_msg_clean);
        msgclean.setVisibility(View.GONE);

        listfill = (LinearLayout) view.findViewById(R.id.msg_list_fill);
        leftfill = (LinearLayout) view.findViewById(R.id.msg_left_fill);

        msgs.addTextChangedListener(new TextChange(msgclean));
        msgs.setOnClickListener(this);
        msgclean.setOnClickListener(this);
        listfill.setOnClickListener(this);
        leftfill.setOnClickListener(this);
        listView.setOnItemClickListener(this);


        handler.sendEmptyMessage(0);

        return view;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (select != null) {
            select.setVisibility(View.INVISIBLE);
        }
        select = view.findViewById(R.id.msg_item_ind);
        select.setVisibility(View.VISIBLE);

        if (cursor.moveToPosition(position)) {
            ids.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex("_id"))));
            msgs.setText(new String(cursor.getBlob(cursor.getColumnIndex("texts"))));
            spinner.setSelectedIndex(cursor.getInt(cursor.getColumnIndex("types")));
            edit.setVisibility(View.VISIBLE);
            ((LinearLayout) view.findViewById(R.id.msg_item_del)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sqLiteDatabase.execSQL("delete from sendstrs where _id = " + cursor.getInt(cursor.getColumnIndex("_id")));
                    handler.sendEmptyMessage(0);
                }
            });
        }
        hidekeyboard(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.msg_msg_clean:
                msgs.setText("");
                ids.setText("");
                edit.setVisibility(View.GONE);
                break;
            case R.id.msg_strs:
                hidekeyboard(msgs);
            default:
                hidekeyboard(null);
                break;
        }
    }

    public void hidekeyboard(EditText editText) {
        if (keyBoarded()) {
            imm.hideSoftInputFromWindow(msgs.getWindowToken(), 0);
            return;
        }
        if (editText != null) {
            imm.showSoftInput(editText, 0);
        }
    }

    public class TextChange implements TextWatcher {
        private ImageView imageView;

        public TextChange(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }


    }

    public boolean keyBoarded() {
        View decorView = getActivity().getWindow().getDecorView();
        Rect rect = new Rect();
        decorView.getWindowVisibleDisplayFrame(rect);
        int displayHight = rect.bottom - rect.top;
        int hight = decorView.getHeight();
        return ((double) displayHight / hight < 0.8);
    }

}
