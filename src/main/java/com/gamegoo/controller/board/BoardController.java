package com.gamegoo.controller.board;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.domain.board.Board;
import com.gamegoo.dto.board.BoardRequest;
import com.gamegoo.dto.board.BoardResponse;
import com.gamegoo.service.board.BoardService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

        List<Long> gameStyles = saveBoard.getBoardGameStyles().stream()
                .map(boardGameStyle -> boardGameStyle.getGameStyle().getId())
                .collect(Collectors.toList());

        BoardResponse.boardInsertResponseDTO result = BoardResponse.boardInsertResponseDTO.builder()
                .boardId(saveBoard.getId())
                .memberId(memberId)
                .gameMode(saveBoard.getMode())
                .mainPosition(saveBoard.getMainPosition())
                .subPosition(saveBoard.getSubPosition())
                .wantPosition(saveBoard.getWantPosition())
                .voice(saveBoard.getVoice())
                .gameStyles(gameStyles)
                .contents(saveBoard.getContent())
                .build();

        return ApiResponse.onSuccess(result);
    }

    @PutMapping("/{postId}")
    @Operation(summary = "게시판 글 수정 API", description = "게시판에서 글을 수정하는 API 입니다.")
    public ApiResponse<BoardResponse.boardUpdateResponseDTO> boardUpdate(
            @PathVariable long postId,
            @RequestBody BoardRequest.boardUpdateDTO request
        ){

        Long memberId = JWTUtil.getCurrentUserId();

        Board updateBoard = boardService.update(request, memberId, postId);

        List<Long> gameStyles = updateBoard.getBoardGameStyles().stream()
                .map(boardGameStyle -> boardGameStyle.getGameStyle().getId())
                .collect(Collectors.toList());

        BoardResponse.boardUpdateResponseDTO result= BoardResponse.boardUpdateResponseDTO.builder()
                .boardId(updateBoard.getId())
                .memberId(memberId)
                .gameMode(updateBoard.getMode())
                .mainPosition(updateBoard.getMainPosition())
                .subPosition(updateBoard.getSubPosition())
                .wantPosition(updateBoard.getWantPosition())
                .voice(updateBoard.getVoice())
                .gameStyles(gameStyles)
                .contents(updateBoard.getContent())
                .build();

        return ApiResponse.onSuccess(result);
    }
}
