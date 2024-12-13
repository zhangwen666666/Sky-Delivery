package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品批量删除
     *   业务规则
     *   * 1. 可以一次删除一个菜品，也可以批量删除菜品
     *   * 2. 起售中的菜品不能删除
     *   * 3. 被套餐关联的菜品不能删除
     *   * 4. 删除菜品后，关联的口味数据也需要删除掉
     *
     * @param ids
     */
    void batchDelete(List<Long> ids);

}
