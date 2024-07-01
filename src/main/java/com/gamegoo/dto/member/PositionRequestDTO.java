package com.gamegoo.dto.member;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PositionRequestDTO {
    @NonNull
    int mainP;
    @NotNull
    int subP;
}
