package com.example.awx.Service;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 2016/7/26.
 */
public class MInfo {
    public static final String name = "com.tencent.mm";

    public static final String tab_item = "com.tencent.mm:id/bhg";

    public static final String group_back = "com.tencent.mm:id/f0";
    public static final String top_search_back = "com.tencent.mm:id/es";
    public static final String chat_back = "com.tencent.mm:id/ei";


    public static final String contact_list = "com.tencent.mm:id/fu";
    public static final String group_item_text = "com.tencent.mm:id/gi";
    public static final String group_list = "com.tencent.mm:id/fu";

    public static final String top_search_bar = "android:id/action_bar";
    public static final String top_search_input = "com.tencent.mm:id/ew";
    public static final String top_search_list = "com.tencent.mm:id/aq3";
    public static final String top_search_clean = "com.tencent.mm:id/ex";
    public static final String top_search_result_list = "android:id/list";

    public static final String chat_input = "com.tencent.mm:id/yv";
    public static final String chat_send = "com.tencent.mm:id/z1";
    public static final String chat_input_noise = "com.tencent.mm:id/yx";
    public static final String chat_input_left = "com.tencent.mm:id/yt";


    public static final String near_people_set_nextTime = "com.tencent.mm:id/b3s";
    public static final String near_people_set_ok = "com.tencent.mm:id/bhe";
    public static final String near_people_list = "com.tencent.mm:id/bmw";
    public static final String near_people_call = "com.tencent.mm:id/a84";
    public static final String near_people_input = "com.tencent.mm:id/c0y";
    public static final String near_people_send = "com.tencent.mm:id/eg";
    public static final String near_people_click_view = "com.tencent.mm:id/bmz";
    public static final String near_people_send_msg = "com.tencent.mm:id/a88";
    public static final String near_people_people_name = "com.tencent.mm:id/nt";

    public static final String send_friend_new = "com.tencent.mm:id/bg0";
    public static final String send_friend_search = "com.tencent.mm:id/bm_";
    public static final String send_friend_select_all = "com.tencent.mm:id/bgj";
    public static final String send_friend_new_next = "com.tencent.mm:id/eg";
    public static final String send_friend_tag = "com.tencent.mm:id/aez";
    public static final String send_friend_tag_container = "com.tencent.mm:id/aez";
    public static final String send_friend_select_tag_item = "com.tencent.mm:id/bl_";
    public static final String send_friend_select_tag_item2 = "com.tencent.mm:id/l6";
    public static final String send_friend_new_send = "com.tencent.mm:id/bg2";


    public static final int BackToHome = 1;
    public static final int BackFromSearche = 2;
    public static final int BackFromChat = 3;
    public static final int BackFromGroup = 4;
    public static final int BackFromNearPeople = 5;


    public static boolean back(AccessibilityNodeInfo root, int backTag) {
        switch (backTag) {
            case BackFromChat:
                return Nodeinfos.click(Nodeinfos.findById(root, chat_back));
            case BackFromGroup:
                return Nodeinfos.click(Nodeinfos.findById(root, group_back));
            case BackFromSearche:
                return Nodeinfos.click(Nodeinfos.findById(root, top_search_back));
            case BackToHome:
                Nodeinfos.click(Nodeinfos.findById(root, chat_back));
                Nodeinfos.click(Nodeinfos.findById(root, group_back));
                Nodeinfos.click(Nodeinfos.findById(root, top_search_back));

                if (Nodeinfos.findById(root, tab_item) != null) {
                    return tabs(root, 1);
                }
            default:
                break;
        }
        return false;
    }

    public static boolean tabs(AccessibilityNodeInfo root, int i) {
        try {
            List<AccessibilityNodeInfo> list = root.findAccessibilityNodeInfosByViewId(tab_item);
            if (list.size() > i) {
                return list.get(i - 1).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        } catch (Exception e) {
            //结构错误
        }
        return false;
    }

    public static AccessibilityNodeInfo homeSearchInputNode(AccessibilityNodeInfo root) {
        AccessibilityNodeInfo list = Nodeinfos.findById(root, top_search_input);
        AccessibilityNodeInfo clear = Nodeinfos.findById(root, top_search_clean);
        if (list != null) {
            if (clear != null) {
                clear.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            return list;
        }
        return null;
    }

    public static boolean sendFriendClickAll(AccessibilityNodeInfo containRoot) {
        AccessibilityNodeInfo parent = Nodeinfos.findById(containRoot, "com.tencent.mm:id/bby");
        if (parent != null) {
            try {
                for (int i = 0; i < parent.getChildCount(); i++) {
                    parent.getChild(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            } catch (Exception e) {
            }
            return true;
        }
        return false;
    }

    public static List<String> groupData(AccessibilityNodeInfo root, long sleep) {
        List<String> groupData = new ArrayList<String>();
        AccessibilityNodeInfo grouplist = Nodeinfos.findById(root, group_list);
        if (grouplist != null) {
            int size;
            do {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                size = groupData.size();
                List<AccessibilityNodeInfo> list = root.findAccessibilityNodeInfosByViewId(group_item_text);
                if (list.size() > 0) {
                    for (AccessibilityNodeInfo item : list) {
                        String names = (item.getText() == null ? "" : item.getText().toString());
                        ///加入长度判断只取前10个字
                        if (names.length() > 20) {
                            names = names.substring(0, 20);
                        }
                        //////
                        if (!names.trim().equals("") && !groupData.contains(names)) {
                            groupData.add(names);
                        }
                    }
                }
                grouplist.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            } while (size < groupData.size());
        }
        return groupData;
    }

    public static List<AccessibilityNodeInfo> nearPeopleNodeinfo(AccessibilityNodeInfo root, List<String> total) {
        List<AccessibilityNodeInfo> res = new ArrayList<>();
        AccessibilityNodeInfo nodeInfo = Nodeinfos.findById(root, near_people_list);
        if (nodeInfo != null) {
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                try {
                    AccessibilityNodeInfo nameNode = Nodeinfos.findById(nodeInfo.getChild(i), near_people_people_name);
                    CharSequence name = (nameNode != null ? nameNode.getText() : "");
                    if (!name.equals("") && !total.contains(name.toString())) {
                        total.add(name.toString());
                        res.add(nodeInfo.getChild(i));
                    }
                } catch (Exception e) {
                }

            }
        }
        return res;
    }
}
