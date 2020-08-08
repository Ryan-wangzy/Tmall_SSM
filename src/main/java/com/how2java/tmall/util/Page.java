package com.how2java.tmall.util;

public class Page {
    private int start;
    private int total;
    private int count;
    private String param;

    private static final int defaultCount=5; //默认每页5条

    public Page(){
        count=defaultCount;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    //判断是否有前一页
    public boolean isHasPrevious(){
        if(start==0){
            return false;
        }
        return true;
    }

    //判断是否有后一页
    public boolean isHasNext(){
        if(start == getLast()){
            return false;
        }
        return true;
    }

    //获取总页数
    public int getTotalPage(){
        int totalPage;
        //若不能被整除，则多一页
        if(0 == total%count){
            totalPage = total/count;
        }else{
            totalPage = total/count+1;
        }
        if(0==totalPage){
            totalPage=1;
        }
        return totalPage;
    }

    //计算最后一页的数值
    public int getLast(){
        int last;
        if(0 == total%count){
            last = total - count;
        }else{
            last = total - total%count;
        }
        last = last<0? 0:last;
        return last;
    }

    @Override
    public String toString() {
        return "Page [start=" + start + ", count=" + count + ", total=" + total + ", getStart()=" + getStart()
                + ", getCount()=" + getCount() + ", isHasPrevious()=" + isHasPrevious() + ", isHasNext()="
                + isHasNext() + ", getTotalPage()=" + getTotalPage() + ", getLast()=" + getLast() + "]";
    }


}
