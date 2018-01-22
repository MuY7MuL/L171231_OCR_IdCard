package com.example.mumu.OCRProject.com.google.zxing.client.android.result;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.example.mumu.OCRProject.R;
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
 * Created by MuMu on 2017/2/20.(识别银行卡卡号)  待处理(当前通过固定的卡号位置进行裁剪找到卡号位置进行识别显然不可行)   通过身份证的处理方式显然也不可行,银行卡背景复杂
 * 干扰大   通过模版匹配即通过匹配银联标志  但由于银行卡的银联标志各异, 大概在70%左右
 */

public class BankCardIdentify {
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

    public static String bankCardIdentify(Activity activity, Bitmap bitmap) {
        Bitmap template = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_bank);//银行卡银联模版图片
        copyToSD(activity, LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
        bitmap = ImgCutUtil.scaleImage(bitmap, 900, 450); //根据像素放大缩小图片
        bitmap = ImageFilter.doBKPretreatment(bitmap, template);//图像预处理
        saveBitmap(ImageFilter.doBCPretreatmentTwo(bitmap));
        return getResult(ImageFilter.doBCPretreatmentTwo(bitmap));
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
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        baseApi.setImage(bitmap);
        baseApi.setVariable("tessedit_char_whitelist", "0123456789"); //暂时只识别银行卡卡号()
        result = baseApi.getUTF8Text();
        result = result.replaceAll("\\s*", "");
        if (result.equals("")) {
            result = null;
        }
        baseApi.end();
        return result;

    }


    /**
     * 获取语言包处理
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
