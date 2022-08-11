package com.itheima.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增菜品套餐，同时需要保存套餐和菜品的关联
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);
        Long id = setmealDto.getId();//setmealDto就是setmeal的ID

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();//1.遍历出一个个菜品
        setmealDishes=setmealDishes.stream().map((item)->{//2.用迭代·方法为每一个菜品附上套餐的id与套餐表setmeal关联起来
            item.setSetmealId(id);
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }



    @Override
    public void removeWithDish(List<Long> ids) {

//        先进行套餐表的查询
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper();
        lqw.in(Setmeal::getId,ids);
        lqw.eq(Setmeal::getStatus,1);
       int count=this.count(lqw);
       if (count>0){
           throw new CustomException("套餐正在售卖，删除失败");
       }
       this.removeByIds(ids);
//        在进行关系表里面套餐绑定的菜品删除
        LambdaQueryWrapper<SetmealDish> lqw2 = new LambdaQueryWrapper();
        lqw2.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lqw2);



    }
}
