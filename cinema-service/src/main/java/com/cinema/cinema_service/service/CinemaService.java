package com.cinema.cinema_service.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cinema.cinema_service.domain.Cinema;
import com.cinema.cinema_service.domain.request.ReqCreateCinemaDTO;
import com.cinema.cinema_service.domain.request.ReqUpdateCinemaDTO;
import com.cinema.cinema_service.domain.response.ResCinemaDTO;
import com.cinema.cinema_service.repository.CinemaRepository;
import com.cinema.cinema_service.util.error.IdInvalidException;
import com.cinema.cinema_service.util.error.NameInvalidException;

@Service
public class CinemaService {
    final private CinemaRepository cinemaRepository;

    public CinemaService(CinemaRepository cinemaRepository) {
        this.cinemaRepository = cinemaRepository;
    }

    public ResCinemaDTO handleCreateCinema(ReqCreateCinemaDTO reqDTO) {
        Cinema cinema = new Cinema();
        cinema.setName(reqDTO.getName());
        cinema.setAddress(reqDTO.getAddress());
        cinema.setDescription(reqDTO.getDescription());

        Cinema savedCinema = this.cinemaRepository.save(cinema);
        return this.convertToResCinemaDTO(savedCinema);
    }

    public List<ResCinemaDTO> fetchAllCinemas() {
        List<Cinema> cinemas = this.cinemaRepository.findAll();
        return cinemas.stream()
                .map(this::convertToResCinemaDTO)
                .collect(Collectors.toList());
    }

    public List<ResCinemaDTO> fetchCinemaByName(String name) throws NameInvalidException {
        List<Cinema> cinemas = this.cinemaRepository.findByNameContainingIgnoreCase(name);

        if (cinemas.isEmpty()) {
            throw new NameInvalidException("Cinema " + name + " không tồn tại");
        }

        return cinemas.stream()
                .map(this::convertToResCinemaDTO)
                .collect(Collectors.toList());
    }

    public ResCinemaDTO handleUpdaCinema(long id, ReqUpdateCinemaDTO reqDTO) throws IdInvalidException {
        Optional<Cinema> cOptional = this.cinemaRepository.findById(id);
        if (!cOptional.isPresent()) {
            throw new IdInvalidException("Cinema với id = " + id + " không tồn tại");
        }

        Cinema currentCinema = cOptional.get();

        if (reqDTO.getName() != null && !reqDTO.getName().trim().isEmpty()) {
            currentCinema.setName(reqDTO.getName());
        }
        if (reqDTO.getAddress() != null && !reqDTO.getAddress().trim().isEmpty()) {
            currentCinema.setAddress(reqDTO.getAddress());
        }
        if (reqDTO.getDescription() != null) {
            currentCinema.setDescription(reqDTO.getDescription());
        }

        Cinema updatedCinema = this.cinemaRepository.save(currentCinema);
        return this.convertToResCinemaDTO(updatedCinema);
    }

    public void handleDeteCinema(long id) throws IdInvalidException {
        Optional<Cinema> cOptional = this.cinemaRepository.findById(id);
        if (!cOptional.isPresent()) {
            throw new IdInvalidException("Cinema với id = " + id + " không tồn tại");
        }
        this.cinemaRepository.deleteById(id);
    }

    public ResCinemaDTO convertToResCinemaDTO(Cinema cinema) {
        ResCinemaDTO res = new ResCinemaDTO();
        res.setId(cinema.getId());
        res.setName(cinema.getName());
        res.setAddress(cinema.getAddress());
        res.setDescription(cinema.getDescription());
        res.setCreatedAt(cinema.getCreatedAt());
        res.setUpdatedAt(cinema.getUpdatedAt());
        return res;
    }
}
