package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

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

    /**
     * 根据id查询菜品并查询其关联的口味
     * @param id
     * @return
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 根据id修改菜品基本信息和对应的口味信息
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 根据分类id查询所有的菜品
     * @param categoryId
     * @return
     */
    List<Dish> getAllByCategoryId(Long categoryId);

    /**
     * 修改菜品起售停售
     * @param id
     * @param status
     */
    void modifyStatus(Long id, Integer status);
}
