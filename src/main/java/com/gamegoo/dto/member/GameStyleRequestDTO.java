package com.gamegoo.dto.member;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Setter
@Getter
public class GameStyleRequestDTO {
    @NotNull
    private List<String> gamestyle;
}
