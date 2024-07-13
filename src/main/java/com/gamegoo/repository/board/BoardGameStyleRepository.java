package com.gamegoo.repository.board;

import com.gamegoo.domain.board.BoardGameStyle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardGameStyleRepository  extends JpaRepository<BoardGameStyle,Long> {
}
