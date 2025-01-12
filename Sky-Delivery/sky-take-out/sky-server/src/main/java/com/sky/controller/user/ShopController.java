package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@Api(tags = "店铺操作接口")
@Slf4j
@RequestMapping("/user/shop")
public class ShopController {
    public static final String KEY = "SHOP_STATUS";


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 获取店铺营业状态 1表示营业中，0表示打烊中
     * @return
     */
    @ApiOperation("获取店铺营业信息")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        return Result.success(status);
    }
}
