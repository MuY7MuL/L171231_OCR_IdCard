package com.example.mumu.OCRProject;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by linyan on 04/01/2018.
 */

public class TestActivity extends Activity implements View.OnClickListener {

    TextView testTitle_text;
    private static final int PHOTO_PIC = 2;

    public static final String testImgSrc="";

    private ImageView testImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
    }

    private void initView(){

        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar_capture);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        View view = actionBar.getCustomView();
        view.findViewById(R.id.back).setOnClickListener(this);
        testTitle_text = (TextView)view.findViewById(R.id.title);
        testImg = (ImageView) findViewById(R.id.testImageView);
        if(testImgSrc!=""){
            Bitmap bm = BitmapFactory.decodeFile(testImgSrc);
            testImg.setImageBitmap(bm);
        }
        Button butChoose = (Button) findViewById(R.id.testBtn1);
        butChoose.setOnClickListener(this);
        butChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //开始识别
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case  R.id.testBtn1:

                break;
        }

    }
}
