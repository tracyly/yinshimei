package com.cuci.enticement.plate.cart.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.alipay.sdk.app.PayTask;
import com.cuci.enticement.BasicApp;
import com.cuci.enticement.Constant;
import com.cuci.enticement.R;
import com.cuci.enticement.base.BaseActivity;
import com.cuci.enticement.bean.AddressBean;
import com.cuci.enticement.bean.AllOrderList;
import com.cuci.enticement.bean.CartDataBean;
import com.cuci.enticement.bean.CartIntentInfo;
import com.cuci.enticement.bean.CommitOrder;
import com.cuci.enticement.bean.ExpressCost;
import com.cuci.enticement.bean.OrderGoods;
import com.cuci.enticement.bean.OrderPay;
import com.cuci.enticement.bean.OrderResult;
import com.cuci.enticement.bean.Status;
import com.cuci.enticement.bean.UserInfo;
import com.cuci.enticement.bean.WxPayBean;
import com.cuci.enticement.bean.ZFBBean;
import com.cuci.enticement.plate.common.eventbus.CartEvent;
import com.cuci.enticement.plate.common.popup.TipsPopup;
import com.cuci.enticement.plate.common.popup.WarningPopup;
import com.cuci.enticement.plate.common.vm.CommonViewModel;
import com.cuci.enticement.plate.mine.activity.RecAddressActivity;
import com.cuci.enticement.plate.mine.adapter.ItemProdViewBinder;
import com.cuci.enticement.plate.mine.adapter.ItemYuProdViewBinder;
import com.cuci.enticement.plate.mine.fragment._MineFragment;
import com.cuci.enticement.plate.mine.vm.OrderViewModel;
import com.cuci.enticement.utils.Arith;
import com.cuci.enticement.utils.FLog;
import com.cuci.enticement.utils.FToast;
import com.cuci.enticement.utils.PayResult;
import com.cuci.enticement.utils.SharedPrefUtils;
import com.cuci.enticement.utils.ViewUtils;
import com.cuci.enticement.widget.OrderItemDecoration;
import com.cuci.enticement.wxapi.WXEntryActivity;
import com.google.gson.Gson;
import com.lxj.xpopup.XPopup;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;
import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;
import okhttp3.ResponseBody;

import static androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance;
import static com.cuci.enticement.plate.cart.fragment._CartFragment.ACTION_REFRESH_DATA;
import static com.cuci.enticement.plate.common.MainActivity.ACTION_GO_TO_HOME;

/**
 *
 */
public class OrderActivity extends BaseActivity {
    private static final int SDK_PAY_FLAG = 1;
    private static final String TAG = OrderActivity.class.getSimpleName();
    @BindView(R.id.text_dizi)
    TextView textDizi;
    @BindView(R.id.text_address)
    TextView tvAddress;

    @BindView(R.id.text_shangpingmoney)
    TextView textShangpingmoney;
    @BindView(R.id.text_yunfeimoney)
    TextView textYunfeimoney;
    @BindView(R.id.ali_iv)
    ImageView aliIv;
    @BindView(R.id.wechat_iv)
    ImageView wechatIv;

    @BindView(R.id.tv_total_money)
    TextView tvTotalMoney;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private OrderViewModel mViewModel;
    private UserInfo mUserInfo;
    private String mAddressId="";
    private int mPayType=2;
    private AllOrderList.DataBean.ListBeanX   mInfo;
    private LinearLayoutManager mLayoutManager;
    private MultiTypeAdapter mAdapter;
    private Items mItems;


    @SuppressLint("HandlerLeak")
    private  Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {

                        //支付成功后，刷新个人中心状态
                        Intent intent= new Intent(_MineFragment.ACTION_REFRESH_STATUS);
                        LocalBroadcastManager.getInstance(OrderActivity.this).sendBroadcast(intent);

                        FToast.success("支付成功");
                        finish();

                    } else {

                        if (TextUtils.equals(resultStatus, "6001")) {
                            FToast.warning("支付取消");
                            finish();
                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            FToast.error("支付失败");
                            finish();
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    public int getLayoutId() {
        return R.layout.activity_sendorder;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        mInfo = (AllOrderList.DataBean.ListBeanX) intent.getSerializableExtra("intentInfo");

        List<OrderGoods> items = mInfo.getList();

        mUserInfo = SharedPrefUtils.get(UserInfo.class);
        if (mUserInfo == null) {
            return;
        }
        mViewModel = ViewModelProviders.of(this).get(OrderViewModel.class);






        // ImageLoader.loadPlaceholder(mOrderBean.get);
        //设置商品总价，运费，订单总价
        textShangpingmoney.setText(mInfo.getPrice_goods());


        String  address = SharedPrefUtils.getDefaultAdress();

        if (!TextUtils.isEmpty(address)) {

            ViewUtils.hideView(textDizi);
            ViewUtils.showView(tvAddress);
            tvAddress.setText(address);
            //默认地址id，不去选中就用这个
            mAddressId = SharedPrefUtils.getDefaultAdressId();

            mViewModel.getExpressCost(mUserInfo.getToken(),String.valueOf(mUserInfo.getId()),String.valueOf(mInfo.getOrder_no()),mAddressId)
                    .observe(OrderActivity.this,mExpressCostObserver);

        }else {
            ViewUtils.showView(textDizi);
            ViewUtils.hideView(tvAddress);
        }


        mAdapter = new MultiTypeAdapter();
        mItems = new Items();
           mItems.addAll(items);
        mAdapter.setItems(mItems);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter.register(OrderGoods.class, new ItemYuProdViewBinder());


        OrderItemDecoration mDecoration = new OrderItemDecoration(this, 6);

        mRecyclerView.addItemDecoration(mDecoration);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(mAdapter);


       /* CommonViewModel commonViewModel = ViewModelProviders.of(this).get(CommonViewModel.class);
        commonViewModel.getAdressList(mUserInfo.getToken(),String.valueOf(mUserInfo.getId()),Status.LOAD_REFRESH)
                .observe(this,mAddressObserver);*/

    }


    @Override
    protected void onResume() {
        super.onResume();



    }
    @OnClick({R.id.text_dizi, R.id.text_address,R.id.tv_commit,R.id.back_iv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.text_dizi:
            case R.id.text_address:
                Intent intent = new Intent(OrderActivity.this, RecAddressActivity.class);
                intent.putExtra("code",100);
                startActivityForResult(intent, 100);
                break;
            case R.id.tv_commit:
                //支付生成订单
                if(TextUtils.isEmpty(tvAddress.getText())){

                    new XPopup.Builder(OrderActivity.this)
                            .dismissOnBackPressed(false)
                            .dismissOnTouchOutside(false)
                            .asCustom(new WarningPopup(OrderActivity.this,
                                    "请选择收货地址","确定")).show();


                            //FToast.warning("请选择收货地址");
                    return;
                }
                //提交订单，成功后，去调用获取支付参数接口
                mViewModel.udpateAdress(mUserInfo.getToken(), String.valueOf(mUserInfo.getId()), String.valueOf(mInfo.getOrder_no()), mAddressId)
                        .observe(OrderActivity.this, mCommitObserver);
                break;
            case R.id.back_iv:
                finish();
                break;
        }
    }




    //暂不使用  可缩起来
    private Observer<Status<AddressBean>> mAddressObserver = new Observer<Status<AddressBean>>() {
        @Override
        public void onChanged(Status<AddressBean> status) {
            switch (status.status) {

                case Status.SUCCESS:
                    dismissLoading();
                    AddressBean data = status.content;
                    List<AddressBean.DataBean.ListBean> list = data.getData().getList();
                    if (list == null||list.size()==0) {
                        ViewUtils.showView(textDizi);
                        ViewUtils.hideView(tvAddress);
                        return;
                    }

                    if (data.getCode() == 1) {
                        boolean hasDefault=false;
                        int num=0;
                        for (int i = 0; i < list.size(); i++) {
                            AddressBean.DataBean.ListBean item = list.get(i);

                            int is_default = item.getIs_default();
                            if(is_default==1){
                                //存在默认就设置
                                hasDefault=true;
                                num=i;
                                break;
                            }
                        }

                        if(hasDefault){

                            AddressBean.DataBean.ListBean item = list.get(num);
                            ViewUtils.hideView(textDizi);
                            ViewUtils.showView(tvAddress);
                            StringBuilder sb = new StringBuilder();
                            sb.append(item.getName()).append(" ")
                                    .append(item.getPhone()).append(" ").append("\n")
                                    .append(item.getProvince()).append(" ")
                                    .append(item.getCity()).append(" ")
                                    .append(item.getArea()).append(" ")
                                    .append(item.getAddress());

                            tvAddress.setText(sb.toString());
                            mViewModel.getExpressCost(mUserInfo.getToken(),String.valueOf(mUserInfo.getId()),String.valueOf(mInfo.getOrder_no()),mAddressId)
                                    .observe(OrderActivity.this,mExpressCostObserver);
                        }




                    } else {
                        ViewUtils.showView(textDizi);
                        ViewUtils.hideView(tvAddress);
                        FToast.error(data.getInfo());
                    }
                    break;
                case Status.ERROR:
                    dismissLoading();
                    FToast.error(status.message);

                    break;
                case Status.LOADING:
                    showLoading();
                    break;
            }
        }
    };










    /**
     * 获取支付参数接口
     */
    private Observer<Status<ResponseBody>> mPayObserver = status -> {
        switch (status.status) {
            case Status.SUCCESS:
                ResponseBody body = status.content;
                try {
                    String result = body.string();

                    if(mPayType==2){
                        ZFBBean orderPay = new Gson().fromJson(result, ZFBBean.class);
                        if(orderPay.getCode()==1){

                                sendReq2ZFB(orderPay.getData());

                        }else {
                            FToast.warning(orderPay.getInfo());
                        }

                    }else if(mPayType==1){
                        OrderPay orderPay = new Gson().fromJson(result, OrderPay.class);
                        if(orderPay.getCode()==1){
                            OrderPay.DataBean data = orderPay.getData();
                            String appid = data.getAppid();
                            String prepayid = data.getPrepayid();
                            String sign = data.getSign();
                            String timestamp = data.getTimestamp();
                            String partnerid = data.getPartnerid();
                            String noncestr = data.getNoncestr();
                            String packageX = data.getPackageX();
                            //weixin

                                WxPayBean wxPayBean = new WxPayBean();
                                wxPayBean.setAppId(appid);
                                wxPayBean.setPrepayId(prepayid);
                                wxPayBean.setPartnerId(partnerid);
                                wxPayBean.setNonceStr(noncestr);
                                wxPayBean.setPaySign(sign);
                                wxPayBean.setTimestamp(timestamp);
                                wxPayBean.setPackageX(packageX);
                                sendReq2WX(wxPayBean);

                    }else {
                            FToast.warning("");
                        }






                    }



                } catch (IOException e) {
                    e.printStackTrace();
                }


                break;
            case Status.LOADING:

                break;
            case Status.ERROR:
                FToast.error(status.message);

                break;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == 101) {
            //返回更新地址
            //todo
            /*String adress = data.getStringExtra("address");
            mAddressId= data.getStringExtra("addressId");*/
            AddressBean.DataBean.ListBean bean = data.getParcelableExtra("addressBean");
            mAddressId=String.valueOf(bean.getId());
            StringBuilder sb = new StringBuilder();
            sb.append(bean.getName()).append(" ")
                    .append(bean.getPhone()).append(" ").append("\n")
                    .append(bean.getProvince()).append(" ")
                    .append(bean.getCity()).append(" ")
                    .append(bean.getArea()).append(" ")
                    .append(bean.getAddress());
            String adress=sb.toString();

            tvAddress.setText(adress);
            mViewModel.getExpressCost(mUserInfo.getToken(),String.valueOf(mUserInfo.getId()),String.valueOf(mInfo.getOrder_no()),mAddressId)
                    .observe(this,mExpressCostObserver);
        }
    }


    /**
     * 获取运费接口
     */
    private Observer<Status<ResponseBody>> mExpressCostObserver = status -> {
        switch (status.status) {
            case Status.SUCCESS:
                dismissLoading();
                ResponseBody body = status.content;
                try {
                    String result = body.string();
                    ExpressCost expressCost = new Gson().fromJson(result, ExpressCost.class);
                    if(expressCost.getCode()==1){
                        double express_price = expressCost.getData().getExpress_price();
                        textYunfeimoney.setText(String.format(Locale.CHINA,"¥%s",express_price));
                        //计算总价
                        double totalMoney= Arith.add(Double.parseDouble(mInfo.getPrice_goods()),express_price);
                        tvTotalMoney.setText(String.format(Locale.CHINA,"¥%s",totalMoney));
                    }else {
                        FToast.warning(expressCost.getInfo());
                    }




                } catch (IOException e) {
                    e.printStackTrace();
                }



                break;
            case Status.LOADING:
                showLoading();
                break;
            case Status.ERROR:
                dismissLoading();
                FToast.error(status.message);

                break;
        }
    };


    /**
     * 提交订单接口   将预订单变成待支付订单  status由1变成2
     */
    private Observer<Status<ResponseBody>> mCommitObserver = status -> {
        switch (status.status) {
            case Status.SUCCESS:
                ResponseBody body = status.content;
                try {
                    String result = body.string();
                    CommitOrder commitOrder = new Gson().fromJson(result, CommitOrder.class);
                    if(commitOrder.getCode()==1){


                        //订单生成成功后，刷新购物车列表
                        EventBus.getDefault().postSticky(new CartEvent(CartEvent.REFRESH_CART_LIST));

                        //订单生成成功后，刷新个人中心状态
                        Intent intent2 = new Intent(_MineFragment.ACTION_REFRESH_STATUS);

                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent2);

                        mViewModel.getOrderPay(mUserInfo.getToken(),String.valueOf(mUserInfo.getId()),
                                String.valueOf(mInfo.getOrder_no()),String.valueOf(mPayType))
                                .observe(this,mPayObserver);
                    }else {
                        FToast.warning("提交订单失败");
                    }






                } catch (IOException e) {
                    e.printStackTrace();
                }



                break;
            case Status.LOADING:

                break;
            case Status.ERROR:
                FToast.error(status.message);

                break;
        }
    };






    @OnClick({R.id.con_fangshi1, R.id.con_fangshi2})
    public void onPayViewClicked(View view) {
        switch (view.getId()) {
            case R.id.con_fangshi1:
                aliIv.setImageResource(R.drawable.xuanzhong);
                wechatIv.setImageResource(R.drawable.noxuanzhong);

                mPayType=2;
                break;
            case R.id.con_fangshi2:
                aliIv.setImageResource(R.drawable.noxuanzhong);
                wechatIv.setImageResource(R.drawable.xuanzhong);

                mPayType=1;

                break;
           /* case R.id.con_fangshi3:
                aliIv.setImageResource(R.drawable.noxuanzhong);
                wechatIv.setImageResource(R.drawable.noxuanzhong);
                unionIv.setImageResource(R.drawable.xuanzhong);
                mPayType=3;
                break;*/
        }
    }

    /**
     * 调支付的方法
     * <p>
     * 注意： app支付请求参数字符串，主要包含商户的订单信息，key=value形式，以&连接。
     *
     * @param oInfo
     */
//支付宝oInfo参数，以后台返回为准
    private void sendReq2ZFB(String oInfo) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //1、生成订单数据
                //2、支付
                PayTask payTask = new PayTask(OrderActivity.this);
				  /*参数1：订单信息
	                参数2：表示在支付钱包显示之前，true会显示一个dialog提示用户表示正在唤起支付宝钱包
				    返回值：
					就是同步返回的支付结果（在实际开发过程中，不应该以此同步结果作为支付成功的依据。以异步结果作为成功支付的依据）
										                 */
                Map<String, String> result = payTask.payV2(oInfo, true);
                Message message = mHandler.obtainMessage();
                message.what = SDK_PAY_FLAG;
                message.obj = result;
                mHandler.sendMessage(message);
            }
        }).start();
    }
    /**
     * 调支付的方法
     * <p>
     * 注意： 每次调用微信支付的时候都会校验 appid 、包名 和 应用签名的。 这三个必须保持一致才能够成功调起微信
     *
     * @param wxPayBean
     */
    //这个WxPayBean以后台返回为准,这里是我手动拿接口文档里生成的
    private  void sendReq2WX(WxPayBean wxPayBean) {

        //这里的appid，替换成自己的即可
        IWXAPI api = WXAPIFactory.createWXAPI(BasicApp.getContext(), Constant.WX_APP_ID);
        api.registerApp(Constant.WX_APP_ID);

        //这里的bean，是服务器返回的json生成的bean
        PayReq payRequest = new PayReq();
        payRequest.appId = wxPayBean.getAppId();
        payRequest.partnerId = wxPayBean.getPartnerId();//这里参数也需要，目前没有就屏蔽了
        payRequest.prepayId = wxPayBean.getPrepayId();//这里参数也需要，目前没有就屏蔽了
        payRequest.packageValue = "Sign=WXPay";//固定值
        payRequest.nonceStr = wxPayBean.getNonceStr();
        payRequest.timeStamp = wxPayBean.getTimestamp();
        payRequest.sign = wxPayBean.getPaySign();

        //发起请求，调起微信前去支付
        api.sendReq(payRequest);
    }




}