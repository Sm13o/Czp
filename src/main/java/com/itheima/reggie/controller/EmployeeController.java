package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 人员管理
 */
@RestController
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录逻辑处理
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> info(HttpServletRequest request, @RequestBody Employee employee) {


//        1.将页面提交数据中的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

//        2，根据页面提供的用户名username查询数据库
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
//                                            第一个形参为数据库中存放的username 第二个形参为页面提交中的username
        lqw.eq(Employee::getUsername, employee.getUsername());
//        通过username获取出对应的一个employee对象 封装起来
        Employee emp = employeeService.getOne(lqw);

//        3.如果没有查询则返回查询失败结果
        if (emp == null) {
            return R.error("查询失败");
        } else if (!emp.getPassword().equals(password)) {    //4.判断密码
            return R.error("密码错误");
        } else if (emp.getStatus() == 0) {       //5.判断状态
            return R.error("账号已禁用");
        }
//        6.登陆成功,将员工id存入session中并且返回登陆成功结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出逻辑处理
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> out(HttpServletRequest request) {
//        获取session对象 退出存入request作用域中employee对象他的值
         request.getSession().removeAttribute("employee");
          return  R.success("退出成功");
    }

    /**
     * 新增人工功能
     * @param employee
     * @return
     */

    @PostMapping
    public R<String> save(@RequestBody Employee employee){
        log.info("邢增的人员信息为: {}",employee.toString());

//        初始密码需要md5加密
         employee.setPassword(DigestUtils.md5DigestAsHex("20010612".getBytes()));

//        补全用户创建更新时间信息  后面用自动填充 统一管理这些变量，前提是在这些实体类的变量名上面加上 @TableField注解
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        获取当前id
//        Long id = (Long)request.getSession().getAttribute("employee");
//        employee.setCreateUser(id);
//        employee.setUpdateUser(id);

//         用employeeService对象进行对 新employee对象进行保存
         employeeService.save(employee);

        return R.success("保存对象成功");
    }

    /**
     *                 分页的 功能实现
     * @param page
     * @param pageSize
     * @param name
     * @return
     */

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page page1 = new Page(page,pageSize);

        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        //过滤条件
        lqw.like(StringUtils.isNotEmpty(name),Employee::getName,name);
//       排序条件 orderByDesc
        lqw.orderByDesc(Employee::getUpdateTime);
//        执行查询   注意这个方法的形参
        employeeService.page(page1,lqw);

        return R.success(page1);
    }

    /**
     * 通用修改员工方法
     * @param employee
     * @return
     */

     @PutMapping
     public R<String> Update(@RequestBody Employee employee){

//         employee.setUpdateTime(LocalDateTime.now());
//         Long ep = (Long)request.getSession().getAttribute("employee");
//         employee.setUpdateUser(ep);
//         updateById方法只是修改状态的方法     修改员工信息的一个POST方法
         employeeService.updateById(employee);

         return R.success("修改成功");
     }

    /**
     *
     * 通过id识别需要修改的员工
     * @param id
     * @return
     */
     @GetMapping("/{id}")//restful风格
    public R<Employee> getById(@PathVariable Long id){
         //为什么getById就能修改成功呢？ 因为调用了上面的通用修改方法Update()
         Employee employee = employeeService.getById(id);

         if (employee!=null){
            return R.success(employee);
         }
         return R.error("没有查到用户信息");
     }
}
