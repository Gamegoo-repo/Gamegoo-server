package com.gamegoo.service.board;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.BoardHandler;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.board.Board;
import com.gamegoo.domain.Member;
import com.gamegoo.domain.board.BoardGameStyle;
import com.gamegoo.domain.gamestyle.GameStyle;
import com.gamegoo.dto.board.BoardRequest;
import com.gamegoo.repository.board.BoardGameStyleRepository;
import com.gamegoo.repository.board.BoardRepository;
import com.gamegoo.repository.member.GameStyleRepository;
import com.gamegoo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
                    .board(board)
                    .build();

            boardGameStyle.setBoard(saveBoard);
            boardGameStyleRepository.save(boardGameStyle);
        });

        return saveBoard;
    }
}
