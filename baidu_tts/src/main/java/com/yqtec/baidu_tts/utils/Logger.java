package com.yqtec.baidu_tts.utils;

import android.util.Log;

public class Logger {
    private static String className = "";
    private static String methodName = "";
    private static int lineNumber;
    private static String LOCAL_SEARCH_LOG = "local_yq";
    private static boolean PRINT_LOG = false;

    public static void printLog(boolean print) {
        PRINT_LOG = print;
    }

    private static String createLog(String log) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(methodName);
        buffer.append("(").append(className).append(":").append(lineNumber).append(")");
        buffer.append(log);
        return buffer.toString();
    }

    private static void getMethodNames(StackTraceElement[] sElements) {
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }

    public static void e(String message) {
        if (PRINT_LOG) {
            getMethodNames(new Throwable().getStackTrace());
            Log.e(className, LOCAL_SEARCH_LOG + " : " + createLog(message));
        }
    }


    public static void i(String message) {
        if (PRINT_LOG) {
            getMethodNames(new Throwable().getStackTrace());
            Log.i(className, LOCAL_SEARCH_LOG + " : " + createLog(message));
        }
    }

    public static void d(String message) {
        if (PRINT_LOG) {
            getMethodNames(new Throwable().getStackTrace());
            Log.d(className, LOCAL_SEARCH_LOG + " : " + createLog(message));
        }
    }

    public static void v(String message) {
        if (PRINT_LOG) {
            getMethodNames(new Throwable().getStackTrace());
            Log.v(className, LOCAL_SEARCH_LOG + " : " + createLog(message));
        }
    }

    public static void w(String message) {
        if (PRINT_LOG) {
            getMethodNames(new Throwable().getStackTrace());
            Log.w(className, LOCAL_SEARCH_LOG + " : " + createLog(message));
        }
    }

    public static void wtf(String message) {
        if (PRINT_LOG) {
            getMethodNames(new Throwable().getStackTrace());
            Log.wtf(className, LOCAL_SEARCH_LOG + " : " + createLog(message));
        }
    }

    public static String createLogImg(String... img) {
        StringBuffer StringBuffer = new StringBuffer();
        for (String s : img) {
            StringBuffer.append(s).append(":");
        }
        return StringBuffer.toString();
    }

}
