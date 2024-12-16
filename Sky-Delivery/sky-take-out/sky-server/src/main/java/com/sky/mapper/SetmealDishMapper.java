package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品的id查询其关联的套餐的id
     * @param dishId
     * @return
     */
    @Select("select setmeal_id from setmeal_dish where dish_id = #{dishId}")
    List<Long> getSetmealIdsByDishId(Long dishId);

    /**
     * 批量插入菜品和套餐的关联关系的数据
      * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);


    /**
     * 根据套餐id查询菜品和套餐的关联关系的数据
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> selectBySetmealId(Long setmealId);

    /**
     * 根据套餐id删除套餐和菜品的关联关系
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);

    /**
     * 根据套餐id集合批量删除套餐和菜品的关联关系
     * @param setmealIds
     */
    void deleteBySetmealIds(List<Long> setmealIds);
}
