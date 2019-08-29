package com.spd.print.jx.barcodeprint;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.shizhefei.fragment.LazyFragment;
import com.spd.print.jx.R;
import com.spd.print.jx.application.BaseApp;
import com.spd.print.jx.popupwindow.PopupWindowActivity;
import com.speedata.libutils.SharedXmlUtil;

/**
 * @author zzc
 */
public class BarcodeFragment extends LazyFragment implements View.OnClickListener {

    private EditText etBarcodeContent;
    private int typeInt;
    private TextView tvBarcodeType;

    public BarcodeFragment() {
    }

    public BarcodeFragment setArguments(String type) {
        Bundle bundle = new Bundle();
        bundle.putString("_type", type);
        setArguments(bundle);
        return this;
    }

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_barcode);
        initView();
        tvBarcodeType.setText(getResources().getStringArray(R.array.barcode1)[0]);
        SharedXmlUtil.getInstance(getActivity(), "setting").write("barcode", 0);
    }

    private void initView() {
        tvBarcodeType = (TextView) findViewById(R.id.tv_barcode_type);
        tvBarcodeType.setOnClickListener(this);
        findViewById(R.id.btn_send_print).setOnClickListener(this);
        etBarcodeContent = (EditText) findViewById(R.id.et_barcode_content);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_barcode_type:
                Intent intent = new Intent(getActivity(), PopupWindowActivity.class);
                intent.putExtra("setting", "barcode");
                startActivityForResult(intent, 3);
                break;
            case R.id.btn_send_print:
                sendPrint();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            Bundle bundle = data.getExtras();
            String type = bundle.getString("listResult");
            typeInt = bundle.getInt("position");
            tvBarcodeType.setText(type);
            SharedXmlUtil.getInstance(getActivity(), "setting").write("barcode", typeInt);
        }
    }

    private void sendPrint() {
        String content = etBarcodeContent.getText().toString();
        if (!content.isEmpty()) {
//            BaseApp.getPrinterImpl().printBarCode(content);
        }
    }
}
