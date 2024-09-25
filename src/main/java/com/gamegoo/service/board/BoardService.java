package com.gamegoo.service.board;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.BoardHandler;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.apiPayload.exception.handler.PageHandler;
import com.gamegoo.domain.board.Board;
import com.gamegoo.domain.board.BoardGameStyle;
import com.gamegoo.domain.gamestyle.GameStyle;
import com.gamegoo.domain.member.Member;
import com.gamegoo.domain.member.Tier;
import com.gamegoo.dto.board.BoardRequest;
import com.gamegoo.dto.board.BoardResponse;
import com.gamegoo.dto.manner.MannerResponse;
import com.gamegoo.dto.member.MemberResponse;
import com.gamegoo.repository.board.BoardGameStyleRepository;
import com.gamegoo.repository.board.BoardRepository;
import com.gamegoo.repository.member.GameStyleRepository;
import com.gamegoo.repository.member.MemberRepository;
import com.gamegoo.service.manner.MannerService;
import com.gamegoo.service.member.FriendService;
import com.gamegoo.util.MemberUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Autowired
    private FriendService friendService;
    @Autowired
    private MannerService mannerService;
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final GameStyleRepository gameStyleRepository;
    private final BoardGameStyleRepository boardGameStyleRepository;

    private final static int PAGE_SIZE = 20;  // 페이지당 표시할 게시물 수

    // 게시판 글 작성.
    @Transactional
    public Board save(BoardRequest.boardInsertDTO request, Long memberId, Member memberProfile) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 게임 모드 값 검증. (1 ~ 4 값만 가능)
        if (request.getGameMode() < 1 || request.getGameMode() > 4) {
            throw new BoardHandler(ErrorStatus.GAME_MODE_INVALID);
        }

        // 주 포지션 값 검증. (0 ~ 5값만 가능)
        if (request.getMainPosition() < 0 || request.getMainPosition() > 5) {
            throw new BoardHandler(ErrorStatus.MAIN_POSITION_INVALID);
        }

        // 부 포지션 값 검증. (0 ~ 5값만 가능)
        if (request.getSubPosition() < 0 || request.getSubPosition() > 5) {
            throw new BoardHandler(ErrorStatus.SUB_POSITION_INVALID);
        }

        // 상대 포지션 값 검증. (0 ~ 5값만 가능)
        if (request.getWantPosition() < 0 || request.getWantPosition() > 5) {
            throw new BoardHandler(ErrorStatus.WANT_POSITION_INVALID);
        }

        // 마이크 설정 (default=false)
        if (request.getMike() == null) {
            request.setMike(false);
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

        // 게시판 글 작성 - 프로필 이미지 수정 여부 검증.
        Integer boardProfileImage;
        if (request.getBoardProfileImage() != null) {
            boardProfileImage = request.getBoardProfileImage();
        } else {
            // 프로필 페이지에 있는 프로필 이미지 정보 가져오기.
            boardProfileImage = memberProfile.getProfileImage();
        }

        Board board = Board.builder()
                .mode(request.getGameMode())
                .mainPosition(request.getMainPosition())
                .subPosition(request.getSubPosition())
                .wantPosition(request.getWantPosition())
                .mike(request.getMike())
                .boardGameStyles(new ArrayList<>())
                .content(request.getContents())
                .boardProfileImage(boardProfileImage)
                .build();

        board.setMember(member);

        board.setDeleted(false);
        
        Board saveBoard = boardRepository.save(board);

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

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardHandler(ErrorStatus.BOARD_NOT_FOUND));

        // 게시글 작성자가 맞는지 검증.
        if (!board.getMember().getId().equals(memberId)) {
            throw new BoardHandler(ErrorStatus.BOARD_UNAUTHORIZED);
        }

        // 삭제된 게시글인지 검증.
        if(board.getDeleted()){
            throw new BoardHandler(ErrorStatus.BOARD_DELETED);
        }

        // 게임 모드 값 검증. (1 ~ 4 값만 가능)
        if (request.getGameMode() < 1 || request.getGameMode() > 4) {
            throw new BoardHandler(ErrorStatus.GAME_MODE_INVALID);
        }

        // 주 포지션 값 검증. (0 ~ 5값만 가능)
        if (request.getMainPosition() < 0 || request.getMainPosition() > 5) {
            throw new BoardHandler(ErrorStatus.MAIN_POSITION_INVALID);
        }

        // 부 포지션 값 검증. (0 ~ 5값만 가능)
        if (request.getSubPosition() < 0 || request.getSubPosition() > 5) {
            throw new BoardHandler(ErrorStatus.SUB_POSITION_INVALID);
        }

        // 상대 포지션 값 검증. (0 ~ 5값만 가능)
        if (request.getWantPosition() < 0 || request.getWantPosition() > 5) {
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

        // 게시판 글 수정 - 프로필 이미지 수정 여부 검증.
        Integer boardProfileImage;
        if (request.getBoardProfileImage() != null) {
            boardProfileImage = request.getBoardProfileImage();
        } else {
            // 기존 게시글에 있는 프로필 이미지 정보 가져오기.
            boardProfileImage = board.getBoardProfileImage();
        }

        // 마이크 설정 (null인 경우 기본값 false)
        if (request.getMike() == null) {
            request.setMike(false);
        }

        // 게시판 글 데이터 수정
        board.updateBoard(
                request.getGameMode(),
                request.getMainPosition(),
                request.getSubPosition(),
                request.getWantPosition(),
                request.getMike(),
                request.getContents(),
                boardProfileImage
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
    public void delete(Long boardId, Long memberId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardHandler(ErrorStatus.BOARD_NOT_FOUND));

        // 게시글 작성자가 맞는지 검증.
        if (!board.getMember().getId().equals(memberId)) {
            throw new BoardHandler(ErrorStatus.BOARD_DELETE_UNAUTHORIZED);
        }

        board.setDeleted(true);

        boardRepository.save(board);
    }


    // 게시판 글 목록 조회
    @Transactional(readOnly = true)
    public List<BoardResponse.boardListResponseDTO> getBoardList(Integer mode, Tier tier,
                                                                 Integer mainPosition, Boolean mike, int pageIdx) {

        // pageIdx 값 검증.
        if (pageIdx <= 0) {
            throw new PageHandler(ErrorStatus.PAGE_INVALID);
        }

        // 사용자로부터 받은 pageIdx를 1 감소 -> pageIdx=1 일 때, 1 페이지.
        Pageable pageable = PageRequest.of(pageIdx - 1, PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Board> boards = boardRepository.findByFilters(mode, tier, mainPosition, mike, pageable)
                .getContent();

        return boards.stream().map(board -> {

            Member member = board.getMember();

            List<MemberResponse.ChampionResponseDTO> championResponseDTOList = null;
            if (member.getMemberChampionList() != null) {
                championResponseDTOList = member.getMemberChampionList().stream()
                        .map(memberChampion -> MemberResponse.ChampionResponseDTO.builder()
                                .championId(memberChampion.getMember().getId())
                                .championName(memberChampion.getChampion().getName())
                                .build()).collect(Collectors.toList());
            }

            return BoardResponse.boardListResponseDTO.builder()
                    .boardId(board.getId())
                    .memberId(member.getId())
                    .profileImage(board.getBoardProfileImage())
                    .gameName(member.getGameName())
                    .mannerLevel(member.getMannerLevel())
                    .tier(member.getTier())
                    .rank(member.getRank())
                    .gameMode(board.getMode())
                    .mainPosition(board.getMainPosition())
                    .subPosition(board.getSubPosition())
                    .wantPosition(board.getWantPosition())
                    .championResponseDTOList(championResponseDTOList)
                    .winRate(member.getWinRate())
                    .createdAt(board.getCreatedAt())
                    .mike(board.getMike())
                    .build();

        }).collect(Collectors.toList());
    }

    // 비회원 게시판 글 조회
    @Transactional(readOnly = true)
    public BoardResponse.boardByIdResponseDTO getBoardById(Long boardId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardHandler(ErrorStatus.BOARD_NOT_FOUND));

        if(board.getDeleted()){
            throw new BoardHandler(ErrorStatus.BOARD_DELETED);
        }

        Member poster = board.getMember();

        List<MemberResponse.ChampionResponseDTO> championResponseDTOList = null;
        if (poster.getMemberChampionList() != null) {
            championResponseDTOList = poster.getMemberChampionList().stream()
                    .map(memberChampion -> MemberResponse.ChampionResponseDTO.builder()
                            .championId(memberChampion.getMember().getId())
                            .championName(memberChampion.getChampion().getName())
                            .build()).collect(Collectors.toList());
        }


        return BoardResponse.boardByIdResponseDTO.builder()
                .boardId(board.getId())
                .memberId(poster.getId())
                .createdAt(board.getCreatedAt())
                .profileImage(board.getBoardProfileImage())
                .gameName(poster.getGameName())
                .tag(poster.getTag())
                .mannerLevel(poster.getMannerLevel())
                .tier(poster.getTier())
                .rank(poster.getRank())
                .championResponseDTOList(championResponseDTOList)
                .mike(board.getMike())
                .gameMode(board.getMode())
                .mainPosition(board.getMainPosition())
                .subPosition(board.getSubPosition())
                .wantPosition(board.getWantPosition())
                .recentGameCount(poster.getGameCount())
                .winRate(poster.getWinRate())
                .gameStyles(board.getBoardGameStyles().stream()
                        .map(boardGameStyle -> boardGameStyle.getGameStyle().getId())
                        .collect(Collectors.toList()))
                .contents(board.getContent())
                .build();

    }

    // 회원 게시판 글 조회
    @Transactional(readOnly = true)
    public BoardResponse.boardByIdResponseForMemberDTO getBoardByIdForMember(Long boardId,
                                                                             Long memberId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardHandler(ErrorStatus.BOARD_NOT_FOUND));

        if (board.getDeleted()){
            throw new BoardHandler(ErrorStatus.BOARD_DELETED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Member poster = board.getMember();

        List<MannerResponse.mannerKeywordDTO> mannerKeywordDTOs = mannerService.mannerKeyword(
                poster);

        List<MannerResponse.mannerKeywordDTO> mannerKeywords = mannerService.sortMannerKeywordDTOs(
                mannerKeywordDTOs);

        List<MemberResponse.ChampionResponseDTO> championResponseDTOList = null;
        if (member.getMemberChampionList() != null) {
            championResponseDTOList = member.getMemberChampionList().stream()
                    .map(memberChampion -> MemberResponse.ChampionResponseDTO.builder()
                            .championId(memberChampion.getMember().getId())
                            .championName(memberChampion.getChampion().getName())
                            .build()).collect(Collectors.toList());
        }


        return BoardResponse.boardByIdResponseForMemberDTO.builder()
                .boardId(board.getId())
                .memberId(poster.getId())
                .isBlocked(MemberUtils.isBlocked(member, poster))
                .isFriend(friendService.isFriend(member, poster))
                .friendRequestMemberId(friendService.getFriendRequestMemberId(member, poster))
                .createdAt(board.getCreatedAt())
                .profileImage(board.getBoardProfileImage())
                .gameName(poster.getGameName())
                .tag(poster.getTag())
                .mannerLevel(poster.getMannerLevel())
                .mannerKeywords(mannerKeywords)
                .tier(poster.getTier())
                .rank(poster.getRank())
                .championResponseDTOList(championResponseDTOList)
                .mike(board.getMike())
                .gameMode(board.getMode())
                .mainPosition(board.getMainPosition())
                .subPosition(board.getSubPosition())
                .wantPosition(board.getWantPosition())
                .recentGameCount(poster.getGameCount())
                .winRate(poster.getWinRate())
                .gameStyles(board.getBoardGameStyles().stream()
                        .map(boardGameStyle -> boardGameStyle.getGameStyle().getId())
                        .collect(Collectors.toList()))
                .contents(board.getContent())
                .build();
    }

    // 내가 작성한 게시판 글 목록 조회
    @Transactional(readOnly = true)
    public List<BoardResponse.myBoardListResponseDTO> getMyBoardList(Long memberId, int pageIdx) {

        // pageIdx 값 검증.
        if (pageIdx <= 0) {
            throw new PageHandler(ErrorStatus.PAGE_INVALID);
        }

        // 사용자로부터 받은 pageIdx를 1 감소 -> pageIdx=1 일 때, 1 페이지. 페이지당 표시할 게시물 수 = 10개.
        Pageable pageable = PageRequest.of(pageIdx - 1, 10,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Board> boards = boardRepository.findByMemberIdAndDeletedFalse(memberId, pageable).getContent();

        return boards.stream().map(board -> {
            Member member = board.getMember();

            return BoardResponse.myBoardListResponseDTO.builder()
                    .boardId(board.getId())
                    .memberId(member.getId())
                    .profileImage(board.getBoardProfileImage())
                    .gameName(member.getGameName())
                    .tag(member.getTag())
                    .tier(member.getTier())
                    .rank(member.getRank())
                    .contents(board.getContent())
                    .createdAt(board.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * boardId로 board 엔티티 조회
     *
     * @param boardId
     * @return
     */
    public Board findBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardHandler(ErrorStatus.BOARD_NOT_FOUND));
    }
}
