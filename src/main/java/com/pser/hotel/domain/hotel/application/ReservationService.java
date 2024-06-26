package com.pser.hotel.domain.hotel.application;

import com.pser.hotel.domain.hotel.dao.ReservationDao;
import com.pser.hotel.domain.hotel.dao.RoomDao;
import com.pser.hotel.domain.hotel.dao.UserDao;
import com.pser.hotel.domain.hotel.domain.Reservation;
import com.pser.hotel.domain.hotel.domain.ReservationStatusEnum;
import com.pser.hotel.domain.hotel.domain.Room;
import com.pser.hotel.domain.hotel.dto.ReservationDto;
import com.pser.hotel.domain.hotel.dto.mapper.ReservationMapper;
import com.pser.hotel.domain.hotel.dto.request.ReservationCreateRequest;
import com.pser.hotel.domain.hotel.dto.response.ReservationResponse;
import com.pser.hotel.domain.hotel.kafka.producer.ReservationStatusProducer;
import com.pser.hotel.domain.member.domain.User;
import com.pser.hotel.global.common.PaymentDto;
import com.pser.hotel.global.common.RefundDto;
import com.pser.hotel.global.common.StatusUpdateDto;
import com.pser.hotel.global.error.SameStatusException;
import com.pser.hotel.global.error.ValidationFailedException;
import io.vavr.control.Try;
import java.time.LocalDate;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationDao reservationDao;
    private final RoomDao roomDao;
    private final UserDao userDao;
    private final ReservationMapper reservationMapper;
    private final ReservationStatusProducer reservationStatusProducer;

    public ReservationResponse getById(long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId)
                .orElseThrow();
        return reservationMapper.toResponse(reservation);
    }

    public ReservationResponse getByMerchantUid(String merchantUid) {
        Reservation reservation = reservationDao.findByMerchantUid(merchantUid)
                .orElseThrow();
        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public long save(ReservationCreateRequest request) {
        checkSchedule(request);

        User user = userDao.findById(request.getAuthId())
                .orElseThrow();
        Room room = roomDao.findById(request.getRoomId())
                .orElseThrow();
        request.setUser(user);
        request.setRoom(room);

        Reservation reservation = reservationMapper.toEntity(request);
        reservationDao.save(reservation);
        ReservationDto reservationDto = reservationMapper.toDto(reservation);
        reservationStatusProducer.produceCreated(reservationDto);
        return reservation.getId();
    }

    @Transactional
    public void refund(long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId)
                .orElseThrow();
        int price = reservation.getPrice();
        LocalDate reservationStartDate = reservation.getStartAt();
        int refundPrice = calculateRefundPrice(price, reservationStartDate);
        RefundDto refundDto = RefundDto.builder()
                .impUid(reservation.getImpUid())
                .merchantUid(reservation.getMerchantUid())
                .amount(refundPrice)
                .build();

        StatusUpdateDto<ReservationStatusEnum> statusUpdateDto = StatusUpdateDto.<ReservationStatusEnum>builder()
                .id(reservationId)
                .targetStatus(ReservationStatusEnum.REFUND_REQUIRED)
                .build();
        updateStatus(statusUpdateDto);

        reservationStatusProducer.produceRefundRequired(refundDto);
    }

    @Transactional
    public ReservationStatusEnum checkPayment(long reservationId, String impUid) {
        Reservation reservation = reservationDao.findById(reservationId)
                .orElseThrow();
        ReservationStatusEnum status = reservation.getStatus();

        if (status.equals(ReservationStatusEnum.CREATED)) {
            PaymentDto paymentDto = PaymentDto.builder()
                    .impUid(impUid)
                    .amount(reservation.getPrice())
                    .merchantUid(reservation.getMerchantUid())
                    .build();
            updateToPaymentValidationRequired(paymentDto);
        }
        return status;
    }

    @Transactional
    public void updateToPaymentValidationRequired(PaymentDto paymentDto) {
        Try.run(() -> {
                    StatusUpdateDto<ReservationStatusEnum> statusUpdateDto = StatusUpdateDto.<ReservationStatusEnum>builder()
                            .merchantUid(paymentDto.getMerchantUid())
                            .targetStatus(ReservationStatusEnum.PAYMENT_VALIDATION_REQUIRED)
                            .build();
                    updateStatus(statusUpdateDto, reservation -> {
                        int paidAmount = paymentDto.getAmount();

                        if (reservation.getPrice() != paidAmount) {
                            throw new ValidationFailedException("결제 금액 불일치");
                        }
                        reservation.updateImpUid(paymentDto.getImpUid());
                    });
                })
                .onSuccess(unused -> reservationStatusProducer.producePaymentValidationRequired(paymentDto))
                .recover(SameStatusException.class, e -> null)
                .get();
    }

    @Transactional
    public void updateStatus(StatusUpdateDto<ReservationStatusEnum> statusUpdateDto) {
        updateStatus(statusUpdateDto, null);
    }

    @Transactional
    public void updateStatus(StatusUpdateDto<ReservationStatusEnum> statusUpdateDto, Consumer<Reservation> validator) {
        Reservation reservation = reservationDao.findById(statusUpdateDto.getId())
                .orElseThrow();
        ReservationStatusEnum targetStatus = (ReservationStatusEnum) statusUpdateDto.getTargetStatus();

        if (validator != null) {
            validator.accept(reservation);
        }

        reservation.updateStatus(targetStatus);
    }

    @Transactional
    public void rollbackStatus(StatusUpdateDto<ReservationStatusEnum> statusUpdateDto) {
        rollbackStatus(statusUpdateDto, null);
    }

    @Transactional
    public void rollbackStatus(StatusUpdateDto<ReservationStatusEnum> statusUpdateDto,
                               Consumer<Reservation> validator) {
        Reservation reservation = reservationDao.findById(statusUpdateDto.getId())
                .orElseThrow();
        ReservationStatusEnum targetStatus = (ReservationStatusEnum) statusUpdateDto.getTargetStatus();

        if (validator != null) {
            validator.accept(reservation);
        }

        reservation.rollbackStatusTo(targetStatus);
    }

    private void checkSchedule(ReservationCreateRequest request) {
        Room room = roomDao.findById(request.getRoomId())
                .orElseThrow();
        LocalDate startAt = request.getStartAt();

        if (startAt.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("과거 일자로 예약할 수 없습니다");
        }

        int overlappingCount = reservationDao.countOverlappingReservations(request);
        if (overlappingCount >= room.getMaxCapacity()) {
            throw new IllegalArgumentException("해당 객실은 요청 일자에 비어 있지 않습니다");
        }
    }

    @Transactional
    public void closeReservation(long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId)
                .orElseThrow();
        ReservationStatusEnum targetStatus = ReservationStatusEnum.PAST;
        reservation.updateStatus(targetStatus);
    }

    private int calculateRefundPrice(int price, LocalDate reservationStartDate) {
        LocalDate now = LocalDate.now();
        LocalDate dateFor100Percent = reservationStartDate.minusDays(6);
        LocalDate dateFor50Percent = reservationStartDate.minusDays(3);
        if (now.isBefore(dateFor100Percent)) {
            return price;
        } else if (now.isBefore(dateFor50Percent)) {
            return price / 2;
        }
        throw new IllegalArgumentException("환불 가능 기한이 아닙니다");
    }
}
