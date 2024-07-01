package com.gamegoo.dto.member;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class PasswordRequestDTO {
    @NonNull
    private String password;
}
