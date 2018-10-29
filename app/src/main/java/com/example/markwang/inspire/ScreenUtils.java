package com.example.markwang.inspire;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

public class ScreenUtils {

    private ScreenUtils() {
        throw new UnsupportedOperationException("ScreenUtils cannot be instantiated");
    }

    private static DisplayMetrics calDisplayMetrics(Context context){
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics=new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        return  displayMetrics;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics= calDisplayMetrics(context);
        return  displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Context context){
        DisplayMetrics displayMetrics= calDisplayMetrics(context);
        return  displayMetrics.heightPixels;
    }

    public static int getStatusBarHeight(Context context){
        int statusBarHeight=-1;
        try{
            //android.R.dimen – 系統尺寸資源。
            Class<?> SystemSizeResource=Class.forName("com.android.internal.R$dimen");
            Object systemSizeResource=SystemSizeResource.newInstance();
            int height=Integer.parseInt(SystemSizeResource.getField("status_bar_height").get(systemSizeResource).toString());
            statusBarHeight=context.getResources().getDimensionPixelSize(height);
        }catch (Exception e){
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    public static int getVirtualBarHeight(Context context){
        int virtualBarHeight=0;
        WindowManager windowManager=(WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display=windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics=new DisplayMetrics();

        try{
            Class Display=Class.forName("android.view.Display");

            Method getRealMetricsMethod=Display.getMethod("getRealMetrics", DisplayMetrics.class);
            getRealMetricsMethod.invoke(display,displayMetrics);
            virtualBarHeight=displayMetrics.heightPixels-display.getHeight();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  virtualBarHeight;
    }

}
