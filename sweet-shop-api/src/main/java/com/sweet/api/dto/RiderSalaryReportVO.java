package com.sweet.api.dto;

/**
 * 管理端：按天按骑手统计薪资
 * dateList: 日期
 * riderSalaryList: 格式与 RiderOrdersReportVO.riderList 相同，表示每个骑手每天的总薪资
 */
public class RiderSalaryReportVO {
    private String riderNameList;   // 张三,李四
    private String totalSalaryList;
    private String avgSalaryList;

    public String getRiderNameList() {
        return riderNameList;
    }

    public void setRiderNameList(String riderNameList) {
        this.riderNameList = riderNameList;
    }

    public String getTotalSalaryList() {
        return totalSalaryList;
    }

    public void setTotalSalaryList(String totalSalaryList) {
        this.totalSalaryList = totalSalaryList;
    }

    public String getAvgSalaryList() {
        return avgSalaryList;
    }

    public void setAvgSalaryList(String avgSalaryList) {
        this.avgSalaryList = avgSalaryList;
    }
}

