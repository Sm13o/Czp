package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    public SetmealDishService setmealDish;

    @Autowired
    public SetmealService setmealService;

    @Autowired
    public CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("封装对象为 {}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("添加成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
//         创建一个page构造器把参数放进去
        Page<Setmeal> page1 = new Page<>(page,pageSize);
        Page<SetmealDto> page2 = new Page<>(page,pageSize);

        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(name!=null,Setmeal::getName,name);
        lqw.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(page1,lqw);


//        页面基本信息拷贝 对象拷贝
        BeanUtils.copyProperties(page1,page2,"records");//records菜品是分类的一个集合

//        把页面菜品信息封装成一个个实体对象的集合
        List<Setmeal> records = page1.getRecords();
        List<SetmealDto> list=records.stream().map((item)->{
            SetmealDto setmealDto = new SetmealDto();

//            除了菜品分类 先拷贝进dishdto对象里
            BeanUtils.copyProperties(item,setmealDto);

//            获取当前菜品对应的CategoryId
            Long categoryId = item.getCategoryId();

//            通过Category表   搜索categoryId对应的分类名称
            Category byId = categoryService.getById(categoryId);
            if (byId!=null){

                String categoryName = byId.getName();

//            set进CategoryName中在页面显示
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

//         最后把页面菜品信息添加进去
        page2.setRecords(list);

        return R.success(page2);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
          log.info("id 为 {}",ids);
        setmealService.removeWithDish(ids);
                return R.success("删除套餐成功");
    }

    /**
     * 手机端套餐分类请求的实现
     * @param setmeal
     * @return
     */

    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        Long categoryId = setmeal.getCategoryId();
        Integer status = setmeal.getStatus();

            LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
            lqw.eq(categoryId!=null,Setmeal::getCategoryId, categoryId);
            lqw.eq(status!=null,Setmeal::getStatus, status);
            lqw.orderByDesc(Setmeal::getUpdateTime);
            List<Setmeal> list = setmealService.list(lqw);

            return R.success(list);

    }
}
