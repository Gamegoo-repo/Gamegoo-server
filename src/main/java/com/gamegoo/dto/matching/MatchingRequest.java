package com.gamegoo.dto.matching;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class MatchingRequest {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class InitializingMatchingRequestDTO {
        // 1: 빠른 대전, 2: 솔로 랭크, 3: 자유 랭크, 4: 칼바람 나락
        @NotNull(message = "gameMode는 비워둘 수 없습니다.")
        @Min(value = 1, message = "gameMode 값은 1이상이어야 합니다.")
        @Max(value = 4, message = "gameMode 값은 4이하이어야 합니다.")
        int gameMode;

        @NotNull(message = "mike는 비워둘 수 없습니다.")
        Boolean mike;

        // BASIC, PRECISE
        @NotBlank(message = "matching_type은 비워둘 수 없습니다.")
        String matchingType;

        @Min(value = 0, message = "메인 포지션의 값은 0이상이어야 합니다.")
        @Max(value = 5, message = "메인 포지션의 값은 5이하이어야 합니다.")
        int mainP;

        @Min(value = 0, message = "서브 포지션의 값은 0이상이어야 합니다.")
        @Max(value = 5, message = "서브 포지션의 값은 5이하이어야합니다.")
        int subP;

        @Min(value = 0, message = "원하는 상대 포지션의 값은 0이상이어야 합니다.")
        @Max(value = 5, message = "원하는 상대 포지션의 값은 5이하이어야합니다.")
        int wantP;

        @NotNull(message = "gameStyleIdList는 비워둘 수 없습니다")
        List<Long> gameStyleIdList;
    }

    @Getter
    @NoArgsConstructor
    public static class ModifyMatchingRequestDTO {
        @NotBlank(message = "status는 비워둘 수 없습니다")
        String status;
        @NotBlank(message = "gameMode는 비워둘 수 없습니다")
        Integer gameMode;
    }
}
