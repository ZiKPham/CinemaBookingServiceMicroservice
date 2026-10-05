package com.cinema.pay_service.controller.client;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.pay_service.service.MoMoPaymentService;
import com.cinema.pay_service.service.PaymentService;
import com.cinema.pay_service.util.error.IdInvalidException;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final MoMoPaymentService momoPaymentService; // 1. Khai báo service MoMo

    public PaymentController(PaymentService paymentService, MoMoPaymentService momoPaymentService) {
        this.paymentService = paymentService;
        this.momoPaymentService = momoPaymentService; // 2. Inject qua constructor
    }

    @GetMapping("/vn-pay")
    public ResponseEntity<String> getVNPayUrl(
            @RequestParam("bookingId") Long bookingId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            HttpServletRequest request) throws IdInvalidException {

        String paymentUrl = this.paymentService.createVNPayPayment(bookingId, userEmail, request);
        return ResponseEntity.ok(paymentUrl);
    }

    @GetMapping("/vn-pay-callback")
    public ResponseEntity<String> vnPayCallback(@RequestParam Map<String, String> queryParams)
            throws IdInvalidException {
        boolean isSuccess = this.paymentService.processCallback(queryParams);
        if (isSuccess) {
            return ResponseEntity.ok("Thanh toán thành công! Đơn hàng đã chuyển sang trạng thái PAID.");
        } else {
            return ResponseEntity.badRequest().body("Thanh toán thất bại hoặc bị hủy!");
        }
    }

    @PostMapping("/momo/{bookingId}")
    public ResponseEntity<String> createMoMoPayment(
            @PathVariable("bookingId") Long bookingId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) throws IdInvalidException {

        String payUrl = this.momoPaymentService.createMoMoPayment(bookingId, userEmail);
        return ResponseEntity.ok(payUrl);
    }

    @GetMapping("/momo/mock-payment-page")
    public ResponseEntity<String> momoMockPage(
            @RequestParam(value = "bookingId", required = false) Long bookingId,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam("amount") Double amount) {

        // Nếu truyền orderId dạng "12_1790910211226", bạn có thể tách lấy bookingId ở
        // đây nếu cần
        if (bookingId == null && orderId != null && orderId.contains("_")) {
            try {
                bookingId = Long.parseLong(orderId.split("_")[0]);
            } catch (Exception e) {
                bookingId = 0L;
            }
        }

        String htmlResponse = "<div style='text-align:center; margin-top:50px; font-family:Arial;'>"
                + "<h2>[MOMO SANDBOX MOCK] Thanh toán đơn hàng #" + (bookingId != null ? bookingId : orderId) + "</h2>"
                + "<p>Số tiền: <b>" + amount + " VND</b></p>"
                + "<a href='http://localhost:8080/api/v1/payments/momo-ipn?orderId="
                + (orderId != null ? orderId : bookingId + "_mock")
                + "&resultCode=0&message=Success' "
                + "style='padding: 10px 20px; background: #ae1f62; color: white; text-decoration: none; border-radius: 5px; font-weight: bold;'>Xác nhận thanh toán thành công</a>"
                + "&nbsp;&nbsp;&nbsp;"
                + "<a href='http://localhost:8080/api/v1/payments/momo-ipn?orderId="
                + (orderId != null ? orderId : bookingId + "_mock")
                + "&resultCode=1006&message=UserCancelled' "
                + "style='padding: 10px 20px; background: gray; color: white; text-decoration: none; border-radius: 5px;'>Hủy giao dịch</a>"
                + "</div>";

        return ResponseEntity.ok().body(htmlResponse);
    }

    @GetMapping("/momo-ipn")
    public ResponseEntity<String> momoIpn(
            @RequestParam("orderId") String orderId,
            @RequestParam("resultCode") int resultCode) throws IdInvalidException {

        boolean isSuccess = this.momoPaymentService.processMoMoCallback(orderId, resultCode);
        if (isSuccess) {
            return ResponseEntity.ok("Thanh toán MoMo thành công! Đơn hàng đã chuyển sang trạng thái PAID.");
        } else {
            return ResponseEntity.badRequest().body("Giao dịch MoMo bị hủy hoặc thất bại!");
        }
    }
}