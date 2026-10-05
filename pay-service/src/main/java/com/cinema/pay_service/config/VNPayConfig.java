package com.cinema.pay_service.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class VNPayConfig {

    @Value("${vnpay.pay-url}")
    public String vnp_PayUrl;

    @Value("${vnpay.return-url}")
    public String vnp_ReturnUrl;

    @Value("${vnpay.tmn-code}")
    public String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    public String secretKey;

    @Value("${vnpay.api-url}")
    public String vnp_ApiUrl;

    public String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null || ipAdress.isEmpty() || "unknown".equalsIgnoreCase(ipAdress)) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "127.0.0.1";
        }
        return ipAdress;
    }

    public String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Xác thực chữ ký trả về từ VNPay
     */
    public boolean validateSignature(Map<String, String> params) {
        // Lấy ra mã bảo mật vnp_SecureHash từ params trả về
        if (!params.containsKey("vnp_SecureHash")) {
            return false;
        }
        String vnp_SecureHash = params.get("vnp_SecureHash");

        // Loại bỏ các trường hash không liên quan hoặc rỗng trước khi băm lại
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        // Sắp xếp các tham số theo thứ tự alphabet giống như lúc tạo URL
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (Exception e) {
                    // Xử lý ngoại lệ encoding nếu có
                }
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        // Tạo lại mã băm HMAC SHA512 từ secretKey và chuỗi dữ liệu vừa gom
        String calculatedHash = hmacSHA512(secretKey, hashData.toString());

        // So sánh chữ ký tính toán được với chữ ký VNPay gửi về (không phân biệt hoa
        // thường hoặc dùng equals)
        return calculatedHash.equalsIgnoreCase(vnp_SecureHash);
    }
}