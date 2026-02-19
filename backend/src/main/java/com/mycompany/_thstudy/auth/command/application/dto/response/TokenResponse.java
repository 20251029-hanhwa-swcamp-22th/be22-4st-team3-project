package com.mycompany._thstudy.auth.command.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
}
