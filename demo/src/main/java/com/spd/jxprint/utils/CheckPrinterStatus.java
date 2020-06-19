package com.spd.jxprint.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import com.spd.jxprint.R;
import com.spd.jxprint.application.BaseApp;
import com.spd.print.jx.utils.ToastUtil;

/**
 * 查询打印机缺纸状态
 *
 * @author zzc
 */
public class CheckPrinterStatus {
    private static CheckPrinterStatus mCheckPrinterStatus;
    private boolean isLoop = false;
    private static Context mContext;

    private CheckPrinterStatus() {
    }

    public static CheckPrinterStatus getInstance(Context context) {
        if (mCheckPrinterStatus == null) {
            mCheckPrinterStatus = new CheckPrinterStatus();
            mContext = context;
        }
        return mCheckPrinterStatus;
    }

    public void startCheck() {
        isLoop = true;
        CheckStatusThread checkStatusThread = new CheckStatusThread();
        checkStatusThread.start();
    }

    public void stopCheck() {
        isLoop = false;
    }

    class CheckStatusThread extends Thread {
        @Override
        public void run() {
            while (isLoop) {
                int status = BaseApp.getPrinterImpl().getPrinterStatus();
                if (status == 1) {
                    ToastUtil.customToastView(mContext, mContext.getString(R.string.out_of_paper), Toast.LENGTH_SHORT
                            , (TextView) LayoutInflater.from(mContext).inflate(R.layout.layout_toast, null));
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
