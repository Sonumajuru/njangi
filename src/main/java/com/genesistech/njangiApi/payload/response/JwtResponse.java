package com.genesistech.njangiApi.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class JwtResponse {
    private Long id;
    private String accessToken;
    private String type = "Bearer";
    private String refreshToken;
    private String email;
    private Instant expiryDate;
    private List<String> roles;
}