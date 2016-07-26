package com.example.awx.Service;

import android.view.accessibility.AccessibilityNodeInfo;

import com.example.utils.Strs;

import java.util.Arrays;
import java.util.List;

/**
 * Created by john on 2016/7/23.
 */
public class Nodeinfos {
    public static String vEdit = "android.widget.EditText";
    public static String vAList = "android.widget.AbsListView";
    public static String vText = "android.widget.TextView";

    public static boolean click(AccessibilityNodeInfo nodeInfo) {
        return nodeInfo == null ? false : nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }


    public static AccessibilityNodeInfo findById(AccessibilityNodeInfo root, String id) {
        List<AccessibilityNodeInfo> list = root.findAccessibilityNodeInfosByViewId(id);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public static AccessibilityNodeInfo findByText(AccessibilityNodeInfo root, String text) {
        if (root != null) {
            List<AccessibilityNodeInfo> rs = root.findAccessibilityNodeInfosByText(text);
            if (rs.size() > 0) {
                return rs.get(0);
            }
        }
        return null;
    }


    public static boolean getAllChildNodeTextAdd2List(AccessibilityNodeInfo root, List<String> paramList) {
        if (root == null ? false : root.getChildCount() > 0) {
            int k = 0;
            for (int i = 0; i < root.getChildCount(); i++) {
                AccessibilityNodeInfo nodeInfo = root.getChild(i);
                if (nodeInfo != null) {
                    if (nodeInfo.getClassName().toString().equals("android.widget.TextView")) {
                        CharSequence str = nodeInfo.getText();
                        if (Strs.isEmpty(str) ? false : !paramList.contains(str.toString())) {
                            if (str.length() > 1 && !str.toString().contains("我加入") && !str.toString().contains("我管理") && !str.toString().contains("我创建")) {
                                paramList.add(str.toString());
                                k++;
                            }
                        }
                    } else {
                        if (getAllChildNodeTextAdd2List(nodeInfo, paramList)) {
                            k++;
                        }
                    }
                }
            }
            if (k > 0) {
                return true;
            }
        }
        return false;
    }

    /*ok*/
    public static AccessibilityNodeInfo matchClickNodeByStr(AccessibilityNodeInfo root, String Desc, int type, AccessibilityNodeInfo nullNode) {
        AccessibilityNodeInfo click = nullNode;
        if (root != null && root.getChildCount() > 0) {
            for (int i = 0; i < root.getChildCount(); i++) {
                AccessibilityNodeInfo node1 = root.getChild(i);
                if (node1 != null) {
                    click = (!node1.getClassName().toString().equals(Nodeinfos.vEdit) && node1.isClickable()) ? node1 : click;
                    CharSequence text;
                    if (type == 0) {
                        text = (node1.getText() == null ? "" : node1.getText());
                    } else {
                        text = (node1.getContentDescription() == null ? "" : node1.getContentDescription());
                    }
                    if ((!Strs.isEmpty(text) && click != null) ? text.toString().equals(Desc) : false) {
                        return click;
                    }
                    AccessibilityNodeInfo find = matchClickNodeByStr(node1, Desc, type, click);
                    if (find != null) {
                        return find;
                    }
                }
            }

        }
        return null;
    }

    public static AccessibilityNodeInfo matchNodeByDesc(AccessibilityNodeInfo root, String Desc) {
        if (root != null && root.getChildCount() > 0) {
            for (int i = 0; i < root.getChildCount(); i++) {
                AccessibilityNodeInfo node1 = root.getChild(i);
                if (node1 != null) {
                    CharSequence desc = node1.getContentDescription();
                    if (Strs.isEmpty(desc) ? false : node1.toString().equals(Desc)) {
                        return node1;
                    }
                    AccessibilityNodeInfo res = matchNodeByDesc(node1, Desc);
                    if (res != null) {
                        return res;
                    }
                }

            }
        }
        return null;
    }

    /*ok*/
    public static AccessibilityNodeInfo matchNodeByClass(AccessibilityNodeInfo root, String className) {
        if (root != null && root.getChildCount() > 0) {
            for (int i = 0; i < root.getChildCount(); i++) {
                AccessibilityNodeInfo node = root.getChild(i);
                if (node != null) {
                    if (Strs.isEmpty(node.getClassName()) ? false : node.getClassName().toString().equals(className)) {
                        return node;
                    } else {
                        AccessibilityNodeInfo nodeInfo = matchNodeByClass(node, className);
                        if (nodeInfo != null) {
                            return nodeInfo;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static boolean paste(AccessibilityNodeInfo nodeInfo) {
        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
    }

    /*ok*/
    public static boolean scroll(AccessibilityNodeInfo nodeInfo) {
        boolean bool = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return bool;
    }
}
