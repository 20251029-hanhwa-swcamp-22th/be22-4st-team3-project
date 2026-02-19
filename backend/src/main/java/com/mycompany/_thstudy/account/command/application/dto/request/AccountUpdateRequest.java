package com.mycompany._thstudy.account.command.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountUpdateRequest {

    @NotBlank(message = "계좌명은 필수입니다.")
    @Size(max = 100, message = "계좌명은 100자 이하여야 합니다.")
    private String name;

    @NotNull(message = "잔액은 필수입니다.")
    @Min(value = 0, message = "잔액은 0 이상이어야 합니다.")
    private Long balance;
}
