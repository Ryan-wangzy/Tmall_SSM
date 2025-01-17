package com.how2java.tmall.service;

import com.how2java.tmall.pojo.Order;
import com.how2java.tmall.pojo.Review;

import java.util.List;

public interface ReviewService {
    void add(Review r);
    void add(Review r, Order o);
    void delete(int id);
    void update(Review r);
    Review get(int id);
    List list(int pid);

    int getCount(int pid);
}
