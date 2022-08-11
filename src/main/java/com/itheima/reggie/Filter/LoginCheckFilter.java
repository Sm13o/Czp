package com.itheima.reggie.Filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "我是过滤器",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //      路径匹配器用来识别下面urls的通配符
    public  static  final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response=(HttpServletResponse)servletResponse;

//      把需要的页面放进集合
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login",


        };

//        1.获取本次URI
        String uri = request.getRequestURI();

//        2.判定本次请求是否需要处理
        boolean check = check(urls, uri);


//        3.如果不需要处理,则直接发行
        if (check){
//            log.info("本次请求不需要{}",uri);
            filterChain.doFilter(request,response);
            return;
        }

//        4-1.判断是否登录成功 已登录放行
        if (request.getSession().getAttribute("employee") !=null){
            log.info("id为： {}",request.getSession().getAttribute("employee"));

            Long id = (Long) request.getSession().getAttribute("employee");
            BaseContext.setById(id);

            filterChain.doFilter(request,response);
            return;
        }

        //4-2、判断登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            log.info("userId {}",userId);
            BaseContext.setById(userId);

            filterChain.doFilter(request,response);
            return;
        }


//        5，如果未登录则返回未登录结果,通过输出流方式向客户端响应
        log.info("用户未登录");
       response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }
    public boolean check(String[] urls,String uri){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, uri);
            if (match){
                return  true;
            }
        }
         return false;
        }
}

