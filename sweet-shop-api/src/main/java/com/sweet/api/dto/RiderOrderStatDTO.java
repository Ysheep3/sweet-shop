package com.sweet.api.dto;

public class RiderOrderStatDTO {
    private Long riderId;
    private Integer totalOrderCount;
    private Integer validOrderCount;

    public Long getRiderId() {
        return riderId;
    }

    public void setRiderId(Long riderId) {
        this.riderId = riderId;
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
}