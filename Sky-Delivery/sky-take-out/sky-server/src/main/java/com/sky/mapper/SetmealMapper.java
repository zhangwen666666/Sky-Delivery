package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {
    /**
     * 查询当前分类下的套餐数量
     * @param categoryId 当前分类的id
     * @return
     */
    @Select("select count(*) from setmeal where category_id = #{categoryId}")
    int countByCategoryId(Long categoryId);

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */

    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 新增套餐
     * @param setmeal
     */
    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);


    /**
     * 根据套餐id查询套餐
     * @param id
     * @return
     */
    @Select("select * from setmeal where id = #{id} ")
    Setmeal selectById(Long id);


    /**
     * 根据id修改套餐基本信息
     * @param setmeal
     */
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);


    void deleteBatch(List<Long> ids);

    /**
     * 条件查询
     * @param setmeal 查询条件封装在该对象中
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询包含的菜品数据
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemBySetmealId(Long id);

    /**
     * 根据动态条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
