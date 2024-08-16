package com.gamegoo.dto.matching;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberPriority {
    private Long memberId;
    private int priorityValue;
}
