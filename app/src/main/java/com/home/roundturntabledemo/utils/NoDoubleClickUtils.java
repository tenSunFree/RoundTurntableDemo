package com.home.roundturntabledemo.utils;

/**
 * 避免快速連續點擊
 */
public class NoDoubleClickUtils {

    private static long lastClickTime;
    private final static int SPACE_TIME = 500;

    public synchronized static boolean isDoubleClick() {
        long currentTime = System.currentTimeMillis();
        boolean isClick2;
        isClick2 = currentTime - lastClickTime <= SPACE_TIME;
        lastClickTime = currentTime;
        return isClick2;
    }
}
