package com.example.mumu.OCRProject.com.google.zxing.client.android.util;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import static org.opencv.core.CvType.CV_8U;
import static org.opencv.imgproc.Imgproc.MORPH_CROSS;

/**
 * Created by MuMu on 2017/4/14. (身份证和银行卡图像优化预处理处理)
 */
public class ImageFilter {

    /**
     * 银行卡图像预处理
     *
     * @param bitmap 源图像
     * @return
     */
    public static Bitmap doBKPretreatment(Bitmap bitmap, Bitmap template) {
        Mat rgbMat = new Mat(); //原图
        Mat templateMat = new Mat();//银联模版图
        Mat grayMat = new Mat();  //原图灰度图
        Mat templategrayMat = new Mat();//银联模版图灰度图
        Mat cropBank;//卡号图
        Mat contours = new Mat();
        Mat templatecontours = new Mat();
        Utils.bitmapToMat(bitmap, rgbMat);
        Utils.bitmapToMat(template, templateMat);
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//灰度化
        Imgproc.blur(grayMat, grayMat, new Size(3, 3));//高斯滤波处理
        Imgproc.Canny(grayMat, contours, 50, 50 * 3); //边缘检测
        Imgproc.cvtColor(templateMat, templategrayMat, Imgproc.COLOR_RGB2GRAY);//灰度化
        Imgproc.blur(templategrayMat, templategrayMat, new Size(3, 3));//高斯滤波处理
        Imgproc.Canny(templategrayMat, templatecontours, 50, 50 * 3); //边缘检测
        cropBank = isFindUPflag(contours, templatecontours);
        if (cropBank != null && cropBank.rows() > 0 && cropBank.cols() > 0) {
            bitmap = Bitmap.createBitmap(cropBank.cols(), cropBank.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(cropBank, bitmap);
        } else {
            bitmap = ImgCutUtil.cropBitmap(bitmap, 40, 200, 820, 100, true); //模版匹配失败  粗略得到卡号的位置
        }
        return bitmap;
    }


    /**
     * 银联标志模版匹配  找到银行卡的银联标志位置(有局限性)
     */
    static Mat isFindUPflag(Mat grayMat, Mat templategrayMat) {
        Mat UPflag = new Mat();
        int result_cols = grayMat.cols() - templategrayMat.cols() + 1;
        int result_rows = grayMat.rows() - templategrayMat.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_8U);
        Imgproc.matchTemplate(grayMat, templategrayMat, result, Imgproc.TM_CCORR_NORMED);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc;
        matchLoc = mmr.maxLoc;
        Imgproc.rectangle(grayMat, matchLoc, new Point(matchLoc.x + templategrayMat.cols(),
                matchLoc.y + templategrayMat.rows()), new Scalar(255, 0, 0), 1, 8, 0);
        if (matchLoc.x < grayMat.cols() / 2 || matchLoc.y < grayMat.rows() / 2) { //暂时只对银联标志在右下的位置进行处理(其它位置待匹配)
        } else {
            UPflag = cropBankNoByUPflag(grayMat, matchLoc);
        }
        return UPflag;
    }

    /**
     * 根据银联标志的位置粗略得到银行卡号位置的mat
     */
    static Mat cropBankNoByUPflag(Mat cropped, Point minLoc) {
        Rect rc = new Rect();
        rc.x = 35;//左起
        rc.width = cropped.cols() - 35;//右结束
        rc.y = (int) (minLoc.y - 150);
        rc.height = 130;
        return new Mat(cropped, rc);
    }


    /**
     * 对银行卡卡号的位置图像进行处理
     */
    public static Bitmap doBCPretreatmentTwo(Bitmap bit) {
        Mat bitMat = new Mat();//卡号位置图
        Mat binaryMat = new Mat(); //二值化图
        Mat grayMat = new Mat();
        Mat blur = new Mat();
        Mat grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_x = new Mat();
        Mat abs_grad_y = new Mat();
        Mat grad = new Mat();
        Utils.bitmapToMat(bit, bitMat);
        Imgproc.cvtColor(bitMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(grayMat, grayMat, new Size(3, 3));//高斯滤波处理
        Imgproc.adaptiveThreshold(grayMat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 17, 9);//二值化处理
        Imgproc.medianBlur(binaryMat, binaryMat, 3);//中值平滑处理
//        Mat element_9 = new Mat(4, 4, CV_8U, new Scalar(1));
//        Imgproc.morphologyEx(binaryMat, element_9, MORPH_CROSS, element_9);//闭运算
        /**
         * 锐化处理(增加对比强度)
         */
//        Imgproc.GaussianBlur(binaryMat, blur, new Size(3, 3), 0, 0, BORDER_DEFAULT);
//        // 分别计算x方向和y方向的导数，ddepth为图像的深度，应该避免溢出的情况，因此设置CV_16S
//        Imgproc.Sobel(binaryMat, grad_x, CV_16S, 1, 0, 3, 1, 0, BORDER_DEFAULT);
//        Imgproc.Sobel(binaryMat, grad_y, CV_16S, 0, 1, 3, 1, 0, BORDER_DEFAULT);
//        //将其转成CV_8U
//        Core.convertScaleAbs(grad_x, abs_grad_x);
//        Core.convertScaleAbs(grad_y, abs_grad_y);
//        //用两个方向的倒数去模拟梯度
//        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, grad);
        bit = Bitmap.createBitmap(binaryMat.cols(), binaryMat.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(binaryMat, bit);
        return bit;
    }

    /**
     * 身份证图像预处理 (找到卡号的位置)
     *
     * @param bitmap 源图像
     * @return
     */
    public static Bitmap doICPretreatmentOne(Bitmap bitmap) {
        Mat rgbMat = new Mat(); //原图
        Mat grayMat = new Mat();  //灰度图
        Mat binaryMat = new Mat(); //二值化图
        Mat canny = new Mat();
        Utils.bitmapToMat(bitmap, rgbMat);
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//灰度化
        Imgproc.blur(grayMat, canny, new Size(3, 3));//低通滤波处理
        Imgproc.Canny(grayMat, canny, 125, 225);//边缘检测处理类
        Imgproc.threshold(canny, binaryMat, 165, 255, Imgproc.THRESH_BINARY);//二值化
        Imgproc.medianBlur(binaryMat, binaryMat, 3);//中值平滑处理
        Mat element_9 = new Mat(20, 20, CV_8U, new Scalar(1));
        Imgproc.morphologyEx(binaryMat, element_9, MORPH_CROSS, element_9);//闭运算
        /**
         * 轮廓提取()
         */
        ArrayList<MatOfPoint> contoursList = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(element_9, contoursList, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE);
        Mat resultImage = Mat.zeros(element_9.size(), CV_8U);
        Imgproc.drawContours(resultImage, contoursList, -1, new Scalar(255, 0, 255));
        Mat effective = new Mat(); //身份证位置
        //外包矩形区域
        for (int i = 0; i < contoursList.size(); i++) {
            Rect rect = Imgproc.boundingRect(contoursList.get(i));
            if (rect.width != rect.height && rect.width / rect.height > 8) { //初步判断找到有效位置
                Imgproc.rectangle(resultImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 255), 1);
                effective = new Mat(rgbMat, rect);
            }
        }
        if (effective != null && effective.cols() > 0 && effective.rows() > 0) {
            bitmap = Bitmap.createBitmap(effective.cols(), effective.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(effective, bitmap);
        } else {
            bitmap = ImgCutUtil.cropBitmap(bitmap, 280, 360, 600, 70, true);
        }
        return bitmap;
    }


    /**
     * 身份证识别第二步(对有效行的图像处理)
     *
     * @param bitmap
     * @return
     */
    public static Bitmap doICPretreatmentTwo(Bitmap bitmap) {
        Mat rgbMat = new Mat(); //原图
        Mat grayMat = new Mat();  //灰度图
        Mat binaryMat = new Mat(); //二值化图
        Utils.bitmapToMat(bitmap, rgbMat);
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//灰度化
        Imgproc.threshold(grayMat, binaryMat, 150, 255, Imgproc.THRESH_BINARY);//二值化
        bitmap = Bitmap.createBitmap(binaryMat.cols(), binaryMat.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(binaryMat, bitmap);
        return bitmap;
    }
}
