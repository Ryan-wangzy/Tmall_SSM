package com.how2java.tmall.controller;

import com.github.pagehelper.PageHelper;
import com.how2java.tmall.comparator.*;
import com.how2java.tmall.pojo.*;
import com.how2java.tmall.service.*;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("")
public class ForeController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ReviewService reviewService;

    @RequestMapping("forehome")
    public String home(Model model){
        List<Category> cs = categoryService.list();
        productService.fill(cs);
        productService.fillByRow(cs);
        model.addAttribute("cs",cs);
        return "fore/home";
    }

    @RequestMapping("foreregister")
    public String register(Model model, User user){
        String name = user.getName();
        //特殊符号转义
        name = HtmlUtils.htmlEscape(name);
        user.setName(name);
        boolean exist = userService.isExist(name);
        if(exist){
            String m = "用户名已经被使用";
            model.addAttribute("msg",m);
            model.addAttribute("user",null);
            return "fore/register";
        }
        userService.add(user);
        return "redirect:registerSuccessPage";
    }

    @RequestMapping("checkName")
    @ResponseBody
    public boolean check(String name){
        boolean exist = userService.isExist(name);
//        System.out.println("name:"+name+","+"exist:"+exist);
        if(exist) {
            return true;
        }
        return false;
    }

    @RequestMapping("forelogin")
    public String login(String name, String password, Model model, HttpSession session){
        name = HtmlUtils.htmlEscape(name);
        User user = userService.get(name, password);
        if(null==user) {
            model.addAttribute("msg", "账号密码错误");
            return "fore/login";
        }
        session.setAttribute("user",user);
        return "redirect:forehome";
    }

    @RequestMapping("forelogout")
    public String logout( HttpSession session) {
        session.removeAttribute("user");
        return "redirect:forehome";
    }

    @RequestMapping("foreproduct")
    public String product(Model model, int pid){
        Product product = productService.get(pid);
        List<ProductImage> productSingleImages= productImageService.list(pid, ProductImageService.type_single);
        List<ProductImage> productDetailImages= productImageService.list(pid, ProductImageService.type_detail);
        product.setProductSingleImages(productSingleImages);
        product.setProductDetailImages(productDetailImages);

        List<PropertyValue> pvs = propertyValueService.list(pid);
        List<Review> reviews = reviewService.list(pid);
        productService.setSaleAndReviewNumber(product);
        model.addAttribute("reviews",reviews);
        model.addAttribute("p",product);
        model.addAttribute("pvs",pvs);
        return "fore/product";
    }

    @RequestMapping("forecheckLogin")
    @ResponseBody
    public String checkLogin(HttpSession session){
        User user = (User) session.getAttribute("user");
        if(null!=user)
            return "success";
        return "fail";
    }

    @RequestMapping("foreloginAjax")
    @ResponseBody
    public String loginAjax(String name, String password, HttpSession session){
        name = HtmlUtils.htmlEscape(name);
        User user = userService.get(name, password);
        if(null == user){
            return "fail";
        }
        session.setAttribute("user",user);
        return "success";
    }

    @RequestMapping("forecategory")
    public String category(int cid, Model model, String sort){
        Category c = categoryService.get(cid);
        productService.fill(c);
        productService.setSaleAndReviewNumber(c.getProducts());
        if(null!=sort){
            switch (sort){
                case "review":
                    Collections.sort(c.getProducts(), new ProductReviewComparator());
                    break;
                case "date" :
                    Collections.sort(c.getProducts(),new ProductDateComparator());
                    break;

                case "saleCount" :
                    Collections.sort(c.getProducts(),new ProductSaleCountComparator());
                    break;

                case "price":
                    Collections.sort(c.getProducts(),new ProductPriceComparator());
                    break;

                case "all":
                    Collections.sort(c.getProducts(),new ProductAllComparator());
                    break;
            }
        }
        model.addAttribute("c",c);
        return "fore/category";
    }

    @RequestMapping("foresearch")
    public String search(String keyword, Model model){
        PageHelper.offsetPage(0,20);
        List<Product> ps = productService.search(keyword);
        productService.setSaleAndReviewNumber(ps);
        model.addAttribute("ps",ps);
        return "fore/searchResult";
    }

    @RequestMapping("forebuyone")
    public String buyone(int pid,int num, HttpSession session){
        Product p = productService.get(pid);
        User user = (User) session.getAttribute("user");

        int orderItemID = 0;
        boolean found = false;
        List<OrderItem> orderItems = orderItemService.listByUser(user.getId());
        for(OrderItem orderItem:orderItems){
            if(orderItem.getProduct().getId().intValue() == p.getId().intValue()){
                orderItem.setNumber(orderItem.getNumber()+num);
                orderItemService.update(orderItem);
                found=true;
                orderItemID = orderItem.getId();
                break;
            }
        }

        if(!found){
            OrderItem orderItem = new OrderItem();
            orderItem.setNumber(num);
            orderItem.setUid(user.getId());
            orderItem.setPid(p.getId());
            orderItemService.add(orderItem);
            orderItemID = orderItem.getId();
        }
        return "redirect:forebuy?oiid="+orderItemID;
    }

    @RequestMapping("forebuy")
    public String buy(Model model,String[] oiid, HttpSession session){
        List<OrderItem> ois = new ArrayList<>();
        float total=0;
        for(String strid:oiid){
            int id = Integer.parseInt(strid);
            OrderItem orderItem = orderItemService.get(id);
            total += orderItem.getProduct().getPromotePrice() * orderItem.getNumber();
            ois.add(orderItem);
        }
        //1. session 里放的数据可以在其他页面使用
        //2. model的数据，只能在接下来的页面使用，其他页面就不能使用了
        session.setAttribute("ois",ois);
        model.addAttribute("total",total);
        return "fore/buy";
    }

    @RequestMapping("foreaddCart")
    @ResponseBody
    public String addCart(int pid, int num, HttpSession session){
        Product p = productService.get(pid);
        User user = (User) session.getAttribute("user");

        boolean found = false;
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        for(OrderItem oi:ois){
            if(p.getId().intValue() == oi.getProduct().getId().intValue()){
                oi.setNumber(oi.getNumber()+ num);
                orderItemService.update(oi);
                found=true;
                break;
            }
        }

        if(!found){
            OrderItem oi = new OrderItem();
            oi.setUid(user.getId());
            oi.setNumber(num);
            oi.setPid(pid);
            orderItemService.add(oi);
        }

        return "success";
    }

    @RequestMapping("forecart")
    public String cart(Model model,HttpSession session){
        User user = (User) session.getAttribute("user");
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        model.addAttribute("ois",ois);
        return "fore/cart";
    }

    @RequestMapping("forechangeOrderItem")
    @ResponseBody
    public String changeOrderItem(HttpSession session, int pid, int number){
        User user = (User) session.getAttribute("user");
        if(null == user) {
            return "fail";
        }
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        for (OrderItem oi:ois) {
            if(pid == oi.getPid()){
                oi.setNumber(number);
                orderItemService.update(oi);
                break;
            }
        }
        return "success";
    }

    @RequestMapping("foredeleteOrderItem")
    @ResponseBody
    public String deleteOrderItem(int oiid,HttpSession session){
        User user = (User) session.getAttribute("user");
        if(null == user){
            return "fail";
        }
        orderItemService.delete(oiid);
        return "success";
    }

    @RequestMapping("forecreateOrder")
    public String createOrder(Model model, Order order, HttpSession session){
        User user = (User) session.getAttribute("user");
        //根据当前时间加上一个4位随机数生成订单号[0-10000)
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);
        order.setOrderCode(orderCode);
        order.setCreateDate(new Date());
        order.setUid(user.getId());
        order.setStatus(OrderService.waitPay);
        List<OrderItem> ois = (List<OrderItem>) session.getAttribute("ois");

        float total = orderService.add(order,ois);
        return "redirect:forealipay?oid="+order.getId()+"&total="+total;
    }

    @RequestMapping("forepayed")
    public String payed(Model model, int oid, float total){
        Order order = orderService.get(oid);
        order.setStatus(OrderService.waitDelivery);
        order.setPayDate(new Date());
        orderService.update(order);
        model.addAttribute("o",order);
        return "fore/payed";
    }

    @RequestMapping("forebought")
    public String bought(Model model, HttpSession session){
        User user = (User) session.getAttribute("user");
        List<Order> os = orderService.list(user.getId(), OrderService.delete);
        orderItemService.fill(os);
        model.addAttribute("os",os);
        return "fore/bought";
    }

    @RequestMapping("foreconfirmPay")
    public String confirmPay(Model model, int oid){
        Order order = orderService.get(oid);
        orderItemService.fill(order);
        model.addAttribute("o",order);
        return "fore/confirmPay";
    }

    @RequestMapping("foreorderConfirmed")
    public String orderConfirmed(int oid){
        Order o = orderService.get(oid);
        o.setStatus(OrderService.waitReview);
        o.setConfirmDate(new Date());
        orderService.update(o);
        return "fore/orderConfirmed";
    }

    @RequestMapping("foredeleteOrder")
    @ResponseBody
    public String deleteOrder(int oid){
        Order o = orderService.get(oid);
        o.setStatus(OrderService.delete);
        orderService.update(o);
        return "success";
    }

    @RequestMapping("forereview")
    public String review(Model model, int oid){
        Order o = orderService.get(oid);
        orderItemService.fill(o);
        Product p = o.getOrderItems().get(0).getProduct();
        List<Review> reviews = reviewService.list(p.getId());
        productService.setSaleAndReviewNumber(p);
        model.addAttribute("p",p);
        model.addAttribute("o",o);
        model.addAttribute("reviews",reviews);
        return "fore/review";
    }

    @RequestMapping("foredoreview")
    public String doreview(Review review, HttpSession session, int oid){
        Order o = orderService.get(oid);
        o.setStatus(OrderService.finish);

        User user = (User) session.getAttribute("user");
        review.setUid(user.getId());
        review.setCreateDate(new Date());
        reviewService.add(review,o);

        return "redirect:forereview?oid="+oid+"&showonly=true";

    }
}
