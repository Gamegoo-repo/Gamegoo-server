package com.gamegoo.controller.board;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.domain.Board;
import com.gamegoo.dto.board.BoardRequest;
import com.gamegoo.dto.board.BoardResponse;
import com.gamegoo.service.board.BoardService;
import com.gamegoo.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/posts")
public class BoardController {
    private final BoardService boardService;

    @PostMapping("")
    public ApiResponse<BoardResponse.boardInsertResponseDTO> boardInsert(
            @RequestBody BoardRequest.boardInsertDTO request
            ){
        Long memberId = JWTUtil.getCurrentUserId();

        Board saveBoard = boardService.save(request,memberId);

        BoardResponse.boardInsertResponseDTO result = BoardResponse.boardInsertResponseDTO.builder()
                .boardId(saveBoard.getId())
                .memberId(memberId)
                .gameMode(saveBoard.getMode())
                .mainPosition(saveBoard.getMainPosition())
                .subPosition(saveBoard.getSubPosition())
                .wantPosition(saveBoard.getWantPosition())
                .voice(saveBoard.getVoice())
                .contents(saveBoard.getContent())
                .build();

        return ApiResponse.onSuccess(result);
    }
}
