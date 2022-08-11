package com.itheima.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private  DishService dishService;

    /**
     * 新增菜品方法
     * @param dishDto
     */
    @Override
    //5.最后要加上一个事务控制
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
//        1.this.save()将基本信息保存到Dish中
        this.save(dishDto);//dishService.save(dishDto)

//       2.菜品id
        Long dishId = dishDto.getId();

//        3.菜品的口味      dish_flavor表里的 dish_id意思是对应你下单哪个菜的id在数据库这一个字段附上给他
        List<DishFlavor> flavors = dishDto.getFlavors();//这里为什么是集合呢？因为可能不止一种备注 辣度 忌口 温度等都要给他set上dishId
        flavors=flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

//        4.保存菜品的口味数据到菜品口味表中dish_flavor  saveBatch批量保存
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 在修改指定商品时通过id查询对应信息和菜品口味 然后回显在页面
     * @param id
     * @return
     */

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = dishService.getById(id);
        DishDto dishDto=new DishDto();
//       从dish表来查询基本信息  用拷贝解决
        BeanUtils.copyProperties(dish,dishDto);

//       查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> list = dishFlavorService.list(lqw);
        dishDto.setFlavors(list);

        return dishDto;
    }

    /**
     *修改指定菜品方法
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
//      先把基本数据存进
        this.updateById(dishDto);

//        清除之前设置的口味
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(lqw);

//        设置成封装成dishdto对象请求过来的口味
        List<DishFlavor> flavors = dishDto.getFlavors();//这里为什么是集合呢？因为可能不止一种备注 辣度 忌口 温度等都要给他set上dishId
        flavors=flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

//        4.保存菜品的口味数据到菜品口味表中dish_flavor  saveBatch批量保存
        dishFlavorService.saveBatch(flavors);
    }


}
