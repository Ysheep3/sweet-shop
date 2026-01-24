package com.sweet.api.dto;

public class OrderOverViewVO {
    private Integer allOrders;

    private Integer cancelledOrders;

    private Integer completedOrders;

    private Integer deliveredOrders;

    private Integer waitingOrders;

    public Integer getAllOrders() {
        return allOrders;
    }

    public void setAllOrders(Integer allOrders) {
        this.allOrders = allOrders;
    }

    public Integer getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(Integer cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public Integer getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Integer completedOrders) {
        this.completedOrders = completedOrders;
    }

    public Integer getDeliveredOrders() {
        return deliveredOrders;
    }

    public void setDeliveredOrders(Integer deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }

    public Integer getWaitingOrders() {
        return waitingOrders;
    }

    public void setWaitingOrders(Integer waitingOrders) {
        this.waitingOrders = waitingOrders;
    }
}
