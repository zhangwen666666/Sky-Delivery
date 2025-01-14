package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@Api(tags = "C端-菜品浏览接口")
@RequestMapping("/user/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 根据分类id查询菜品(查询起售中的菜品)
     * @param categoryId 分类id（必须）
     * @return
     */
    @ApiOperation("根据分类id查询菜品")
    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId){
        List<DishVO> list = dishService.getAllByCategoryIdWithFlavorsOnSale(categoryId);
        return Result.success(list);
    }
}
