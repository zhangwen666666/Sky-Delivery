package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 分类分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 修改分类
     *
     * @param categoryDTO
     */
    @Override
    public void modifyCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        // category.setUpdateTime(LocalDateTime.now());
        // category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.update(category);
    }

    /**
     * 启用禁用分类
     * @param id
     * @param status
     */
    @Override
    public void startOrStop(Long id, Integer status) {
        categoryMapper.update(Category.builder().id(id).status(status)
                // .updateTime(LocalDateTime.now())
                // .updateUser(BaseContext.getCurrentId())
                .build());
    }

    /**
     * 新增分类
     * @param categoryDTO
     */
    @Override
    public void save(CategoryDTO categoryDTO) {
        categoryMapper.insert(Category.builder().name(categoryDTO.getName())
                .sort(categoryDTO.getSort())
                .type(categoryDTO.getType())
                .status(StatusConstant.ENABLE)
                // .createTime(LocalDateTime.now())
                // .updateTime(LocalDateTime.now())
                // .createUser(BaseContext.getCurrentId())
                // .updateUser(BaseContext.getCurrentId())
                .build());
    }

    /**
     * 根据id删除分类
     * @param id
     */
    @Override
    public void removeById(Long id) {
        // 如果当前分类下关联有菜品，不能删除
        if(dishMapper.countByCategoryId(id) > 0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        // 如果当前分类下关联有套餐，不能删除
        if(setmealMapper.countByCategoryId(id) > 0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        categoryMapper.deleteById(id);
    }


    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }
}
