package com.study.board.repository;

import com.study.board.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


//entity에 의해 생성된 DB에 접근하는 메소드를 사용하기 위한 interface 파일들, JpaRepository 상속받아 CRUD 가능
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    //update board_table set board_hits=board_hits+1 where id=? 조회수를 1 올려주는 쿼리문
    //BoardEntity를 b로 정의한다, 설정한 boardHits 값에 접근해 +1 해줌, :id는 param의 id와 매칭됨
    @Modifying
    //CRUD 할때(?) modifying 붙여줘야 함
    @Query(value = "update BoardEntity b set b.boardHits=b.boardHits+1 where b.id=:id")
    void updateHits( @Param("id") Long id);
}
