package com.nicasia.cyberintel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private Long expiration;
    
    // Getters
    public String getSecret() { 
        return secret; 
    }
    
    public Long getExpiration() { 
        return expiration; 
    }
    
    // Setters
    public void setSecret(String secret) { 
        this.secret = secret; 
    }
    
    public void setExpiration(Long expiration) { 
        this.expiration = expiration; 
    }
}