package com.ssafy.finalproject.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.finalproject.exception.ResourceNotFoundException;
import com.ssafy.finalproject.model.dao.NoticeLikesDao;
import com.ssafy.finalproject.model.dao.NoticeDao;
import com.ssafy.finalproject.model.dto.response.NoticeLikesResponse;
import com.ssafy.finalproject.model.entity.NoticeLikes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeLikesService {
    
    private final NoticeLikesDao noticeLikesDao;
    private final NoticeDao noticeDao;
    
    /**
     * 좋아요 토글 (좋아요/좋아요 취소)
     */
    public NoticeLikesResponse toggleLike(Long userId, Long noticeId) {
        // 공지사항 존재 여부 확인
        noticeDao.selectNoticeById(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException("공지사항을 찾을 수 없습니다."));
        
        // 이미 좋아요를 눌렀는지 확인
        boolean alreadyLiked = noticeLikesDao.checkUserLiked(noticeId, userId) > 0;
        
        if (alreadyLiked) {
            // 좋아요 취소
            noticeLikesDao.deleteNoticeLike(noticeId, userId);
        } else {
            // 좋아요 추가
            NoticeLikes noticeLikes = NoticeLikes.builder()
                    .noticeId(noticeId)
                    .userId(userId)
                    .build();
            noticeLikesDao.insertNoticeLike(noticeLikes);
        }
        
        // 현재 좋아요 수 조회
        int likeCount = noticeLikesDao.countLikesByNoticeId(noticeId);
        
        return NoticeLikesResponse.builder()
                .noticeId(noticeId)
                .likeCount(likeCount)
                .isLiked(!alreadyLiked)  // 토글 후 상태
                .build();
    }
    
    /**
     * 좋아요 상태 조회 (좋아요 수 + 현재 사용자 좋아요 여부)
     */
    @Transactional(readOnly = true)
    public NoticeLikesResponse getLikeStatus(Long userId, Long noticeId) {
        // 공지사항 존재 여부 확인
        noticeDao.selectNoticeById(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException("공지사항을 찾을 수 없습니다."));
        
        int likeCount = noticeLikesDao.countLikesByNoticeId(noticeId);
        boolean isLiked = noticeLikesDao.checkUserLiked(noticeId, userId) > 0;
        
        return NoticeLikesResponse.builder()
                .noticeId(noticeId)
                .likeCount(likeCount)
                .isLiked(isLiked)
                .build();
    }
}
