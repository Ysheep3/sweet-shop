package com.sweet.api.dto;

import java.util.List;

/**
 * 管理端：按天按骑手统计完成订单数
 */
public class RiderOrdersReportVO {
    private String riderNameList;   // 张三,李四
    private String totalOrderList;  // 12,8
    private String validOrderList;

    public String getRiderNameList() {
        return riderNameList;
    }

    public void setRiderNameList(String riderNameList) {
        this.riderNameList = riderNameList;
    }

    public String getTotalOrderList() {
        return totalOrderList;
    }

    public void setTotalOrderList(String totalOrderList) {
        this.totalOrderList = totalOrderList;
    }

    public String getValidOrderList() {
        return validOrderList;
    }

    public void setValidOrderList(String validOrderList) {
        this.validOrderList = validOrderList;
    }
}

