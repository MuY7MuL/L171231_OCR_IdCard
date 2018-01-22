package com.example.mumu.OCRProject.com.google.zxing.client.android.result;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.example.mumu.OCRProject.com.google.zxing.client.android.util.ImageFilter;
import com.example.mumu.OCRProject.com.google.zxing.client.android.util.ImgCutUtil;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;

/**
 * Created by MuMu on 2017/3/20. (识别身份证)  待处理需判断获得的有效行是否为卡号位置,tess-two识别会对有像素的位置进行识别,无论是卡号的位置还是其它位置都会进行识别(解决方案:同时满足模版匹配
 * 身份证号码前的字符图片)
 * 当前识别误差字符  0  6  9 需进一步训练    3  5扫描时会出现误差
 */
public class IdCardIdentify {
    /**
     * TessBaseAPI初始化用到的第一个参数，是个目录。
     */
    private static final String DATAPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "tesseract";
    /**
     * 在DATAPATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录。
     */
    private static final String tessdata = DATAPATH + File.separator + "tessdata";
    /**
     * TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
     */
    private static final String DEFAULT_LANGUAGE = "font";
    /**
     * assets中的文件名
     */
    private static final String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
    /**
     * 保存到SD卡中的完整文件名
     */
    private static final String LANGUAGE_PATH = tessdata + File.separator + DEFAULT_LANGUAGE_NAME;

    public static String idCardIdentify(Activity activity, Bitmap bitmap) {
        //Android6.0之前安装时就能复制，6.0之后要先请求权限，所以6.0以上的这个方法无用。
        copyToSD(activity, LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
        bitmap = ImgCutUtil.scaleImage(bitmap, 900, 450); //根据像素放大缩小图片
        bitmap = ImageFilter.doICPretreatmentOne(bitmap);//图像预处理
        saveBitmap(ImageFilter.doICPretreatmentTwo(bitmap));
        return getResult(ImageFilter.doICPretreatmentTwo(bitmap)); //返回有效行识别结果
    }

    /**
     * 将裁剪的图片保存到文件夹里
     *
     * @param bitmap 要识别的图片
     */
    public static void saveBitmap(Bitmap bitmap) {
        Log.e(TAG, "保存图片");
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "img", "Card_number");
        if (f.exists()) {
            f.delete();
        } else {
            f.mkdirs();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(TAG, "已经保存");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    /**
     * 对要识别的图像进行识别
     *
     * @param bitmap 要识别的bitmap
     * @return
     */
    public static String getResult(Bitmap bitmap) {
        String result;
        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATAPATH, "font");
//        baseApi.init(DATAPATH,"chi_sim");
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        baseApi.setImage(bitmap);
        baseApi.setVariable("tessedit_char_whitelist", "0123456789X"); //暂时只识别身份证信息(身份证号码特征0123456789X)
        result = baseApi.getUTF8Text();
        result = result.replaceAll("\\s*", "");


        if (result.equals("") || result.length() <= 20 || result.length() >= 40) { //允许4个字符的误差
            result = null;
        }
        baseApi.end();
        bitmap.recycle();
        return result;

    }


    /**
     * 获取自己训练的语言包文件
     *
     * @param activity
     * @param path
     * @param name
     */
    public static void copyToSD(Activity activity, String path, String name) {
        Log.i(TAG, "copyToSD: " + path);
        Log.i(TAG, "copyToSD: " + name);
        //如果存在就删掉
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
        if (!f.exists()) {
            File p = new File(f.getParent());
            if (!p.exists()) {
                p.mkdirs();
            }
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            is = activity.getAssets().open(name);
            File file = new File(path);
            os = new FileOutputStream(file);
            byte[] bytes = new byte[2048];
            int len = 0;
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
                if (os != null)
                    os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
