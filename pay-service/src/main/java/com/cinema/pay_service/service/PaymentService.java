package com.cinema.pay_service.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinema.pay_service.client.BookingClient;
import com.cinema.pay_service.config.VNPayConfig;
import com.cinema.pay_service.domain.Payment;
import com.cinema.pay_service.domain.response.ResBookingDTO;
import com.cinema.pay_service.domain.response.RestResponse;
import com.cinema.pay_service.repository.PaymentRepository;
import com.cinema.pay_service.util.constant.BookingStatus;
import com.cinema.pay_service.util.constant.PaymentMethod;
import com.cinema.pay_service.util.constant.PaymentStatus;
import com.cinema.pay_service.util.error.IdInvalidException;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class PaymentService {

    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;
    private final QRCodeService qrCodeService;
    private final BookingClient bookingClient;

    public PaymentService(VNPayConfig vnPayConfig,
            PaymentRepository paymentRepository,
            EmailService emailService,
            QRCodeService qrCodeService,
            BookingClient bookingClient) {
        this.vnPayConfig = vnPayConfig;
        this.paymentRepository = paymentRepository;
        this.emailService = emailService;
        this.qrCodeService = qrCodeService;
        this.bookingClient = bookingClient;
    }

    /**
     * Tạo URL thanh toán VNPay Sandbox và lưu bản ghi Payment trạng thái PENDING
     */
    public String createVNPayPayment(Long bookingId, String userEmail, HttpServletRequest request)
            throws IdInvalidException {
        RestResponse<ResBookingDTO> response = bookingClient.getBookingById(bookingId, userEmail);

        if (response == null || response.getData() == null) {
            throw new IdInvalidException("Không tìm thấy thông tin đơn hàng với ID: " + bookingId);
        }

        ResBookingDTO bookingInfo = response.getData();
        if (BookingStatus.PAID.name().equals(bookingInfo.getStatus()) ||
                BookingStatus.REFUNDED.name().equals(bookingInfo.getStatus())) {
            throw new IdInvalidException("Đơn hàng này đã được thanh toán thành công trước đó!");
        }
        if (BookingStatus.CANCELLED.name().equals(bookingInfo.getStatus())) {
            throw new IdInvalidException("Đơn hàng này đã bị hủy, không thể tiếp tục thanh toán!");
        }

        Double amount = bookingInfo.getTotalPrice();

        System.out.println("=== VNPAY TMN CODE CURRENTLY USED: " + vnPayConfig.vnp_TmnCode + " ===");

        // 1. Tạo bản ghi Payment ở trạng thái PENDING trước khi chuyển hướng sang VNPay
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserEmail(userEmail);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.VNPAY);
        String txnRef = bookingId + "_" + vnPayConfig.getRandomNumber(4);
        payment.setTransactionCode(txnRef);
        payment.setStatus(PaymentStatus.PENDING);
        this.paymentRepository.save(payment);

        long vnpAmount = (long) (amount * 100);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnpAmount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang booking ID: " + bookingId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnPayConfig.getIpAddress(request));

        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        vnp_Params.put("vnp_CreateDate", now.format(formatter));
        vnp_Params.put("vnp_ExpireDate", now.plusMinutes(15).format(formatter));

        // Sắp xếp các tham số theo thứ tự alphabet để tạo chữ ký
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnPayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    /**
     * Xử lý kết quả trả về từ VNPay (Callback / IPN)
     */
    @Transactional
    @CacheEvict(value = "revenue-stats", allEntries = true)
    public boolean processCallback(Map<String, String> queryParams) throws IdInvalidException {
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        String vnp_TxnRef = queryParams.get("vnp_TxnRef");

        // 1. Kiểm tra chữ ký bảo mật từ VNPay
        boolean checkSecureHash = vnPayConfig.validateSignature(queryParams);
        if (!checkSecureHash) {
            throw new IdInvalidException("Dữ liệu không hợp lệ hoặc chữ ký không khớp (Invalid Signature)");
        }

        if (vnp_TxnRef == null || vnp_TxnRef.isEmpty()) {
            throw new IdInvalidException("Mã tham chiếu giao dịch không hợp lệ");
        }

        Payment payment = this.paymentRepository.findByTransactionCode(vnp_TxnRef)
                .orElseThrow(() -> new IdInvalidException("Giao dịch không tồn tại với mã: " + vnp_TxnRef));

        // Idempotency: Nếu thanh toán đã được xử lý trước đó rồi thì trả về kết quả
        // luôn
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return payment.getStatus() == PaymentStatus.SUCCESS;
        }

        if ("00".equals(vnp_ResponseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            this.paymentRepository.save(payment);

            // --- LOGIC GỬI EMAIL & MÃ QR SAU KHI THANH TOÁN THÀNH CÔNG ---
            try {
                bookingClient.updateBookingStatus(payment.getBookingId(), "PAID");
            } catch (Exception e) {
                System.err.println("Không thể cập nhật trạng thái booking sang PAID: " + e.getMessage());
            }

            try {
                byte[] qrCode = qrCodeService.generateQRCodeImage("BOOKING_" + payment.getBookingId(), 250, 250);

                String emailContent = "<h3>Cảm ơn bạn đã đặt vé tại Cinema!</h3>"
                        + "<p>Mã đơn hàng: <b>" + payment.getBookingId() + "</b></p>"
                        + "<p>Mã giao dịch: <b>" + payment.getTransactionCode() + "</b></p>"
                        + "<p>Tổng tiền: <b>" + payment.getAmount() + " VND</b></p>"
                        + "<p>Vui lòng xuất trình mã QR đính kèm khi đến rạp.</p>";

                // Lưu ý: Nếu bạn có user email riêng hoặc truyền qua thông tin, ở đây dùng tạm
                // thông tin giao dịch
                // emailService.sendBookingConfirmationEmail(...);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            this.paymentRepository.save(payment);

            try {
                bookingClient.updateBookingStatus(payment.getBookingId(), "CANCELLED");
            } catch (Exception e) {
                System.err.println("Không thể cập nhật trạng thái booking sang CANCELLED: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Thống kê doanh thu từ các giao dịch thành công trong pay-service
     */
    @Cacheable(value = "revenue-stats", key = "#startDate + '_' + #endDate")
    public Map<String, Object> getRevenueStatistics(String startDate, String endDate) {
        List<Payment> successPayments = this.paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .collect(Collectors.toList());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Lọc theo startDate nếu có
        if (startDate != null && !startDate.isEmpty()) {
            Instant startInstant = LocalDate.parse(startDate, dateFormatter)
                    .atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh"))
                    .toInstant();
            successPayments = successPayments.stream()
                    .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isBefore(startInstant))
                    .collect(Collectors.toList());
        }

        // Lọc theo endDate nếu có
        if (endDate != null && !endDate.isEmpty()) {
            Instant endInstant = LocalDate.parse(endDate, dateFormatter)
                    .atTime(23, 59, 59)
                    .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                    .toInstant();
            successPayments = successPayments.stream()
                    .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isAfter(endInstant))
                    .collect(Collectors.toList());
        }

        double totalRevenue = successPayments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        long totalPaidBookings = successPayments.size();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalRevenue", totalRevenue);
        statistics.put("totalPaidBookings", totalPaidBookings);
        statistics.put("details", successPayments);

        return statistics;
    }
}