package com.library.service;

import com.library.dto.ReservationDTO;
import com.library.entity.Reservation;
import java.util.List;

public interface ReservationService {
    List<ReservationDTO> getAllReservations();
    ReservationDTO getReservationById(Integer id);
    List<ReservationDTO> getReservationsByReaderId(Integer readerId);
    List<ReservationDTO> getReservationsByReaderIdString(String readerId);
    List<ReservationDTO> getReservationsByBookId(Integer bookId);
    List<ReservationDTO> getReservationsByStatus(Integer status);
    Reservation addReservation(Reservation reservation);
    Reservation updateReservation(Reservation reservation);
    void deleteReservation(Integer id);
    void updateReservationStatus(Integer id, Integer status);
}
