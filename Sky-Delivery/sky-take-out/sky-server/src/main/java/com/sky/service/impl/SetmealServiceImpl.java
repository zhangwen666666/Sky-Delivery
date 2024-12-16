package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        // 新增套餐基本信息，操作setmeal表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        // 新增套餐菜品关系，操作setmeal_dish表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            // 集合中每一个元素都是一个套餐包含的菜品
            // 但是前端在传过来时并没有套餐id，即属性setmealId未赋值
            // 需要后端插入套餐之后赋值到该属性中
            setmealDish.setSetmealId(setmeal.getId());
        }
        // 批量将套餐和菜品关系插入到数据库中
        setmealDishMapper.insertBatch(setmealDishes);
    }


    /**
     * 根据id查询套餐以及其关联的菜品
     * @param id
     * @return
     */
    @Override
    public SetmealVO getSetmealByIdWithDishes(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        List<SetmealDish> setmealDishes = setmealDishMapper.selectBySetmealId(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 修改套餐以及其所对应的菜品关联关系
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void modifySetmealWithDishes(SetmealDTO setmealDTO) {
        // 修改套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        // 删除套餐关联的旧的菜品信息
        setmealDishMapper.deleteBySetmealId(setmeal.getId());
        // 批量插入新的套餐和菜品的关联关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmeal.getId());
        }
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 套餐起售停售
     * @param id
     * @param status
     */
    @Override
    public void modifyStatus(Long id, Integer status) {
        setmealMapper.update(Setmeal.builder().id(id).status(status).build());
    }
}
