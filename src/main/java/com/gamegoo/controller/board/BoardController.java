package com.gamegoo.controller.board;

import com.gamegoo.apiPayload.ApiResponse;
import com.gamegoo.domain.member.Member;
import com.gamegoo.domain.board.Board;
import com.gamegoo.domain.member.Tier;
import com.gamegoo.dto.board.BoardRequest;
import com.gamegoo.dto.board.BoardResponse;
import com.gamegoo.service.board.BoardService;
import com.gamegoo.service.member.ProfileService;
import com.gamegoo.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
    @Operation(summary = "게시판 글 작성 API", description = "게시판에서 글을 작성하는 API 입니다. 게임 모드 1~4, 포지션 0~5를 입력하세요. 게임스타일은 최대 3개까지 입력가능합니다.")
    public ApiResponse<BoardResponse.boardInsertResponseDTO> boardInsert(
            @RequestBody BoardRequest.boardInsertDTO request
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        Member memberProfile = profileService.findMember(memberId);

        Board saveBoard = boardService.save(request, memberId, memberProfile);

        List<Long> gameStyles = saveBoard.getBoardGameStyles().stream()
                .map(boardGameStyle -> boardGameStyle.getGameStyle().getId())
                .collect(Collectors.toList());

        BoardResponse.boardInsertResponseDTO result = BoardResponse.boardInsertResponseDTO.builder()
                .boardId(saveBoard.getId())
                .memberId(memberId)
                .profileImage(saveBoard.getBoardProfileImage())
                .gameName(memberProfile.getGameName())
                .tag(memberProfile.getTag())
                .tier(memberProfile.getTier())
                .gameMode(saveBoard.getMode())
                .mainPosition(saveBoard.getMainPosition())
                .subPosition(saveBoard.getSubPosition())
                .wantPosition(saveBoard.getWantPosition())
                .mike(saveBoard.getMike())
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
    ) {

        Long memberId = JWTUtil.getCurrentUserId();

        Member memberProfile = profileService.findMember(memberId);

        Board updateBoard = boardService.update(request, memberId, postId);

        List<Long> gameStyles = updateBoard.getBoardGameStyles().stream()
                .map(boardGameStyle -> boardGameStyle.getGameStyle().getId())
                .collect(Collectors.toList());

        BoardResponse.boardUpdateResponseDTO result = BoardResponse.boardUpdateResponseDTO.builder()
                .boardId(updateBoard.getId())
                .memberId(memberId)
                .profileImage(updateBoard.getBoardProfileImage())
                .gameName(memberProfile.getGameName())
                .tag(memberProfile.getTag())
                .tier(memberProfile.getTier())
                .gameMode(updateBoard.getMode())
                .mainPosition(updateBoard.getMainPosition())
                .subPosition(updateBoard.getSubPosition())
                .wantPosition(updateBoard.getWantPosition())
                .mike(updateBoard.getMike())
                .gameStyles(gameStyles)
                .contents(updateBoard.getContent())
                .build();

        return ApiResponse.onSuccess(result);
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시판 글 삭제 API", description = "게시판에서 글을 삭제하는 API 입니다.")
    @Parameter(name = "postId", description = "삭제할 게시판 글 id 입니다.")
    public ApiResponse<String> delete(@PathVariable Long postId
    ) {
        Long memberId = JWTUtil.getCurrentUserId();

        boardService.delete(postId, memberId);

        return ApiResponse.onSuccess("게시글을 삭제하였습니다.");
    }

    @GetMapping("/list")
    @Operation(summary = "게시판 글 목록 조회 API", description = "게시판에서 글 목록을 조회하는 API 입니다.")
    @Parameters({@Parameter(name = "pageIdx", description = "조회할 페이지 번호를 입력해주세요. 페이지 당 20개의 게시물을 볼 수 있습니다."),
                @Parameter(name = "mode", description = "(선택) 게임 모드를 입력해주세요. < 빠른대전: 1, 솔로랭크: 2, 자유랭크: 3, 칼바람 나락: 4 >"),
                @Parameter(name = "tier", description = "(선택) 티어를 선택해주세요."),
                @Parameter(name = "mainPosition", description = "(선택) 포지션을 입력해주세요. < 전체: 0, 탑: 1, 정글: 2, 미드: 3, 바텀: 4, 서포터: 5 >"),
                @Parameter(name = "mike", description = "(선택) 마이크 여부를 선택해주세요.")})
    public ApiResponse<List<BoardResponse.boardListResponseDTO>> boardList(@RequestParam(defaultValue = "1") int pageIdx,
                                                                           @RequestParam(required = false) Integer mode,
                                                                           @RequestParam(required = false) Tier tier,
                                                                           @RequestParam(required = false) Integer mainPosition,
                                                                           @RequestParam(required = false) Boolean mike) {

        // <포지션 정보> 전체: 0, 탑: 1, 정글: 2, 미드: 3, 바텀: 4, 서포터: 5
        if (mainPosition != null && mainPosition == 0) {
            // 전체 포지션 선택 시 필터링에서 제외
            mainPosition = null;
        }

        List<BoardResponse.boardListResponseDTO> result = boardService.getBoardList(mode, tier, mainPosition, mike, pageIdx);
        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/list/{boardId}")
    @Operation(summary = "비회원용 게시판 글 조회 API", description = "게시판에서 글을 조회하는 API 입니다.")
    @Parameter(name = "boardId", description = "조회할 게시판 글 id 입니다.")
    public ApiResponse<BoardResponse.boardByIdResponseDTO> getBoardById(@PathVariable Long boardId) {

        BoardResponse.boardByIdResponseDTO result = boardService.getBoardById(boardId);

        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/member/list/{boardId}")
    @Operation(summary = "회원용 게시판 글 조회 API", description = "게시판에서 글을 조회하는 API 입니다.")
    @Parameter(name = "boardId", description = "조회할 게시판 글 id 입니다.")
    public ApiResponse<BoardResponse.boardByIdResponseForMemberDTO> getBoardByIdForMember(@PathVariable Long boardId) {

        Long memberId = JWTUtil.getCurrentUserId();

        BoardResponse.boardByIdResponseForMemberDTO result = boardService.getBoardByIdForMember(boardId, memberId);

        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/my")
    @Operation(summary = "내가 작성한 게시판 글 목록 조회 API", description = "내가 작성한 게시판 글을 조회하는 API 입니다.")
    @Parameter(name = "pageIdx", description = "조회할 페이지 번호를 입력해주세요.")
    public ApiResponse<List<BoardResponse.myBoardListResponseDTO>> getMyBoardList(@RequestParam(defaultValue = "1") int pageIdx) {

        Long memberId = JWTUtil.getCurrentUserId();

        List<BoardResponse.myBoardListResponseDTO> result = boardService.getMyBoardList(memberId, pageIdx);

        return ApiResponse.onSuccess(result);
    }
}


