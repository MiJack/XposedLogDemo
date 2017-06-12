package com.sqslab.xposedlogdemo;

import android.text.TextUtils;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author Mr.Yuan
 * @date 2017/5/6
 */
public class XposedLoadPackageHook implements IXposedHookLoadPackage {
    //    public static final String MLF_FILE_SUFFIX = ".mlf";
//    public static final String FRAMEWORK = "android";
    public static final Set<String> METHOD_NAMES = new HashSet<>();

    static {
        METHOD_NAMES.add("android.app.Activity onCreate android.os.Bundle");
        METHOD_NAMES.add("java.net.ServerSocket <init>");
        METHOD_NAMES.add("java.net.ServerSocket <init> int");
        METHOD_NAMES.add("java.net.ServerSocket <init> int int");
        METHOD_NAMES.add("java.net.ServerSocket <init> int int java.net.InetAddress");
//        METHOD_NAMES.add("java.net.ServerSocket <init> java.net.SocketImpl");
        METHOD_NAMES.add("java.net.ServerSocket accept");
//        METHOD_NAMES.add("java.nio.channels.SocketChannel accept");
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam)
            throws Throwable {
        XposedBridge.log("load process:" + lpparam.processName);
        XlogUtils.setProcessName(lpparam.processName);
        for (String line : METHOD_NAMES) {
            //查找对应的文件
            try {
                if (TextUtils.isEmpty(line)) {
                    continue;
                }
                if (line.startsWith("//") || line.startsWith("#")) {
                    XposedBridge.log("注释无效：" + line);
                    continue;
                }
                XposedBridge.log("开始加载配置：" + line);
                hookMethod(line, lpparam);
                XposedBridge.log("加载配置成功：" + line);
            } catch (Exception e) {
                XposedBridge.log("加载配置失败：" + line + "\t" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void hookMethod(String line, XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException {
        String[] raw = line.split(" ");
        if (raw == null || raw.length < 2) {
            throw new IllegalArgumentException("缺少类名或者函数名");
        }
        int index = 0;
        boolean isStatic = false;
        for (int i = 0; i < raw.length; i++) {
            if ("[static]".equals(raw[i]) || "[abstract]".equals(raw[i])) {
                index++;
            }
            if ("[static]".equals(raw[i])) {
                isStatic = true;
            }
        }
        String[] split = new String[raw.length - index];
        System.arraycopy(raw, index, split, 0, split.length);
        if (split == null || split.length < 2) {
            throw new IllegalArgumentException("缺少类名或者函数名");
        }
        //拷args的类型
        String[] argsType = new String[split.length - 2];
        System.arraycopy(split, 2, argsType, 0, argsType.length);
        Class clazz = lpparam.classLoader.loadClass(split[0]);
        String methodName = split[1];
        Object[] argArray = new Object[split.length - 1];//最后一位是MethodHook
        for (int i = 0; i < argArray.length - 1; i++) {
            argArray[i] = loadClass(split[i + 2], lpparam);
        }
        argArray[argArray.length - 1] = new HookCallBack(argsType, isStatic);
        XposedBridge.log("method:" + methodName);
        if (methodName.equals("<init>")) {
            XposedHelpers.findAndHookConstructor(clazz, argArray);
        } else if (methodName.equals("<clinit>")) {
            XposedHelpers.findAndHookConstructor(clazz, argArray);
        } else {
            XposedHelpers.findAndHookMethod(clazz, methodName, argArray);
        }
    }

    private Class loadClass(String type, XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException {
        int lastIndexOf = type.lastIndexOf("[");
        int dimens = lastIndexOf + 1;
        if (dimens > 0) {
            String rawType = type.substring(dimens);
            int[] ds = new int[dimens];
            return Array.newInstance(loadClass(rawType, lpparam), ds).getClass();
        }
        if (boolean.class.getName().equals(type)) {
            return boolean.class;
        } else if (byte.class.getName().equals(type)) {
            return byte.class;
        } else if (char.class.getName().equals(type)) {
            return char.class;
        } else if (double.class.getName().equals(type)) {
            return double.class;
        } else if (float.class.getName().equals(type)) {
            return float.class;
        } else if (int.class.getName().equals(type)) {
            return int.class;
        } else if (long.class.getName().equals(type)) {
            return long.class;
        } else if (short.class.getName().equals(type)) {
            return short.class;
        } else {
            return lpparam.classLoader.loadClass(type);
        }
    }

}
