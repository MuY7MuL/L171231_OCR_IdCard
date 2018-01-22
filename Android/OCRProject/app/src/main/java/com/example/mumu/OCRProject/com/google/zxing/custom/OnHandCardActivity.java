package com.example.mumu.OCRProject.com.google.zxing.custom;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mumu.OCRProject.R;
import com.example.mumu.OCRProject.com.google.zxing.client.android.camera.CameraManager;
import com.example.mumu.OCRProject.com.google.zxing.client.android.decode.BeepManager;
import com.example.mumu.OCRProject.com.google.zxing.client.android.decode.CaptureActivityHandler;
import com.example.mumu.OCRProject.com.google.zxing.client.android.decode.InactivityTimer;
import com.example.mumu.OCRProject.com.google.zxing.client.android.decode.ViewfinderView;
import com.example.mumu.OCRProject.com.google.zxing.client.android.result.BankCardIdentify;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by linyan on 22/12/2017.
 */

public abstract class OnHandCardActivity extends FragmentActivity implements SurfaceHolder.Callback , View.OnClickListener {

    public static final String RESULT = "com.libs.zxing.CaptureActivity.RESULT";
    public static final String SCAN_TYPE = "com.libs.zxing.CaptureActivity.SCAN_TYPE";

    public static final String SCAN_TYPE_QRCODESCAN = "com.libs.zxing.CaptureActivity.SCAN_TYPE_QRCODESCAN ";
    public static final String SCAN_TYPE_BANK_CARD = "com.libs.zxing.CaptureActivity.SCAN_TYPE_BANK_CARD";

    public static final String SCAN_TYPE_HAND_ID_CARD="";

    public static final String SCAN_TYPE_ID_CARD_UP = "com.libs.zxing.CaptureActivity.SCAN_TYPE_ID_CARD";
    public static final String SCAN_TYPE_ID_CARD_DOWN ="com.libs.zxing.CaptureActivity.SCAN_TYPE_ID_CARD";

    public static final String SCAN_TYPE_CREDIT_ONE = "";
    public static final String SCAN_TYPE_CREDIT_TWO = "";



    private static final String TAG = CaptureActivity.class.getSimpleName();
    //	private AppConfiguration appConfig = AppConfiguration.getInstance();
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private Result savedResultToShow;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Collection<BarcodeFormat> decodeFormats;
    private InactivityTimer inactivityTimer;
    private String characterSet;
    private BeepManager beepManager;

    final static String profix1 = "?appid=";
    final static String profix2 = "-title=";
    final static String action = "muzhiwan.action.detail";
    final static String bundle_key = "detail";
    private  int btnType = 0;
    static String scan_type;
    ImageView opreateView;
    TextView barcode_notice, title;
    Button scanBut;

    ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    //OpenCV库加载并初始化成功后的回调函数
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "成功加载");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i(TAG, "加载失败");
                    break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_arcodescan);
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar_capture);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        View view = actionBar.getCustomView();
        view.findViewById(R.id.back).setOnClickListener(this);
        view.findViewById(R.id.photo).setOnClickListener(this);
        title = (TextView)view.findViewById(R.id.title);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        opreateView = (ImageView)findViewById(R.id.button_openorcloseClick);

        barcode_notice = (TextView)findViewById(R.id.barcode_notice);

        scanBut = (Button) findViewById(R.id.shenfenzhengscanOk);
        Intent intent = getIntent();
        if(intent != null){
            scan_type = intent.getStringExtra(SCAN_TYPE);
            if(scan_type.equals(SCAN_TYPE_HAND_ID_CARD)){
                barcode_notice.setText("请将手持身份证对准人型框，点击拍摄即可");
                title.setText("手持身份证");
            }else if (scan_type.equals(SCAN_TYPE_ID_CARD_UP)){
                barcode_notice.setText("请将身份证的正面放在框内，点击拍摄即可");
                title.setText("身份证正面");
            }else if(scan_type.equals(SCAN_TYPE_ID_CARD_DOWN)){
                barcode_notice.setText("请将身份证的反面放在框内，点击拍摄既可");
                title.setText("身份证反面");
            }else if(scan_type.equals(SCAN_TYPE_CREDIT_ONE)||scan_type.equals(SCAN_TYPE_CREDIT_TWO)){
                barcode_notice.setText("请将征信证书放在框内，点击拍摄既可");
                title.setText("征信证书");
            }
        }

        opreateView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (cameraManager != null){
                    Config.KEY_FRONT_LIGHT = !Config.KEY_FRONT_LIGHT;
                    if(Config.KEY_FRONT_LIGHT = true){
                        opreateView.setImageResource(R.mipmap.mzw_camera_close);
                    }else{
                        opreateView.setImageResource(R.mipmap.mzw_camera_open);
                    }
                    cameraManager.getConfigManager().initializeTorch(cameraManager.getCamera().getParameters(),false);
                    onPause();
                    onResume();
                }
            }
        });

        scanBut.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(cameraManager != null){
                    btnType = 1;
                    Config.KEY_FRONT_LIGHT = !Config.KEY_FRONT_LIGHT;
                    if(Config.KEY_FRONT_LIGHT == true){
                        opreateView.setImageResource(R.mipmap.mzw_camera_close);
                    }
                    onPause();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG,"Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,this,mLoaderCallback);
        }else{
            Log.d(TAG,"OpenCV library found inside packeg ");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        cameraManager = new CameraManager(getApplication());
        viewfinderView = (ViewfinderView)findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager , scan_type);

        handler = null;
        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if(hasSurface){
            initCamera(surfaceHolder);
        }else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(surfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        beepManager.updatePrefs();
        inactivityTimer.onResume();
    }


    @Override
    protected void onPause() {

        if(handler != null){
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        cameraManager.closeDriver();
        if(!hasSurface){
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                return  true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                cameraManager.setTorch(false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                cameraManager.setTorch(true);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(holder == null){
            Log.e(TAG,"======");
        }
        if(!hasSurface){
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     *
     * @param rawResult
     * @param barcode
     */
    public void  handleDecode(Result rawResult ,Bitmap barcode){
         inactivityTimer.onActivity();

         boolean fromLiveScan = barcode != null;
         if(fromLiveScan){
            beepManager.playBeepSoundAndVibrate();
            viewfinderView.drawResultBitmap(barcode);
         }
         String text = rawResult.getText();
         finish(text);
    }

    /**
     *
     */
    public void handleDecodeTwo (String rawResult ,Bitmap barcode){
        inactivityTimer.onActivity();
        boolean formLiveScan = barcode !=null;
        if (formLiveScan){
            beepManager.playBeepSoundAndVibrate();
            viewfinderView.drawResultBitmap(barcode);
        }
        finish(rawResult);
    }

    /**
     * 获取finish掉 返回给上一个Activity 解析图片结果
     *
     * @param text
     */
    private void finish (String text){
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(RESULT , text);
        resultIntent.putExtras(bundle);
        setResult(RESULT_OK ,resultIntent);
    }

    /**
     * 画线
     *
     * @param barcode
     * @param rawResult
     */
    private void drawResultPoints(Bitmap barcode ,Result rawResult){
        ResultPoint[] points  = rawResult.getResultPoints();
        if(points !=null && points.length >0){
            Canvas canvas =  new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.result_points));
            if(points.length == 2){
               paint.setStrokeWidth(4.0f);
               drawLine(canvas,paint,points[0],points[1]);
            }else if(points.length == 4 && (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13) ) {
                drawLine(canvas, paint, points[0], points[1]);
                drawLine(canvas, paint, points[2], points[3]);
            }else{
                paint.setStrokeWidth(10.0f);
                for(ResultPoint point :points){
                    canvas.drawPoint(point.getX(),point.getY(),paint);
                }
            }
        }
    }

    private static void drawLine(Canvas canvas,Paint paint,ResultPoint a, ResultPoint b){
        canvas.drawLine(a.getX(),a.getY(),b.getX(),b.getY(),paint);
    }

    private  void decodeOrStoreSaveBitmap(Bitmap bitmap ,Result result){
        if(handler == null){
           savedResultToShow = result;
        }else{
            if(result != null){
                savedResultToShow = result;
            }
            if(savedResultToShow != null){
                Message message  = Message.obtain(handler,R.id.decode_succeeded,savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }

    }

    public  void  initCamera(SurfaceHolder surfaceHolder){
         if(surfaceHolder == null){
              throw  new IllegalStateException("No SurfaceHolder provided");
         }
         if(cameraManager.isOpen()){
             Log.w(TAG,"initCamera while already open");
             return;
         }
        try{
            cameraManager.openDriver(surfaceHolder);
//            if(handler == null){
//               handler = new CaptureActivityHandler(this,viewfinderView,decodeFormats,characterSet,cameraManager,scan_type);
//            }
            decodeOrStoreSaveBitmap(null,null);
        } catch (IOException ioe){
            Log.w(TAG,ioe);
            finish();
        } catch (RuntimeException e) {
            Log.w(TAG,"initCamear error" ,e);
        }
    }

    public void restartPreviewAfterDelay(long delayMS){
         if(handler != null){
             handler.sendEmptyMessageAtTime(R.id.restart_preview,delayMS);
        }
    }

    public void drawViewFinder(){
        viewfinderView.drawViewfinder();
    }

    private  static  final int CHOOSE_PIC = 0;

   @Override
   public void onClick(View v) {
       switch (v.getId()){
           case R.id.back:
               finish();
               break;
           case R.id.photo:
               String type;
               Intent intent1 = new Intent();
               intent1.setAction(Intent.ACTION_PICK);
               intent1.setType("image/*");
               if(scan_type.equals(SCAN_TYPE_HAND_ID_CARD)){
                   type = "选择手持身份证图片";
               }else if(scan_type.equals(SCAN_TYPE_ID_CARD_UP)){
                   type = "征信身份证正面";
               }else if(scan_type.equals(SCAN_TYPE_ID_CARD_DOWN)){
                   type = "征信身份证反面";
               }else if(scan_type.equals(SCAN_TYPE_CREDIT_ONE) || scan_type.equals(SCAN_TYPE_CREDIT_TWO)){
                   type = "征信扫描";
               }
       }

   }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imgPath = null;
        if (requestCode == FragmentActivity.RESULT_OK){
            switch (requestCode) {
                case CHOOSE_PIC:
                    String[] proj = new String[]{MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(data.getData(),proj,null,null,null);
                    if(cursor.moveToFirst()){
                        int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        //获取图片的绝对路径
                        imgPath = cursor.getString(columnIndex);
                    }
                    cursor.close();
                    //获取解析结果
                    if(scan_type.equals(SCAN_TYPE_HAND_ID_CARD)){
                        String res = null;

                        if(res != null) finish(res);
                    }else if(scan_type.equals(SCAN_TYPE_ID_CARD_UP)){
                        String res = null;
                        try{
                            res = parseIdCardBitmap(imgPath);

                        }catch (Exception e){

                        }
                        if(res != null) finish(res);
                    }else if(scan_type.equals(SCAN_TYPE_ID_CARD_DOWN)){
                        String res = null;

                        if(res != null) finish(res);
                    }else if(scan_type.equals(SCAN_TYPE_CREDIT_ONE) || scan_type.equals(SCAN_TYPE_CREDIT_TWO)){
                        String res = null;

                        if(res != null) finish(res);
                    }
            }
        }
    }

    /*
     *    身份证通过相册选择图片
     */
    private String parseIdCardBitmap(String bitmapPath){
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath);
        return BankCardIdentify.bankCardIdentify(this,bitmap);
    }

    /*

     */
}
