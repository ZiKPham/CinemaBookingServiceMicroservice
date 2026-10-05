package com.cinema.movie_service.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieSearchCriteria {
    private String name; // Tìm gần đúng theo tên phim (LIKE)
    private String genre; // Thể loại phim
    private String director; // Đạo diễn
    private String language; // Ngôn ngữ
    private String ageRestriction; // Giới hạn độ tuổi (ví dụ: T18, P...)
    private Boolean active; // Trạng thái hoạt động
}