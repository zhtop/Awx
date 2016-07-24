package com.example.awx;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.example.awx.Service.Utils;
import com.example.uis.spinner.MaterialSpinner;
import com.example.uis.switchbutton.SwitchButton;
import com.example.utils.DataManager;
import com.example.utils.Pres;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SetFragment extends Fragment implements View.OnClickListener {
    private EditText delay, friendTag, groupnum;
    private Button commit, friendTagCommit, subgroupnum, subgrouptype;
    private SwitchButton addName, repeat;
    private MaterialSpinner spinner;
    private SQLiteDatabase sqLiteDatabase;
    private Cursor cursor;
    private List<String> spinnerData = new ArrayList<>();

    public SetFragment() {

    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set, container, false);
        delay = (EditText) view.findViewById(R.id.set_delay);
        commit = (Button) view.findViewById(R.id.sub_commit);
        addName = (SwitchButton) view.findViewById(R.id.set_add_username);
        addName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Pres.putBoolean(getActivity(), "set_add_username", isChecked);
            }
        });
        addName.setChecked(Pres.getBoolean(getActivity(), "set_add_username", false));

        delay.setText(String.valueOf(Pres.getInt(getActivity(), "set_delay", 100)));
        commit.setOnClickListener(this);

        friendTag = (EditText) view.findViewById(R.id.set_friend_tag);
        friendTag.setText(Pres.getString(getActivity(), "send_friend_tag", ""));
        friendTagCommit = (Button) view.findViewById(R.id.sub_friend_tag);
        friendTagCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Pres.putString(getActivity(), "send_friend_tag", friendTag.getText().toString().trim())) {
                    Utils.makeText(getActivity(), "设置成功");
                }
            }
        });

        groupnum = (EditText) view.findViewById(R.id.set_addgroup_num);
        subgroupnum = (Button) view.findViewById(R.id.sub_addgroup_num);
        subgroupnum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nums = groupnum.getText().toString().trim();
                int intnums = nums.equals("") ? 0 : Integer.valueOf(nums);
                if (Pres.putInt(getActivity(), "set_group_num", intnums)) {
                    Utils.makeText(getActivity(), "设置成功");
                }
            }
        });

        repeat = (SwitchButton) view.findViewById(R.id.set_no_repeat);
        repeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Pres.putBoolean(getActivity(), "set_group_repeat", isChecked);
            }
        });
        repeat.setChecked(Pres.getBoolean(getActivity(), "set_group_repeat", true));

        sqLiteDatabase = DataManager.getManager(getActivity()).getDatabase("db.db");
        cursor = sqLiteDatabase.query("grouptype", null, null, null, null, null, "ids ASC");
        spinner = (MaterialSpinner) view.findViewById(R.id.set_group_type);
        if (spinnerData.size() <= 0 && cursor.moveToFirst()) {
            do {
                spinnerData.add(cursor.getString(cursor.getColumnIndex("types")));
            } while (cursor.moveToNext());
        }
        spinner.setItems(spinnerData);
        spinner.setSelectedIndex(spinnerData.indexOf(Pres.getString(getActivity(), "set_group_type", "全部")));

        subgrouptype = (Button) view.findViewById(R.id.sub_group_type);
        subgrouptype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Pres.putString(getActivity(), "set_group_type", spinnerData.get(spinner.getSelectedIndex()))) {
                    Utils.makeText(getActivity(), "设置成功");
                }
            }
        });
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sub_commit && !delay.getText().toString().trim().equals("")) {
            int d = Integer.valueOf(delay.getText().toString());
            if (Pres.putInt(getActivity(), "set_delay", d > 99 ? d : 100)) {
                Utils.makeText(getActivity(), "设置成功！");
            }
        }
    }
}