package com.gamegoo.dto.member;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class JoinRequestDTO {
    @NotNull
    private String email;
    @NotNull
    private String password;

}
