package com.gamegoo.service.board;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.BoardHandler;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.board.Board;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.board.BoardGameStyle;
import com.gamegoo.domain.champion.MemberChampion;
import com.gamegoo.domain.gamestyle.GameStyle;
import com.gamegoo.dto.board.BoardRequest;
import com.gamegoo.dto.board.BoardResponse;
import com.gamegoo.repository.board.BoardGameStyleRepository;
import com.gamegoo.repository.board.BoardRepository;
import com.gamegoo.repository.member.GameStyleRepository;
import com.gamegoo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final GameStyleRepository gameStyleRepository;
    private final BoardGameStyleRepository boardGameStyleRepository;

    // 게시판 글 작성.
    @Transactional
    public Board save(BoardRequest.boardInsertDTO request,Long memberId){

        Member member = memberRepository.findById(memberId).orElseThrow(()->new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 게임 모드 값 검증. (1 ~ 4 값만 가능)
        if (request.getGameMode()<1 || request.getGameMode()>4){
            throw new BoardHandler(ErrorStatus.GAME_MODE_INVALID);
        }

        // 주 포지션 값 검증. (1 ~ 5값만 가능)
        if (request.getMainPosition()<1 || request.getMainPosition()>5){
            throw new BoardHandler(ErrorStatus.MAIN_POSITION_INVALID);
        }

        // 부 포지션 값 검증. (1 ~ 5값만 가능)
        if (request.getSubPosition()<1 || request.getSubPosition()>5){
            throw new BoardHandler(ErrorStatus.SUB_POSITION_INVALID);
        }

        // 상대 포지션 값 검증. (1 ~ 5값만 가능)
        if (request.getWantPosition()<1 || request.getWantPosition()>5){
            throw new BoardHandler(ErrorStatus.WANT_POSITION_INVALID);
        }

        // 마이크 설정 (default=false)
        if (request.getVoice()==null){
            request.setVoice(false);
        }

        // 게임 스타일 길이 검증.
        if (request.getGameStyles().size() > 3) {
            throw new BoardHandler(ErrorStatus.BOARD_GAME_STYLE_BAD_REQUEST);
        }

        // 게임 스타일 실제 존재 여부 검증.
        List<GameStyle> gameStyleList = request.getGameStyles().stream()
                .map(gameStyleId -> gameStyleRepository.findById(gameStyleId)
                        .orElseThrow(() -> new BoardHandler(ErrorStatus._BAD_REQUEST)))
                .collect(Collectors.toList());

        Board board = Board.builder()
                .mode(request.getGameMode())
                .mainPosition(request.getMainPosition())
                .subPosition(request.getSubPosition())
                .wantPosition(request.getWantPosition())
                .voice(request.getVoice())
                .boardGameStyles(new ArrayList<>())
                .content(request.getContents())
                .build();

        board.setMember(member);
        Board saveBoard= boardRepository.save(board);

        // BoardGameStyle 엔티티 생성 및 연관관계 매핑.
        gameStyleList.forEach(gameStyle -> {
            BoardGameStyle boardGameStyle = BoardGameStyle.builder()
                    .gameStyle(gameStyle)
                    .build();

            boardGameStyle.setBoard(saveBoard);
            boardGameStyleRepository.save(boardGameStyle);
        });

        return saveBoard;
    }

    // 게시판 글 수정.
    @Transactional
    public Board update(BoardRequest.boardUpdateDTO request, Long memberId, Long boardId) {

        Board board = boardRepository.findById(boardId).orElseThrow(() -> new BoardHandler(ErrorStatus.BOARD_NOT_FOUND));

        // 게시글 작성자가 맞는지 검증.
        if (!board.getMember().getId().equals(memberId)) {
            throw new BoardHandler(ErrorStatus.BOARD_UNAUTHORIZED);
        }

        // 게임 모드 값 검증. (1 ~ 4 값만 가능)
        if (request.getGameMode() < 1 || request.getGameMode() > 4) {
            throw new BoardHandler(ErrorStatus.GAME_MODE_INVALID);
        }

        // 주 포지션 값 검증. (1 ~ 5값만 가능)
        if (request.getMainPosition() < 1 || request.getMainPosition() > 5) {
            throw new BoardHandler(ErrorStatus.MAIN_POSITION_INVALID);
        }

        // 부 포지션 값 검증. (1 ~ 5값만 가능)
        if (request.getSubPosition() < 1 || request.getSubPosition() > 5) {
            throw new BoardHandler(ErrorStatus.SUB_POSITION_INVALID);
        }

        // 상대 포지션 값 검증. (1 ~ 5값만 가능)
        if (request.getWantPosition() < 1 || request.getWantPosition() > 5) {
            throw new BoardHandler(ErrorStatus.WANT_POSITION_INVALID);
        }

        // 게임 스타일 길이 검증.
        if (request.getGameStyles().size() > 3) {
            throw new BoardHandler(ErrorStatus.BOARD_GAME_STYLE_BAD_REQUEST);
        }

        // 게임 스타일 실제 존재 여부 검증.
        List<GameStyle> gameStyleList = request.getGameStyles().stream()
                .map(gameStyleId -> gameStyleRepository.findById(gameStyleId)
                        .orElseThrow(() -> new BoardHandler(ErrorStatus._BAD_REQUEST)))
                .collect(Collectors.toList());

        // 마이크 설정 (null인 경우 기본값 false)
        if (request.getVoice() == null) {
            request.setVoice(false);
        }

        // 게시판 글 데이터 수정
        board.updateBoard(
                request.getGameMode(),
                request.getMainPosition(),
                request.getSubPosition(),
                request.getWantPosition(),
                request.getVoice(),
                request.getContents()
        );

        // 기존 BoardGameStyle 엔티티 업데이트
        Map<Long, BoardGameStyle> existingGameStyles = board.getBoardGameStyles().stream()
                .collect(Collectors.toMap(
                        boardGameStyle -> boardGameStyle.getGameStyle().getId(),
                        boardGameStyle -> boardGameStyle,
                        (existing, replacement) -> existing));

        Set<Long> newGameStyleIds = gameStyleList.stream()
                .map(GameStyle::getId)
                .collect(Collectors.toSet());

        // 삭제할 엔티티를 검색
        List<BoardGameStyle> toRemove = new ArrayList<>();
        for (BoardGameStyle existingStyle : board.getBoardGameStyles()) {
            if (!newGameStyleIds.contains(existingStyle.getGameStyle().getId())) {
                toRemove.add(existingStyle);
            }
        }
        toRemove.forEach(board::removeBoardGameStyle);

        // 새로 추가하거나 업데이트할 엔티티
        for (GameStyle gameStyle : gameStyleList) {
            BoardGameStyle boardGameStyle = existingGameStyles.get(gameStyle.getId());
            if (boardGameStyle == null) {
                boardGameStyle = BoardGameStyle.builder()
                        .gameStyle(gameStyle)
                        .build();
                // 연관관계 메소드 사용
                boardGameStyle.setBoard(board);
            } else {
                // 기존 엔티티 업데이트
                boardGameStyle.setGameStyle(gameStyle);
            }
        }

        return boardRepository.save(board);
    }

    // 게시판 글 삭제.
    @Transactional
    public void delete(Long boardId, Long memberId){

        Board board = boardRepository.findById(boardId).orElseThrow(() -> new BoardHandler(ErrorStatus.BOARD_NOT_FOUND));

        // 게시글 작성자가 맞는지 검증.
        if (!board.getMember().getId().equals(memberId)) {
            throw new BoardHandler(ErrorStatus.BOARD_DELETE_UNAUTHORIZED);
        }

        boardRepository.delete(board);
    }

    // 게시판 글 목록 조회
    public List<BoardResponse.boardListResponseDTO> getBoardList(int pageIdx, int pageSize){

        // 사용자로부터 받은 pageIdx를 1 감소 -> pageIdx=1 일 때, 1 페이지.
        Pageable pageable = PageRequest.of(pageIdx-1, pageSize);
        List<Board> boards = boardRepository.findAll(pageable).getContent();

        return boards.stream().map(board -> {
            Member member = board.getMember();

            return BoardResponse.boardListResponseDTO.builder()
                    .boardId(board.getId())
                    .memberId(member.getId())
                    .profileImage(member.getProfileImage())
                    .gameName(member.getGameName())
                    .mannerLevel(member.getMannerLevel())
                    .tier(member.getTier())
                    .gameMode(board.getMode())
                    .mainPosition(board.getMainPosition())
                    .subPosition(board.getSubPosition())
                    .wantPosition(board.getWantPosition())
                    .championList(member.getMemberChampionList().stream().map(MemberChampion::getId).collect(Collectors.toList()))
                    .winRate(member.getWinRate())
                    .createdAt(board.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        }
}
