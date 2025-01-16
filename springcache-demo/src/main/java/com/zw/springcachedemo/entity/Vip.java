package com.zw.springcachedemo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Vip用户对象")
public class Vip {
    @Schema(description = "用户姓名", example = "张三",required = true)
    private String name;
    private int age;
}
