package com.example.mumu.OCRProject.com.google.zxing.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by linyan on 05/01/2018.
 */

public class SystemBaseUtils {

    /*
    截取屏幕宽度
     */

    public static int getDisplayWidth(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();

        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
}
