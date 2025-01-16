package com.zw.springcachedemo.controller;

import com.zw.springcachedemo.entity.Vip;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vip")
@Tag(name = "Vip相关接口")
public class VipController {
    @GetMapping("/{age}")
    @Operation(summary = "这是简短描述", description = "这是详细描述信息")
    @Parameters({
            @Parameter(name = "age", description = "这是用户的年龄", example = "18", required = true)
    })
    public Vip getVip(@PathVariable int age){
        return new Vip("张三", age);
    }

    @PostMapping
    public String saveVip(@RequestBody Vip vip){
        return "保存成功：" + vip.toString();
    }

    @PutMapping
    public String update(@RequestBody Vip vip){
        return "更新成功：" + vip;
    }

    @DeleteMapping("/{age}")
    public String del(@PathVariable int age){
        return "删除成功, age = " + age;
    }
}
