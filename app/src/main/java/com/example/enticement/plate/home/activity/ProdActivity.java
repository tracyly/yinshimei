package com.example.enticement.plate.home.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.cuci.enticement.R;
import com.example.enticement.base.BaseActivity;
import com.example.enticement.bean.BannerDataBean;

import com.example.enticement.bean.HomeDetailsBean;
import com.example.enticement.bean.Status;
import com.example.enticement.plate.common.GlideImageLoader;

import com.example.enticement.plate.home.vm.HomeViewModel;
import com.example.enticement.utils.FToast;
import com.example.enticement.widget.ShareBottom2TopProdPopup;
import com.example.enticement.widget.SmoothScrollview;
import com.lxj.xpopup.XPopup;
import com.tencent.smtt.sdk.WebView;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProdActivity extends BaseActivity {
    private static final String TAG = ProdActivity.class.getSimpleName();
    public static final int PUT_IN_CART=100;
    public static final int QUICK_BUY=101;
    @BindView(R.id.image_back)
    ImageView imageBack;
    @BindView(R.id.image_top)
    TextView imageTop;
    @BindView(R.id.banner)
    Banner banner;
    @BindView(R.id.home_detail_goodsname)
    TextView homeDetailGoodsname;
    @BindView(R.id.img_share)
    ImageView imgShare;
    @BindView(R.id.con_info)
    ConstraintLayout conInfo;
    @BindView(R.id.web_details)
    WebView webDetails;
    @BindView(R.id.scroll_details)
    SmoothScrollview scrollDetails;
    @BindView(R.id.text_cart)
    TextView textCart;
    @BindView(R.id.text_sell)
    TextView textSell;

    private BannerDataBean mData;
    private HomeDetailsBean.DataBean mProData;
    private HomeViewModel mHomeViewModel;

    @Override
    public int getLayoutId() {
        return R.layout.activity_prod;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent == null) {
            FToast.error("数据错误");
            return;
        }

        mData = intent.getParcelableExtra("bannerData");
         String url = mData.getUrl();

        if (mData == null) {
            FToast.error("数据错误");
            return;
        }
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        banner.setIndicatorGravity(BannerConfig.CENTER);
        banner.setImageLoader(new GlideImageLoader());

        mHomeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        mHomeViewModel.getHomeDetails(mData.getUrl()).observe(this, mObserver);


    }


    private String getHtmlData(String bodyHTML) {
        String head = "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width: 100%; width:auto; height:auto!important;}</style>" +
                "</head>";
        return "<html>" + head + "<body>" + bodyHTML + "</body></html>";
    }


    private Observer<Status<HomeDetailsBean>> mObserver = new Observer<Status<HomeDetailsBean>>() {

        @Override
        public void onChanged(Status<HomeDetailsBean> baseListStatus) {
            switch (baseListStatus.status) {
                case Status.LOADING:
                    break;
                case Status.SUCCESS:
                    HomeDetailsBean content = baseListStatus.content;
                    if (content == null) {

                        return;
                    }
                    if (content.getCode() == 1) {
                        mProData=content.getData();
                        final List<String> images = content.getData().getImage();
                        banner.setImages(images);
                        banner.start();
                        String htmlContent = content.getData().getContent();
                        webDetails.loadDataWithBaseURL(null,
                                getHtmlData(htmlContent), "text/html", "utf-8", null);

                    } else {
                        FToast.error(content.getInfo());
                    }
                    break;
                case Status.ERROR:

                    FToast.error(baseListStatus.message);
                    break;
            }

        }
    };




    @OnClick({R.id.image_back, R.id.text_cart, R.id.text_sell, R.id.iv_to_top})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.image_back:
                finish();
                break;

            case R.id.text_cart:
                new XPopup.Builder(ProdActivity.this)
                        .dismissOnTouchOutside(true)
                        .dismissOnBackPressed(true)
                        .asCustom(new ShareBottom2TopProdPopup(ProdActivity.this, mProData,PUT_IN_CART))
                        .show();
                break;
            case R.id.text_sell:
                new XPopup.Builder(ProdActivity.this)
                        .dismissOnTouchOutside(true)
                        .dismissOnBackPressed(true)
                        .asCustom(new ShareBottom2TopProdPopup(ProdActivity.this, mProData,QUICK_BUY))
                        .show();
                break;
            case R.id.iv_to_top:
                scrollDetails.smoothScrollTo(0,0);
                break;
        }
    }
}