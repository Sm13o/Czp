package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    /**
     * 新增分类成功
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        if (category.toString()!=null){
        categoryService.save(category);
        }
    return R.success("添加成功");
    }

    /**
     * 分类管理分页
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        Page<Category> page1 = new Page(page,pageSize);

        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();

//       排序条件 orderByDesc
        lqw.orderByAsc(Category::getSort);
//        执行查询   注意这个方法的形参
        categoryService.page(page1,lqw);

        return R.success(page1);
    }

    /**
     * 根据id删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){//注意浏览器发出的请求是/ids?

//        categoryService.removeById(id);
        //定义的删除功能
        categoryService.remove(ids);
        return R.success("删除成功");

    }

    /**
     * 修改菜品套餐功能
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    /**
     * 根据条件查询分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
//        条件构造器
       LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
//       添加条件
        lqw.eq((category.getType())!=null,Category::getType,category.getType());
//       添加排序条件
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
//        服务层xxService.list 返回一个集合
        List<Category> list = categoryService.list(lqw);

        return R.success(list);
    }

}
