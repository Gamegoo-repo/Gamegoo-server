package com.gamegoo.controller.board;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.domain.Board;
import com.gamegoo.dto.board.BoardRequest;
import com.gamegoo.dto.board.BoardResponse;
import com.gamegoo.service.board.BoardService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Board", description = "게시판 관련 API")
public class BoardController {
    private final BoardService boardService;

    @PostMapping("")
    @Operation(summary = "게시판 글 작성 API", description = "게시판에서 글을 작성하는 API 입니다.")
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
