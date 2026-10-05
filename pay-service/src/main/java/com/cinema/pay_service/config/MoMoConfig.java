package com.cinema.pay_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MoMoConfig {

    @Value("${momo.partner-code}")
    public String partnerCode;

    @Value("${momo.access-key}")
    public String accessKey;

    @Value("${momo.secret-key}")
    public String secretKey;

    @Value("${momo.endpoint}")
    public String endpoint;

    @Value("${momo.redirect-url}")
    public String redirectUrl;

    @Value("${momo.ipn-url}")
    public String ipnUrl;
}