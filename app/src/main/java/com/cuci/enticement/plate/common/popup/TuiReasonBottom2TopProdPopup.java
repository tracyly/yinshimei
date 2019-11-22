package com.cuci.enticement.plate.common.popup;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.cuci.enticement.R;
import com.cuci.enticement.plate.mine.vm.OrderViewModel;
import com.cuci.enticement.utils.DimensionUtils;
import com.lxj.xpopup.core.BottomPopupView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TuiReasonBottom2TopProdPopup extends BottomPopupView {

    private static final String TAG = TuiReasonBottom2TopProdPopup.class.getSimpleName();


    private OrderViewModel mViewModel;

    private int mScreenWidth;
    private Context mContext;


    @BindView(R.id.container)
    ConstraintLayout mContainer;


    public TuiReasonBottom2TopProdPopup(@NonNull Context context, OnCommitClickListener OnCommitClickListener) {
        super(context);
        mContext = context;

        mScreenWidth = DimensionUtils.getScreenWidth(context);
        mOnCommitClickListener = OnCommitClickListener;
    }


    @Override
    protected int getImplLayoutId() {

        return R.layout.popup_share_bottom_to_top_tui_reason;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        ButterKnife.bind(this);


        ViewGroup.LayoutParams layoutParams = mContainer.getLayoutParams();
        layoutParams.width = mScreenWidth;
        mContainer.setLayoutParams(layoutParams);

    }

    @OnClick({R.id.text_1, R.id.text_2,R.id.text_3,R.id.text_4,R.id.text_5, R.id.cancel_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.text_1:
                if (mOnCommitClickListener != null) {
                    mOnCommitClickListener.onCommitClick("7天无理由退换货");
                    dismiss();
                }
                break;
            case R.id.text_2:
                if (mOnCommitClickListener != null) {
                    mOnCommitClickListener.onCommitClick("少件");
                    dismiss();
                }
                break;
            case R.id.text_3:
                if (mOnCommitClickListener != null) {
                    mOnCommitClickListener.onCommitClick("收到商品时有破损/污渍/变形");
                    dismiss();
                }
                break;
            case R.id.text_4:
                if (mOnCommitClickListener != null) {
                    mOnCommitClickListener.onCommitClick("大小尺寸与商品描述不符");
                    dismiss();
                }
                break;
            case R.id.text_5:
                if (mOnCommitClickListener != null) {
                    mOnCommitClickListener.onCommitClick("卖家发错货");
                    dismiss();
                }
                break;
            case R.id.cancel_tv:
                dismiss();
                break;
        }
    }


/*
      if (mOnCommitClickListener != null) {
        mOnCommitClickListener.onCommitClick(mType);
        dismiss();
    }
*/


    public interface OnCommitClickListener {

        void onCommitClick(String sex);

    }

    private OnCommitClickListener mOnCommitClickListener;


}