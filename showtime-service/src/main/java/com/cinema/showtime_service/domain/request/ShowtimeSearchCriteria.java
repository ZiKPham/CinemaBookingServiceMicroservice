package com.cinema.showtime_service.domain.request;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShowtimeSearchCriteria {
    private Long movieId; // Lọc theo ID phim cụ thể
    private Long roomId; // Lọc theo ID phòng chiếu cụ thể
    private Instant fromDate; // Suất chiếu từ thời điểm này trở đi
    private Instant toDate; // Suất chiếu đến thời điểm này
    private Double minPrice; // Giá vé từ mức này
    private Double maxPrice; // Giá vé đến mức này
}