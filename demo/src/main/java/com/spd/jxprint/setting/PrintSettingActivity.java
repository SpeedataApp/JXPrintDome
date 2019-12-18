package com.spd.jxprint.setting;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.spd.jxprint.R;
import com.spd.jxprint.application.BaseApp;
import com.spd.jxprint.popupwindow.PopupWindowActivity;
import com.spd.jxprint.setting.contract.PrintSettingContract;
import com.spd.jxprint.setting.presenter.PrintSettingPresenter;
import com.spd.lib.mvp.BaseMvpActivity;
import com.spd.print.jx.constant.PrintConstant;
import com.spd.print.jx.inter.IConnectCallback;
import com.spd.print.jx.utils.ToastUtil;
import com.speedata.libutils.SharedXmlUtil;

import java.util.Objects;

/**
 * @author :Reginer in  2019/8/21 12:11.
 * 联系方式:QQ:282921012
 * 功能描述:打印设置
 */
public class PrintSettingActivity extends BaseMvpActivity<PrintSettingPresenter> implements View.OnClickListener, PrintSettingContract.View, IConnectCallback {

    private AlertDialog dialog;
    private TextView tvPaperType, tvDensity, statusName, statusAddress, tvFatigue;
    private Button btnConnect, btnPrintTest, btnAligning;
    private SharedXmlUtil mSharedXmlUtil;
    /**
     * 是否正在打印
     */
    private boolean isPrint = false;
    private int typeInt, densityInt;

    @Override
    protected int getActLayoutId() {
        return R.layout.activity_print_setting;
    }

    @Override
    protected void initToolbar() {
        super.initToolbar();
        mToolBar.setNavigationIcon(R.drawable.ic_back);
    }

    @Override
    protected void initView(@Nullable Bundle savedInstanceState) {
        findViewById(R.id.tv_update).setOnClickListener(this);
        findViewById(R.id.rl_feed_paper).setOnClickListener(this);
        findViewById(R.id.rl_back_paper).setOnClickListener(this);
        btnPrintTest = findViewById(R.id.btn_print_test);
        btnPrintTest.setOnClickListener(this);
        findViewById(R.id.rl_print_self).setOnClickListener(this);
        findViewById(R.id.rl_paper_type).setOnClickListener(this);
        findViewById(R.id.rl_density).setOnClickListener(this);
        tvPaperType = findViewById(R.id.set_paper_type);
        tvDensity = findViewById(R.id.set_density);
        findViewById(R.id.btn_set_density).setOnClickListener(this);
        findViewById(R.id.btn_set_paper_type).setOnClickListener(this);
        findViewById(R.id.rl_fatigue_test).setOnClickListener(this);
        btnConnect = findViewById(R.id.setting_connectPrinter);
        btnConnect.setOnClickListener(this);
        statusName = findViewById(R.id.tv_status_name);
        statusAddress = findViewById(R.id.tv_status_address);
        tvFatigue = findViewById(R.id.tv_fatigue_test);
        btnAligning = findViewById(R.id.btn_aligning);
        btnAligning.setOnClickListener(this);
    }

    @Override
    protected PrintSettingPresenter createPresenter() {
        return new PrintSettingPresenter();
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.tv_update:
                    UpdateDialog updateDialog = new UpdateDialog(this);
                    updateDialog.setOnSureListener(new UpdateDialog.OnSureListener() {
                        @Override
                        public void click() {
                            progressDialogShow();
                            mPresenter.systemUpdate();
                        }
                    });
                    updateDialog.show();
                    break;
                case R.id.rl_feed_paper:
                    mPresenter.setPaperFeed();
                    break;
                case R.id.rl_back_paper:
                    mPresenter.setPaperBack();
                    break;
                case R.id.btn_print_test:
                    if (mSharedXmlUtil.read("paper_type", 0) == 0) {
                        mPresenter.printNormalTest(getString(R.string.example_text));
                    } else {
                        mPresenter.printLabelTest(getString(R.string.example_text));
                    }
                    break;
                case R.id.rl_print_self:
                    mPresenter.printSelfCheck();
                    break;
                case R.id.btn_set_density:
                    mPresenter.setDensity(densityInt);
                    mSharedXmlUtil.write("density", densityInt);
                    break;
                case R.id.btn_set_paper_type:
                    mPresenter.setPaperType(typeInt);
                    setPaperType(typeInt);
                    mSharedXmlUtil.write("paper_type", typeInt);
                    break;
                case R.id.rl_fatigue_test:
                    if (!BaseApp.isConnection) {
                        ToastUtil.customToastView(mContext, getString(R.string.toast_error_tips), Toast.LENGTH_SHORT
                                , (TextView) LayoutInflater.from(mContext).inflate(R.layout.layout_toast, null));
                    }
                    if (isPrint || !BaseApp.isConnection) {
                        isPrint = false;
                        mPresenter.stopFatigueTest();
                        tvFatigue.setText(getString(R.string.start_fatigue_test));
                    } else {
                        isPrint = true;
                        mPresenter.fatigueTest(getResources());
                        tvFatigue.setText(getString(R.string.stop_fatigue_test));
                    }
                    break;
                case R.id.setting_connectPrinter:
                    if (!BaseApp.isConnection) {
                        mPresenter.connectPrinter();
                    } else {
                        mPresenter.disconnectPrinter();
                        statusName.setText(getResources().getString(R.string.status_disconnect));
                        statusAddress.setText(getResources().getString(R.string.status_disconnect));
                        btnConnect.setText(getResources().getString(R.string.connect_printer));
                    }
                    break;
                case R.id.rl_paper_type:
                    Intent intentType = new Intent(this, PopupWindowActivity.class);
                    intentType.putExtra("setting", "type");
                    startActivityForResult(intentType, 1);
                    break;
                case R.id.rl_density:
                    Intent intentDensity = new Intent(this, PopupWindowActivity.class);
                    intentDensity.putExtra("setting", "density");
                    startActivityForResult(intentDensity, 2);
                    break;
                case R.id.btn_aligning:
                    mPresenter.printAligning();
                    break;
                default:
                    break;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            ToastUtil.customToastView(mContext, getString(R.string.toast_error_tips), Toast.LENGTH_SHORT
                    , (TextView) LayoutInflater.from(mContext).inflate(R.layout.layout_toast, null));
        }
    }

    @Override
    public void onUpdateSuccess() {
        // TODO: 2019/8/21 升级成功
        progressDialogDismiss();
        mPresenter.disconnectPrinter();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                statusName.setText(getResources().getString(R.string.status_disconnect));
                statusAddress.setText(getResources().getString(R.string.status_disconnect));
                btnConnect.setText(getResources().getString(R.string.connect_printer));
                ToastUtil.customToastView(mContext, getString(R.string.toast_operation_success), Toast.LENGTH_SHORT
                        , (TextView) LayoutInflater.from(mContext).inflate(R.layout.layout_toast, null));
            }
        });
    }

    @Override
    public void onUpdateError(Exception e) {
        // TODO: 2019/8/21 升级失败
        progressDialogDismiss();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtil.customToastView(mContext, getString(R.string.toast_operation_fail), Toast.LENGTH_SHORT
                        , (TextView) LayoutInflater.from(mContext).inflate(R.layout.layout_toast, null));
            }
        });
    }

    private void progressDialogShow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_progress);
        WindowManager.LayoutParams wm = window.getAttributes();
        // 设置对话框的宽
        wm.width = 700;
        // 设置对话框的高
        wm.height = 500;
        // 对话框背景透明度
        wm.alpha = 1f;
        // 遮罩层亮度
        wm.dimAmount = 0.4f;
        window.setAttributes(wm);
    }

    private void progressDialogDismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onPrinterConnectSuccess() {
        BaseApp.isConnection = true;
        BaseApp.deviceName = "Serial device";
        BaseApp.deviceAddress = "/dev/ttyMT0";
        statusName.setText(BaseApp.deviceName);
        statusAddress.setText(BaseApp.deviceAddress);
        btnConnect.setText(getResources().getString(R.string.disconnect_printer));
        densityInt = mSharedXmlUtil.read("density", 1);
        typeInt = mSharedXmlUtil.read("paper_type", 1);
        mPresenter.initPrint(typeInt, densityInt);
        ToastUtil.customToastView(mContext, getString(R.string.toast_success), Toast.LENGTH_SHORT
                , (TextView) LayoutInflater.from(mContext).inflate(R.layout.layout_toast, null));
    }

    @Override
    public void onPrinterConnectFailed(int errorCode) {
        BaseApp.isConnection = false;
        BaseApp.deviceName = getResources().getString(R.string.status_disconnect);
        BaseApp.deviceAddress = getResources().getString(R.string.status_disconnect);
        switch (errorCode) {
            case PrintConstant.CONNECT_CLOSED:
                ToastUtil.customToastView(mContext, getString(R.string.toast_close), Toast.LENGTH_SHORT
                        , (TextView) LayoutInflater.from(mContext).inflate(R.layout.layout_toast, null));
                break;
            default:
                ToastUtil.customToastView(mContext, getString(R.string.toast_fail), Toast.LENGTH_SHORT
                        , (TextView) LayoutInflater.from(mContext).inflate(R.layout.layout_toast, null));
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedXmlUtil = SharedXmlUtil.getInstance(this, "setting");
        densityInt = mSharedXmlUtil.read("density", 1);
        typeInt = mSharedXmlUtil.read("paper_type", 1);
        setPaperType(typeInt);
        setDensity(densityInt);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BaseApp.isConnection) {
            btnConnect.setText(getResources().getString(R.string.disconnect_printer));
        } else {
            btnConnect.setText(getResources().getString(R.string.connect_printer));
        }
        statusName.setText(BaseApp.deviceName);
        statusAddress.setText(BaseApp.deviceAddress);
    }

    private void setPaperType(int i) {
        switch (i) {
            case 1:
                tvPaperType.setText(getString(R.string.label_paper));
                btnPrintTest.setText(getString(R.string.print_label_paper));
                btnAligning.setVisibility(View.VISIBLE);
                break;
            default:
                tvPaperType.setText(getString(R.string.normal_paper));
                btnPrintTest.setText(getString(R.string.print_normal_paper));
                btnAligning.setVisibility(View.GONE);
                break;
        }
    }

    @SuppressLint("DefaultLocale")
    private void setDensity(int i) {
        tvDensity.setText(String.format("%d", i + 1));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = Objects.requireNonNull(data).getExtras();
            switch (requestCode) {
                case 1:
                    if (bundle != null) {
                        String type = bundle.getString("listResult");
                        typeInt = bundle.getInt("position");
                        tvPaperType.setText(type);
                    }
                    break;
                case 2:
                    if (bundle != null) {
                        String density = bundle.getString("listResult");
                        densityInt = bundle.getInt("position");
                        tvDensity.setText(density);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
