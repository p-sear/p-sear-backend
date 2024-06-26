package com.pser.hotel.domain.hotel.dto.mapper;

import com.pser.hotel.domain.hotel.domain.Amenity;
import com.pser.hotel.domain.hotel.domain.Facility;
import com.pser.hotel.domain.hotel.domain.Hotel;
import com.pser.hotel.domain.hotel.domain.HotelImage;
import com.pser.hotel.domain.hotel.domain.Room;
import com.pser.hotel.domain.hotel.domain.RoomImage;
import com.pser.hotel.domain.hotel.dto.AmenityDto;
import com.pser.hotel.domain.hotel.dto.FacilityDto;
import com.pser.hotel.domain.hotel.dto.HotelDto;
import com.pser.hotel.domain.hotel.dto.RoomDto;
import com.pser.hotel.domain.hotel.dto.request.HotelCreateRequest;
import com.pser.hotel.domain.hotel.dto.request.HotelUpdateRequest;
import com.pser.hotel.domain.hotel.dto.response.HotelResponse;
import com.pser.hotel.domain.hotel.dto.response.HotelSummaryResponse;
import com.pser.hotel.domain.member.domain.User;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = ComponentModel.SPRING)
public interface HotelMapper {
    @Named("getImageUrls")
    default List<String> getImageUrls(List<HotelImage> hotelImages) {
        return hotelImages.stream().map(HotelImage::getImageUrl).toList();
    }

    @Named("getRoomImageUrls")
    default List<String> getRoomImageUrls(List<RoomImage> roomImages) {
        return roomImages.stream().map(RoomImage::getImageUrl).toList();
    }

    default HotelResponse changeToHotelResponse(Hotel hotel, double averageRating, int salePrice, int previousPrice) {
        HotelResponse hotelResponse = changeToHotelResponseExcludeImage(hotel);
        List<String> imageUrls = hotel.getImages().stream()
                .map(HotelImage::getImageUrl)
                .toList();
        hotelResponse.setHotelImageUrls(imageUrls);
        hotelResponse.setGradeAverage(averageRating);
        hotelResponse.setSalePrice(salePrice);
        hotelResponse.setPreviousPrice(previousPrice);
        return hotelResponse;
    }

    default HotelSummaryResponse changeToHotelSummaryResponse(Hotel hotel, double averageRating, int salePrice,
                                                              int previousPrice) {
        HotelSummaryResponse hotelSummaryResponse = changeToHotelSummaryResponseExcludeImage(hotel);
        List<String> imageUrls = hotel.getImages().stream()
                .map(HotelImage::getImageUrl)
                .toList();
        hotelSummaryResponse.setHotelImageUrls(imageUrls);
        hotelSummaryResponse.setGradeAverage(averageRating);
        hotelSummaryResponse.setSalePrice(salePrice);
        hotelSummaryResponse.setPreviousPrice(previousPrice);
        return hotelSummaryResponse;
    }

    Hotel changeToHotel(HotelCreateRequest hotelCreateRequest, User user);

    Facility changeToFacility(HotelCreateRequest hotelCreateRequest, Hotel hotel);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    void updateHotelFromDto(HotelUpdateRequest hotelUpdateRequest, @MappingTarget Hotel hotel);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    void updateFacilityFromDto(HotelUpdateRequest hotelUpdateRequest, @MappingTarget Facility facility);

    @Mapping(target = "parkingLot", source = "hotel.facility.parkingLot")
    @Mapping(target = "wifi", source = "hotel.facility.wifi")
    @Mapping(target = "barbecue", source = "hotel.facility.barbecue")
    @Mapping(target = "sauna", source = "hotel.facility.sauna")
    @Mapping(target = "swimmingPool", source = "hotel.facility.swimmingPool")
    @Mapping(target = "restaurant", source = "hotel.facility.restaurant")
    @Mapping(target = "roofTop", source = "hotel.facility.roofTop")
    @Mapping(target = "fitness", source = "hotel.facility.fitness")
    @Mapping(target = "dryer", source = "hotel.facility.dryer")
    @Mapping(target = "breakfast", source = "hotel.facility.breakfast")
    @Mapping(target = "smokingArea", source = "hotel.facility.smokingArea")
    @Mapping(target = "allTimeDesk", source = "hotel.facility.allTimeDesk")
    @Mapping(target = "luggageStorage", source = "hotel.facility.luggageStorage")
    @Mapping(target = "snackBar", source = "hotel.facility.snackBar")
    @Mapping(target = "petFriendly", source = "hotel.facility.petFriendly")
    @Mapping(target = "userId", source = "user.id")
    HotelResponse changeToHotelResponseExcludeImage(Hotel hotel);

    HotelSummaryResponse changeToHotelSummaryResponseExcludeImage(Hotel hotel);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "images", source = "images", qualifiedByName = "getImageUrls")
    HotelDto toDto(Hotel hotel);

    @Mapping(target = "hotelId", source = "hotel.id")
    FacilityDto toDto(Facility facility);

    @Mapping(target = "hotelId", source = "hotel.id")
    @Mapping(target = "roomImages", source = "roomImages", qualifiedByName = "getRoomImageUrls")
    RoomDto toDto(Room room);

    @Mapping(target = "roomId", source = "room.id")
    AmenityDto toDto(Amenity amenity);
}
