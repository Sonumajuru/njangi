package com.genesistech.njangiApi.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TokenRefreshResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
}