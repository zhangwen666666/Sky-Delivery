package com.sky.service;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);


    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 根据id查询套餐以及其关联的菜品
     * @param id
     * @return
     */
    SetmealVO getSetmealByIdWithDishes(Long id);


    /**
     * 修改套餐以及其所对应的菜品关联关系
     * @param setmealDTO
     */
    void modifySetmealWithDishes(SetmealDTO setmealDTO);

    /**
     * 套餐起售停售
     * @param id
     * @param status
     */
    void modifyStatus(Long id, Integer status);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);
}
