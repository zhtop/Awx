package com.example.awx.Service;

import android.os.Handler;
import android.os.Message;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by john on 2016/7/23.
 */
public class Progress {
    private int actionType = -1;
    private List<String> count = new ArrayList();
    private List<String> data = new ArrayList();
    private List<String> dataTwo = new ArrayList();
    private StepType last;
    private StepType next;
    private List<AccessibilityNodeInfo> nodes = new ArrayList();
    private String packageName;
    private Sets sets = new Sets();
    private long sleep;
    private String tipString;
    private int totalCount = 0;
    private Handler handler;

    public Progress(String paramString, int paramInt, Handler handler) {
        this.packageName = paramString;
        this.sleep = paramInt;
        this.last = StepType.None;
        this.next = StepType.Begin;
        this.handler = handler;
    }

    public int getActionType() {
        return this.actionType;
    }

    public List<String> getCount() {
        return this.count;
    }

    public List<String> getData() {
        return this.data;
    }

    public List<String> getDataTwo() {
        return this.dataTwo;
    }

    public StepType getLast() {
        return this.last;
    }

    public StepType getNext() {
        return this.next;
    }

    public List<AccessibilityNodeInfo> getNodes() {
        return this.nodes;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public Sets getSets() {
        return this.sets;
    }

    public String getTipString() {
        return this.tipString;
    }

    public void set(StepType paramStepType1, StepType paramStepType2, String paramString) {
        try {
            Thread.sleep(this.sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.last = paramStepType1;
        this.next = paramStepType2;
        this.tipString = paramString;
    }

    public void set(StepType paramStepType1, StepType paramStepType2, String paramString, long paramLong) {
        long l = this.sleep;
        try {
            Thread.sleep(l + paramLong);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.last = paramStepType1;
        this.next = paramStepType2;
        this.tipString = paramString;
     }

    public void setActionType(int paramActionType) {
        this.actionType = paramActionType;
    }

    public void setCount(List<String> paramList) {
        this.count = paramList;
    }

    public void setData(List<String> paramList) {
        this.data = paramList;
    }

    public void setDataTwo(List<String> paramList) {
        this.dataTwo = paramList;
    }

    public void setNodes(List<AccessibilityNodeInfo> paramList) {
        this.nodes = paramList;
    }

    public void setPackageName(String paramString) {
        this.packageName = paramString;
    }

    public void setSets(Sets paramSets) {
        this.sets = paramSets;
    }

    public void setTipString(String paramString) {
        this.tipString = paramString;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public long getSleep() {
        return sleep;
    }

    public void setSleep(long sleep) {
        this.sleep = sleep;
    }

    public void pushMsg(String str) {
        if (handler != null) {
            Message message = new Message();

            handler.sendMessage(message);
        }
    }
}
