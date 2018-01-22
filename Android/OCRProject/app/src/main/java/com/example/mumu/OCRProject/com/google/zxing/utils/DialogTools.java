package com.example.mumu.OCRProject.com.google.zxing.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.example.mumu.OCRProject.R;

/**
 * Created by linyan on 05/01/2018.
 */

public class DialogTools {
    public static Dialog createLoadingDialog(Context context) {
        View view;
        LayoutInflater factory = LayoutInflater.from(context);
        view = factory.inflate(R.layout.layout_loading_football, null);
        ImageView iv_anim = (ImageView) view.findViewById(R.id.iv_anim);
        iv_anim.setBackgroundResource(R.drawable.anim_pullview_loading);
        AnimationDrawable animationDrawable = (AnimationDrawable) iv_anim
                .getBackground();
        animationDrawable.start();

        Dialog dialog = new Dialog(context, R.style.LodingDialog);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }
}
