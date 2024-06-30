package com.gamegoo.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RefreshTokenResponseDTO {
    String access_token;
    String refresh_token;
}
