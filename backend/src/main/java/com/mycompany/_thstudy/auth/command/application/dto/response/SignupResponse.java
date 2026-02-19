package com.mycompany._thstudy.auth.command.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SignupResponse {

    private Long id;
    private String email;
    private String nickname;
}
