package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/admin/workspace")
@Api(tags = "工作台接口")
public class WorkspaceController {
    @Autowired
    private WorkspaceService workspaceService;

    @ApiOperation("查询今日运营数据")
    @GetMapping("/businessData")
    public Result<BusinessDataVO> businessData(){
        // 获得当天的开始时间
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        // 获得当天的结束时间
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);
        return Result.success(workspaceService.businessData(begin, end));
    }


    @ApiOperation("查询订单管理数据")
    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> overviewOrders(){
        return Result.success(workspaceService.overviewOrders());
    }


    @ApiOperation("查询菜品总览")
    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> overviewDishes(){
        return Result.success(workspaceService.overviewDishes());
    }


    @ApiOperation("查询套餐总览")
    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> overviewSetmeals(){
        return Result.success(workspaceService.overviewSetmeals());
    }
}
