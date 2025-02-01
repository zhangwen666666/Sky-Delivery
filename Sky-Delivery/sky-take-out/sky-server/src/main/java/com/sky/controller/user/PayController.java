package com.sky.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.sky.dto.PaymentDTO;
import com.sky.entity.PrepayOrder;
import com.sky.entity.TransactionRecord;
import com.sky.result.PayResult;
import com.sky.result.Result;
import com.sky.service.PrepayOrderService;
import com.sky.service.TransactionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/pay/transactions")
@Slf4j
public class PayController {

    @Autowired
    private PrepayOrderService prepayOrderService;
    @Autowired
    private TransactionRecordService transactionRecordService;


    /**
     * 模拟微信支付后端返回预支付交易标识
     * 商家后端发送post请求，请求体中携带的是封装的JSONObject对象，
     * 包含金额、商家号、订单号、appid、订单描述、下单微信用户的openid、通知地址
     * 这里的方法模拟的是微信的JSAPI下单功能，生成预支付交易会话表示
     * @return PayResult(prepay_id, )
     */
    @PostMapping("/jsapi")
    public PayResult jsapi(@RequestBody JSONObject body){
        log.info("模拟微信预支付交易{}",body);//{"amount":{"total":1,"currency":"CNY"},"mchid":"123456","out_trade_no":"1733894836304","appid":"wx*********","description":"外卖订单","notify_url":"http://localhost:8080/notify/paySuccess","payer":{"openid":"*******"}}
        //将该预支付单加入数据库
        PrepayOrder prepayOrder = new PrepayOrder();
        prepayOrder.setAppid(body.getString("appid"));
        prepayOrder.setMchid(body.getString("mchid"));
        prepayOrder.setDescription(body.getString("description"));
        prepayOrder.setNotifyUrl(body.getString("notify_url"));
        prepayOrder.setOutTradeNo(body.getString("out_trade_no"));
        prepayOrder.setOpenid(body.getJSONObject("payer").getString("openid"));
        prepayOrder.setTotal(body.getJSONObject("amount").getBigDecimal("total"));
        prepayOrder.setCurrency(body.getJSONObject("amount").getString("currency"));
        prepayOrder.setCreateTime(LocalDateTime.now());
        prepayOrderService.save(prepayOrder);
        return new PayResult(prepayOrder.getId(),"SUCCESS");
    }


    /**
     * 模拟微信后端JSAPI调起支付功能
     * 在生成预支付交易单之后，小程序端会发送请求到该url上
     * 参数：
     *  * appid 公众号id
     *  * timeStamp 时间戳
     *  * nonceStr 随机字符串
     *  * package 订单详情扩展字符串，即JSAPI下单接口返回的prepay_id
     *  * signType 签名类型
     *  * paySign 签名
     * @param paymentDTO
     * @return
     */
    @Transactional
    @PostMapping("/payment")
    public Result payment(@RequestBody PaymentDTO paymentDTO){
        log.info("模拟微信正式支付交易{}",paymentDTO);
        //根据预支付订单id获取商户系统订单号
        PrepayOrder prepayOrder = prepayOrderService.getById(paymentDTO.getPrepayId());
        //如果该预支付订单查不到，则返回错误信息
        if(prepayOrder == null){
            return Result.error("支付失败");
        } else {
            //获取客户端向商户后端发送通知
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String outTradeNo = prepayOrder.getOutTradeNo();
            //生成支付记录
            TransactionRecord transactionRecord = new TransactionRecord();
            BeanUtils.copyProperties(prepayOrder, transactionRecord);
            transactionRecordService.save(transactionRecord);
            //删除预支付订单
            prepayOrderService.removeById(prepayOrder);
            //支付成功，向商家后端推送结果
            HttpPost httpPost = new HttpPost(prepayOrder.getNotifyUrl());
            httpPost.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
            httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("out_trade_no", outTradeNo);
            jsonObject.put("transaction_id", transactionRecord.getId());
            httpPost.setEntity(new StringEntity(jsonObject.toJSONString(), "UTF-8"));
            try {
                httpClient.execute(httpPost);
            } catch (IOException e) {
                e.printStackTrace();
                return Result.error("交易失败");
            }
            //给小程序返回结果
            return Result.success("支付成功");
        }
    }
}
