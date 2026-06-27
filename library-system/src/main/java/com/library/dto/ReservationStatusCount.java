package com.library.dto;

public class ReservationStatusCount {
    private Integer status;
    private Integer count;

    public ReservationStatusCount() {}

    public ReservationStatusCount(Integer status, Integer count) {
        this.status = status;
        this.count = count;
    }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
