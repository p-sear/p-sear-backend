package com.pser.hotel.domain.hotel.api;

import com.pser.hotel.domain.hotel.application.ReservationService;
import com.pser.hotel.domain.hotel.domain.ReservationStatusEnum;
import com.pser.hotel.domain.hotel.dto.request.ReservationCreateRequest;
import com.pser.hotel.domain.hotel.dto.response.ReservationResponse;
import com.pser.hotel.global.common.response.ApiResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationApi {
    private final ReservationService reservationService;

    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getById(@PathVariable long reservationId) {
        ReservationResponse response = reservationService.getById(reservationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<Void> save(@RequestHeader("User-Id") long authId,
                                     @Validated @RequestBody ReservationCreateRequest reservationCreateRequest) {
        reservationCreateRequest.setAuthId(authId);
        long id = reservationService.save(reservationCreateRequest);
        return ResponseEntity.created(URI.create("/reservations/%s".formatted(id))).build();
    }

    @PostMapping("/{reservationId}/refund")
    public ResponseEntity<Void> refund(@PathVariable long reservationId) {
        reservationService.refund(reservationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reservationId}/check-payment")
    public ResponseEntity<ApiResponse<ReservationStatusEnum>> checkPayment(@PathVariable long reservationId,
                                                                           @RequestBody String impUid) {
        ReservationStatusEnum response = reservationService.checkPayment(reservationId, impUid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
