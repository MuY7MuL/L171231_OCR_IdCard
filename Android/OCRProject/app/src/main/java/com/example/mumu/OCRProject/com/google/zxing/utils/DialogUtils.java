package com.example.mumu.OCRProject.com.google.zxing.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mumu.OCRProject.R;
import com.example.mumu.OCRProject.com.google.zxing.Interface.DlgInterface;

/**
 * Created by linyan on 05/01/2018.
 */

public class DialogUtils {
    Context context;
    Dialog dialog;
    LayoutInflater factory;

    public DialogUtils(Context context){
        super();
        this.context = context;
        factory = LayoutInflater.from(context);
    }

    public void ShowCustomeDialog(int first ,int second , final DlgInterface inter){
        View dialogView = factory.inflate(R.layout.dialog_custome,null);
        dialog = new Dialog(context, R.style.FullHeightDialog);
        LinearLayout ll_first , ll_second , ll_cancel;
        TextView tv_first , tv_second;
        View line;
        ll_first = (LinearLayout)dialogView.findViewById(R.id.ll_first);
        ll_second = (LinearLayout)dialogView.findViewById(R.id.ll_second);
        ll_cancel = (LinearLayout)dialogView.findViewById(R.id.ll_cancel);
        tv_first = (TextView)dialogView.findViewById(R.id.tv_first);
        tv_second = (TextView)dialogView.findViewById(R.id.tv_second);
        line = dialogView.findViewById(R.id.line);
        tv_first.setText(first);

        if(second == 0){
            ll_second.setVisibility(View.GONE);
            line.setVisibility(View.GONE);
        }else{
            tv_second.setText(second);
        }
        InitDialogSize(dialogView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        ll_first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inter.sure(1);
                dialog.dismiss();
            }
        });
        ll_second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inter.sure(2);
                dialog.dismiss();
            }
        });

        ll_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }

    private void InitDialogSize(View view){
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.DialogAnimtion);
        dialog.setContentView(view);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = SystemBaseUtils.getDisplayWidth(context);
        window.setAttributes(lp);
    }

    /**
     * 一个按钮 dialog
     *
     * @param title    标题
     * @param content  内容
     *
     */
    public void ShowSimpleAlertDialog(String title ,String content, final DlgInterface inter){
        this.ShowAlertDialogBase(title, content, false, inter);
    }

    /**
     * 两个按钮 dialog
     *
     * @param title    标题
     * @param content  内容
     *
     */

    public void ShowAlertDialog(String title ,String content, final DlgInterface inter){
        this.ShowAlertDialogBase(title, content, true, inter);
    }

    public void ShowAlertDialogBase(String title, String content,
                                    boolean isTwo, final DlgInterface inter) {
        LayoutInflater factory = LayoutInflater.from(context);
        View dialogView = factory.inflate(R.layout.dialog_alert, null);
        dialog = new Dialog(context, R.style.FullHeightDialog);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setWindowAnimations(R.style.DialogAnimtion);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        TextView tv_title = (TextView) dialogView.findViewById(R.id.tv_title);
        TextView tv_content = (TextView) dialogView
                .findViewById(R.id.tv_content);
        tv_title.setText(title);
        tv_content.setText(content);
        LinearLayout ll_sure, ll_cancel;
        ll_sure = (LinearLayout) dialogView.findViewById(R.id.ll_sure);
        ll_cancel = (LinearLayout) dialogView.findViewById(R.id.ll_cancel);
        if (!isTwo) {
            ll_cancel.setVisibility(View.GONE);
            ll_cancel.setEnabled(false);
        }
        dialog.show();

        ll_sure.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                inter.sure(null);
                dialog.dismiss();
            }
        });

        ll_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
    }


}
