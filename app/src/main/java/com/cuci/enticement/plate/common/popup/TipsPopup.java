package com.cuci.enticement.plate.common.popup;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cuci.enticement.R;
import com.lxj.xpopup.core.CenterPopupView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TipsPopup extends CenterPopupView {


    @BindView(R.id.desc)
    TextView desc;

    private String mDesc;

    public TipsPopup(@NonNull Context context, String desc, OnExitListener listener) {
        super(context);
        mOnExitListener = listener;
        mDesc = desc;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popup_tips_view;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        ButterKnife.bind(this);
        desc.setText(mDesc);
    }

    @OnClick({R.id.ok, R.id.cancel})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ok:
                if (mOnExitListener != null) {
                    mOnExitListener.onPositive();
                }
                dismiss();
                break;
            case R.id.cancel:
                dismiss();
                break;
        }
    }

    private OnExitListener mOnExitListener;

    public interface OnExitListener {
        void onPositive();
    }

}
