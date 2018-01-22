package com.example.mumu.OCRProject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mumu.OCRProject.com.google.zxing.custom.CaptureActivity;
import com.example.mumu.OCRProject.com.google.zxing.utils.CameraUtil;
import com.example.mumu.OCRProject.com.google.zxing.utils.DialogTools;
import com.example.mumu.OCRProject.com.google.zxing.utils.DialogUtils;
import com.example.mumu.OCRProject.com.google.zxing.Interface.DlgInterface;
import com.example.mumu.OCRProject.com.google.zxing.utils.ToastUtils;

import java.io.File;

public class MainActivity extends Activity implements View.OnClickListener {
    TextView result_txt;
    private static final int PHOTO_PIC = 1;
    private ImageView img;

    //ImageDialogTools
    private String cameraFile;
    private String tempFilePath;//头像路径
    public int Pic_Type = -1;   //-1为头像
    DialogUtils dialogUtils;
    CameraUtil cameraUtil;
    public Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        Button mQrcodeScan = (Button) findViewById(R.id.qrcodescan);
        Button mBankCardScan = (Button) findViewById(R.id.bankcardscan);

        //手持身份证
        Button mhandCard =(Button)findViewById(R.id.handCard);

        //身份证扫描
        Button mShenfenzhengScan = (Button) findViewById(R.id.shenfenzhengscan);
        Button mShenfenzhengScanDown = (Button)findViewById(R.id.shenfenzhengscanDown);

        //征信报告扫描
        Button mcridetOne = (Button)findViewById(R.id.cridetOne);
        Button mcridetTwo = (Button)findViewById(R.id.cridetTwo);


        //测试
        Button testBtn = (Button)findViewById(R.id.allPhotos);

        img = (ImageView) findViewById(R.id.img);
        result_txt = (TextView) findViewById(R.id.result);

        //dialog
        loadingDialog = DialogTools.createLoadingDialog(this);
        dialogUtils = new  DialogUtils(this);


        mQrcodeScan.setOnClickListener(this);
        mBankCardScan.setOnClickListener(this);

        mhandCard.setOnClickListener(this);
        mShenfenzhengScan.setOnClickListener(this);
        mShenfenzhengScanDown.setOnClickListener(this);
        mcridetOne.setOnClickListener(this);
        mcridetTwo.setOnClickListener(this);

        testBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.qrcodescan:
                Intent intent = new Intent(this, CaptureActivity.class);
                intent.putExtra(CaptureActivity.SCAN_TYPE, CaptureActivity.SCAN_TYPE_QRCODESCAN);
                startActivityForResult(intent, PHOTO_PIC); //识别二维码(相机)

                break;
            case R.id.bankcardscan:
                Intent intent1 = new Intent(this, CaptureActivity.class);
                intent1.putExtra(CaptureActivity.SCAN_TYPE, CaptureActivity.SCAN_TYPE_BANK_CARD);
                startActivityForResult(intent1, PHOTO_PIC);//识别银行卡(相机)
                break;
            case  R.id.handCard:
                Intent intentHand = new Intent(this,CaptureActivity.class );
                intentHand.putExtra(CaptureActivity.SCAN_TYPE,CaptureActivity.SCAN_TYPE_ONHAND_CARD);
                startActivityForResult(intentHand,PHOTO_PIC);//手持身份证
                break;
            case R.id.shenfenzhengscan:
                Intent intent2 = new Intent(this, CaptureActivity.class);
                intent2.putExtra(CaptureActivity.SCAN_TYPE, CaptureActivity.SCAN_TYPE_ID_CARD);
                startActivityForResult(intent2, PHOTO_PIC);//识别身份证(相机)
                break;
            case R.id.shenfenzhengscanDown:
                Intent intent3 = new Intent(this , CaptureActivity.class);
                intent3.putExtra(CaptureActivity.SCAN_TYPE , CaptureActivity.SCAN_TYPE_ID_CARD_DOWN);
                startActivityForResult(intent3 , PHOTO_PIC);//识别身份证背面
                break;
            case  R.id.cridetOne:
                Intent intent4 = new Intent(this,CaptureActivity.class);
                intent4.putExtra(CaptureActivity.SCAN_TYPE , CaptureActivity.SCAN_TYPE_CREDIT_ONE);
                startActivityForResult(intent4 , PHOTO_PIC);//征信报告1
                break;
            case R.id.cridetTwo:
                Intent intent5 = new Intent(this , CaptureActivity.class);
                intent5.putExtra(CaptureActivity.SCAN_TYPE , CaptureActivity.SCAN_TYPE_CREDIT_TWO);
                startActivityForResult(intent5 , PHOTO_PIC);//征信报告2
                break;
            case R.id.allPhotos:
                choosePic();
                //Intent intent6 = new Intent(this , CaptureActivity.class);
//                Intent intent6 = new Intent(this,TestActivity.class);
//                intent6.putExtra(TestActivity.testImgSrc , "");
//                //  startActivityForResult(intent6,PHOTO_PIC);
//               // intent6.putExtra(CaptureActivity.SCAN_TYPE , CaptureActivity.SCAN_TYPE_CREDIT_TWO);
//                startActivityForResult(intent6 , PHOTO_PIC);//征信报告2

//                Intent intent6 = new Intent();
//                intent6.setClass(MainActivity.this, TestActivity.class);
//
//                startActivityForResult(intent6, PHOTO_PIC);
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == FragmentActivity.RESULT_OK) {
            switch (requestCode) {
                case PHOTO_PIC:
                    String result = data.getExtras().getString(CaptureActivity.RESULT);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "img" + File.separator + "Card_number");
                            if (file.exists()) {
                                Bitmap bm = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "img" + File.separator + "Card_number");
//                                img.setImageBitmap(bm);
                            }
                        }
                    });
                    setScanResult(result);
                    break;
                case CameraUtil.REQUEST_CODE_TAKE_IMAGE_FROM_ALBUM:// 发送本地图片
                    if (data == null || data.getData() == null) {
                        ToastUtils.show(this, "请重新选择～");
                        return;
                    }
                    Uri selectedImage = data.getData();
                    tempFilePath = CameraUtil.getSelectPicPath(this, selectedImage); // 获取图片的绝对路径
//                    if (Pic_Type == -1)
//                        cutImage(Uri.parse("file://" + tempFilePath), Uri.parse("file://" + cameraFile));
//                    else
//                        handlePic(tempFilePath);

                    Intent intent6 = new Intent(this,TestActivity.class);
                    intent6.putExtra(TestActivity.testImgSrc , tempFilePath);
                  //  startActivityForResult(intent6,PHOTO_PIC);
                    this.startActivity(intent6);


                    break;
            }
        }

//        //获取图片路径
//        if (requestCode ==  CameraUtil.REQUEST_CODE_TAKE_IMAGE_FROM_ALBUM && resultCode == Activity.RESULT_OK && data != null) {
//            Uri selectedImage = data.getData();
//            tempFilePath = CameraUtil.getSelectPicPath(this, selectedImage); // 获取图片的绝对路径
//        }

    }

    private void setScanResult(String result) {
        // AppConfiguration.getInstance().handle(result);
        if (!TextUtils.isEmpty(result)) {
            String[] strings = result.split("=");
            result = strings[strings.length - 1];
        }
        result_txt.setText(result);
    }

    public void choosePic() {

        Log.e("choosePic", "*** WARNING *** surfaceCreated() gave us a null surface!");

        dialogUtils.ShowCustomeDialog(R.string.text_take_photo,R.string.text_choose_photo
                , new DlgInterface() {
                    @Override
                    public void sure(Object obj) {
                        // 初始化圖片路徑
                        cameraFile = CameraUtil.IMAGE_DIR
                                + System.currentTimeMillis() + ".jpg";
                        int value = (Integer) obj;
                        switch (value) {
                            case 1:
//                                LogUtils.showLog("request :" + cameraFile);
                                if(!cameraIsCanUse())
                                {
                                    ToastUtils.show(MainActivity.this,"请打开相机权限");
                                    return;
                                }
//                                if (Pic_Type == -1 || Pic_Type == 1 || Pic_Type == 2 || Pic_Type == 3 || Pic_Type == 8 || Pic_Type == 9 || Pic_Type == 10 || Pic_Type ==11 || Pic_Type == 12 || Pic_Type == 13) {
//                                    cameraUtil.setRequestId(CameraUtil.REQUEST_CODE_TAKE_IMAGE_FROM_CAMERA)
//                                            .setOutImageUri(Uri.fromFile(new File(cameraFile)))
//                                            .takePhotoFromActivity(MainActivity.this);
//                                }else if(Pic_Type == 4)
//                                {
//                                    //手持身份证
//                                    Intent it = new Intent(MainActivity.this, MainActivity.class);
//                                    it.putExtra("URL", cameraFile);
//                                    it.putExtra("TYPE",1);
//                                    MainActivity.this.startActivityForResult(it, CameraUtil.REQUEST_CODE_TAKE_IMAGE_FROM_CAMERA);
//                                }else if(Pic_Type == 5)
//                                {
//                                    //手持申请表
//                                    Intent it = new Intent(MainActivity.this, CameraActivity.class);
//                                    it.putExtra("URL", cameraFile);
//                                    it.putExtra("TYPE",2);
//                                    MainActivity.this.startActivityForResult(it, CameraUtil.REQUEST_CODE_TAKE_IMAGE_FROM_CAMERA);
//                                } else {
//                                    Intent it = new Intent(MainActivity.this, CameraActivity.class);
//                                    it.putExtra("URL", cameraFile);
//                                    it.putExtra("TYPE",0);
//                                    MainActivity.this.startActivityForResult(it, CameraUtil.REQUEST_CODE_TAKE_IMAGE_FROM_CAMERA);
//                                }
                                break;
                            case 2:
                                selectPicFromLocal();
                                break;
                        }
                    }

                    @Override
                    public void cancel(Object obj) {

                    }
                });
    }

    public void selectPicFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, CameraUtil.REQUEST_CODE_TAKE_IMAGE_FROM_ALBUM);
    }


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK) {
//            switch (requestCode) {
//                case CameraUtil.REQUEST_CODE_TAKE_IMAGE_FROM_CAMERA:// 发送照片
//                    LogUtils.showLog("REQUEST_CODE_TAKE_IMAGE_FROM_CAMERA :" + cameraFile);
//                    if (Pic_Type == -1)
//                        cutImage(Uri.parse("file://" + cameraFile), Uri.parse("file://" + cameraFile));
//                    else
//                        handlePic(cameraFile);
//                    break;
//                case CameraUtil.REQUEST_CODE_TAKE_IMAGE_FROM_ALBUM:// 发送本地图片
//                    if (data == null || data.getData() == null) {
//                        ToastUtils.show(this, "请重新选择～");
//                        return;
//                    }
//                    Uri selectedImage = data.getData();
//                    tempFilePath = CameraUtil.getSelectPicPath(this, selectedImage); // 获取图片的绝对路径
//                    if (Pic_Type == -1)
//                        cutImage(Uri.parse("file://" + tempFilePath), Uri.parse("file://" + cameraFile));
//                    else
//                        handlePic(tempFilePath);
//                    break;
//                case CameraUtil.REQUEST_CODE_IMAGE_CROP:
//                    LogUtils.showLog("REQUEST_CODE_IMAGE_CROP");
//                    handlePic(cameraFile);
//                    break;
//            }
//        }
//    }

    public boolean cameraIsCanUse() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            isCanUse = false;
        }
        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                return isCanUse;
            }
        }
        return isCanUse;
    }



}
