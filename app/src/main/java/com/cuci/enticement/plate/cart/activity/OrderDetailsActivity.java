package com.cuci.enticement.plate.cart.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.cuci.enticement.BasicApp;
import com.cuci.enticement.Constant;
import com.cuci.enticement.R;
import com.cuci.enticement.base.BaseActivity;
import com.cuci.enticement.bean.AllOrderList;
import com.cuci.enticement.bean.CommitOrder;
import com.cuci.enticement.bean.ItemOrderBottom;
import com.cuci.enticement.bean.ItemOrderTitle;
import com.cuci.enticement.bean.OrderCancel;
import com.cuci.enticement.bean.OrderGoods;
import com.cuci.enticement.bean.OrderPay;
import com.cuci.enticement.bean.Status;
import com.cuci.enticement.bean.UserInfo;
import com.cuci.enticement.bean.WxPayBean;
import com.cuci.enticement.plate.common.eventbus.OrderEvent;
import com.cuci.enticement.plate.mine.adapter.ItemBottomViewBinder;
import com.cuci.enticement.plate.mine.adapter.ItemProdViewBinder;
import com.cuci.enticement.plate.mine.adapter.ItemTitleViewBinder;
import com.cuci.enticement.plate.mine.fragment._MineFragment;
import com.cuci.enticement.plate.mine.vm.OrderViewModel;
import com.cuci.enticement.utils.FToast;
import com.cuci.enticement.utils.PayResult;
import com.cuci.enticement.utils.SharedPrefUtils;
import com.cuci.enticement.utils.ViewUtils;
import com.cuci.enticement.widget.OrderItemDecoration;
import com.google.gson.Gson;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;
import okhttp3.ResponseBody;

import static com.cuci.enticement.plate.cart.fragment._CartFragment.ACTION_REFRESH_DATA;

/**
 * 订单详情页
 */
public class OrderDetailsActivity extends BaseActivity {


    @BindView(R.id.text_zhuangtai)
    TextView textZhuangtai;
    @BindView(R.id.text_name)
    TextView textName;
    @BindView(R.id.text_dizi)
    TextView textDizi;
    @BindView(R.id.tv_order_no)
    TextView tvOrderNo;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_goods_money)
    TextView tvGoodsMoney;
    @BindView(R.id.tv_express)
    TextView tvExpress;
    @BindView(R.id.tv_create_time)
    TextView tvCreateTime;
    @BindView(R.id.tv_total_money)
    TextView tvTotalMoney;
    @BindView(R.id.tv_left)
    TextView tvLeft;
    @BindView(R.id.tv_right)
    TextView tvRight;
    private OrderViewModel mViewModel;
    private UserInfo mUserInfo;

    private int mPayType = 1;

    private AllOrderList.DataBean.ListBeanX mInfo;
    private LinearLayoutManager mLayoutManager;
    private MultiTypeAdapter mAdapter;
    private Items mItems;
    @Override
    public int getLayoutId() {
        return R.layout.order_details;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {

        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        mInfo = intent.getParcelableExtra("intentInfo");
     //   List<OrderGoods> items = mInfo.getList();

        mUserInfo = SharedPrefUtils.get(UserInfo.class);
        if (mUserInfo == null) {
            return;
        }

        initViewStatus();

        mAdapter = new MultiTypeAdapter();
        mItems = new Items();
        mAdapter.setItems(mItems);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter.register(OrderGoods.class, new ItemProdViewBinder());


        OrderItemDecoration mDecoration = new OrderItemDecoration(this, 4);

        mRecyclerView.addItemDecoration(mDecoration);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(mAdapter);
        mItems.clear();
     //   mItems.addAll(items);



        mViewModel = ViewModelProviders.of(this).get(OrderViewModel.class);

    }

    private void initViewStatus() {
        int status = mInfo.getStatus();
        if (status == 0) {
            //已取消          重新购买
            ViewUtils.hideView(tvLeft);
            ViewUtils.hideView(tvRight);
            
        } else if (status == 2) {
            //待付款  取消订单  立即支付
            ViewUtils.showView(tvLeft);
            ViewUtils.showView(tvRight);
            tvLeft.setText("取消订单");
            tvRight.setText("立即支付");
        } else if (status == 3) {
            //待发货  取消订单
            ViewUtils.showView(tvLeft);
            ViewUtils.hideView(tvRight);
            tvLeft.setText("取消订单");
        } else if (status == 4) {
            //待收货  查看物流  确认收货
            ViewUtils.showView(tvLeft);
            ViewUtils.showView(tvRight);
            tvLeft.setText("查看物流");
            tvRight.setText("确认收货");
        } else if (status == 5) {
            //已完成  查看物流
            ViewUtils.showView(tvLeft);
            ViewUtils.hideView(tvRight);
            tvLeft.setText("查看物流");

        }
    }


    @OnClick({R.id.image_back, R.id.tv_left, R.id.tv_right})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.image_back:
                finish();
                break;
            case R.id.tv_left:

                mViewModel.orderCancel(mUserInfo.getToken(), String.valueOf(mUserInfo.getId()), String.valueOf(mInfo.getOrder_no()))
                        .observe(this, mCancelObserver);

                break;
            case R.id.tv_right:
                //提交订单，成功后，去调用获取支付参数接口
                mViewModel.udpateAdress(mUserInfo.getToken(), String.valueOf(mUserInfo.getId()), String.valueOf(mInfo.getOrder_no()),
                        String.valueOf(mInfo.getExpress_address_id()))
                        .observe(OrderDetailsActivity.this, mCommitObserver);

                break;
        }
    }



    /**
     * 获取支付参数接口
     */
    private Observer<Status<ResponseBody>> mPayObserver = status -> {
        switch (status.status) {
            case Status.SUCCESS:

                ResponseBody body = status.content;

                try {
                    String result = body.string();
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

                        if(mPayType==1){
                            StringBuilder sb = new StringBuilder();
                            sb.append("appid").append("=").append(appid).append("&")
                                    .append("prepayid").append("=").append(prepayid).append("&")
                                    .append("sign").append("=").append(sign).append("&")
                                    .append("timestamp").append("=").append(timestamp).append("&")
                                    .append("partnerid").append("=").append(partnerid).append("&")
                                    .append("noncestr").append("=").append(noncestr).append("&")
                                    .append("package").append("=").append(packageX);
                            sendReq2ZFB(sb.toString());
                        }else if(mPayType==2){
                            WxPayBean wxPayBean = new WxPayBean();
                            wxPayBean.setAppId(appid);
                            wxPayBean.setNonceStr(noncestr);
                            wxPayBean.setPaySign(sign);
                            wxPayBean.setTimestamp(timestamp);
                            wxPayBean.setTimeStamp(timestamp);
                            sendReq2WX(wxPayBean);
                        }



                    }else {
                        FToast.warning(orderPay.getInfo());
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



    private Observer<Status<ResponseBody>> mCancelObserver = status -> {
        switch (status.status) {
            case Status.SUCCESS:
                ResponseBody content = status.content;
                try {
                    String result = content.string();
                    OrderCancel orderCancel = new Gson().fromJson(result, OrderCancel.class);
                    if (orderCancel.getCode() == 1) {
                        //取消订单
                        EventBus.getDefault().postSticky(new OrderEvent(OrderEvent.REFRESH_OUTSIDE));


                        //刷新状态
                        Intent intent = new Intent(_MineFragment.ACTION_LOGIN_SUCCEED);

                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


                        FToast.success(orderCancel.getInfo());
                    } else {
                        FToast.error("订单取消失败");
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

                        //todo 发送广播去刷新购物车列表  和  个人中心状态
                        //刷新购物车列表
                        Intent intent1 = new Intent(ACTION_REFRESH_DATA);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);
                        //刷新个人中心状态
                        Intent intent2 = new Intent(_MineFragment.ACTION_LOGIN_SUCCEED);

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
                PayTask payTask = new PayTask(OrderDetailsActivity.this);
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
        //  payRequest.partnerId = wxPayBean.getPartnerid();//这里参数也需要，目前没有就屏蔽了
        //  payRequest.prepayId = wxPayBean.getPrepayid();//这里参数也需要，目前没有就屏蔽了
        payRequest.packageValue = "Sign=WXPay";//固定值
        payRequest.nonceStr = wxPayBean.getNonceStr();
        payRequest.timeStamp = wxPayBean.getTimestamp();
        payRequest.sign = wxPayBean.getPaySign();

        //发起请求，调起微信前去支付
        api.sendReq(payRequest);
    }



    private static final int SDK_PAY_FLAG = 1;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        FToast.success("支付成功");
                        finish();

                    } else {

                        if (TextUtils.equals(resultStatus, "6001")) {
                            FToast.success("支付取消");

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



}
