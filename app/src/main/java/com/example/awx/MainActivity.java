package com.example.awx;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.awx.Service.Utils;
import com.example.utils.Dates;
import com.example.utils.Jsons;
import com.example.utils.Pres;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity {
    private TabStrip tabStrip;
    private ViewPager viewPager;
    private TabStripDataAdapter tabStripDataAdapter;
    private AsyncHttpClient client;
    private RequestParams params;
    private String ids;
    private TextView spot, spotNum;
    private ClipboardManager clipboardManager;
    private Button spotCopy;
    private List<Map<String, String>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        if (!Utils.isListenNotify(getApplicationContext())) {
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }

        spot = (TextView) findViewById(R.id.awx_spot);
        spotNum = (TextView) findViewById(R.id.awx_spot_num);
        spotCopy = (Button) findViewById(R.id.awx_ids_button);
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        spotCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clip = ClipData.newPlainText("label", ids);
                clipboardManager.setPrimaryClip(clip);
                Utils.makeText(getApplicationContext(), "复制成功!授权QQ:8383246");
            }
        });
        ids = Utils.getids(getApplicationContext());
        params = new RequestParams();
        params.put("ids", ids);
        params.put("logintime", Dates.getNowDate());

        spotNum.setText("授权码:" + ids.substring(0, 18));

        client = new AsyncHttpClient();

        tabStrip = (TabStrip) findViewById(R.id.main_tabstrip);
        tabStrip.post(new Runnable() {
            @Override
            public void run() {
                client.post("http://www.zh8341.top/regspot.php", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {

                    }

                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

                    }
                });
            }
        });
        viewPager = (ViewPager) findViewById(R.id.man_viewpager);
        tabStripDataAdapter = new TabStripDataAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabStripDataAdapter);
        tabStrip.setViewPager(viewPager);

    }

    @Override
    protected void onResume() {
        super.onResume();
        tabStrip.post(new Runnable() {
            @Override
            public void run() {
                client.get("http://www.zh8341.top/spot.php", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                        list = Jsons.parseList(new String(bytes));
                        boolean reg = true;
                        if (list.size() > 0) {
                            String times = list.get(0).get("times");
                            if (!times.equals("null") && times != null) {
                                reg = Dates.comDateStr(times, Dates.getNowDate());
                                if (reg) {
                                    spot.setText("已授权");
                                    spotNum.setVisibility(View.GONE);
                                    spotCopy.setVisibility(View.GONE);
                                } else {
                                    spot.setText("授权过期");
                                    spotNum.setVisibility(View.VISIBLE);
                                    spotCopy.setVisibility(View.VISIBLE);
                                }
                            } else {
                                spot.setText("试用中");
                                spotNum.setVisibility(View.VISIBLE);
                                spotCopy.setVisibility(View.VISIBLE);
                            }
                            Pres.putBoolean(getApplication(), "awx_spot", reg);
                        }

                    }

                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                        spot.setText("联网重试");
                    }
                });

            }
        });
    }
}
