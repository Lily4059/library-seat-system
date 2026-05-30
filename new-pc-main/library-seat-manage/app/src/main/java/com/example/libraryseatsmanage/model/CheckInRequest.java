package com.example.libraryseatsmanage.model;

public class CheckInRequest {
    private int reservationId;

    public CheckInRequest(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }
}
