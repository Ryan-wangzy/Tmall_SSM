package com.how2java.tmall.service.impl;

import com.how2java.tmall.mapper.OrderItemMapper;
import com.how2java.tmall.pojo.Order;
import com.how2java.tmall.pojo.OrderItem;
import com.how2java.tmall.pojo.OrderItemExample;
import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.service.OrderItemService;
import com.how2java.tmall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class OrderItemServiceImpl implements OrderItemService {
    @Autowired
    OrderItemMapper orderItemMapper;
    @Autowired
    ProductService productService;

    @Override
    public void add(OrderItem c) {
        orderItemMapper.insert(c);
    }

    @Override
    public void delete(int id) {
        orderItemMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void update(OrderItem c) {
        orderItemMapper.updateByPrimaryKeySelective(c);
    }

    @Override
    public OrderItem get(int id) {
        OrderItem orderItem = orderItemMapper.selectByPrimaryKey(id);
        setProduct(orderItem);
        return orderItem;
    }

    @Override
    public List<OrderItem> list() {
        OrderItemExample orderItemExample = new OrderItemExample();
        orderItemExample.setOrderByClause("id desc");
        return orderItemMapper.selectByExample(orderItemExample);
    }

    @Override
    public void fill(List<Order> os) {
        for(Order o:os){
            fill(o);
        }
    }


    /*
    1. 根据订单id查询出其对应的所有订单项
    2. 通过setProduct为所有的订单项设置Product属性
    3. 遍历所有的订单项，然后计算出该订单的总金额和总数量
    4. 最后再把订单项设置在订单的orderItems属性上。
     */
    @Override
    public void fill(Order o) {
        OrderItemExample orderItemExample = new OrderItemExample();
        orderItemExample.createCriteria().andOidEqualTo(o.getId());
        orderItemExample.setOrderByClause("id desc");
        List<OrderItem> orderItems = orderItemMapper.selectByExample(orderItemExample);
        setProduct(orderItems);

        float total=0;
        int number =0;
        for(OrderItem orderItem:orderItems){
            number += orderItem.getNumber();
            total += orderItem.getNumber() * orderItem.getProduct().getPromotePrice();
        }
        o.setTotal(total);
        o.setTotalNumber(number);
        o.setOrderItems(orderItems);


    }

    public void setProduct(List<OrderItem> ois){
        for (OrderItem oi: ois) {
            setProduct(oi);
        }
    }

    private void setProduct(OrderItem oi) {
        Product p = productService.get(oi.getPid());
        oi.setProduct(p);
    }
}
