package com.library.dto;

/**
 * 座位推荐DTO
 */
public class SeatRecommendDTO {
    private Integer seatId;
    private String seatNumber;
    private String area;
    private String reason;
    private Integer score;

    public Integer getSeatId() { return seatId; }
    public void setSeatId(Integer seatId) { this.seatId = seatId; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}
