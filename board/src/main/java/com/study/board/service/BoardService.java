package com.study.board.service;

import com.study.board.dto.BoardDTO;
import com.study.board.entity.BoardEntity;
import com.study.board.entity.BoardFileEntity;
import com.study.board.repository.BoardFileRepository;
import com.study.board.repository.BoardRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//DTO->Entity (repository로 넘겨줄 때) (Entity Class)
//Entity->DTO (DB에서 조회할 때) (DTO Class)
//service: DB와 상호작용하는 비즈니스 로직을 수행하는 class 파일들

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;

    //Alt+Enter 유용하다
    public void save(BoardDTO boardDTO) throws IOException {
        //파일 첨부 여부에 따라 로직 분리
        if(boardDTO.getBoardFile().isEmpty()){
            //첨부 파일 없음
            BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDTO);
            boardRepository.save(boardEntity);
        }
        else{
            //첨부 파일 있음
            /*
            1. DTO에 담긴 파일을 꺼냄
            2. 파일의 이름 가져옴
            3. 서버 저장용 이름을 만듦
            ex. 내사진.jpg -> 8321794172_내사진.jpg
            4. 저장 경로 설정
            5. 해당 경로에 파일 저장
            6. board_table에 해당 데이터 save 처리
            7. board_file_table에 해당 데이터(file 정보) save 처리
            -board_table(부모) - board_file_table(자식), 여러 개 가질 수 있음
            -board_file_table의 board_id는 board_table의 id를 참고한다, on delete cascade

            */
            BoardEntity boardEntity = BoardEntity.toSaveFileEntity(boardDTO);
            Long savedID = boardRepository.save(boardEntity).getId();
            BoardEntity board = boardRepository.findById(savedID).get();//부모를 db로부터 가져옴 -> multiple에서 먼저
            for(MultipartFile boardFile: boardDTO.getBoardFile()) {
                //MultipartFile boardFile = boardDTO.getBoardFile(); //1.
                String originalFilename = boardFile.getOriginalFilename(); //2.
                String storedFileName = System.currentTimeMillis() + "_" + originalFilename; // 3.
                String savePath = "D:/springboot_img/" + storedFileName; //4.  D:/프로그래밍 공부/springboot_img/23479827_내사진.jpg
                boardFile.transferTo(new File(savePath)); //5.
                BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(board, originalFilename, storedFileName);
                boardFileRepository.save(boardFileEntity);
            }

        }

    }

    @Transactional
    public List<BoardDTO> findAll(){
        List<BoardEntity> boardEntityList = boardRepository.findAll();
        List<BoardDTO> boardDTOList = new ArrayList<>();
        for(BoardEntity boardEntity: boardEntityList){
            boardDTOList.add(BoardDTO.toBoardDTO(boardEntity));
        }
        return boardDTOList;

    }

    @Transactional
    public void updateHits(Long id){
        boardRepository.updateHits(id);
    }

    @Transactional
    public BoardDTO findByID(Long id){
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);
        if(optionalBoardEntity.isPresent()){
            BoardEntity boardEntity = optionalBoardEntity.get();
            BoardDTO boardDTO = BoardDTO.toBoardDTO(boardEntity);
            return boardDTO;
        }
        else{
            return null;
        }
    }

    public BoardDTO update(BoardDTO boardDTO) {
        BoardEntity boardEntity = BoardEntity.toUpdateEntity(boardDTO);
        boardRepository.save(boardEntity);
        return findByID(boardDTO.getId());
    }

    public void delete(Long id) {
        boardRepository.deleteById(id);
    }

    public Page<BoardDTO> paging(Pageable pageable){
        int page=pageable.getPageNumber()-1;
        int pageLimit=3; // 한 페이지에 보여줄 글 갯수, 만약 사용자 요청 따라 변경한다면 파라미터로 받아옴
        //properties: entity 기준, DB id 기준 X
        //한 페이지당 3개의 글을 보여주고 정렬 기준은 id 기준으로 내림차순 정렬
        //page 위치에 있는 값은 0부터 시작, 그래서 getPageNumber에서 -1 해줌
        Page<BoardEntity> boardEntities =
                boardRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));

        System.out.println("boardEntities.getContent() = " + boardEntities.getContent()); // 요청 페이지에 해당하는 글
        System.out.println("boardEntities.getTotalElements() = " + boardEntities.getTotalElements()); // 전체 글갯수
        System.out.println("boardEntities.getNumber() = " + boardEntities.getNumber()); // DB로 요청한 페이지 번호
        System.out.println("boardEntities.getTotalPages() = " + boardEntities.getTotalPages()); // 전체 페이지 갯수
        System.out.println("boardEntities.getSize() = " + boardEntities.getSize()); // 한 페이지에 보여지는 글 갯수
        System.out.println("boardEntities.hasPrevious() = " + boardEntities.hasPrevious()); // 이전 페이지 존재 여부
        System.out.println("boardEntities.isFirst() = " + boardEntities.isFirst()); // 첫 페이지 여부
        System.out.println("boardEntities.isLast() = " + boardEntities.isLast()); // 마지막 페이지 여부

        //page 객체로 담아가기
        // 목록: id, writer, title, hits, createdTime
        Page<BoardDTO> boardDTOS = boardEntities.map(board -> new BoardDTO(board.getId(), board.getBoardWriter(), board.getBoardTitle(), board.getBoardHits(), board.getCreatedTime()));

        //page 개수 20개, 현재 사용자가 3페이지 -> 보여지는 페이지 개수 3개
        return boardDTOS;
    }
}
