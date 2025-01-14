package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/user/category")
@Api(tags = "C端-分类接口")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据分类类型查询分类
     * @param type 分类类型：1 菜品  2 套餐
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("条件查询")
    public Result<List<Category>> list(Integer type){
        List<Category> categoryList = categoryService.list(type);
        return Result.success(categoryList);
    }
}
