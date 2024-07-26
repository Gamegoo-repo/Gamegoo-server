package com.gamegoo.repository.board;

import com.gamegoo.domain.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board,Long>{
}
