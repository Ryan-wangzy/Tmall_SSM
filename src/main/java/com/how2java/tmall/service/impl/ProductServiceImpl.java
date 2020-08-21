package com.how2java.tmall.service.impl;

import com.how2java.tmall.mapper.CategoryMapper;
import com.how2java.tmall.mapper.ProductMapper;
import com.how2java.tmall.pojo.Category;
import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.ProductExample;
import com.how2java.tmall.pojo.ProductImage;

import com.how2java.tmall.service.OrderItemService;
import com.how2java.tmall.service.ProductImageService;
import com.how2java.tmall.service.ProductService;
import com.how2java.tmall.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductMapper productMapper;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    ProductImageService productImageService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    ReviewService reviewService;

    @Override
    public void add(Product p) {
        productMapper.insert(p);
    }

    @Override
    public void delete(int id) {
        productMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void update(Product p) {
        productMapper.updateByPrimaryKeySelective(p);
    }

    @Override
    public Product get(int id) {
        Product p = productMapper.selectByPrimaryKey(id);
        setCategory(p);
        setFirstProductImage(p);
        return p;
    }


    public void setCategory(Product p){
        int cid = p.getCid();
        p.setCategory(categoryMapper.selectByPrimaryKey(cid));
    }

    public void setCategory(List<Product> ps){
        for(Product p:ps){
            setCategory(p);
        }
    }

    @Override
    public List list(int cid) {
        ProductExample productExample = new ProductExample();
        productExample.createCriteria().andCidEqualTo(cid);
        productExample.setOrderByClause("id desc");
        List<Product> ps = productMapper.selectByExample(productExample);
        setCategory(ps);
        setFirstProductImage(ps);
        return ps;
    }

    @Override
    public void setFirstProductImage(Product p) {
        List<ProductImage> pis = productImageService.list(p.getId(), ProductImageService.type_single);
        if (!pis.isEmpty()) {
            ProductImage pi = pis.get(0);
            p.setFirstProductImage(pi);
        }
    }

    @Override
    public void fill(List<Category> cs) {
        for(Category c:cs){
            fill(c);
        }

    }

    @Override
    public void fill(Category c) {
        List<Product> ps = list(c.getId());
        c.setProducts(ps);
    }

    /**
     * 即把分类下的产品集合，按照8个为一行，拆成多行，以利于后续页面上进行显示
     * @param cs
     */
    @Override
    public void fillByRow(List<Category> cs) {
        int productNumberEachRow = 8;
        for(Category c:cs){
            List<Product> ps = list(c.getId());
            List<List<Product>> productsByRow = new ArrayList<>();
            for(int i=0;i<ps.size();i+=productNumberEachRow){
                int end = i+productNumberEachRow;
                end = end>ps.size()?ps.size():end;
                List<Product> productsOfEachRow = ps.subList(i,end);
                productsByRow.add(productsOfEachRow);
            }
            c.setProductsByRow(productsByRow);
        }

    }

    @Override
    public void setSaleAndReviewNumber(Product p) {
        int saleCount = orderItemService.getSaleCount(p.getId());
        p.setSaleCount(saleCount);

        int count = reviewService.getCount(p.getId());
        p.setReviewCount(count);
    }

    @Override
    public void setSaleAndReviewNumber(List<Product> ps) {
        for (Product p : ps) {
            setSaleAndReviewNumber(p);
        }
    }

    @Override
    public List<Product> search(String keyword) {
        ProductExample productExample = new ProductExample();
        productExample.createCriteria().andNameLike("%"+keyword+"%");
        productExample.setOrderByClause("id desc");
        List<Product> products = productMapper.selectByExample(productExample);
        setFirstProductImage(products);
        setCategory(products);
        return products;
    }

    public void setFirstProductImage(List<Product> ps) {
        for (Product p : ps) {
            setFirstProductImage(p);
        }
    }
}
