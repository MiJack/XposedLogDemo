package com.sqslab.xposedlogdemo;

import de.robv.android.xposed.XC_MethodHook;

import static com.sqslab.xposedlogdemo.XlogUtils.method2String;

/**
 * @author Mr.Yuan
 * @date 2017/5/6
 */

public class HookCallBack extends XC_MethodHook {
    public static final ThreadLocal<String> THREAD_INFO_LOCAL = new ThreadLocal<>();
    private String[] argsType;
    private boolean isStatic;
    public static final char LINE_SPLIT_CHAR = '\t';

    public HookCallBack(String[] argsType, boolean isStatic) {
        this.argsType = argsType;
        this.isStatic = isStatic;
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        if (!isStatic) {
            Xlog.logMethodEnter(System.identityHashCode(param),method2String(param.method), param.thisObject, param.args);
        } else {
            Xlog.logStaticMethodEnter(System.identityHashCode(param),method2String(param.method), param.args);
        }
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//        super.afterHookedMethod(param);
        boolean hasThrowable = param.hasThrowable();
        if (isStatic && hasThrowable) {
            Xlog.logStaticMethodExitWithThrowable(System.identityHashCode(param),method2String(param.method), param.getThrowable());
        } else if (isStatic && !hasThrowable) {
            Xlog.logStaticMethodExitWithResult(System.identityHashCode(param),method2String(param.method),param.getResult());
        } else if (!isStatic && hasThrowable) {
            Xlog.logMethodExitWithThrowable(System.identityHashCode(param),method2String(param.method), param.thisObject, param.getThrowable());
        } else {
            Xlog.logMethodExitWithResult(System.identityHashCode(param),method2String(param.method), param.thisObject,param.getResult());
        }
    }


}
