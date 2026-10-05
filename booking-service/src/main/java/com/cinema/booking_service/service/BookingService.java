package com.cinema.booking_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.cinema.booking_service.client.CinemaClient;
import com.cinema.booking_service.client.ShowtimeClient;
import com.cinema.booking_service.client.UserClient;
import com.cinema.booking_service.domain.Booking;
import com.cinema.booking_service.domain.Ticket;
import com.cinema.booking_service.domain.request.BookingSearchCriteria;
import com.cinema.booking_service.domain.request.ReqBookingDTO;
import com.cinema.booking_service.domain.request.ReqHoldSeatDTO;
import com.cinema.booking_service.domain.response.ResBookingDTO;
import com.cinema.booking_service.domain.response.ResSeatDTO;
import com.cinema.booking_service.domain.response.ResSeatLockDetailDTO;
import com.cinema.booking_service.domain.response.ResShowtimeDTO;
import com.cinema.booking_service.domain.response.ResUserDTO;
import com.cinema.booking_service.domain.response.RestResponse;
import com.cinema.booking_service.domain.response.ResultPaginationDTO;
import com.cinema.booking_service.repository.BookingRepository;
import com.cinema.booking_service.repository.TicketRepository;
import com.cinema.booking_service.util.constant.BookingStatus;
import com.cinema.booking_service.util.error.IdInvalidException;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final CinemaClient cinemaClient;
    private final UserClient userClient;
    private final ShowtimeClient showtimeClient;

    public BookingService(BookingRepository bookingRepository, TicketRepository ticketRepository,
            CinemaClient cinemaClient, UserClient userClient, ShowtimeClient showtimeClient) {
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.cinemaClient = cinemaClient;
        this.userClient = userClient;
        this.showtimeClient = showtimeClient;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "user-bookings", key = "#userEmail"),
            @CacheEvict(value = "booking-detail", allEntries = true)
    })
    public ResBookingDTO handleCreateBooking(ReqBookingDTO req, String userEmail) throws IdInvalidException {
        // 0. (Tùy chọn) Kiểm tra suất chiếu có tồn tại không qua showtime-servic
        ResShowtimeDTO showtimeInfo = null;
        try {
            RestResponse<ResShowtimeDTO> response = showtimeClient.getShowtimeById(req.getShowtimeId());
            if (response != null && response.getData() != null) {
                showtimeInfo = response.getData(); // Lấy đúng đối tượng bên trong "data"
            }
        } catch (Exception e) {
            throw new IdInvalidException(
                    "Suất chiếu với ID " + req.getShowtimeId() + " không tồn tại hoặc hệ thống đang bận!");
        }

        if (showtimeInfo == null) {
            throw new IdInvalidException("Không tìm thấy thông tin suất chiếu!");
        }

        Long roomId = showtimeInfo.getRoom() == null ? null : showtimeInfo.getRoom().getId();
        if (roomId == null) {
            throw new IdInvalidException("Suất chiếu chưa được gán phòng chiếu!");
        }

        RestResponse<List<ResSeatDTO>> roomSeatsResponse = cinemaClient.getSeatsByRoomId(roomId);
        if (roomSeatsResponse == null || roomSeatsResponse.getData() == null) {
            throw new IdInvalidException("Không thể lấy danh sách ghế của phòng chiếu!");
        }

        List<Long> roomSeatIds = roomSeatsResponse.getData().stream()
                .map(ResSeatDTO::getId)
                .toList();
        for (Long seatId : req.getSeatIds()) {
            if (!roomSeatIds.contains(seatId)) {
                throw new IdInvalidException(
                        "Ghế " + seatId + " không thuộc phòng " + roomId + " của suất chiếu "
                                + req.getShowtimeId() + "!");
            }
        }

        // 1. Kiểm tra xem các ghế có đang bị ai giữ tạm thời bên cinema-service (Redis)
        // không
        // 1. Kiểm tra xem các ghế có đang bị ai giữ tạm thời bên cinema-service (Redis)
        // không
        for (Long seatId : req.getSeatIds()) {
            String holderId = null;
            try {
                RestResponse<ResSeatLockDetailDTO> response = cinemaClient.getSeatHolder(req.getShowtimeId(), seatId);
                if (response != null && response.getData() != null) {
                    holderId = response.getData().getUserEmail();
                }
            } catch (Exception e) {
                throw new IdInvalidException("Không thể kết nối tới hệ thống giữ ghế!");
            }

            // TRƯỜNG HỢP 2: Người dùng đặt thẳng không cần suy nghĩ (Ghế chưa có ai giữ)
            if (holderId == null) {
                try {
                    // Chuẩn bị request hold ghế tự động
                    ReqHoldSeatDTO holdRequest = new ReqHoldSeatDTO();
                    holdRequest.setShowtimeId(req.getShowtimeId());
                    holdRequest.setSeatIds(List.of(seatId)); // Giữ từng ghế hoặc gom lại tùy thiết kế endpoint /hold
                                                             // của bạn

                    // Gọi API hold ghế bên cinema-service kèm theo email người dùng
                    cinemaClient.holdSeats(userEmail, holdRequest);
                } catch (Exception e) {
                    // Nếu lúc gọi hold mà bị trùng lặp (có người khác vừa nhanh tay giữ mất)
                    throw new IdInvalidException(
                            "Ghế " + seatId + " vừa có người khác đặt hoặc không thể giữ lúc này!");
                }
            }
            // TRƯỜNG HỢP 1: Đã được giữ từ trước, kiểm tra xem có phải chính user này giữ
            // không
            else if (!holderId.equals(userEmail)) {
                throw new IdInvalidException("Ghế " + seatId + " đang được giữ bởi người dùng khác!");
            }
        }

        List<BookingStatus> activeStatuses = List.of(BookingStatus.PENDING, BookingStatus.PAID);
        List<Long> bookedSeatIds = this.ticketRepository.findBookedSeatIds(req.getShowtimeId(), req.getSeatIds(),
                activeStatuses);
        if (!bookedSeatIds.isEmpty()) {
            throw new IdInvalidException("Các ghế sau đã được đặt hoặc đang giữ chỗ: " + bookedSeatIds);
        }

        Booking booking = new Booking();
        booking.setUserEmail(userEmail);
        booking.setShowtimeId(req.getShowtimeId());
        booking.setPaymentMethod(req.getPaymentMethod());
        booking.setStatus(BookingStatus.PENDING);

        double totalPrice = 0;
        List<Ticket> tickets = new ArrayList<>();
        double ticketPrice = 0.0;
        if (showtimeInfo != null) {
            // Nếu showtimeInfo là RestResponse bọc ngoài, hãy đổi thành:
            // showtimeInfo.getData().getPrice()
            ticketPrice = showtimeInfo.getPrice();
        }

        for (Long seatId : req.getSeatIds()) {
            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setSeatId(seatId);
            ticket.setPrice(ticketPrice);
            ticket.setTicketCode("TK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

            totalPrice += ticketPrice;
            tickets.add(ticket);
        }

        booking.setTotalPrice(totalPrice);
        booking.setTickets(tickets);

        Booking savedBooking = this.bookingRepository.save(booking);

        for (Long seatId : req.getSeatIds()) {
            try {
                cinemaClient.unlockSeat(req.getShowtimeId(), seatId);
            } catch (Exception e) {
                System.err
                        .println("Không thể unlock ghế " + seatId + " trên Redis cho showtime " + req.getShowtimeId());
            }
        }

        return convertToResBookingDTO(savedBooking);
    }

    @Transactional
    public void updateBookingStatus(Long bookingId, String statusStr) throws IdInvalidException {
        Booking booking = this.bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy đơn hàng với ID: " + bookingId));

        BookingStatus newStatus = BookingStatus.valueOf(statusStr);
        booking.setStatus(newStatus);
        this.bookingRepository.save(booking);
    }

    @Cacheable(value = "user-bookings", key = "#p0")
    public List<ResBookingDTO> getMyBookings(String userEmail) throws IdInvalidException {
        if (userEmail == null || userEmail.isBlank()) {
            throw new IdInvalidException("Xác thực người dùng không hợp lệ");
        }
        List<Booking> bookings = this.bookingRepository.findByUserEmailOrderByIdDesc(userEmail);
        return bookings.stream().map(this::convertToResBookingDTO).collect(Collectors.toList());
    }

    public List<ResBookingDTO> getBookingHistoryByUser(String userEmail) throws IdInvalidException {
        if (userEmail == null || userEmail.isBlank()) {
            throw new IdInvalidException("Xác thực người dùng không hợp lệ");
        }

        List<Booking> bookings = this.bookingRepository.findByUserEmail(userEmail);
        return bookings.stream()
                .map(this::convertToResBookingDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "booking-detail", key = "#p0")
    public ResBookingDTO getBookingDetail(Long id, String userEmail) throws IdInvalidException {
        if (userEmail == null || userEmail.isBlank()) {
            throw new IdInvalidException("Xác thực người dùng không hợp lệ");
        }

        Booking booking = this.bookingRepository.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy đơn hàng hoặc bạn không có quyền xem"));

        return convertToResBookingDTO(booking);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "booking-detail", key = "#p0"),
            @CacheEvict(value = "user-bookings", allEntries = true)
    })
    public ResBookingDTO cancelBooking(Long id, String userEmail) throws IdInvalidException {
        if (userEmail == null || userEmail.isBlank()) {
            throw new IdInvalidException("Xác thực người dùng không hợp lệ");
        }

        Booking booking = this.bookingRepository.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy đơn hàng"));

        // Chỉ cho phép hủy khi đơn hàng còn đang ở trạng thái PENDING (chờ thanh toán)
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IdInvalidException("Chỉ có thể hủy các đơn hàng đang chờ thanh toán (PENDING)");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updatedBooking = this.bookingRepository.save(booking);

        return convertToResBookingDTO(updatedBooking);
    }

    public ResultPaginationDTO fetchAllBookings(BookingSearchCriteria criteria, Pageable pageable) {
        Specification<Booking> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            if (criteria.getPaymentMethod() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), criteria.getPaymentMethod()));
            }

            if (criteria.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), criteria.getUserId()));
            }

            if (criteria.getShowtimeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("showtimeId"), criteria.getShowtimeId()));
            }

            if (criteria.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), criteria.getFromDate()));
            }

            if (criteria.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), criteria.getToDate()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Booking> pageBooking = this.bookingRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageBooking.getTotalPages());
        mt.setTotal(pageBooking.getTotalElements());

        rs.setMeta(mt);

        List<ResBookingDTO> listBooking = pageBooking.getContent().stream()
                .map(this::convertToResBookingDTO).collect(Collectors.toList());

        rs.setResult(listBooking);
        return rs;
    }

    public ResBookingDTO getBookingDetailAdmin(Long id) throws IdInvalidException {
        Booking booking = this.bookingRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy đơn hàng với ID: " + id));
        return convertToResBookingDTO(booking);
    }

    private ResBookingDTO convertToResBookingDTO(Booking booking) {
        ResBookingDTO res = new ResBookingDTO();
        res.setId(booking.getId());
        res.setTotalPrice(booking.getTotalPrice());
        res.setStatus(booking.getStatus());
        res.setPaymentMethod(booking.getPaymentMethod());
        res.setCreatedAt(booking.getCreatedAt());

        // Lấy thông tin User thông qua OpenFeign từ user-service
        ResBookingDTO.UserSummary userSummary = new ResBookingDTO.UserSummary();
        userSummary.setEmail((booking.getUserEmail()));
        try {
            ResUserDTO userInfo = userClient.getUserByEmail(booking.getUserEmail());
            if (userInfo != null) {
                userSummary.setEmail(userInfo.getEmail());
            }
        } catch (Exception e) {
            System.err.println("Không thể lấy user info cho userId: " + booking.getUserEmail());
        }
        res.setUser(userSummary);

        ResBookingDTO.ShowtimeSummary showtimeSummary = new ResBookingDTO.ShowtimeSummary();
        showtimeSummary.setId(booking.getShowtimeId());
        try {
            RestResponse<ResShowtimeDTO> response = showtimeClient.getShowtimeById(booking.getShowtimeId());
            if (response != null && response.getData() != null) {
                ResShowtimeDTO showtimeInfo = response.getData();
                if (showtimeInfo.getMovie() != null) {
                    showtimeSummary.setMovieTitle(showtimeInfo.getMovie().getName());
                }
                if (showtimeInfo.getRoom() != null) {
                    showtimeSummary.setRoomName(showtimeInfo.getRoom().getName());
                }
            }

        } catch (Exception e) {
            System.err.println("Không thể lấy showtime info cho showtimeId: " + booking.getShowtimeId());
        }
        res.setShowtime(showtimeSummary);

        List<ResBookingDTO.TicketSummary> ticketSummaries = booking.getTickets().stream().map(t -> {
            ResBookingDTO.TicketSummary ts = new ResBookingDTO.TicketSummary();
            ts.setId(t.getId());
            ts.setSeatId(t.getSeatId());
            ts.setPrice(t.getPrice());
            ts.setTicketCode(t.getTicketCode());
            ts.setQrCode(t.getTicketCode()); // Gán mã QR chính là mã vé hoặc chuỗi định danh riêng
            return ts;
        }).toList();

        res.setTickets(ticketSummaries);
        return res;
    }
}
