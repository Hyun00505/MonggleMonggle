// 비즈니스 로직 처리를 위한 Service 인터페이스
package com.ssafy.finalproject.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.finalproject.exception.ResourceNotFoundException;
import com.ssafy.finalproject.model.dao.NoticeDao;
import com.ssafy.finalproject.model.dao.NoticeLikesDao;
import com.ssafy.finalproject.model.dto.request.CreateNoticeRequest;
import com.ssafy.finalproject.model.dto.request.UpdateNoticeRequest;
import com.ssafy.finalproject.model.dto.response.NoticeListResponse;
import com.ssafy.finalproject.model.dto.response.NoticeResponse;
import com.ssafy.finalproject.model.entity.Notice;

import lombok.RequiredArgsConstructor;

@Service                    // ← 스프링 빈으로 등록
@RequiredArgsConstructor    // ← final 필드 자동 생성자 주입
@Transactional              // ← 트랜잭션 처리
public class NoticeService {
    
    private final NoticeDao noticeDao;  // ← DAO 주입
    private final NoticeLikesDao noticeLikesDao;  // ← 좋아요 DAO 주입

    // 전체 목록 조회
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public NoticeListResponse getAllNotices(){
        // 1. DAO로 전체 목록 가져오기 
        List<Notice> notices = noticeDao.selectAllNotice();
        
        // 2. Entity 리스트 -> Response 리스트로 변환 
        List<NoticeResponse> responseList = notices.stream()
                .map(notice -> {
                    // 각 공지사항의 좋아요 수 조회
                    int likeCount = noticeLikesDao.countLikesByNoticeId(notice.getNoticeId());
                    
                    return NoticeResponse.builder()
                            .noticeId(notice.getNoticeId())
                            .userId(notice.getUserId())
                            .title(notice.getTitle())
                            .content(notice.getContent())
                            .viewCount(notice.getViewCount())
                            .likeCount(likeCount)
                            .createdDate(notice.getCreatedDate())
                            .updatedDate(notice.getUpdatedDate())
                            .build();
                })
                .toList();
        
        // 3. NoticeListResponse로 감싸서 반환 
        return NoticeListResponse.builder()
            .notices(responseList)
            .build();
    }

    // 상세 조회 (조회수 증가 포함)
    @Transactional
    public NoticeResponse getNoticeById(Long noticeId) {       
        // 1. 존재 여부 확인
        noticeDao.selectNoticeById(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException("공지사항을 찾을 수 없습니다."));
        
        // 2. 조회수 증가 먼저!
        noticeDao.increaseViewCount(noticeId);

        // 3. 증가된 조회수를 반영한 데이터 다시 조회
        Notice notice = noticeDao.selectNoticeById(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException("공지사항을 찾을 수 없습니다."));

        // 4. 좋아요 수 조회
        int likeCount = noticeLikesDao.countLikesByNoticeId(noticeId);

        // 5. Entity → Response DTO 변환 후 반환
        return NoticeResponse.builder()
                .noticeId(notice.getNoticeId())
                .userId(notice.getUserId()) 
                .title(notice.getTitle())
                .content(notice.getContent())
                .viewCount(notice.getViewCount())  
                .likeCount(likeCount)
                .createdDate(notice.getCreatedDate())
                .updatedDate(notice.getUpdatedDate())
                .deletedDate(notice.getDeletedDate())
                .build();
    }

    // 공지사항 등록
    public NoticeResponse createNotice(Long userId, CreateNoticeRequest request){
        // 1. Request DTO → Entity 변환
        Notice notice = Notice.builder()
            .userId(userId)
            .title(request.getTitle())
            .content(request.getContent())
            .viewCount(0) //초기 조회수 0 
            .build();
        
        // 2. dao로 저장 
        noticeDao.insertNotice(notice);

        // 3. 저장된 entity -> Response DTO 변환 후 반환
        return NoticeResponse.builder()
            .noticeId(notice.getNoticeId())
            .userId(notice.getUserId())
            .title(notice.getTitle())
            .content(notice.getContent())
            .viewCount(notice.getViewCount())
            .likeCount(0)  // 새 공지사항은 좋아요 0
            .createdDate(notice.getCreatedDate())
            .build();
    }

    // 공지사항 수정
    public NoticeResponse updateNotice(Long noticeId, UpdateNoticeRequest request){
        // 1. 기존 데이터 조회
        Notice notice = noticeDao.selectNoticeById(noticeId)
        .orElseThrow(() -> new ResourceNotFoundException("공지사항을 찾을 수 없습니다."));

        // 2. 값 변경
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());

        // 3. DAO로 업데이트
        noticeDao.updateNotice(notice);

        // 4. 좋아요 수 조회
        int likeCount = noticeLikesDao.countLikesByNoticeId(noticeId);

        // 5. 변경된 Entity → Response 반환
        return NoticeResponse.builder()
            .noticeId(notice.getNoticeId())
            .userId(notice.getUserId())
            .title(notice.getTitle())
            .content(notice.getContent())
            .viewCount(notice.getViewCount())
            .likeCount(likeCount)
            .createdDate(notice.getCreatedDate())
            .updatedDate(notice.getUpdatedDate())
            .build();
    }

    // 공지사항 삭제
    public void deleteNotice(Long noticeId) {
        // 1. 존재 여부 확인
        noticeDao.selectNoticeById(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException("공지사항을 찾을 수 없습니다."));
        
        // 2. 삭제
        noticeDao.deleteNotice(noticeId);
    }
}