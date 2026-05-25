package com.lendledger.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lendledger.jwt")
public class JwtProperties {
    private String secret = "change-me-in-production-use-long-secret-key-32chars-min";
    private long accessExpirationMinutes = 15;
    private long refreshExpirationDays = 7;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getAccessExpirationMinutes() { return accessExpirationMinutes; }
    public void setAccessExpirationMinutes(long accessExpirationMinutes) { this.accessExpirationMinutes = accessExpirationMinutes; }
    public long getRefreshExpirationDays() { return refreshExpirationDays; }
    public void setRefreshExpirationDays(long refreshExpirationDays) { this.refreshExpirationDays = refreshExpirationDays; }
}
