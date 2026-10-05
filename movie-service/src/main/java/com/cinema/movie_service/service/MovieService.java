package com.cinema.movie_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.cinema.movie_service.domain.Movie;
import com.cinema.movie_service.domain.request.MovieSearchCriteria;
import com.cinema.movie_service.domain.request.ReqCreateMovieDTO;
import com.cinema.movie_service.domain.request.ReqUpdateMovieDTO;
import com.cinema.movie_service.domain.response.ResMovieDTO;
import com.cinema.movie_service.domain.response.ResultPaginationDTO;
import com.cinema.movie_service.repository.MovieRepository;
import com.cinema.movie_service.util.error.IdInvalidException;
import com.cinema.movie_service.util.error.NameInvalidException;

import jakarta.persistence.criteria.Predicate;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public ResMovieDTO handleCreateMovie(ReqCreateMovieDTO reqDTO) {
        Movie movie = new Movie();
        movie.setName(reqDTO.getName());
        movie.setDescription(reqDTO.getDescription());
        movie.setDirector(reqDTO.getDirector());
        movie.setCast(reqDTO.getCast());
        movie.setGenre(reqDTO.getGenre());
        movie.setDuration(reqDTO.getDuration());
        movie.setReleaseDate(reqDTO.getReleaseDate());
        movie.setPoster(reqDTO.getPoster());
        movie.setTrailer(reqDTO.getTrailer());
        movie.setLanguage(reqDTO.getLanguage());
        movie.setAgeRestriction(reqDTO.getAgeRestriction());
        movie.setActive(reqDTO.isActive());

        Movie saveMovie = this.movieRepository.save(movie);
        return this.convertToResMovieDTO(saveMovie);
    }

    public ResultPaginationDTO fetchAllMovies(MovieSearchCriteria criteria, Pageable pageable) {
        // 1. Xây dựng Specification thủ công từ Search Criteria DTO
        Specification<Movie> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo tên phim (tìm gần đúng, không phân biệt hoa thường)
            if (criteria.getName() != null && !criteria.getName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + criteria.getName().toLowerCase() + "%"));
            }

            // Lọc theo thể loại
            if (criteria.getGenre() != null && !criteria.getGenre().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("genre")),
                        "%" + criteria.getGenre().toLowerCase() + "%"));
            }

            // Lọc theo đạo diễn
            if (criteria.getDirector() != null && !criteria.getDirector().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("director")),
                        "%" + criteria.getDirector().toLowerCase() + "%"));
            }

            // Lọc theo ngôn ngữ
            if (criteria.getLanguage() != null && !criteria.getLanguage().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("language"), criteria.getLanguage()));
            }

            // Lọc theo giới hạn độ tuổi
            if (criteria.getAgeRestriction() != null && !criteria.getAgeRestriction().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("ageRestriction"), criteria.getAgeRestriction()));
            }

            // Lọc theo trạng thái active (true/false)
            if (criteria.getActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), criteria.getActive()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 2. Thực hiện query phân trang với Specification
        Page<Movie> pageMovie = this.movieRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageMovie.getTotalPages());
        mt.setTotal(pageMovie.getTotalElements());

        rs.setMeta(mt);

        List<ResMovieDTO> listMovies = pageMovie.getContent().stream()
                .map(this::convertToResMovieDTO)
                .collect(Collectors.toList());

        rs.setResult(listMovies);
        return rs;
    }

    public ResMovieDTO fetchMovieById(long id) throws IdInvalidException {
        Optional<Movie> movieOptional = this.movieRepository.findById(id);
        if (!movieOptional.isPresent()) {
            throw new IdInvalidException("Movie với id = " + id + " không tồn tại");
        }

        Movie movie = movieOptional.get();
        return this.convertToResMovieDTO(movie); // Hàm chuyển đổi từ Entity sang DTO có sẵn của bạn
    }

    public List<ResMovieDTO> fetchMovieByName(String name) throws NameInvalidException {
        List<Movie> mOptional = this.movieRepository.findByNameContainingIgnoreCase(name);
        if (mOptional.isEmpty()) {
            throw new NameInvalidException("Phim tên '" + name + "' không tồn tại");
        }

        return mOptional.stream()
                .map(this::convertToResMovieDTO)
                .collect(Collectors.toList());
    }

    public ResMovieDTO handleUpdateMovie(long id, ReqUpdateMovieDTO reqDTO) throws IdInvalidException {
        Optional<Movie> mOptional = this.movieRepository.findById(id);
        if (!mOptional.isPresent()) {
            throw new IdInvalidException("Movie với id = " + id + " không tồn tại");
        }
        Movie currentMovie = mOptional.get();
        if (reqDTO.getName() != null && !reqDTO.getName().trim().isEmpty()) {
            currentMovie.setName(reqDTO.getName());
        }
        if (reqDTO.getDescription() != null) {
            currentMovie.setDescription(reqDTO.getDescription());
        }
        if (reqDTO.getDirector() != null) {
            currentMovie.setDirector(reqDTO.getDirector());
        }
        if (reqDTO.getCast() != null) {
            currentMovie.setCast(reqDTO.getCast());
        }
        if (reqDTO.getGenre() != null) {
            currentMovie.setGenre(reqDTO.getGenre());
        }
        if (reqDTO.getDuration() != null) {
            currentMovie.setDuration(reqDTO.getDuration());
        }
        if (reqDTO.getReleaseDate() != null) {
            currentMovie.setReleaseDate(reqDTO.getReleaseDate());
        }
        if (reqDTO.getPoster() != null) {
            currentMovie.setPoster(reqDTO.getPoster());
        }
        if (reqDTO.getTrailer() != null) {
            currentMovie.setTrailer(reqDTO.getTrailer());
        }
        if (reqDTO.getLanguage() != null) {
            currentMovie.setLanguage(reqDTO.getLanguage());
        }
        if (reqDTO.getAgeRestriction() != null) {
            currentMovie.setAgeRestriction(reqDTO.getAgeRestriction());
        }
        if (reqDTO.getActive() != null) {
            currentMovie.setActive(reqDTO.getActive());
        }

        Movie updatedMovie = this.movieRepository.save(currentMovie);
        return this.convertToResMovieDTO(updatedMovie);
    }

    public void handleDeleteMovie(long id) throws IdInvalidException {
        Optional<Movie> mOptional = this.movieRepository.findById(id);
        if (!mOptional.isPresent()) {
            throw new IdInvalidException("Movie với id = " + id + " không tồn tại!");
        }
        this.movieRepository.deleteById(id);
    }

    public ResMovieDTO convertToResMovieDTO(Movie movie) {
        ResMovieDTO res = new ResMovieDTO();
        res.setId(movie.getId());
        res.setName(movie.getName());
        res.setDescription(movie.getDescription());
        res.setDirector(movie.getDirector());
        res.setCast(movie.getCast());
        res.setGenre(movie.getGenre());
        res.setDuration(movie.getDuration());
        res.setReleaseDate(movie.getReleaseDate());
        res.setPoster(movie.getPoster());
        res.setTrailer(movie.getTrailer());
        res.setLanguage(movie.getLanguage());
        res.setAgeRestriction(movie.getAgeRestriction());
        res.setActive(movie.isActive());
        res.setCreatedAt(movie.getCreatedAt());
        res.setUpdatedAt(movie.getUpdatedAt());
        return res;
    }

}
