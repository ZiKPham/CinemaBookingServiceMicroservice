package com.cinema.pay_service.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinema.pay_service.client.BookingClient;
import com.cinema.pay_service.domain.Payment;
import com.cinema.pay_service.domain.response.ResBookingDTO;
import com.cinema.pay_service.domain.response.RestResponse;
import com.cinema.pay_service.repository.PaymentRepository;
import com.cinema.pay_service.util.constant.BookingStatus;
import com.cinema.pay_service.util.constant.PaymentMethod;
import com.cinema.pay_service.util.constant.PaymentStatus;
import com.cinema.pay_service.util.error.IdInvalidException;

@Service
public class MoMoPaymentService {

    private final PaymentRepository paymentRepository;
    private final EmailService emailService;
    private final QRCodeService qrCodeService;
    private final BookingClient bookingClient; //

    public MoMoPaymentService(PaymentRepository paymentRepository,
            EmailService emailService,
            QRCodeService qrCodeService,
            BookingClient bookingClient) {
        this.paymentRepository = paymentRepository;
        this.emailService = emailService;
        this.qrCodeService = qrCodeService;
        this.bookingClient = bookingClient;
    }

    /**
     * Tạo URL thanh toán MoMo Sandbox / Mock page
     */
    public String createMoMoPayment(Long bookingId, String userEmail) throws IdInvalidException {
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

        // 1. Tạo bản ghi Payment ở trạng thái PENDING trước khi chuyển hướng
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserEmail(userEmail);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.MOMO);

        String orderId = bookingId + "_" + System.currentTimeMillis();
        payment.setTransactionCode(orderId);
        payment.setStatus(PaymentStatus.PENDING);
        this.paymentRepository.save(payment);

        // Trả về thẳng URL trang Mock nội bộ để test cực kỳ mượt mà
        return "http://localhost:8080/api/v1/payments/momo/mock-payment-page?orderId=" + orderId + "&amount=" + amount;
    }

    /**
     * Hàm tính chữ ký HMAC SHA256 cho MoMo
     */
    private String hmacSHA256(String key, String data) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hash.append('0');
                hash.append(hex);
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa chữ ký MoMo", e);
        }
    }

    @Transactional
    @CacheEvict(value = "revenue-stats", allEntries = true)
    public boolean processMoMoCallback(String orderId, int resultCode) throws IdInvalidException {
        Payment payment = this.paymentRepository.findByTransactionCode(orderId)
                .orElseThrow(() -> new IdInvalidException("Giao dịch không tồn tại với mã: " + orderId));

        // Idempotency: Kiểm tra nếu đã thanh toán rồi thì trả về kết quả cũ
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return payment.getStatus() == PaymentStatus.SUCCESS;
        }

        if (resultCode == 0) {
            payment.setStatus(PaymentStatus.SUCCESS);
            this.paymentRepository.save(payment);
            // ---> GỌI BOOKING SERVICE ĐỂ CẬP NHẬT TRẠNG THÁI SANG PAID <---
            try {
                bookingClient.updateBookingStatus(payment.getBookingId(), "PAID");
            } catch (Exception e) {
                System.err.println("Không thể cập nhật trạng thái booking sang PAID qua MoMo: " + e.getMessage());
            }

            // --- LOGIC SINH MÃ QR VÀ GỬI EMAIL XÁC NHẬN ---
            try {
                byte[] qrCode = qrCodeService.generateQRCodeImage("BOOKING_" + payment.getBookingId(), 250, 250);

                String emailContent = "<h3>Cảm ơn bạn đã đặt vé tại Cinema qua MoMo!</h3>"
                        + "<p>Mã đơn hàng: <b>" + payment.getBookingId() + "</b></p>"
                        + "<p>Mã giao dịch: <b>" + payment.getTransactionCode() + "</b></p>"
                        + "<p>Tổng tiền: <b>" + payment.getAmount() + " VND</b></p>"
                        + "<p>Vui lòng xuất trình mã QR đính kèm khi đến rạp.</p>";

                // emailService.sendBookingConfirmationEmail(...);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            this.paymentRepository.save(payment);

            // ---> GỌI BOOKING SERVICE ĐỂ CẬP NHẬT TRẠNG THÁI SANG CANCELLED <---
            try {
                bookingClient.updateBookingStatus(payment.getBookingId(), "CANCELLED");
            } catch (Exception e) {
                System.err.println("Không thể cập nhật trạng thái booking sang CANCELLED qua MoMo: " + e.getMessage());
            }

            return false;
        }
    }
}