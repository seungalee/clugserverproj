package com.study.board.repository;

import com.study.board.entity.BoardEntity;
import com.study.board.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    // select * from comment_table where board_id = ? order by id desc;, 최근에 작성한 것이 먼저

    //대소문자 지켜줘야 함
    List<CommentEntity> findAllByBoardEntityOrderByIdDesc(BoardEntity boardEntity);
}
