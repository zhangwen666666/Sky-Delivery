package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userSetmealController")
@Api(tags = "C端-套餐浏览接口")
@RequestMapping("/user/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @ApiOperation("根据分类id查询套餐")
    @GetMapping("/list")
    @Cacheable(cacheNames = "setmealCache", key = "#categoryId")
    public Result<List<Setmeal>> list(Long categoryId){
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        // 查询起售中的套餐
        setmeal.setStatus(StatusConstant.ENABLE);
        List<Setmeal> list = setmealService.list(setmeal);
        return Result.success(list);
    }

    @ApiOperation("根据套餐id查询套餐包含的菜品")
    @GetMapping("/dish/{id}")
    public Result<List<DishItemVO>> dishList(@PathVariable Long id){
        List<DishItemVO> list = setmealService.getDishItemBySetmealId(id);
        return Result.success(list);
    }
}
