package com.tencent.liteav.demo.play.controller;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.tencent.liteav.demo.play.R;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by SeanLim on 2020/5/28.
 * Company by Shanghai observer information technology Co., Ltd.
 * E-mail linlin.1016@qq.com
 */
public class ConsumptionFlowOBAlertDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private TextView txtNotice, tvContinue, tvCancel;
    private CheckBox checkBoxRemember;
    private DialogSure _sure;
    private SharedPreferences sharedPreferences;

    public interface DialogSure {
        public abstract void onSureResult(ConsumptionFlowOBAlertDialog dialog, boolean flag);
    }

    public void setSure(DialogSure callSure) {
        _sure = callSure;
    }

    public interface DialogCancel {
        public abstract void onCancelResult(ConsumptionFlowOBAlertDialog dialog, boolean flag);
    }


    public ConsumptionFlowOBAlertDialog(Context context, String flow) {
        super(context, R.style.AlertDialogStyle);
        this.context = context;
        sharedPreferences = context.getSharedPreferences("dataIsRemember", Context.MODE_PRIVATE);
        setContentView(R.layout.view_alertdialog);
        txtNotice = (TextView) findViewById(R.id.dialog_message);
        tvCancel = (TextView) findViewById(R.id.tv_cancel);
        tvContinue = (TextView) findViewById(R.id.tv_continue);
        checkBoxRemember = (CheckBox) findViewById(R.id.checkBox_remember);
        initView();


        if (TextUtils.isEmpty(flow)) {
            txtNotice.setText(Html.fromHtml(MessageFormat.format(" 正在使用流量播放\n\n \r <br />继续播放将消耗手机流量", "")));
        } else {
            txtNotice.setText(Html.fromHtml(MessageFormat.format(" 正在使用流量播放\n\n \r <br />继续播放将消耗约{0}M流量", Integer.parseInt(flow) * 512 / 8 / 1000)));
        }
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _sure.onSureResult(ConsumptionFlowOBAlertDialog.this, true);
            }
        });

        checkBoxRemember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("isRemember", "true");
                    editor.commit();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("isRemember", "false");
                    editor.commit();
                }
            }
        });
    }


    private void initView() {
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float density = dm.density;
        density = 1;
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        params.width = (int) (width * density);
        params.height = (int) (height * density);
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
        window.setWindowAnimations(R.style.AnimBottom);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }
}
