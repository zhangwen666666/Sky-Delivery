package com.sky.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模拟微信支付后端返回的预支付交易标识
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayResult {
    private Long prepay_id; // 预支付标识
    private String code;
}
