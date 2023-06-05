package com.study.board.dto;

import com.study.board.entity.CommentEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class CommentDTO {
    private Long id;
    private String commentWriter;
    private String commentContents;
    private Long boardId;
    private LocalDateTime commentCreatedTime;

    public static CommentDTO toCommentDTO(CommentEntity commentEntity, Long boardId) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(commentEntity.getId());
        commentDTO.setCommentWriter(commentEntity.getCommentWriter());
        commentDTO.setCommentContents(commentEntity.getCommentContents());
        commentDTO.setCommentCreatedTime(commentEntity.getCreatedTime());
        //자식한테 있는 부모 entity 값에서 꺼냄, service 메서드에 @Transactional
        //commentDTO.setBoardId(commentEntity.getBoardEntity().getId());
        //함수에서 boardID 넘겨받아 set해주기
        commentDTO.setBoardId(boardId);
        return commentDTO;
    }
}
