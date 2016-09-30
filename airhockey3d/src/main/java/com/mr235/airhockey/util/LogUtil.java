package com.mr235.airhockey.util;

import android.util.Log;

import com.mr235.airhockey.BuildConfig;

/**
 * Created by Administrator on 2016/9/27.
 */

public class LogUtil {
    public static void i(String text) {
        i(LogUtil.class.getSimpleName(), text);
    }

    public static void i(String tag, String text) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, text);
        }
    }
}
