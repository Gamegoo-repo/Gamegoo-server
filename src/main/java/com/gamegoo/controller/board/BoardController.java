package com.gamegoo.controller.board;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.board.Board;
import com.gamegoo.dto.board.BoardRequest;
import com.gamegoo.dto.board.BoardResponse;
import com.gamegoo.service.board.BoardService;
import com.gamegoo.service.member.ProfileService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/posts")
@Tag(name = "Board", description = "게시판 관련 API")
public class BoardController {
    private final ProfileService profileService;
    private final BoardService boardService;

    @PostMapping("")
    @Operation(summary = "게시판 글 작성 API", description = "게시판에서 글을 작성하는 API 입니다.")
    public ApiResponse<BoardResponse.boardInsertResponseDTO> boardInsert(
        @RequestBody BoardRequest.boardInsertDTO request
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        Member memberProfile = profileService.findMember(memberId);

        Board saveBoard = boardService.save(request,memberId);

        List<Long> gameStyles = saveBoard.getBoardGameStyles().stream()
            .map(boardGameStyle -> boardGameStyle.getGameStyle().getId())
            .collect(Collectors.toList());

        BoardResponse.boardInsertResponseDTO result = BoardResponse.boardInsertResponseDTO.builder()
                .boardId(saveBoard.getId())
                .memberId(memberId)
                .profileImage(memberProfile.getProfileImage())
                .gameName(memberProfile.getGameName())
                .tag(memberProfile.getTag())
                .tier(memberProfile.getTier())
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
    @Parameter(name = "postId", description = "수정할 게시판 글 id 입니다.")
    public ApiResponse<BoardResponse.boardUpdateResponseDTO> boardUpdate(
            @PathVariable long postId,
            @RequestBody BoardRequest.boardUpdateDTO request
        ){

        Long memberId = JWTUtil.getCurrentUserId();

        Member memberProfile = profileService.findMember(memberId);

        Board updateBoard = boardService.update(request, memberId, postId);

        List<Long> gameStyles = updateBoard.getBoardGameStyles().stream()
                .map(boardGameStyle -> boardGameStyle.getGameStyle().getId())
                .collect(Collectors.toList());

        BoardResponse.boardUpdateResponseDTO result= BoardResponse.boardUpdateResponseDTO.builder()
                .boardId(updateBoard.getId())
                .memberId(memberId)
                .profileImage(memberProfile.getProfileImage())
                .gameName(memberProfile.getGameName())
                .tag(memberProfile.getTag())
                .tier(memberProfile.getTier())
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

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시판 글 삭제 API", description = "게시판에서 글을 삭제하는 API 입니다.")
    @Parameter(name = "postId", description = "삭제할 게시판 글 id 입니다.")
    public ApiResponse<String> delete(@PathVariable Long postId
    ){
        Long memberId = JWTUtil.getCurrentUserId();

        boardService.delete(postId, memberId);

        return ApiResponse.onSuccess("게시글을 삭제하였습니다.");
    }

    @GetMapping("")
    public ApiResponse<List<BoardResponse.boardListResponseDTO>> boardList(@RequestParam(defaultValue = "1") int pageIdx){
        int pageSize = 20;
        List<BoardResponse.boardListResponseDTO> result = boardService.getBoardList(pageIdx, pageSize);
        return ApiResponse.onSuccess(result);
    }

}
