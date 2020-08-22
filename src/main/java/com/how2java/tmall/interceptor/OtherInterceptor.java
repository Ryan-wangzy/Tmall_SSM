package com.how2java.tmall.interceptor;

import com.how2java.tmall.pojo.Category;
import com.how2java.tmall.pojo.OrderItem;
import com.how2java.tmall.pojo.User;
import com.how2java.tmall.service.CategoryService;
import com.how2java.tmall.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

public class OtherInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    CategoryService categoryService;

    @Autowired
    OrderItemService orderItemService;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    /**
     * 在业务Controller处理请求执行完成后,生成视图之前执行的动作
     * 可在modelAndView中加入数据，比如当前时间
     */
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        List<Category> cs = categoryService.list();
        HttpSession session = request.getSession();
        session.setAttribute("cs",cs);

        String contextPath = session.getServletContext().getContextPath();
        session.setAttribute("contextPath",contextPath);

        User user = (User) session.getAttribute("user");
        int cartTotalItemNumber=0;
        if(null!=user){
            List<OrderItem> ois = orderItemService.listByUser(user.getId());
            for(OrderItem oi:ois){
                cartTotalItemNumber += oi.getNumber();
            }
        }
        session.setAttribute("cartTotalItemNumber",cartTotalItemNumber);

    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
