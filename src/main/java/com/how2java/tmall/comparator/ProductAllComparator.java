package com.how2java.tmall.comparator;

import com.how2java.tmall.pojo.Product;

import java.util.Comparator;

public class ProductAllComparator implements Comparator<Product> {
    /**
     * compare方法根据其返回值确定比较对象的大小，如果返回值为正，认为o1>o2;返回值为负，认为o1<o2;返回值为0，认为两者相等；
     * 把 销量x评价 高的放前面
     * @param p1
     * @param p2
     * @return
     */
    @Override
    public int compare(Product p1, Product p2) {
        return p2.getReviewCount()*p2.getSaleCount() - p1.getReviewCount()*p1.getSaleCount();
    }
}
