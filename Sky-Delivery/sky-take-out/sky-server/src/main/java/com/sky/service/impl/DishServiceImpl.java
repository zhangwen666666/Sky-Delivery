package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品和对应的口味
     * 需要操作菜品表和口味表
     * 为了保证数据的安全，需要开启事务处理
     *
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 向菜品表插入一条数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        // 获取insert语句生成的主键值dishId
        Long dishId = dish.getId();
        // 向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            // 前端提交过来的数据中，口味只有名称和值，
            // 而dishId需要后端在添加菜品之后获取并赋值给口味
            flavors.forEach(flavor -> {
                flavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.selectAllByQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 菜品批量删除
     *
     * @param ids
     */
    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            // 起售中的菜品不能删除
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
            // 被套餐关联的菜品不能删除
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(id);
            if (setmealIds != null && !setmealIds.isEmpty()) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
        }

        // 以下循环删除存在效率问题，可以采用批量删除的方式优化
        /*for (Long id : ids) {
            // 删除菜品数据
            dishMapper.deleteById(id);
            // 将其关联的口味数据删除
            dishFlavorMapper.deleteByDishId(id);
        }*/

        // 根据菜品id批量删除菜品
        dishMapper.deleteByIds(ids);
        // 批量删除关联的口味数据
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id查询菜品并查询其关联的口味
     *
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        // 根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
        // 根据菜品id查询口味数据
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        // 封装成VO对象
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 根据id修改菜品基本信息和对应的口味信息
     * @param dishDTO
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
       // 修改菜品基本信息
       Dish dish = new Dish();
       BeanUtils.copyProperties(dishDTO, dish);
       dishMapper.update(dish);
       // 删除原有的口味信息
       dishFlavorMapper.deleteByDishId(dish.getId());
       // 新增修改后的口味信息
       List<DishFlavor> flavors = dishDTO.getFlavors();
       if (flavors != null && !flavors.isEmpty()) {
           // 前端提交过来的数据中，口味只有名称和值，
           // 而dishId需要后端在添加菜品之后获取并赋值给口味
           flavors.forEach(flavor -> {
               flavor.setDishId(dish.getId());
           });
           dishFlavorMapper.insertBatch(flavors);
       }
    }


    /**
     * 根据分类id查询所有的菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> getAllByCategoryId(Long categoryId) {
        return dishMapper.selectAllByCategoryId(categoryId);
    }


    /**
     * 修改菜品起售停售
     * @param id
     * @param status
     */
    @Override
    public void modifyStatus(Long id, Integer status) {
        dishMapper.update(Dish.builder().id(id).status(status).build());
    }
}
