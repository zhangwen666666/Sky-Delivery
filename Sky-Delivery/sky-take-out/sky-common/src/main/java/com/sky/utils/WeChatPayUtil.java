package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.properties.WeChatProperties;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * 微信支付工具类
 */
@Component
public class WeChatPayUtil {

    //微信支付下单接口地址
    // public static final String JSAPI = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
    // 模拟的微信支付下单接口地址
    public static final String JSAPI = "http://111.119.211.126:9101/pay/transactions/jsapi";

    //申请退款接口地址
    // public static final String REFUNDS = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";
    // 模拟申请退款的接口
    public static final String REFUNDS = "http://localhost:8081/refund/domestic/refunds";
    

    @Autowired
    private WeChatProperties weChatProperties;

    /**
     * 获取调用微信接口的客户端工具对象
     * 创建和配置一个 CloseableHttpClient 实例，该实例可以与微信支付API进行安全通信
     * 加载商户的私钥和微信支付平台证书，并使用这些凭证来构建一个能够自动处理签名和验签的 HTTP 客户端
     * 我们只是模拟实现微信支付相关功能，不需要真正进行认证，因此我们不使用以下方法来获取一个Client
     * @return
     */
    /*private CloseableHttpClient getClient() {
        PrivateKey merchantPrivateKey = null;
        try {
            //merchantPrivateKey商户API私钥，如何加载商户API私钥请看常见问题
            merchantPrivateKey = PemUtil.loadPrivateKey(new FileInputStream(new File(weChatProperties.getPrivateKeyFilePath())));
            //加载平台证书文件
            X509Certificate x509Certificate = PemUtil.loadCertificate(new FileInputStream(new File(weChatProperties.getWeChatPayCertFilePath())));
            //wechatPayCertificates微信支付平台证书列表。你也可以使用后面章节提到的“定时更新平台证书功能”，而不需要关心平台证书的来龙去脉
            List<X509Certificate> wechatPayCertificates = Arrays.asList(x509Certificate);

            WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                    .withMerchant(weChatProperties.getMchid(), weChatProperties.getMchSerialNo(), merchantPrivateKey)
                    .withWechatPay(wechatPayCertificates);

            // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
            CloseableHttpClient httpClient = builder.build();
            return httpClient;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }*/

    /**
     * 发送post方式请求
     *
     * @param url
     * @param body
     * @return
     */
    private String post(String url, String body) throws Exception {
        // 以下这行代码获取的HttpClient是需要安全认证的
        // CloseableHttpClient httpClient = getClient();

        // 获取默认的httpClient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(url);

        // 调用JSAPI下单接口和JSAPI调起支付接口时需要设置的请求头参数
        httpPost.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        httpPost.addHeader("Wechatpay-Serial", weChatProperties.getMchSerialNo()); //商户API证书的证书序列号
        httpPost.setEntity(new StringEntity(body, "UTF-8"));

        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            return bodyAsString;
        } finally {
            httpClient.close();
            response.close();
        }
    }

    /**
     * 发送get方式请求
     *
     * @param url
     * @return
     */
    private String get(String url) throws Exception {
        // 需要认证的httpClient对象
        // CloseableHttpClient httpClient = getClient();
        // 不需要认证的httpClient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        httpGet.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        httpGet.addHeader("Wechatpay-Serial", weChatProperties.getMchSerialNo());

        CloseableHttpResponse response = httpClient.execute(httpGet);
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            return bodyAsString;
        } finally {
            httpClient.close();
            response.close();
        }
    }

    /**
     * jsapi下单 （生成用于调起支付的预支付交易会话标识(prepay_id)。）
     *
     * @param orderNum    商户订单号
     * @param total       总金额
     * @param description 商品描述
     * @param openid      微信用户的openid
     * @return
     */
    private String jsapi(String orderNum, BigDecimal total, String description, String openid) throws Exception {
        // 封装jsapi下单时需要的请求体参数
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appid", weChatProperties.getAppid()); // 商户公众号唯一标识
        jsonObject.put("mchid", weChatProperties.getMchid());// 商户的唯一标识
        jsonObject.put("description", description); // 商品描述信息
        jsonObject.put("out_trade_no", orderNum); // 商户订单号
        jsonObject.put("notify_url", weChatProperties.getNotifyUrl()); // 商户回调地址

        JSONObject amount = new JSONObject(); // 订单金额，包括金额total和单位currency
        amount.put("total", total.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue());
        amount.put("currency", "CNY");

        jsonObject.put("amount", amount);

        JSONObject payer = new JSONObject(); //支付者信息，包含用户标识openid
        payer.put("openid", openid);

        jsonObject.put("payer", payer);

        String body = jsonObject.toJSONString();
        return post(JSAPI, body);
    }

    /**
     * 小程序支付（真正进行支付）
     *
     * @param orderNum    商户订单号
     * @param total       金额，单位 元
     * @param description 商品描述
     * @param openid      微信用户的openid
     * @return
     */
    public JSONObject pay(String orderNum, BigDecimal total, String description, String openid) throws Exception {
        //统一下单，生成预支付交易单
        String bodyAsString = jsapi(orderNum, total, description, openid);
        //解析返回结果
        JSONObject jsonObject = JSON.parseObject(bodyAsString);
        System.out.println(jsonObject);

        String prepayId = jsonObject.getString("prepay_id");

        // 我们是模拟支付，因此不需要进行二次签名认证
        /*if (prepayId != null) {
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000); //时间戳
            String nonceStr = RandomStringUtils.randomNumeric(32);// 随机字符串
            ArrayList<Object> list = new ArrayList<>();
            list.add(weChatProperties.getAppid()); // appid
            list.add(timeStamp); // 时间戳
            list.add(nonceStr); //随机字符串
            list.add("prepay_id=" + prepayId); //预支付id
            //二次签名，调起支付需要重新签名 以下代码是为了生成签名
            StringBuilder stringBuilder = new StringBuilder();
            for (Object o : list) {
                stringBuilder.append(o).append("\n");
            }
            String signMessage = stringBuilder.toString();
            byte[] message = signMessage.getBytes();

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(PemUtil.loadPrivateKey(new FileInputStream(new File(weChatProperties.getPrivateKeyFilePath()))));
            signature.update(message);
            String packageSign = Base64.getEncoder().encodeToString(signature.sign()); // 签名

            //构造数据给微信小程序，用于调起微信支付
            JSONObject jo = new JSONObject();
            jo.put("timeStamp", timeStamp); // 时间戳
            jo.put("nonceStr", nonceStr); // 随机串
            jo.put("package", "prepay_id=" + prepayId); // 预支付id
            jo.put("signType", "RSA");  // 签名方式
            jo.put("paySign", packageSign); // 签名

            return jo;
        }*/

        // 如果成功生成预支付交易单，经过签名验证封装后返回给前端（签名省略）
        if (prepayId != null) {
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            String nonceStr = RandomStringUtils.randomNumeric(32);
            // 二次签名，调起支付需要重新签名(签名省略)
            // 构造数据给微信小程序，用于调起微信支付
            // 商户通过JSAPI下单接口进行下单支付，真正的JSAPI调起支付接口需要以下参数
            JSONObject jo = new JSONObject();
            jo.put("timeStamp", timeStamp); // 时间戳
            jo.put("nonceStr", nonceStr); // 随机串
            jo.put("package", prepayId); // 预支付id
            jo.put("signType", "RSA"); // 签名方式
            jo.put("paySign", "123456789"); // 签名

            return jo;
        }

        return jsonObject;
    }

    /**
     * 申请退款
     *
     * @param outTradeNo    商户订单号
     * @param outRefundNo   商户退款单号
     * @param refund        退款金额
     * @param total         原订单金额
     * @return
     */
    public String refund(String outTradeNo, String outRefundNo, BigDecimal refund, BigDecimal total) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no", outTradeNo);
        jsonObject.put("out_refund_no", outRefundNo);

        JSONObject amount = new JSONObject();
        amount.put("refund", refund.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue());
        amount.put("total", total.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue());
        amount.put("currency", "CNY");

        jsonObject.put("amount", amount);
        jsonObject.put("notify_url", weChatProperties.getRefundNotifyUrl());

        String body = jsonObject.toJSONString();

        //调用申请退款接口
        return post(REFUNDS, body);
    }
}
