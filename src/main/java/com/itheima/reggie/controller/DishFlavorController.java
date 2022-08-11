package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishFlavorController {


    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 添加菜品方法 因为返回数据涉及到两个表，所以重新在服务层编写新的saveWithFlavor保存方法
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){

        dishService.saveWithFlavor(dishDto);
        return R.success("添加成功");
    }

    /**
     * 菜品管理页面得分类查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
//         创建一个page构造器把参数放进去
        Page<Dish> page1 = new Page<>(page,pageSize);
        Page<DishDto> page2 = new Page<>(page,pageSize);

        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(name!=null,Dish::getName,name);
        lqw.orderByDesc(Dish::getUpdateTime);
        dishService.page(page1,lqw);

//        页面基本信息拷贝 对象拷贝
        BeanUtils.copyProperties(page1,page2,"records");

//        把页面菜品信息封装成一个个实体对象的集合
        List<Dish> records = page1.getRecords();
        List<DishDto> list=records.stream().map((item)->{
            DishDto dishDto = new DishDto();

//            除了菜品分类 先拷贝进dishdto对象里
            BeanUtils.copyProperties(item,dishDto);

//            获取当前菜品对应的CategoryId
            Long categoryId = item.getCategoryId();

//            通过Category表搜索categoryId对应的分类名称
            Category category = categoryService.getById(categoryId);
            if (category!=null){

            String categoryName = category.getName();

//            set进CategoryName中在页面显示
            dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

//         最后把页面菜品信息添加进去
        page2.setRecords(list);

        return R.success(page2);
    }

    /**
     * 在服务层中编写通过id查询对应信息和菜品口味
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishdto = dishService.getByIdWithFlavor(id);
        return  R.success(dishdto);
    }

    /**
     * 修改指定菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){

        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功");
    }


    @GetMapping("/list")
    public R<List<DishDto>> list(DishDto dishDto){
        Long id = dishDto.getCategoryId();

        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
//        查询
        lqw.eq(id!=null,Dish::getCategoryId,id);
//        菜品状态
        lqw.eq(Dish::getStatus,1);
//        排序条件
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dish = dishService.list(lqw);
        List<DishDto> dishDtoList = dish.stream().map((item)->{
            DishDto dishDto2 = new DishDto();

//            除了菜品分类 先拷贝进dishdto对象里
            BeanUtils.copyProperties(item,dishDto2);

//            获取当前菜品对应的CategoryId
            Long categoryId = item.getCategoryId();

//            通过Category表搜索categoryId对应的分类名称
            Category category = categoryService.getById(categoryId);
            if (category!=null){

                String categoryName = category.getName();

//            set进CategoryName中在页面显示
                dishDto2.setCategoryName(categoryName);
            }

//            追加手机端的口味选项
            Long id1 = item.getId();
            LambdaQueryWrapper<DishFlavor> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(DishFlavor::getDishId, id1);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lqw2);
            dishDto2.setFlavors(dishFlavorList);
            return dishDto2;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }
}
