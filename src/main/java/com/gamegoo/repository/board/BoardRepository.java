package com.gamegoo.repository.board;

import com.gamegoo.domain.board.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board,Long>{
    Page<Board> findByMemberId(Long memberId, Pageable pageable);
}
