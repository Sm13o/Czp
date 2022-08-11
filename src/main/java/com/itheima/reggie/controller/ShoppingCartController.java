package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
//        指定添加到指定用户的购物车
        Long byID = BaseContext.getByID();
        shoppingCart.setUserId(byID);

        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,byID);

        if(dishId != null){
            //添加到购物车的是菜品
            lqw.eq(ShoppingCart::getDishId,dishId);

        }else{
            //添加到购物车的是套餐
            lqw.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart cartServiceOne = shoppingCartService.getOne(lqw);

//        查询菜品或者套餐是否已经在购物车中
        //已经添加进购物车，多份的时候原来基础上数量加一
        if (cartServiceOne!=null){
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number+1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
        //还没添加进购物车，添加进购物车数量默认是一
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne=shoppingCart;
        }

        return  R.success(cartServiceOne);
    }

    /**
     * 浏览购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        Long byID = BaseContext.getByID();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,byID);
        lqw.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(lqw);

        return R.success(list);
    }
    /**
     * 清空购物车
      * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        Long byID = BaseContext.getByID();

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,byID);
        shoppingCartService.remove(lqw);

        return R.success("清空成功");
    }

    /**
     * 删除购物车菜品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        Long byID = BaseContext.getByID();
        Long setmealId = shoppingCart.getSetmealId();
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> lqw= new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,byID);
        lqw.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, dishId);
        lqw.eq(shoppingCart.getSetmealId()!= null, ShoppingCart::getSetmealId, setmealId);
        ShoppingCart cartServiceOne = shoppingCartService.getOne(lqw);
         log.info("***************{}",cartServiceOne);
        Integer number = cartServiceOne.getNumber();
        cartServiceOne.setNumber(number - 1);
        lqw.orderByDesc(ShoppingCart::getCreateTime);
        shoppingCartService.updateById(cartServiceOne);



        return R.success(cartServiceOne);

    }
}
