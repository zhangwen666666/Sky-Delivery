package com.sky.mapper;

import com.sky.entity.SetmealDish;
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
}
