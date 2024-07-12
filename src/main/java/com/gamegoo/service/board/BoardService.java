package com.gamegoo.service.board;

import com.gamegoo.apiPayload.code.status.ErrorStatus;
import com.gamegoo.apiPayload.exception.handler.MemberHandler;
import com.gamegoo.domain.Board;
import com.gamegoo.domain.Member;
import com.gamegoo.dto.board.BoardRequest;
import com.gamegoo.repository.board.BoardRepository;
import com.gamegoo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;

    // 블로그 글 작성.
    @Transactional
    public Board save(BoardRequest.boardInsertDTO request,Long memberId){

        Member member = memberRepository.findById(memberId).orElseThrow(()->new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 마이크 설정 (default=false)
        if (request.getVoice()==null){
            request.setVoice(false);
        }

        Board board = Board.builder()
                .mode(request.getGameMode())
                .mainPosition(request.getMainPosition())
                .subPosition(request.getSubPosition())
                .wantPosition(request.getWantPosition())
                .voice(request.getVoice())
                .content(request.getContents())
                .build();

        board.setMember(member);
        return boardRepository.save(board);
    }

}
