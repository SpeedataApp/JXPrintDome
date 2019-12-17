package com.spd.jxprint.setting.presenter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.printer.sdk.CanvasPrint;
import com.printer.sdk.FontProperty;
import com.printer.sdk.PrinterConstants;
import com.spd.jxprint.application.BaseApp;
import com.spd.jxprint.setting.PrintSettingActivity;
import com.spd.jxprint.setting.contract.PrintSettingContract;
import com.spd.jxprint.setting.model.PrintSettingModel;
import com.spd.jxprint.utils.XTUtils;
import com.spd.lib.mvp.BasePresenter;
import com.spd.print.jx.constant.ParamsConstant;
import com.spd.print.jx.utils.PictureUtils;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author :Reginer in  2019/8/21 12:10.
 * 联系方式:QQ:282921012
 * 功能描述:
 */
public class PrintSettingPresenter extends BasePresenter<PrintSettingActivity, PrintSettingModel> implements PrintSettingContract.Presenter {

    private ScheduledExecutorService executorService;

    @Override
    protected PrintSettingModel createModel() {
        return new PrintSettingModel();
    }

    @Override
    public void systemUpdate() {
        //开始升级
        UpdateThread update = new UpdateThread();
        update.start();
    }

    @Override
    public void connectPrinter() {
        BaseApp.getPrinterImpl().connectPrinter(getView());
    }

    @Override
    public void disconnectPrinter() {
        BaseApp.getPrinterImpl().closeConnect();
    }

    public void initPrint(int type, int density) {
        BaseApp.getPrinterImpl().setAllParams(ParamsConstant.paperType(type), ParamsConstant.density(density));
    }

    public void setPaperFeed() {
        BaseApp.getPrinterImpl().setPaperFeed(2);
    }

    public void setPaperBack() {
        BaseApp.getPrinterImpl().setPaperBack(2);
    }

    /**
     * 打印普通热敏纸示例
     *
     * @param text
     */
    public void printNormalTest(String text) {
        BaseApp.getPrinterImpl().initPrinter();
        BaseApp.getPrinterImpl().printText(text);
        BaseApp.getPrinterImpl().setFont(0, 0, 0, 0, 0);
        BaseApp.getPrinterImpl().setPrinter(2, 0);
        BaseApp.getPrinterImpl().setPrinter(1, 1);
    }

    /**
     * 打印标签纸示例
     *
     * @param text
     */
    public void printLabelTest(String text) {
        BaseApp.getPrinterImpl().initPrinter();
        BaseApp.getPrinterImpl().setPaperBack(10);
        BaseApp.getPrinterImpl().setPaperFeed(1);
        BaseApp.getPrinterImpl().initPrinter();
        BaseApp.getPrinterImpl().printText(text);
        test();
        BaseApp.getPrinterImpl().initPrinter();
        BaseApp.getPrinterImpl().searchGap();
    }

    public void test() {
        //生成二维码图片
        Bitmap bitmapQrCode = PictureUtils.createBitmapQrCode("物料编号",120,120);
        //创建画布
        CanvasPrint cp = new CanvasPrint();
        //初始化画布
        cp.init(PrinterConstants.PrinterType.M21);
        //创建字体
        FontProperty fp = new FontProperty();
        //字体属性赋值 此处参数个数根据SDK版本不同，有略微差别，酌情增减。
        fp.setFont(false, false, false, false, 26, null);
        //设置字体
        cp.setFontProperty(fp);
        cp.drawText(150, 30, "数量:");

        fp.setFont(false, false, false, false, 20, null);
        cp.setFontProperty(fp);
        //将文字画到画布上指定坐标处
        cp.drawText(10, 30, "物料名称：");
        cp.drawText(10, 60, "wuliaomingcheng");
        cp.drawText(10, 120, "规格型号");
        cp.drawText(10, 150, "guigexinghao");
        cp.drawText(220, 150, "wuliaobianhao");
        if (bitmapQrCode != null) {
            cp.drawImage(200, 0, bitmapQrCode);
        }
        //打印画布
        BaseApp.getPrinterImpl().printImage(cp.getCanvasImage(), 3, 0, false);
    }

    /**
     * 标签纸对齐缝隙
     */
    public void printAligning() {
        BaseApp.getPrinterImpl().searchGap();
    }

    /**
     * 打印自检页
     */
    public void printSelfCheck() {
        BaseApp.getPrinterImpl().printSelfCheck();
    }

    /**
     * 设置灵敏度
     *
     * @param sensitivity 灵敏度值
     */
    public void setSensitivity(String sensitivity) {
        if (sensitivity.isEmpty()) {
            int sen = Integer.parseInt(sensitivity);
            BaseApp.getPrinterImpl().setSensitivity(sen);
        }
    }

    /**
     * 读返回值
     *
     * @return 返回值
     */
    public String readStatus() {
        byte[] result = new byte[6];
        BaseApp.getPrinterImpl().read(result);
        Log.d("zzc", "read====" + Arrays.toString(result));
        return Arrays.toString(result);
    }

    /**
     * 设置浓度
     *
     * @param density 0——5 低到高
     */
    public void setDensity(int density) {
        BaseApp.getPrinterImpl().setDensity(density);
    }

    /**
     * 设置纸类型
     *
     * @param type 类型
     */
    public void setPaperType(int type) {
        BaseApp.getPrinterImpl().setPaperType(type);
    }


    /**
     * 疲劳测试
     */
    public void fatigueTest(final Resources resources) {
        executorService = new ScheduledThreadPoolExecutor(1, new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //do something
                try {
                    XTUtils.printNote(resources, BaseApp.getPrinterImpl());
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 6, TimeUnit.SECONDS);

    }

    /**
     * 停止疲劳测试
     */
    public void stopFatigueTest() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private class UpdateThread extends Thread {
        @Override
        public void run() {
            super.run();
            Looper.prepare();
            InputStream in = null;
            //创建文件夹
            File f = new File("/sdcard/Android/data/updata");
            if (!f.exists()) {
                f.mkdir();
            }
            //复制升级文件到指定目录
            copyFilesFromassets(getView(), "T581U0.73-V0.16-sbtV06.bin", "/sdcard/Android/data/updata/T581U0.73-V0.16-sbtV06.bin");
            //获取升级文件
            File fileParent = new File("/sdcard/Android/data/updata/T581U0.73-V0.16-sbtV06.bin");
            try {
                in = new FileInputStream(fileParent);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                getView().onUpdateError(e);
            }
            int a = 0;
            try {
                a = BaseApp.getPrinterImpl().update(in, "35 31 33 35 32");
                if (a == -2) {
                    getView().onUpdateSuccess();
                } else {
                    getView().onUpdateError(null);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                getView().onUpdateError(null);
            }
            Looper.loop();
        }
    }

    private void copyFilesFromassets(Context context, String oldPath, String newPath) {
        try {
            // 获取assets目录下的所有文件及目录名
            String fileNames[] = context.getAssets().list(oldPath);
            if (Objects.requireNonNull(fileNames).length > 0) {
                // 如果是目录
                File file = new File(newPath);
                file.mkdirs();
                // 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFromassets(context, oldPath + "/" + fileName,
                            newPath + "/" + fileName);
                }
            } else {
                // 如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                // 循环从输入流读取
                while ((byteCount = is.read(buffer)) != -1) {
                    // buffer字节
                    // 将读取的输入流写入到输出流
                    fos.write(buffer, 0, byteCount);
                }
                // 刷新缓冲区
                try {
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
