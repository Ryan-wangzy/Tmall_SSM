package com.how2java.tmall.service.impl;

import com.how2java.tmall.mapper.ReviewMapper;
import com.how2java.tmall.pojo.Order;
import com.how2java.tmall.pojo.Review;
import com.how2java.tmall.pojo.ReviewExample;
import com.how2java.tmall.pojo.User;
import com.how2java.tmall.service.OrderService;
import com.how2java.tmall.service.ReviewService;
import com.how2java.tmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
public class ReviewServiceImpl implements ReviewService {
    @Autowired
    ReviewMapper reviewMapper;

    @Autowired
    UserService userService;

    @Autowired
    OrderService orderService;

    @Override
    public void add(Review r) {
        reviewMapper.insert(r);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public void add(Review r, Order o) {
        reviewMapper.insert(r);
        //事务测试
        if(false)
            throw new RuntimeException();
        orderService.update(o);
    }

    @Override
    public void delete(int id) {
        reviewMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void update(Review r) {
        reviewMapper.updateByPrimaryKeySelective(r);
    }

    @Override
    public Review get(int id) {
        return reviewMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Review> list(int pid) {
        ReviewExample example =new ReviewExample();
        example.createCriteria().andPidEqualTo(pid);
        example.setOrderByClause("id desc");

        List<Review> result =reviewMapper.selectByExample(example);
        setUser(result);
        return result;
    }


    public void setUser(List<Review> reviews){
        for (Review review : reviews) {
            setUser(review);
        }
    }

    private void setUser(Review review) {
        int uid = review.getUid();
        User user = userService.get(uid);
        review.setUser(user);
    }

    @Override
    public int getCount(int pid) {
        return list(pid).size();
    }
}
