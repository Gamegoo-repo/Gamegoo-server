package com.gamegoo.dto.member;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class EmailRequestDTO {
    @NotNull
    private String email;
}
