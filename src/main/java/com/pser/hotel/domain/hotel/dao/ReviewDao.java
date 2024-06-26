package com.pser.hotel.domain.hotel.dao;

import com.pser.hotel.domain.hotel.domain.Review;
import com.pser.hotel.domain.hotel.dto.request.ReviewSearchRequest;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface ReviewDao extends JpaRepository<Review, Long>, ReviewDaoCustom {
    @Override
    @NonNull
    Page<Review> findAll(@NonNull Pageable pageable);

    Page<Review> search(ReviewSearchRequest dto, @NonNull Pageable pageable);

    @Override
    @NonNull
    Optional<Review> findById(@NonNull Long id);
}
