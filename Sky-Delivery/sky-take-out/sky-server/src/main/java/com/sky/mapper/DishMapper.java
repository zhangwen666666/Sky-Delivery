package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {
    @Select("select count(*) from dish where category_id = #{categoryId}")
    int countByCategoryId(Long categoryId);

    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 菜品分类查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> selectAllByQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据主键查询菜品
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 根据主键删除菜品
     * @param id
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    /**
     * 根据菜品id集合批量删除菜品
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 修改菜品信息
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据分类id查询所有起售中的菜品
     * @param categoryId
     * @return
     */
    @Select("select * from dish where category_id = #{categoryId} and status = 1")
    List<Dish> selectAllByCategoryIdOnSale(Long categoryId);

    /**
     * 根据动态条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
