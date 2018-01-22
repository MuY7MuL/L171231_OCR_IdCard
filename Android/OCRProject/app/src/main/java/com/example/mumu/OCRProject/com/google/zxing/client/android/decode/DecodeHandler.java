/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mumu.OCRProject.com.google.zxing.client.android.decode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import android.media.FaceDetector;
import android.graphics.Rect;

import com.example.mumu.OCRProject.R;
import com.example.mumu.OCRProject.com.google.zxing.client.android.result.BankCardIdentify;
import com.example.mumu.OCRProject.com.google.zxing.client.android.result.IdCardIdentify;
import com.example.mumu.OCRProject.com.google.zxing.custom.CaptureActivity;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Map;

final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final CaptureActivity activity;
    private final MultiFormatReader multiFormatReader;
    private boolean running = true;
    private String scan_type;
    public  int cesi = 0;
    final int N_MAX = 2;

    DecodeHandler(CaptureActivity activity, Map<DecodeHintType, Object> hints, String scan_type) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.activity = activity;
        this.scan_type = scan_type;
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        int what = message.what;
        if (what == R.id.decode ) {
            decode((byte[]) message.obj, message.arg1, message.arg2);
        }else if (what == R.id.quit) {
            Looper.myLooper().quit();
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency, reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        long start = System.currentTimeMillis();
        Result rawResult = null;
        String result = null;
        int faceCount = 0;
        int s = data.length;

        byte[] rotatedData =  new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
        int tmp = width;
        width = height;
        height = tmp;

        data = rotatedData;
        Handler handler = activity.getHandler();
        PlanarYUVLuminanceSource source = activity.getCameraManager().buildLuminanceSource(data, width, height, scan_type);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                if (scan_type.equals(CaptureActivity.SCAN_TYPE_QRCODESCAN)) {
                    rawResult = multiFormatReader.decodeWithState(bitmap);//解析二维码图片
                } else if (scan_type.equals(CaptureActivity.SCAN_TYPE_BANK_CARD)) {
                    result = BankCardIdentify.bankCardIdentify(activity, toBitmap(source, source.renderCroppedGreyscaleBitmap()));//解析银行卡
                } else {
                    result = IdCardIdentify.idCardIdentify(activity, toBitmap(source, source.renderCroppedGreyscaleBitmap()));  //解析身份证


                }
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }

        if (rawResult != null || result != null ) {
            // Don't log the barcode contents for security.
            long end = System.currentTimeMillis();
            Log.d(TAG, "Found barcode in " + (end - start) + " ms");
            if (handler != null) {
                Bundle bundle = new Bundle();
                if (scan_type.equals(CaptureActivity.SCAN_TYPE_QRCODESCAN)) {
                    Message message = Message.obtain(handler, R.id.decode_succeeded, rawResult); //解析的相关信息返回
                    Bitmap grayscaleBitmap = toBitmap(source, source.renderCroppedGreyscaleBitmap());  //相机捕捉到图像返回
                    bundle.putParcelable(DecodeThread.BARCODE_BITMAP, grayscaleBitmap);
                    message.setData(bundle);
                    message.sendToTarget();
                } else if (scan_type.equals(CaptureActivity.SCAN_TYPE_BANK_CARD)) {
                    Message message = Message.obtain(handler, R.id.decode_succeeded, result); //解析银行卡的相关信息返回
                    Bitmap grayscaleBitmap = toBitmap(source, source.renderCroppedGreyscaleBitmap());  //相机捕捉到图像返回
                    bundle.putParcelable(DecodeThread.BARCODE_BITMAP, grayscaleBitmap);  //测试 返回要识别的位置图片
                    message.setData(bundle);
                    message.sendToTarget();
                } else {
                    //身份证人脸识别
                    faceCount=initFaceDetect(toBitmap(source, source.renderCroppedGreyscaleBitmap()));
                    if(faceCount>=1) {
                        Message message = Message.obtain(handler, R.id.decode_succeeded, result); //解析身份证的相关信息返回
                        Bitmap grayscaleBitmap = toBitmap(source, source.renderCroppedGreyscaleBitmap());  //相机捕捉到图像返回
                        bundle.putParcelable(DecodeThread.BARCODE_BITMAP, grayscaleBitmap);  //测试 返回要识别的位置图片
                        message.setData(bundle);
                        message.sendToTarget();
                    }
                }
            }
        }else {
            if (handler != null) {
                Message message = Message.obtain(handler, R.id.decode_failed);
                message.sendToTarget();
            }
        }
    }


    public int initFaceDetect(Bitmap bitmap){
        Bitmap srcFace = bitmap.copy(Bitmap.Config.RGB_565, true);
        int w = srcFace.getWidth();
        int h = srcFace.getHeight();
        FaceDetector faceDetector = null;
        FaceDetector.Face[] face;
        faceDetector = new FaceDetector(w, h, N_MAX);
        face = new FaceDetector.Face[N_MAX];

        int nFace = faceDetector.findFaces(srcFace, face);



//        int count = 0;
//        for(int i=0; i<nFace; i++){
//            Face f  = face[i];
//            PointF midPoint = new PointF();
//            float dis = f.eyesDistance();
//            f.getMidPoint(midPoint);
//            int dd = (int)(dis);
//            Point eyeLeft = new Point((int)(midPoint.x - dis/2), (int)midPoint.y);
//            Point eyeRight = new Point((int)(midPoint.x + dis/2), (int)midPoint.y);
//            Rect faceRect = new Rect((int)(midPoint.x - dd), (int)(midPoint.y - dd), (int)(midPoint.x + dd), (int)(midPoint.y + dd));
//            if(checkFace(faceRect)){
//                count = count+1;
//            }
//        }
        return nFace;
    }

    public boolean checkFace(Rect rect){
        int w = rect.width();
        int h = rect.height();
        int s = w*h;
        if(s < 10000){
            return false;
        }
        else{
            return true;
        }
    }

    public Bitmap toBitmap(LuminanceSource source, int[] pixels) {
        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

}
