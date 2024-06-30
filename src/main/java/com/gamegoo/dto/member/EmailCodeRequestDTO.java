package com.gamegoo.dto.member;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class EmailCodeRequestDTO {
    @NotNull
    private String email;
    @NotNull
    private String code;
}
