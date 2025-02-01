package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrepayOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    //金额
    private BigDecimal total;
    //金额单位
    private String currency;
    //商户系统订单号
    private String outTradeNo;
    //openid
    private String openid;
    //商品描述
    private String description;
    //回调通知地址
    private String notifyUrl;
    //创建时间
    private LocalDateTime createTime;
    private String appid;
    private String mchid;
}
