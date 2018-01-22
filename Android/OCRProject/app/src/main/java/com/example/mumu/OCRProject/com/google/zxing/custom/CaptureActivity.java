package com.example.mumu.OCRProject.com.google.zxing.custom;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;

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
import com.example.mumu.OCRProject.com.google.zxing.client.android.result.IdCardIdentify;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public final class CaptureActivity extends FragmentActivity implements
        SurfaceHolder.Callback, View.OnClickListener {
    public static final String RESULT = "com.libs.zxing.CaptureActivity.RESULT";
    public static final String SCAN_TYPE = "com.libs.zxing.CaptureActivity.SCAN_TYPE";

    public static final String SCAN_TYPE_QRCODESCAN = "com.libs.zxing.CaptureActivity.SCAN_TYPE_QRCODESCAN ";
    public static final String SCAN_TYPE_BANK_CARD = "com.libs.zxing.CaptureActivity.SCAN_TYPE_BANK_CARD";


    public static final String SCAN_TYPE_ONHAND_CARD="com.libs.zxing.CaptureActivity.SCAN_TYPE_ONHAND_CARD";

    public static final String SCAN_TYPE_ID_CARD = "com.libs.zxing.CaptureActivity.SCAN_TYPE_ID_CARD";
    public static final String SCAN_TYPE_ID_CARD_DOWN="com.libs.zxing.CaptureActivity.SCAN_TYPE_ID_CARD_DOWN";

    public static final String SCAN_TYPE_CREDIT_ONE = "com.libs.zxing.CaptureActivity.SCAN_TYPE_CREDIT_ONE";
    public static final String SCAN_TYPE_CREDIT_TWO = "com.libs.zxing.CaptureActivity.SCAN_TYPE_CREDIT_TWO";

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
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_arcodescan);
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar_capture);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        View view = actionBar.getCustomView();
        view.findViewById(R.id.back).setOnClickListener(this);
        view.findViewById(R.id.photo).setOnClickListener(this);
        title = (TextView) view.findViewById(R.id.title);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        opreateView = (ImageView) findViewById(R.id.button_openorcloseClick);

        barcode_notice = (TextView) findViewById(R.id.barcode_notice);

        scanBut =  (Button) findViewById(R.id.shenfenzhengscanOk);
        Intent intent = getIntent();
        if (intent != null) {
            scan_type = intent.getStringExtra(SCAN_TYPE);
//            if (scan_type.equals(SCAN_TYPE_QRCODESCAN)) {
//                barcode_notice.setText("将二维码放在框内, 即可自动扫描");
//                title.setText("二维码");
//            } else if (scan_type.equals(SCAN_TYPE_BANK_CARD)) {
//                barcode_notice.setText("将银行卡放在框内, 即可自动扫描");
//                title.setText("银行卡");
//            }else
            if(scan_type.equals(SCAN_TYPE_ONHAND_CARD)){
                barcode_notice.setText("请将手持身份证放在框内，点击拍照即可");
                title.setText("手持身份证");
            } else if(scan_type.equals(SCAN_TYPE_ID_CARD)){
                barcode_notice.setText("请将身份证正面放在框内，点击拍照即可");
                title.setText("身份证正面");
            } else if(scan_type.equals(SCAN_TYPE_ID_CARD_DOWN)){
                barcode_notice.setText("请将身份证反面放在框内，点击拍照即可");
                title.setText("身份证背面");
            } else if(scan_type.equals(SCAN_TYPE_CREDIT_ONE)){
                barcode_notice.setText("请将征信的第一页放在框内，点击拍照即可");
                title.setText("征信报告");
            } else if(scan_type.equals(SCAN_TYPE_CREDIT_TWO)){
                barcode_notice.setText("请将征信的第二页放在框内，点击拍照即可");
                title.setText("征信报告");
            } else {
                barcode_notice.setText("将身份证放在框内, 即可自动扫描");
                title.setText("身份证");
            }
        }
        opreateView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (cameraManager != null) {
                    Config.KEY_FRONT_LIGHT = !Config.KEY_FRONT_LIGHT;
                    if (Config.KEY_FRONT_LIGHT == true) {
                        opreateView
                                .setImageResource(R.mipmap.mzw_camera_close);
                    } else {
                        opreateView
                                .setImageResource(R.mipmap.mzw_camera_open);
                    }
                    cameraManager.getConfigManager().initializeTorch(
                            cameraManager.getCamera().getParameters(), false);
                    onPause();
                    onResume();
                }
            }
        });


        scanBut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (cameraManager != null) {
                    btnType = 1;
                    Config.KEY_FRONT_LIGHT = !Config.KEY_FRONT_LIGHT;
                    if (Config.KEY_FRONT_LIGHT == true) {
                        opreateView
                                .setImageResource(R.mipmap.mzw_camera_close);
                    }
                    onPause();

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // CameraManager must be initialized here, not in onCreate(). This is
        // necessary because we don't
        // want to open the camera driver and measure the screen size if we're
        // going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially
        // off screen.
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        // CameraManager must be initialized here, not in onCreate(). This is
        // necessary because we don't
        // want to open the camera driver and measure the screen size if we're
        // going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());
//        cameraManager.getFramingRect(scan_type);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager, scan_type);

        handler = null;
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        beepManager.updatePrefs();

        inactivityTimer.onResume();
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        cameraManager.closeDriver();
        if (!hasSurface) {
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
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // restartPreviewAfterDelay(0L);
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                // Handle these events so they don't launch the Camera app
                return true;
            // Use volume up/down to turn on light
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                cameraManager.setTorch(false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                cameraManager.setTorch(true);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        // Bitmap isn't used yet -- will be used soon
        if (handler == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(handler,
                        R.id.decode_succeeded, savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG,
                    "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult The contents of the barcode.
     * @param barcode   A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode) {
        inactivityTimer.onActivity();

        boolean fromLiveScan = barcode != null;
        if (fromLiveScan) {
            // Then not from history, so beep/vibrate and we have an image to
            // draw on
            beepManager.playBeepSoundAndVibrate();
            // drawResultPoints(barcode, rawResult);
            viewfinderView.drawResultBitmap(barcode);
        }

        String text = rawResult.getText();
        finish(text);
    }


    /*
         bankcard  and idcard
     */
    public void handleDecodeTwo(String rawResult, Bitmap barcode) {
        inactivityTimer.onActivity();

        boolean fromLiveScan = barcode != null;
        if (fromLiveScan) {
            beepManager.playBeepSoundAndVibrate();
            viewfinderView.drawResultBitmap(barcode);
        }
        finish(rawResult);
    }

    /**
     * 获取finish掉 返回给上一个Activity解析图片结果
     *
     * @param text
     */
    private void finish(String text) {
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(RESULT, text);
        resultIntent.putExtras(bundle);
        setResult(RESULT_OK, resultIntent);

      //  finish();
    }

    /**
     * Superimpose a line for 1D or dots for 2D to highlight the key features of
     * the barcode.
     *
     * @param barcode   A bitmap of the captured image.
     * @param rawResult The decoded results which contains the points to draw.
     */
    private void drawResultPoints(Bitmap barcode, Result rawResult) {
        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0) {
            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.result_points));
            if (points.length == 2) {
                paint.setStrokeWidth(4.0f);
                drawLine(canvas, paint, points[0], points[1]);
            } else if (points.length == 4
                    && (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult
                    .getBarcodeFormat() == BarcodeFormat.EAN_13)) {
                // Hacky special case -- draw two lines, for the barcode and
                // metadata
                drawLine(canvas, paint, points[0], points[1]);
                drawLine(canvas, paint, points[2], points[3]);
            } else {
                paint.setStrokeWidth(10.0f);
                for (ResultPoint point : points) {
                    canvas.drawPoint(point.getX(), point.getY(), paint);
                }
            }
        }
    }

    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a,
                                 ResultPoint b) {
        canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG,
                    "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (handler == null) {
                //-------------------创建相机handler
                handler = new CaptureActivityHandler(this, viewfinderView,
                        decodeFormats, characterSet, cameraManager, scan_type);
            }
            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            finish();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    private static final int CHOOSE_PIC = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.photo:
                String type;
                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_PICK);
                intent1.setType("image/*");
//                if (scan_type.equals(SCAN_TYPE_QRCODESCAN)) {
//                    type = "选择二维码图片";
//                } else if (scan_type.equals(SCAN_TYPE_BANK_CARD)) {
//                    type = "选择银行卡图片";
//                }
                if(scan_type.equals(SCAN_TYPE_ONHAND_CARD)){
                    type = "选择手持身份证";
                } else if (scan_type.equals(SCAN_TYPE_ID_CARD)){
                    type = "选择身份证正面";
                } else if (scan_type.equals(SCAN_TYPE_ID_CARD_DOWN)){
                    type = "选择身份证反面";
                } else if (scan_type.equals(SCAN_TYPE_CREDIT_ONE)){
                    type = "选择征信第一页";
                } else if (scan_type.equals(SCAN_TYPE_CREDIT_TWO)){
                    type = "选择征信第二页";
                } else {
                    type = "选择身份证图片";
                }
                Intent intent2 = Intent.createChooser(intent1, type);
                startActivityForResult(intent2, CHOOSE_PIC);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imgPath = null;
        if (resultCode == FragmentActivity.RESULT_OK) {
            switch (requestCode) {
                case CHOOSE_PIC:
                    String[] proj = new String[]{MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(data.getData(),
                            proj, null, null, null);
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor
                                .getColumnIndex(MediaStore.Images.Media.DATA);
                        // 获取到用户选择的二维码图片的绝对路径
                        imgPath = cursor.getString(columnIndex);
                    }
                    cursor.close();

//                    // 获取二维码解析结果
//                    if (scan_type.equals(SCAN_TYPE_QRCODESCAN)) {
//                        Result res = null;
//                        try {
//                            res = parseQRcodeBitmap(imgPath);
//                        } catch (Exception e) {
//                            //	appConfig.handle("二维码解析失败..." + e.getLocalizedMessage());
//                        }
//                        if (res != null)
//                            finish(res.toString());
//                    } else if (scan_type.equals(SCAN_TYPE_BANK_CARD)) { //获取银行卡的解析结果
//                        String res = null;
//                        try {
//                            res = parseBankCardBitmap(imgPath);
//                        } catch (Exception e) {
//                        }
//                        if (res != null)
//                            finish(res);
//                    } else
                    if (scan_type.equals(SCAN_TYPE_ONHAND_CARD)) { //手持身份证
                        String res = null;
                        try {
                            res = parseOnHandIdCardBitmap(imgPath);
                        } catch (Exception e) {
                        }
                        if (res != null)
                            finish(res);
                    } else if (scan_type.equals(SCAN_TYPE_ID_CARD)) { //身份证正面
                        String res = null;
                        try {
                            res = parseBankCardBitmap(imgPath);
                        } catch (Exception e) {
                        }
                        if (res != null)
                            finish(res);
                    } else if (scan_type.equals(SCAN_TYPE_ID_CARD_DOWN)) { //身份证反面
                        String res = null;
                        try {
                            res = parseBankCardBitmap(imgPath);
                        } catch (Exception e) {
                        }
                        if (res != null)
                            finish(res);
                    } else if (scan_type.equals(SCAN_TYPE_CREDIT_ONE)) { //征信第一页
                        String res = null;
                        try {
                            res = parseCreditBitmap(imgPath);
                        } catch (Exception e) {
                        }
                        if (res != null)
                            finish(res);
                    } else if (scan_type.equals(SCAN_TYPE_CREDIT_TWO)) { //征信第二页
                        String res = null;
                        try {
                            res = parseCreditBitmap(imgPath);
                        } catch (Exception e) {
                        }
                        if (res != null)
                            finish(res);
                    }

                    else { //获取身份证的解析结果
                        String res = null;
                        try {
                            res = parseIdCardBitmap(imgPath);
                        } catch (Exception e) {
                        }
                        if (res != null)
                            finish(res);
                    }
                    break;
            }
        }

    }


    /*
      银行卡通过相册选择图片
     */
    private String parseBankCardBitmap(String bitmapPath) {
        // 获取到待解析的图片
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath);
        return BankCardIdentify.bankCardIdentify(this, bitmap);
    }


    /*
       手持身份证通过相册选择图片
     */
    private  String parseOnHandIdCardBitmap(String bitmapPath){
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath);
        return "";
    }

    /*
      身份证通过相册选择图片
     */
    private String parseIdCardBitmap(String bitmapPath) {
        // 获取到待解析的图片
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath);
        return IdCardIdentify.idCardIdentify(this, bitmap);
    }

    /*
       征信报告通过相册选择图片
     */
    private String parseCreditBitmap(String bitmapPath) {
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath);
        return "";
    }




    /*
       二维码通过相册选择图片
     */
    private com.google.zxing.Result parseQRcodeBitmap(String bitmapPath)
            throws NotFoundException, ChecksumException, FormatException {
        // 解析转换类型UTF-8
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
        // 获取到待解析的图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 如果我们把inJustDecodeBounds设为true，那么BitmapFactory.decodeFile(String path,
        // Options opt)
        // 并不会真的返回一个Bitmap给你，它仅仅会把它的宽，高取回来给你
        options.inJustDecodeBounds = true;
        // 此时的bitmap是null，这段代码之后，options.outWidth 和 options.outHeight就是我们想要的宽和高了
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath, options);
        // 我们现在想取出来的图片的边长（二维码图片是正方形的）设置为400像素
        /**
         * options.outHeight = 400; options.outWidth = 400;
         * options.inJustDecodeBounds = false; bitmap =
         * BitmapFactory.decodeFile(bitmapPath, options);
         */
        // 以上这种做法，虽然把bitmap限定到了我们要的大小，但是并没有节约内存，如果要节约内存，我们还需要使用inSimpleSize这个属性
        options.inSampleSize = options.outHeight / 400;
        if (options.inSampleSize <= 0) {
            options.inSampleSize = 1; // 防止其值小于或等于0
        }
        /**
         * 辅助节约内存设置
         *
         * options.inPreferredConfig = Bitmap.Config.ARGB_4444; //
         * 默认是Bitmap.Config.ARGB_8888 options.inPurgeable = true;
         * options.inInputShareable = true;
         */
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(bitmapPath, options);
        // 新建一个RGBLuminanceSource对象，将bitmap图片传给此对象
        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(bitmap);
        // 将图片转换成二进制图片
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                rgbLuminanceSource));
        // 初始化解析对象
        QRCodeReader reader = new QRCodeReader();
        // 开始解析
        Result result = null;
        result = reader.decode(binaryBitmap, hints);

        return result;
    }

}
