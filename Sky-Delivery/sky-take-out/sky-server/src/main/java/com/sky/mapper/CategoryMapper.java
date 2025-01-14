package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * 分类分页查询
     * @param categoryPageQueryDTO 封装了查询的条件
     * @return
     */
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);


    /**
     * 修改分类
     * @param category
     */
    @AutoFill(OperationType.UPDATE)
    void update(Category category);

    /**
     * 新增分类
     * @param category
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into category(name,type,sort,status,create_time,update_time,create_user,update_user)" +
            "values(#{name},#{type},#{sort},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Category category);

    /**
     * 根据id删除分类
     * @param id
     */
    @Delete("delete from category where id = #{id}")
    void deleteById(Long id);

    /**
     * 根据分类类型查询分类，如果type==null，则返回所有的分类（不包括停售状态的）
     * @param type
     * @return
     */
    List<Category> list(Integer type);
}
