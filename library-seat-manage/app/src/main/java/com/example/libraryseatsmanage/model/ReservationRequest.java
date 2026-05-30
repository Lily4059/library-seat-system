package com.example.libraryseatsmanage.model;

public class ReservationRequest {
    private int seatId;
    private String seatNo;
    private String seatType;
    private String startTime;
    private String endTime;

    public ReservationRequest(int seatId, String seatNo, String seatType, String startTime, String endTime) {
        this.seatId = seatId;
        this.seatNo = seatNo;
        this.seatType = seatType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
