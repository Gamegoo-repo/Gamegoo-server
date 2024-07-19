package com.gamegoo.service.board;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.BoardHandler;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.apiPayload.exception.handler.TempHandler;
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

        // 마이크 설정 (default=false)
        if (request.getVoice()==null){
            request.setVoice(false);
        }

        // 게임 스타일 길이 검증.
        if (request.getGameStyles().size() > 3) {
            throw new BoardHandler(ErrorStatus.BOARD_GAME_STYLE_BAD_REQUEST);
        }

        // 게임 스타일 실제 존재 여부 검증.
        List<GameStyle> gameStyleList = new ArrayList<>();
        request.getGameStyles()
                .forEach(gameStyleId->{
                    GameStyle gameStyle = gameStyleRepository.findById(gameStyleId).orElseThrow(()->new TempHandler(ErrorStatus._BAD_REQUEST));
                    gameStyleList.add(gameStyle);
                });

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

}
