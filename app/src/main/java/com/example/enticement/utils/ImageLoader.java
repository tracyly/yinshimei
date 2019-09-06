package com.example.enticement.utils;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.cuci.enticement.R;
import com.example.enticement.BasicApp;


public class ImageLoader {

    private ImageLoader() {

    }

    public static void loadPlaceholder(String url, ImageView imageView) {

        RequestOptions options = new RequestOptions()
                .placeholder(R.mipmap.ic_launcher);

        Glide.with(BasicApp.getContext())
                .load(url)
                .apply(options)
                .into(imageView);
    }

    public static void loadPlaceholder(int resId, ImageView imageView) {

        RequestOptions options = new RequestOptions()
                .placeholder(R.mipmap.ic_launcher);

        Glide.with(BasicApp.getContext())
                .load(resId)
                .apply(options)
                .into(imageView);
    }

    public static void loadNoPlaceholder(String url, ImageView imageView) {
        Glide.with(BasicApp.getContext())
                .load(url)
                .into(imageView);
    }

    public static void loadNoPlaceholder(int resId, ImageView imageView) {
        Glide.with(BasicApp.getContext())
                .load(resId)
                .into(imageView);
    }

    public static void loadPlaceholderRound(String url, ImageView imageView, int round) {

        RequestOptions options = new RequestOptions()
                .transform(new RoundedCorners(round))
                .placeholder(R.mipmap.ic_launcher);

        Glide.with(BasicApp.getContext())
                .load(url)
                .apply(options)
                .into(imageView);
    }

}
