package com.pser.hotel.domain.hotel.api;

import com.pser.hotel.domain.hotel.application.HotelService;
import com.pser.hotel.domain.hotel.dto.request.HotelCreateRequest;
import com.pser.hotel.domain.hotel.dto.response.HotelResponse;
import com.pser.hotel.domain.hotel.dto.request.HotelSearchRequest;
import com.pser.hotel.domain.hotel.dto.response.HotelSummaryResponse;
import com.pser.hotel.domain.hotel.dto.request.HotelUpdateRequest;
import com.pser.hotel.global.common.response.ApiResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hotels")
public class HotelApi {

    private final HotelService hotelService;

    @PostMapping // 숙소 등록 api
    public ResponseEntity<ApiResponse<Void>> saveHotel(@RequestBody HotelCreateRequest hotelCreateRequest, @RequestHeader("user-id") long userId) {
        Long hotelId = hotelService.saveHotelData(hotelCreateRequest, userId);
        return ResponseEntity.created(URI.create("/hotels/" + hotelId)).build();
    }

    @GetMapping // 숙소 전체 조회 api
    public ResponseEntity<ApiResponse<Slice<HotelSummaryResponse>>> getAllHotel(@PageableDefault Pageable pageable){
        return ResponseEntity.ok(ApiResponse.success(hotelService.getAllHotelData(pageable)));
    }

    @GetMapping("/search") // 숙소 검색 조회 api
    public ResponseEntity<ApiResponse<Slice<HotelSummaryResponse>>> searchHotel(HotelSearchRequest hotelSearchRequest, @PageableDefault Pageable pageable){
        Slice<HotelSummaryResponse> result = hotelService.searchHotelData(hotelSearchRequest, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{hotelId}") // 특정 숙소 조회 api
    public ResponseEntity<ApiResponse<HotelResponse>> getHotel(@PathVariable Long hotelId){
        return ResponseEntity.ok(ApiResponse.success(hotelService.getHotelDataById(hotelId)));
    }

    @PatchMapping("/{hotelId}") // 숙소 수정 api
    @PreAuthorize("@methodAuthorizationManager.isHotelByIdAndUserId(#userId, #hotelId)")
    public ResponseEntity<ApiResponse<Void>> updateHotel(@RequestBody HotelUpdateRequest hotelUpdateRequest, @PathVariable Long hotelId, @RequestHeader("user-id") long userId){
        hotelService.updateHotelData(hotelUpdateRequest, hotelId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{hotelId}") // 숙소 삭제 api
    @PreAuthorize("@methodAuthorizationManager.isHotelByIdAndUserId(#userId, #hotelId)")
    public ResponseEntity<ApiResponse<Void>> deleteHotel(@PathVariable Long hotelId, @RequestHeader("user-id") long userId){
        hotelService.deleteHotelData(hotelId);
        return ResponseEntity.noContent().build();
    }
}
