package com.example.awx.Service;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.utils.DataManager;
import com.example.utils.Dates;
import com.example.utils.Jsons;
import com.example.utils.Pres;
import com.example.utils.Strs;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by john on 2016/7/22.
 */
public class MyThread extends Thread {
    private static String tag = "AWX";
    private AccessibilityService accessibilityService;
    private Handler handler;

    private SQLiteDatabase sqLiteDatabase;
    private ClipboardManager clipboardManager;
    private AsyncHttpClient client;

    private AccessibilityNodeInfo root;
    private Progress progress;

    private List<String> interupAccess = new ArrayList<>();
    private Map<String, Progress> progressGroup = new HashMap<>();
    private int actionType;
    private boolean running = false;

    public MyThread(AccessibilityService accessibilityService, Handler handler) {
        this.accessibilityService = accessibilityService;
        this.handler = handler;
        this.client = new SyncHttpClient();
        sqLiteDatabase = DataManager.getManager(accessibilityService).getDatabase("db.db");
        clipboardManager = (ClipboardManager) accessibilityService.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public void run() {
        while (true) {
            root = accessibilityService.getRootInActiveWindow();
            if (root != null) {
                String packageName = root.getPackageName() == null ? "" : root.getPackageName().toString();
                actionType = Pres.getInt(accessibilityService.getApplicationContext(), "actionType", -1);
                running = Pres.getBoolean(accessibilityService.getApplicationContext(), "running", false);

                progress = progressGroup.get(packageName);
                if (progress == null) {
                    progress = new Progress(packageName, 100, this.handler);
                    progressGroup.put(packageName, progress);
                }
                if (progress.getActionType() != actionType) {
                    progress.getData().clear();
                    progress.getDataTwo().clear();
                    progress.getNodes().clear();
                    progress.set(StepType.None, StepType.Begin, "");
                }
                progress.setActionType(actionType);
                progress.setSleep(Pres.getInt(accessibilityService.getApplicationContext(), "set_delay", 100));
                progress.getSets().setNumFilter(Pres.getInt(accessibilityService.getApplicationContext(), "set_group_num", 0));
                progress.getSets().setNoRepeat(Pres.getBoolean(accessibilityService.getApplicationContext(), "set_group_repeat", true));
                progress.getSets().setAddGroupType(Pres.getString(accessibilityService.getApplicationContext(), "set_group_type", "全部"));

                if (running) {

                    //异常中断处理 检测时间2秒
                    interupAccess.add(progress.getLast() + ":" + progress.getNext());
                    if (interupAccess.size() > 1000) {
                        List<String> listWithoutDup = new ArrayList<>(new HashSet<>(interupAccess));
                        if (listWithoutDup.size() < 2) {
                            progress.set(StepType.None, StepType.Begin, "");
                            //加入通知
                        }
                        interupAccess.clear();
                    }
                    //结束异常终端判断

                    //流程控制
                    //开始逻辑
                    Log.i("action", "last:" + progress.getLast() + "---next:" + progress.getNext());
                    switch (actionType) {
                        case 1://发好友
                            if (packageName.equals(QInfos.name)) {
                                qqSendFriend(root, progress);
                            }
                            break;
                        case 2://发群
                            if (packageName.equals(QInfos.name)) {
                                qqSendGroup(root, progress);
                            }
                            break;
                        case 4://加好友
                            if (packageName.equals(QInfos.name)) {
                                sendQqGroupPeople(root, progress);
                            }
                            break;
                        case 5://加群
                            if (packageName.equals(QInfos.name)) {
                                qqAddGroup(root, progress);
                            }
                            break;
                        case 6://应答
                            if (packageName.equals(QInfos.name)) {
                                qqAutoReplay(root, progress);
                            }
                            break;
                        default:
                            break;
                    }
                }
                //结束逻辑
            }
        }
    }

    private void qqAutoReplay(AccessibilityNodeInfo root, final Progress work) {
        final AccessibilityNodeInfo sendNode = Nodeinfos.findById(root, QInfos.chat_send);
        if (sendNode != null) {
            final AccessibilityNodeInfo chatNode = QInfos.findLastChatItemOfText(root);
            final AccessibilityNodeInfo inputNode = Nodeinfos.findById(root, QInfos.chat_input);
            if (chatNode != null) {
                String key = Strs.isEmpty(chatNode.getText()) ? "" : chatNode.getText().toString();
                if (!Strs.isBlank(key) && !work.getChatLast().equals(key)) {
                    String url = "http://www.zh8341.top/chatword.php";
                    RequestParams rp = new RequestParams();
                    rp.put("word", key);
                    client.get(url, rp, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int i, Header[] headers, byte[] bytes) {
                            List<Map<String, String>> res = Jsons.parseList(new String(bytes));
                            if (res.size() > 0) {
                                work.setChatLast(res.get(0).get("words"));
                                if (!Strs.isBlank(res.get(0).get("words")) && inputNode != null) {
                                    pasteClip(inputNode, res.get(0).get("words"));
                                    Nodeinfos.click(sendNode);
                                }
                            }

                        }

                        @Override
                        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

                        }
                    });

                }
            }
        }
    }


    private void qqSendNear(AccessibilityNodeInfo root, Progress work) {
        switch (work.getNext()) {
            case Begin:
                if (QInfos.back(root, Back.toHome)) {
                    work.set(StepType.Begin, StepType.Tabs, "");
                }
                break;
            case Tabs:
                if (QInfos.tabs(root, 3)) {
                    work.set(StepType.Tabs, StepType.Guide, "");
                }
                break;
            case Guide:
                //此处应加入页面判断
                if (QInfos.titles(root, "动态")) {
//                    AccessibilityNodeInfo nearNode = Nodeinfos.matchByText(root, "附近");
//                    if (nearNode == null ? false : Nodeinfos.click(nearNode.getParent())) {
//                        work.set(StepType.Guide, StepType.Data, "", 1500);
//                    }
                }
                if (QInfos.titles(root, "联系人")) {
                    QInfos.tabs(root, 4);
                }
                break;
            case Data:
//                AccessibilityNodeInfo peopleContainer = Nodeinfos.matchNodeByClass(root, "android.widget.AbsListView");
//                if (peopleContainer != null) {
//                    if (work.getLast().equals(StepType.SearchClick)) {
//                        Nodeinfos.scroll(peopleContainer);
//                    }
//                    if (QInfos.bizFilter(peopleContainer)) {
//                        Nodeinfos.scroll(peopleContainer);
//                    } else {
//                        for (int i = 0; i < peopleContainer.getChildCount(); i++) {
//                            if (peopleContainer.getChild(i) != null) {
//                                if (Nodeinfos.findById(peopleContainer.getChild(i), QInfos.near_people_name) != null) {
//                                    work.getNodes().add(peopleContainer.getChild(i));
//                                }
//                            }
//                        }
//                        if (work.getNodes().size() > 0) {
//                            work.set(StepType.Data, StepType.SearchClick, "");
//                        }
//                    }
//                }
                break;
            case SearchClick:
                if (Nodeinfos.findByText(root, "动态") == null) {
                    QInfos.back(root, Back.fromChat);
                } else {
                    if (work.getNodes().size() > 0) {
                        if (Nodeinfos.click(work.getNodes().get(0))) {
                            work.set(StepType.SearchClick, StepType.SearchEnter, "");
                        }
                    } else {
                        work.set(StepType.SearchClick, StepType.Data, "");
                    }
                }
                break;
            case SearchEnter:
                AccessibilityNodeInfo nameNode = Nodeinfos.findByText(root, "岁");
                if (nameNode != null) {
                    AccessibilityNodeInfo nameNode2 = Nodeinfos.matchNodeByClass(nameNode.getParent(), "android.widget.TextView");
                    if (nameNode2 == null ? false : !work.getData().contains(nameNode2.getText().toString())) {
                        work.getData().add(nameNode2.getText().toString());
                        AccessibilityNodeInfo sendMsg = Nodeinfos.findByText(root, "发消息");
                        if (sendMsg == null ? false : Nodeinfos.click(sendMsg)) {
                            work.set(StepType.SearchEnter, StepType.ChatInput, "");
                        }
                        AccessibilityNodeInfo mi = Nodeinfos.findByText(root, "编辑交友资料");
                        if (mi != null && QInfos.back(root, Back.fromChat)) {
                            work.getNodes().remove(0);
                            work.set(StepType.SearchEnter, StepType.SearchClick, "");
                        }
                    } else {
                        work.getNodes().remove(0);
                        if (QInfos.back(root, Back.fromChat)) {
                            work.set(StepType.SearchEnter, StepType.SearchClick, "");
                        }
                    }
                }
                break;
            case ChatInput:
                AccessibilityNodeInfo chatInput = Nodeinfos.findById(root, QInfos.chat_input);
                if (chatInput == null ? false : pasteClip(chatInput, Utils.getMsgs(sqLiteDatabase, 6))) {
                    progress.set(StepType.ChatInput, StepType.ChatSend, "");
                }
                break;
            case ChatSend:
                AccessibilityNodeInfo commit = Nodeinfos.findById(root, QInfos.near_add_commit);
                if (commit != null) {
                    Nodeinfos.click(commit);
                }
                AccessibilityNodeInfo chatSend = Nodeinfos.findById(root, QInfos.chat_send);
                if (chatSend == null ? false : Nodeinfos.click(chatSend)) {
                    progress.getNodes().get(0).performAction(AccessibilityNodeInfo.ACTION_SELECT);
                    progress.getNodes().remove(0);
                    progress.set(StepType.ChatAdd, StepType.SearchClick, "");
                }
                break;
            default:
                break;
        }

    }

    private void sendQqGroupPeople(AccessibilityNodeInfo root, Progress work) {
        AccessibilityNodeInfo commit = Nodeinfos.findByText(root, "确定");
        if (commit != null) {
            Nodeinfos.click(commit);
        }
        switch (work.getNext()) {
            case Begin:
                if (QInfos.back(root, Back.toHome)) {
                    if (work.getDataTwo().size() > 0) {
                        work.set(StepType.Begin, StepType.SearchClick2, "");
                    } else {
                        work.set(StepType.Begin, StepType.Guide, "");
                    }
                }
                break;
            case Guide:
                if (QInfos.homeGuide(root)) {
                    work.set(StepType.Guide, StepType.Pack, "");
                }
                break;
            case Pack:
                AccessibilityNodeInfo dgcNode = Nodeinfos.findByText(root, "从群聊中选择");
                if (dgcNode == null ? false : dgcNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    work.set(StepType.Pack, StepType.Data, "");
                }
                break;
            case Data:
                //获取群数据 如果群数量不为空 刚不进行这步
                if (work.getData().size() == 0) {
                    List<String> res = Datas.getExistName(sqLiteDatabase, Datas.Q_quareFriendAddwill);//排除今天已经加过的成员的群
                    if (res.size() > 0) {
                        work.setData(res);
                    } else {
                        List<String> searchlist = QInfos.dataFromHomeClick(root, 500);
                        Datas.addItemsToData(sqLiteDatabase, searchlist, Datas.Q_tableGroup);
                        work.setData(searchlist);
                    }
                }
                work.set(StepType.Data, StepType.SearchInput, "");
                break;
            case SearchInput:
                //获取群成员
                if (work.getData().size() > 0) {
                    AccessibilityNodeInfo searchNode = Nodeinfos.matchNodeByClass(root, Nodeinfos.vEdit);
                    if (searchNode == null ? false : Nodeinfos.click(searchNode)) {
                        if (pasteClip(searchNode, work.getData().get(0))) {
                            work.set(StepType.SearchInput, StepType.SearchClick, "");
                        }
                    }
                } else {
                    //没有群请先加点群  确认到这一部分了
                }
                break;
            case SearchClick:
                AccessibilityNodeInfo clickNode = Nodeinfos.findById(root, QInfos.home_search_result);
                if (clickNode == null ? false : Nodeinfos.click(clickNode.getParent())) {
                    work.set(StepType.SearchClick, StepType.SearchData, "");
                }
                break;
            case SearchData:
                //此出将群成员数据保留1周设计 还没做
                work.setDataTwo(QInfos.dataFromHomeClick(root, 500));
                if (work.getDataTwo().size() > 0) {
                    work.set(StepType.SearchData, StepType.Begin, "");
                }
                break;
            case SearchClick2:
                if (work.getDataTwo().size() > 0) {
                    AccessibilityNodeInfo search = Nodeinfos.findById(root, QInfos.home_search);
                    if (search != null) {
                        if (Nodeinfos.click(search)) {
                            work.set(StepType.SearchClick2, StepType.SearchInput2, "");
                        }
                    } else {
                        QInfos.homeScrollToTop(root);
                    }
                } else {
                    Datas.addItemToData(sqLiteDatabase, work.getData().get(0), Datas.Q_tableFriendAdded);//用于排除今天已经添加过成员的群
                    work.getData().remove(0);
                    work.set(StepType.SearchClick2, StepType.Begin, "");
                }
                break;//每个群基本都能到此步
            case SearchInput2:
                AccessibilityNodeInfo input = Nodeinfos.findById(root, QInfos.home_search);
                if (input != null) {
                    if (Nodeinfos.click(input) && work.getDataTwo().size() > 0) {
                        String inputS = (input.getText() == null ? "" : input.getText().toString().trim());
                        //最后一句检测没有成功输入的处理
                        if (inputS.equals("搜索") || inputS.equals("") || Nodeinfos.findById(root, QInfos.home_search_clean) == null) {
                            pasteClip(input, work.getDataTwo().get(0));
                        }
                        work.set(StepType.SearchInput, StepType.SearchEnter2, "", 500);
                    }
                }
                break;
            case SearchEnter2:
                AccessibilityNodeInfo loadNode = Nodeinfos.findByText(root, "加载");
                if (loadNode == null) {
                    AccessibilityNodeInfo resNode = Nodeinfos.matchClickNodeByStr(root, work.getDataTwo().get(0), 0, null);
                    if (resNode != null) {
                        AccessibilityNodeInfo campareNode = Nodeinfos.findByText(resNode, "搜索");
                        if (campareNode == null && Nodeinfos.click(resNode)) {
                            work.set(StepType.SearchEnter2, StepType.ChatInput, "");
                        }
                    }
                }
                break;
            case ChatInput:
                AccessibilityNodeInfo chatInputNode = Nodeinfos.findById(root, QInfos.chat_input);
                if (chatInputNode != null) {
                    pasteClip(chatInputNode, Utils.getMsgs(sqLiteDatabase, 6));
                    work.set(StepType.ChatInput, StepType.ChatSend, "");
                }
                break;
            case ChatSend:
                AccessibilityNodeInfo chatSendNode = Nodeinfos.findById(root, QInfos.chat_send);
                if (chatSendNode != null) {
                    Nodeinfos.click(chatSendNode);
                    work.getDataTwo().remove(0);
                    work.set(StepType.ChatSend, StepType.ChatAdd, "");
                }
                break;
            case ChatAdd:
                AccessibilityNodeInfo chaAddNode = Nodeinfos.findByText(root, "加为好友");
                if (chaAddNode == null ? false : Nodeinfos.click(chaAddNode)) {
                    work.set(StepType.ChatAdd, StepType.AddValidate, "");
                }
            case AddValidate:
                AccessibilityNodeInfo aNextNode = Nodeinfos.findById(root, QInfos.title_right);
                if (Nodeinfos.click(aNextNode)) {
                    work.set(StepType.AddValidate, StepType.ChatAddSend, "");
                }
                break;
            case ChatAddSend:
                AccessibilityNodeInfo vNextNode = Nodeinfos.findById(root, QInfos.title_right);
                if (Nodeinfos.click(vNextNode)) {
                    work.set(StepType.ChatAddSend, StepType.Begin, "");
                }
                break;
            default:
                break;
        }
    }

    private void qqSendFriend(AccessibilityNodeInfo root, Progress work) {
        switch (work.getNext()) {
            case None:
                if (QInfos.back(root, Back.toHome)) {
                    Pres.putBoolean(accessibilityService, "running", false);
                    upNotifycation(work.getActionType(), false);
                    work.set(StepType.None, StepType.None, "");
                }
                break;
            case Begin:
                if (QInfos.back(root, Back.toHome)) {
                    if (work.getData().size() > 0) {
                        work.set(StepType.Begin, StepType.SearchClick, "");
                    } else {
                        List<String> res = Datas.getExistName(sqLiteDatabase, Datas.Q_tableFriend);
                        if (res.size() > 0) {
                            work.setData(res);
                        } else {
                            work.set(StepType.Begin, StepType.Guide, "");
                        }
                    }
                }
                break;
            case Guide:
                AccessibilityNodeInfo clickMorePanel = Nodeinfos.matchClickNodeByStr(root, "快捷入口", 1, null);
                AccessibilityNodeInfo clickMore = Nodeinfos.matchClickNodeByStr(root, "多人聊天", 0, null);
                if (clickMorePanel != null) {
                    Nodeinfos.click(clickMorePanel);
                }
                if (clickMore != null ? Nodeinfos.click(clickMore) : false) {
                    work.set(StepType.Guide, StepType.Pack, "");
                }
                break;
            case Pack:
                List<AccessibilityNodeInfo> list = root.findAccessibilityNodeInfosByViewId(QInfos.friend_expand);
                if (list.size() > 0) {
                    if (Nodeinfos.click(list.get(0))) {
                        work.set(StepType.Pack, StepType.Expand, "");
                    }
                }
                break;
            case Expand:
                List<AccessibilityNodeInfo> lists = root.findAccessibilityNodeInfosByViewId(QInfos.friend_expand);
                if (lists.size() > 0) {
                    for (int i = lists.size() - 1; i > -1; ) {
                        if (Nodeinfos.click(lists.get(i))) {
                            i--;
                        }
                        if (i == 0) {
                            work.set(StepType.Expand, StepType.Data, "采集好友名字");
                        }
                    }
                }
                break;
            case Data:
                //群数据处理 获取并添加到数据库
                List<String> friedlist = QInfos.dataFromHomeClick(root, 500);
                Datas.addItemsToData(sqLiteDatabase, friedlist, Datas.Q_tableFriend);
                work.setData(friedlist);
                if (work.getData().size() > 0) {
                    work.set(StepType.Data, StepType.Begin, "");
                }
                break;
            case SearchClick:
                AccessibilityNodeInfo search = Nodeinfos.findById(root, QInfos.home_search);
                if (search != null) {
                    if (Nodeinfos.click(search)) {
                        work.set(StepType.SearchClick, StepType.SearchInput, "");
                    }
                } else {
                    QInfos.homeScrollToTop(root);
                }
                break;
            case SearchInput:
                AccessibilityNodeInfo input = Nodeinfos.findById(root, QInfos.home_search);
                if (input != null) {
                    if (work.getData().size() > 0) {
                        String inputS = (input.getText() == null ? "" : input.getText().toString().trim());
                        //最后一句检测没有成功输入的处理
                        if (inputS.equals("搜索") || inputS.equals("") || Nodeinfos.findById(root, QInfos.home_search_clean) == null) {
                            if (pasteClip(input, work.getData().get(0))) {
                                work.set(StepType.SearchInput, StepType.SearchEnter, "", 200);
                            }
                        }
                    } else {
                        if (work.getLast() == StepType.SearchEnter) {
                            work.set(StepType.SearchInput, StepType.None, "");
                        }
                    }
                }
                break;
            case SearchEnter:
                AccessibilityNodeInfo rs = QInfos.friendSearchResult(root, work.getData().get(0));
                if (rs == null) {
                    work.getData().remove(0);
                    AccessibilityNodeInfo clean = Nodeinfos.findById(root, QInfos.home_search_clean);
                    boolean ass = clean == null ? false : Nodeinfos.click(clean);
                    work.set(StepType.SearchEnter, StepType.SearchInput, "");
                } else {
                    if (Nodeinfos.click(rs)) {
                        work.set(StepType.SearchEnter, StepType.ChatInput, "");
                    }
                }
                break;
            case ChatInput:
                AccessibilityNodeInfo chatSend = Nodeinfos.findById(root, QInfos.chat_send);
                if (chatSend != null) {
                    AccessibilityNodeInfo inStr = Nodeinfos.findById(root, QInfos.chat_input);
                    if (inStr != null) {
                        pasteClip(inStr, Utils.getMsgs(sqLiteDatabase, 7));
                    }
                    work.set(StepType.ChatInput, StepType.ChatSend, "");
                }
                break;
            case ChatSend:
                AccessibilityNodeInfo chatSending = Nodeinfos.findById(root, QInfos.chat_send);
                if (chatSending == null ? false : Nodeinfos.click(chatSending)) {
                    work.getData().remove(0);
                    work.set(StepType.ChatSend, StepType.Begin, "");
                }
                break;
            default:
                break;
        }

    }

    private void qqSendGroup(AccessibilityNodeInfo root, Progress work) {
        AccessibilityNodeInfo iknow = Nodeinfos.findByText(root, "我知道了");
        if (iknow == null ? false : iknow.isClickable()) {
            Nodeinfos.click(iknow);
        }
        switch (work.getNext()) {
            case Begin:
                if (QInfos.back(root, Back.toHome)) {
                    if (work.getData().size() > 0) {
                        work.set(StepType.Begin, StepType.SearchClick, "发群【" + work.getData().get(0) + "】");
                    } else {
                        if (work.getLast() == StepType.ChatSend) {
                            Pres.putBoolean(accessibilityService, "running", false);
                            upNotifycation(work.getActionType(), false);
                            work.set(StepType.Begin, StepType.None, "所有群发送完毕");
                        } else {
                            List<String> resData = Datas.getExistName(sqLiteDatabase, Datas.Q_tableGroup);
                            if (resData.size() > 0) {
                                work.setData(resData);
                            } else {
                                work.set(StepType.Begin, StepType.Guide, "开始群发群");
                            }
                        }
                    }
                }
                break;
            case Guide:
                if (QInfos.homeGuide(root)) {
                    work.set(StepType.Guide, StepType.Pack, "");
                }
                break;
            case Pack:
                AccessibilityNodeInfo dgcNode = Nodeinfos.findByText(root, "从群聊中选择");
                if (dgcNode == null ? false : dgcNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    work.set(StepType.Pack, StepType.Data, "");
                }
                break;
            case Data:
                //群数据处理 获取并添加到数据库
                List<String> findGroup = QInfos.dataFromHomeClick(root, 500);
                Datas.addItemsToData(sqLiteDatabase, findGroup, Datas.Q_tableGroup);
                work.setData(findGroup);
                if (work.getData().size() > 0) {
                    work.set(StepType.Data, StepType.Begin, "");
                }
                break;
            case SearchClick:
                AccessibilityNodeInfo search = Nodeinfos.findById(root, QInfos.home_search);
                if (search != null) {
                    if (Nodeinfos.click(search)) {
                        work.set(StepType.SearchClick, StepType.SearchInput, "");
                    }
                } else {
                    QInfos.homeScrollToTop(root);
                }
                break;
            case SearchInput:
                AccessibilityNodeInfo input = Nodeinfos.findById(root, QInfos.home_search);
                if (input != null) {
                    if (work.getData().size() > 0) {
                        String inputS = (input.getText() == null ? "" : input.getText().toString().trim());
                        //最后一句检测没有成功输入的处理
                        if (inputS.equals("搜索") || inputS.equals("") || Nodeinfos.findById(root, QInfos.home_search_clean) == null) {
                            pasteClip(input, work.getData().get(0));
                        } else {
                            work.set(StepType.SearchInput, StepType.SearchEnter, "");
                        }
                    }
                }
                break;
            case SearchEnter:
                List<AccessibilityNodeInfo> rs = QInfos.groupSearchResult(root, work.getData().get(0));
                if (rs.size() == 0) {
                    work.getData().remove(0);
                    AccessibilityNodeInfo cleans = Nodeinfos.findById(root, QInfos.home_search_clean);
                    Nodeinfos.click(cleans);
                    work.set(StepType.SearchEnter, StepType.SearchInput, "");
                }
                if (rs.size() == 1) {
                    rs.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    work.set(StepType.SearchEnter, StepType.ChatInput, "");
                }
                if (rs.size() > 1) {
                    for (int i = 1; i < rs.size(); i++) {
                        //最新版已经去掉了群后面号码的ID所以改变算法，此处用于处理同名不同号的群
                        AccessibilityNodeInfo subText = Nodeinfos.findByText(rs.get(i), "(");
                        if (subText != null) {
                            String substr = (subText.getText() == null ? "" : subText.getText().toString());
                            if (!substr.equals("")) {
                                work.getData().add(substr.substring(1, substr.length() - 1));
                            }
                        }
                    }
                    rs.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    work.set(StepType.SearchEnter, StepType.ChatInput, "");
                }
                break;
            case ChatInput:
                //无论是否禁言 尝试强制发送
                AccessibilityNodeInfo inSend = Nodeinfos.findById(root, QInfos.chat_send);
                if (inSend != null) {
                    AccessibilityNodeInfo inStr = Nodeinfos.findById(root, QInfos.chat_input);
                    if (inStr != null) {
                        pasteClip(inStr, Utils.getMsgs(sqLiteDatabase, 3));
                    }
                    work.set(StepType.ChatInput, StepType.ChatSend, "");
                }
                break;
            case ChatSend:
                //此处加入群公告弹出处理
                AccessibilityNodeInfo chatSend = Nodeinfos.findById(root, QInfos.chat_send);
                if (chatSend != null) {
                    chatSend.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    work.getData().remove(0);
                    work.set(StepType.ChatSend, StepType.Begin, "");
                }
                break;
            default:
                break;
        }
    }

    private void qqAddGroup(AccessibilityNodeInfo root, Progress work) {
        switch (work.getNext()) {
            case None:
                if (QInfos.back(root, Back.toHome)) {
                    Pres.putBoolean(accessibilityService, "running", false);
                    upNotifycation(work.getActionType(), false);
                    work.set(StepType.Begin, StepType.None, "加群完毕");
                }
            case Begin:
                //如果是群列表界面则不用返回
                if (QInfos.back(root, Back.toHome)) {
                    if (work.getData().size() > 0) {
                        work.set(StepType.Begin, StepType.SearchClick, "");
                    } else {
                        //已添加群过滤数据处理
                        if (work.getSets().isNoRepeat()) {
                            List<String> added = Datas.getExistName(sqLiteDatabase, Datas.Q_tableGroupAdded);
                            if (added.size() > 0) {
                                work.setData(added);
                                work.setDataTwo(added);
                                work.set(StepType.Begin, StepType.Tabs, "开始添加群,当日已成功添加【" + work.getDataTwo().size() + "】个群");
                            }
                        } else {
                            work.set(StepType.Begin, StepType.Tabs, "开始添加群");
                        }
                    }
                }
                break;
            case Tabs:
                if (QInfos.tabs(root, 3)) {
                    work.set(StepType.Tabs, StepType.Guide, "");
                }
                break;
            case Guide:
                //此处应加入页面判断
                if (QInfos.titles(root, "动态")) {
                    AccessibilityNodeInfo nearGroupNode = QInfos.findNearGroup(root);
                    if (nearGroupNode == null ? false : Nodeinfos.click(nearGroupNode)) {
                        work.set(StepType.Guide, StepType.Fliter, "");
                    }
                }
                if (QInfos.titles(root, "联系人")) {
                    QInfos.tabs(root, 4);
                }
                break;
            case Fliter:
                //此处应加入页面判断
                Utils.sleep(500);
                AccessibilityNodeInfo hotType = Nodeinfos.findByText(root, "热门分类");
                if (QInfos.titles(root, "附近的群") && hotType != null) {
                    AccessibilityNodeInfo types = Nodeinfos.findByText(root, work.getSets().getAddGroupType());
                    if (types != null) {
                        if (types.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                            work.set(StepType.Fliter, StepType.Data, "过滤分类群");
                        }
                    }
                }
                break;
            case Data:
                Utils.sleep(300);
                AccessibilityNodeInfo atFind = Nodeinfos.findByText(root, "正在寻找");
                if (atFind != null) {
                    work.set(StepType.Data, StepType.BackTell, "");
                } else {
                    AccessibilityNodeInfo list = Nodeinfos.matchNodeByClass(root, "android.widget.AbsListView");
                    if (list != null) {
                        if (QInfos.addGroupData(list, work)) {
                            work.set(StepType.Data, StepType.SearchClick, "");
                        } else {
                            work.set(StepType.Data, StepType.BackTell, "");
                        }
                    }
                }
                break;
            case SearchClick:
                AccessibilityNodeInfo more = Nodeinfos.findById(root, "com.tencent.mobileqq:id/info");
                if (more != null) {
                    if (more.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        work.getNodes().clear();
                        work.getData().remove(work.getData().size() - 1);
                        work.set(StepType.SearchClick, StepType.Data, "");
                    }
                } else {
                    if (work.getNodes().get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        work.set(StepType.SearchClick, StepType.SearchEnter, "添加群:" + work.getData().get(work.getData().size() - 1));
                    }
                }
                break;
            case SearchEnter:
                //页面判断
                AccessibilityNodeInfo pageAssis = Nodeinfos.findById(root, "com.tencent.mobileqq:id/common_xlistview");
                if (pageAssis != null) {
                    AccessibilityNodeInfo addGroup = Nodeinfos.findByText(root, "申请加群");
                    if (addGroup != null ? addGroup.performAction(AccessibilityNodeInfo.ACTION_CLICK) : false) {
                        work.set(StepType.SearchEnter, StepType.ChatSend, "");
                    } else {
                        AccessibilityNodeInfo pay = Nodeinfos.findByText(root, "支付");
                        AccessibilityNodeInfo sendmsg = Nodeinfos.findByText(root, "发消息");
                        if (pay != null || sendmsg != null) {
                            work.getNodes().clear();
                            if (QInfos.back(root, Back.fromChat)) {
                                work.set(StepType.SearchEnter, StepType.Data, "");
                            }
                        }
                    }
                }
                break;
            case ChatSend:
                Utils.sleep(300);
                if (QInfos.titles(root, "验证信息")) {
                    AccessibilityNodeInfo sendNode = Nodeinfos.findByText(root, "发送");
                    AccessibilityNodeInfo inputNode = root.findFocus(AccessibilityNodeInfo.ACTION_FOCUS);
                    if (inputNode != null) {
                        pasteClip(inputNode, Utils.getMsgs(sqLiteDatabase, 5));
                    }
                    if (sendNode != null) {
                        if (sendNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                            work.set(StepType.ChatSend, StepType.ChatBack, "");
                        }
                    }
                }
                break;
            case ChatBack:
                //加入计数统计添加失败辅助
                Utils.sleep(500);
                String groupname = work.getData().get(work.getData().size() - 1);
                int groupnum = work.getDataTwo().size();

                AccessibilityNodeInfo anwser = Nodeinfos.findByText(root, "答案错误");
                if (anwser != null) {
                    if (QInfos.back(root, Back.fromChat)) {
                        work.set(StepType.ChatBack, StepType.ChatBack, "");
                    }
                }

                if (QInfos.titles(root, "发送成功")) {
                    AccessibilityNodeInfo close = Nodeinfos.findByText(root, "关闭");
                    if (close != null ? close.performAction(AccessibilityNodeInfo.ACTION_CLICK) : false) {
                        work.getDataTwo().add(groupname);
                        Datas.addItemToData(sqLiteDatabase, groupname, Datas.Q_tableGroupAdded);
                        work.getNodes().clear();
                        work.set(StepType.ChatBack, StepType.Data, groupname + ":请求成功:" + groupnum + 1);
                    }
                }
                AccessibilityNodeInfo backAdd = Nodeinfos.findByText(root, "申请加群");
                if (backAdd != null) {
                    if (QInfos.back(root, Back.fromChat)) {
                        work.getDataTwo().add(groupname);
                        Datas.addItemToData(sqLiteDatabase, groupname, Datas.Q_tableGroupAdded);
                        work.getNodes().clear();
                        work.set(StepType.ChatBack, StepType.Data, groupname + ":请求成功:" + groupnum + 1);
                    }
                }
                //此处加入每天加群数限制
                if (work.getCount().size() > 100) {
                    work.getNodes().clear();
                    work.set(StepType.ChatBack, StepType.None, "已完成当日加群最大数:" + work.getData().size());
                }
                break;
            case BackTell:
                //返回后情况处理
                if (QInfos.addGroupScroll(root)) {
                    work.set(StepType.BackTell, StepType.Data, "查找下一个群");
                }
                break;
            default:
                break;
        }
    }

    public boolean pasteClip(AccessibilityNodeInfo input, String str) {
        ClipData clip = ClipData.newPlainText("label", str);
        clipboardManager.setPrimaryClip(clip);
        input.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        return input.performAction(AccessibilityNodeInfo.ACTION_PASTE);
    }

    private void upNotifycation(int action, boolean runs) {
        Message message = new Message();
        message.what = 0;
        Bundle bundle = new Bundle();
        bundle.putInt("actionType", action);
        bundle.putBoolean("running", runs);
        message.setData(bundle);
        handler.sendMessage(message);
    }
}
