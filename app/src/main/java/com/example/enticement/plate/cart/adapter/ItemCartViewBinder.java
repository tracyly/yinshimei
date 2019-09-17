package com.example.enticement.plate.cart.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cuci.enticement.R;
import com.example.enticement.bean.BannerDataBean;
import com.example.enticement.bean.CartListBean;
import com.example.enticement.plate.home.adapter.ItemBannerViewBinder;
import com.example.enticement.utils.ImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.multitype.ItemViewBinder;

public class ItemCartViewBinder extends ItemViewBinder<CartListBean.DataBean.ListBean, ItemCartViewBinder.ViewHolder> {

    public interface OnItemClickListener {

        void onAddClick(CartListBean.DataBean.ListBean bean);

        void onMinusClick(CartListBean.DataBean.ListBean bean);
        void onCheckedChange();
    }

    private ItemCartViewBinder.OnItemClickListener mOnItemClickListener;

    public ItemCartViewBinder(ItemCartViewBinder.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.rec_cart, parent, false);
        return new ViewHolder(root);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull CartListBean.DataBean.ListBean item) {


        if (item.isCheck()) {
            holder.mImageCheck.setImageResource(R.drawable.icon_popup_checked);
        } else {
            holder.mImageCheck.setImageResource(R.drawable.icon_popup_normal);
        }

        holder.mImageCheck.setOnClickListener(v -> {
            item.setCheck(!item.isCheck());
            if (item.isCheck()) {
                holder.mImageCheck.setImageResource(R.drawable.icon_popup_checked);
            } else {
                holder.mImageCheck.setImageResource(R.drawable.icon_popup_normal);
            }
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onCheckedChange();
            }
        });


        holder.textBiaoti.setText(item.getGoods_title());
        holder.textNeirong.setText(item.getGoods_spec());
        holder.textJiage.setText(item.getGoods_price_selling());
        ImageLoader.loadPlaceholder(item.getGoods_logo(),holder.imgTuxiang);
        holder.textJia.setOnClickListener(v -> {
            if(mOnItemClickListener!=null){
                mOnItemClickListener.onAddClick(item);
            }
        });

        holder.textJian.setOnClickListener(v -> {
            if(mOnItemClickListener!=null){
                mOnItemClickListener.onMinusClick(item);
            }
        });


    }



    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_tuxiang)
        ImageView imgTuxiang;

        @BindView(R.id.image_check)
        ImageView mImageCheck;
        @BindView(R.id.text_biaoti)
        TextView textBiaoti;
        @BindView(R.id.text_neirong)
        TextView textNeirong;
        @BindView(R.id.text_jiage)
        TextView textJiage;
        @BindView(R.id.text_jian)
        TextView textJian;
        @BindView(R.id.text_jia)
        TextView textJia;


        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);


        }
    }
}