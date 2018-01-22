package com.example.mumu.OCRProject.com.google.zxing.client.android.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by MuMu on 2017/4/14.(对身份证和银行卡进行裁剪  取到卡号位置, 后期可能要识别更多数据)
 */

public class ImgCutUtil {
    /**
     * @param src       源Bitmap
     * @param x         开始x坐标
     * @param y         开始y坐标
     * @param width     截取宽度
     * @param height    截取高度
     * @param isRecycle 是否回收原图像
     * @return Bitmap
     * @brief 裁剪Bitmap
     */
    public static Bitmap cropBitmap(Bitmap src, int x, int y, int width, int height, boolean isRecycle) {
        if (x == 0 && y == 0 && width == src.getWidth() && height == src.getHeight()) {
            return src;
        }
        Bitmap dst = Bitmap.createBitmap(src, x, y, width, height);
        if (isRecycle && dst != src) {
            src.recycle();
        }
        return dst;
    }


    /**
     * 缩放图片
     *
     * @param bm        要缩放图片
     * @param newWidth  宽度
     * @param newHeight 高度
     * @return处理后的图片
     */
    public static Bitmap scaleImage(Bitmap bm, int newWidth, int newHeight) {
        if (bm == null) {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        if (bm != null & !bm.isRecycled()) {
            bm.recycle();//销毁原图片
            bm = null;
        }
        return newbm;
    }
}
