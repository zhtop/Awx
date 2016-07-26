package com.example.awx.Service;


import android.view.accessibility.AccessibilityNodeInfo;

import com.example.utils.Strs;

import java.util.ArrayList;
import java.util.List;

public class QInfos {
    public static final String cancel_search = "com.tencent.mobileqq:id/btn_cancel_search";
    public static final String chat_input = "com.tencent.mobileqq:id/input";
    public static final String chat_input_bar = "com.tencent.mobileqq:id/inputBar";
    public static final String chat_send = "com.tencent.mobileqq:id/fun_btn";
    public static final String friend_expand = "com.tencent.mobileqq:id/group_item_layout";
    public static final String home_list = "com.tencent.mobileqq:id/recent_chat_list";
    public static final String home_search = "com.tencent.mobileqq:id/et_search_keyword";
    public static final String home_search_clean = "com.tencent.mobileqq:id/ib_clear_text";
    public static final String home_search_result = "com.tencent.mobileqq:id/title";
    public static final String home_tabs = "android:id/tabs";
    public static final String many_root = "android:id/content";
    public static final String name = "com.tencent.mobileqq";
    public static final String near_add_commit = "com.tencent.mobileqq:id/dialogRightBtn";
    public static final String near_people_name = "com.tencent.mobileqq:id/text1";
    public static final String qzone_near_container = "com.tencent.mobileqq:id/laba_entrys_layout";
    public static final String title_center = "com.tencent.mobileqq:id/ivTitleName";
    public static final String title_left = "com.tencent.mobileqq:id/ivTitleBtnLeft";
    public static final String title_right = "com.tencent.mobileqq:id/ivTitleBtnRightText";
    public static final String titles_container = "com.tencent.mobileqq:id/rlCommenTitle";
    public static final String more_chat_head = " com.tencent.mobileqq:id/head";
    public static final String chat_item = "com.tencent.mobileqq:id/chat_item_content_layout";

    public static final String continuing = "com.tencent.mobileqq:id/dialogLeftBtn";


    public static AccessibilityNodeInfo findLastChatItemOfText(AccessibilityNodeInfo root) {
        List<AccessibilityNodeInfo> chatList = root.findAccessibilityNodeInfosByViewId(chat_item);
        if (chatList.size() > 0) {
            for (int i = chatList.size() - 1; i > 0; i--) {
                String className = (Strs.isEmpty(chatList.get(i).getClassName()) ? "" : chatList.get(i).getClassName().toString());
                if (className.equals(Nodeinfos.vText)) {
                    return chatList.get(i);
                }
            }
        }
        return null;
    }

    /*ok*/
    public static AccessibilityNodeInfo findNearGroup(AccessibilityNodeInfo root) {
        while (true) {
            AccessibilityNodeInfo resNode = Nodeinfos.matchClickNodeByStr(root, "附近的群", 0, null);
            if (resNode != null) {
                return resNode;
            }
            Nodeinfos.scroll(root);
        }
    }

    /*ok*/
    public static AccessibilityNodeInfo friendSearchResult(AccessibilityNodeInfo root, String name) {
        AccessibilityNodeInfo parent = Nodeinfos.matchNodeByClass(root, Nodeinfos.vAList);
        if (parent == null ? false : parent.getChildCount() > 0) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                AccessibilityNodeInfo item = parent.getChild(i);
                if (item != null) {
                    AccessibilityNodeInfo res = Nodeinfos.matchClickNodeByStr(item, name, 0, null);
                    AccessibilityNodeInfo comeFrom = Nodeinfos.findByText(res, "来自分组");
                    if (res != null && comeFrom != null) {
                        return res;
                    }
                }
            }
        }
        return null;
    }

    /*ok*/
    public static boolean homeGuide(AccessibilityNodeInfo root) {
        AccessibilityNodeInfo clickMorePanel = Nodeinfos.matchClickNodeByStr(root, "快捷入口", 1, null);
        AccessibilityNodeInfo clickMore = Nodeinfos.matchClickNodeByStr(root, "多人聊天", 0, null);
        if (clickMorePanel != null) {
            Nodeinfos.click(clickMorePanel);
        }
        if (clickMore != null ? Nodeinfos.click(clickMore) : false) {
            return true;
        }
        return false;
    }


    /*ok*/
    public static boolean addGroupData(AccessibilityNodeInfo list, Progress work) {
        for (int i = 0; i < list.getChildCount(); i++) {
            if (list.getChild(i) == null ? false : list.getChild(i).isClickable()) {
                AccessibilityNodeInfo title = Nodeinfos.matchNodeByClass(list.getChild(i), "android.widget.TextView"); //群名字
                AccessibilityNodeInfo numNode = Nodeinfos.findByText(list.getChild(i), "icon"); //群人数
                if (title != null && numNode != null) {
                    CharSequence titleStr = (Strs.isEmpty(title.getText()) ? "" : title.getText());
                    CharSequence numStr = (Strs.isEmpty(numNode.getText()) ? "0" : numNode.getText());
                    if (!Strs.isEmpty(titleStr) && !Strs.isEmpty(numStr)) {
                        if (!work.getData().contains(titleStr.toString().trim())) {
                            if (Integer.valueOf(numStr.subSequence(6, numStr.length()).toString().trim()) >= work.getSets().getNumFilter()) {
                                work.getData().add(titleStr.toString().trim());
                                work.getNodes().add(list.getChild(i));
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean addGroupScroll(AccessibilityNodeInfo root) {
        AccessibilityNodeInfo list = Nodeinfos.matchNodeByClass(root, "android.widget.AbsListView");
        if (list != null && titles(root, "附近的群")) {
            return list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        }
        return false;
    }

    /*ok*/
    public static boolean back(AccessibilityNodeInfo root, Back backTag) {
        switch (backTag) {
            case toHome:
                AccessibilityNodeInfo close = Nodeinfos.findByText(root, "关闭");
                if (close != null) {
                    close.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                boolean backleft = Nodeinfos.click(Nodeinfos.findById(root, title_left));
                boolean cancelSech = Nodeinfos.click(Nodeinfos.findById(root, cancel_search));
                if (!backleft && !cancelSech) {
                    if (tabs(root, 1)) {
                        AccessibilityNodeInfo noti = Nodeinfos.findByText(root, "消息");
                        boolean news = noti == null ? false : noti.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        return homeScrollToTop(root);
                    }
                }
                break;
            case fromChat:
                return Nodeinfos.click(Nodeinfos.findById(root, title_left));
            default:
                break;
        }
        return false;
    }

    public static boolean homeScrollToTop(AccessibilityNodeInfo root) {
        AccessibilityNodeInfo scrollListNode = Nodeinfos.findById(root, home_list);
        if (scrollListNode != null) {
            while (true) {
                scrollListNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                AccessibilityNodeInfo searchNode = Nodeinfos.findById(root, home_search);
                if (searchNode != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /*ok*/
    public static List<String> dataFromHomeClick(AccessibilityNodeInfo root, long sleep) {
        List<String> groupData = new ArrayList<String>();
        AccessibilityNodeInfo mygroup = Nodeinfos.findByText(root, "我的群");
        if (mygroup != null) {
            Nodeinfos.click(mygroup);
        }
        AccessibilityNodeInfo grouplist = Nodeinfos.matchNodeByClass(root, Nodeinfos.vAList);
        if (grouplist == null ? false : grouplist.getChildCount() > 0) {
            boolean scrollTag = true;
            while (scrollTag) {
                scrollTag = Nodeinfos.getAllChildNodeTextAdd2List(grouplist, groupData);
                Nodeinfos.scroll(grouplist);
                grouplist = Nodeinfos.matchNodeByClass(root, Nodeinfos.vAList);
            }
            Utils.sleep(sleep);
            Nodeinfos.getAllChildNodeTextAdd2List(grouplist, groupData);
        }
        return groupData;
    }

    public static List<AccessibilityNodeInfo> groupSearchResult(AccessibilityNodeInfo root, String groupname) {
        List<AccessibilityNodeInfo> list = root.findAccessibilityNodeInfosByViewId(home_search_result), res = new ArrayList<>();
        List<String> strs = new ArrayList<>();
        for (AccessibilityNodeInfo item : list) {
            //判断群号码
            String substr = "";
            AccessibilityNodeInfo subNode = Nodeinfos.findByText(item.getParent(), "(");
            if (subNode != null) {
                substr = (subNode.getText() == null ? "" : subNode.getText().toString());
            }
            if (substr.startsWith("(") && substr.endsWith(")")) {
                substr = substr.substring(1, substr.length() - 1);
            }
            //判断名字
            if (item.getText() == null ? false : (item.getText().toString().equals(groupname) || substr.equals(groupname))) {
                for (int i = 0; i < item.getParent().getChildCount(); i++) {
                    CharSequence comStr = item.getParent().getChild(i).getText();
                    if (comStr == null ? false : (comStr.toString().equals("来自：群") || comStr.toString().equals("群"))) {
                        res.add(item.getParent());
                    }
                }
            }
        }
        return res;
    }


    public static boolean tabs(AccessibilityNodeInfo root, int position) {
        try {
            AccessibilityNodeInfo list = Nodeinfos.findById(root, home_tabs);
            return (list == null ? false : list.getChild(position - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK));
        } catch (Exception e) {

        }
        return false;
    }

    public static boolean titles(AccessibilityNodeInfo root, String titles) {
        AccessibilityNodeInfo node = Nodeinfos.findById(root, title_center);
        if (node != null) {
            CharSequence title = node.getText();
            if (title == null ? false : title.toString().trim().equals(titles)) {
                return true;
            }
        }
        return false;
    }
}
