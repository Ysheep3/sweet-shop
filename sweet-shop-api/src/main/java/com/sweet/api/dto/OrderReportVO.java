package com.sweet.api.dto;

public class OrderReportVO {
    private String dateList;

    private Double orderCompletionRate;

    private String orderCountList;

    private Integer totalOrderCount;

    private Integer validOrderCount;

    private String validOrderCountList;

    public String getDateList() {
        return dateList;
    }

    public void setDateList(String dateList) {
        this.dateList = dateList;
    }

    public Double getOrderCompletionRate() {
        return orderCompletionRate;
    }

    public void setOrderCompletionRate(Double orderCompletionRate) {
        this.orderCompletionRate = orderCompletionRate;
    }

    public String getOrderCountList() {
        return orderCountList;
    }

    public void setOrderCountList(String orderCountList) {
        this.orderCountList = orderCountList;
    }

    public Integer getTotalOrderCount() {
        return totalOrderCount;
    }

    public void setTotalOrderCount(Integer totalOrderCount) {
        this.totalOrderCount = totalOrderCount;
    }

    public Integer getValidOrderCount() {
        return validOrderCount;
    }

    public void setValidOrderCount(Integer validOrderCount) {
        this.validOrderCount = validOrderCount;
    }

    public String getValidOrderCountList() {
        return validOrderCountList;
    }

    public void setValidOrderCountList(String validOrderCountList) {
        this.validOrderCountList = validOrderCountList;
    }
}
