package com.gamegoo.repository.board;

import com.gamegoo.domain.board.Board;
import com.gamegoo.domain.member.Tier;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board,Long>{
    Page<Board> findByMemberIdAndDeletedFalse(Long memberId, Pageable pageable);

    @Query("SELECT b From Board b JOIN b.member m WHERE" +
            "(b.deleted = false) AND " +
            "(:mode IS NULL OR b.mode = :mode) AND " +
            "(:tier IS NULL OR m.tier = :tier) AND " +
            "(:mainPosition IS NULL OR b.mainPosition = :mainPosition OR b.subPosition = :mainPosition) AND " +
            "(:mike IS NULL OR b.mike = :mike)")
    Page<Board> findByFilters(@Param("mode") Integer mode,
                              @Param("tier") Tier tier,
                              @Param("mainPosition") Integer mainPosition,
                              @Param("mike") Boolean mike,
                              Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Board b SET b.deleted = true WHERE b.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);

    List<Board> findByMemberIdAndDeletedFalse(Long memberId);

}
