package com.findhouse.utils;

public class SpiltUtil {
    public final String[] installType = {"洗衣机", "空调", "衣柜","电视", "冰箱", "热水器","床", "暖气", "宽带", "天然气"};
    public final String[] sellType = {"二手", "租房", "新房"};
    public final String[] priceType = {"万", "元/月"};

    public String[] spiltSemicolon(String url) {
        String[] splitUrl = url.split("\\;");
        return splitUrl;
    }
    public int[] spiltInstall(String url) {
        int length = url.length();
        int[] result = new int[length];
        for(int i=0; i<length; i++) {
            result[i] = url.charAt(i)-'0';
        }
        return result;
    }
}
