package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小程序微信支付传递的数据模型（模拟微信支付后台数据模型）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private String timeStamp;  //时间戳，从 1970 年 1 月 1 日 00:00:00 至今的秒数，即当前的时间
    private String nonceStr;   //随机字符串，长度为32个字符以下
    private Long prepayId;    //统一下单接口返回的 prepay_id 参数值
    private String signType;  //MD5签名算法，应与后台下单时的值一致
    private String paySign;   //签名
}
