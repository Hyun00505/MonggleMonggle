package com.ssafy.finalproject.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.finalproject.exception.ForbiddenException;
import com.ssafy.finalproject.exception.ResourceNotFoundException;
import com.ssafy.finalproject.model.dao.CommentDao;
import com.ssafy.finalproject.model.dao.NoticeDao;
import com.ssafy.finalproject.model.dao.UserDao;
import com.ssafy.finalproject.model.dto.request.CreateCommentRequest;
import com.ssafy.finalproject.model.dto.response.CommentResponse;
import com.ssafy.finalproject.model.entity.Comment;
import com.ssafy.finalproject.model.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    
    private final CommentDao commentDao;
    private final NoticeDao noticeDao;
    private final UserDao userDao;
    
    /**
     * 댓글 작성
     */
    public CommentResponse createComment(Long userId, CreateCommentRequest request) {
        // 공지사항 존재 여부 확인
        noticeDao.selectNoticeById(request.getNoticeId())
                .orElseThrow(() -> new ResourceNotFoundException("공지사항을 찾을 수 없습니다."));
        
        // 사용자 정보 조회
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        
        Comment comment = Comment.builder()
                .noticeId(request.getNoticeId())
                .userId(userId)
                .content(request.getContent())
                .build();
        
        commentDao.insertComment(comment);
        
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .noticeId(comment.getNoticeId())
                .userId(comment.getUserId())
                .userName(user.getName())
                .content(comment.getContent())
                .createdDate(comment.getCreatedDate())
                .isOwner(true)
                .build();
    }
    
    /**
     * 공지사항별 댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public CommentResponse.CommentListResponse getCommentsByNoticeId(Long noticeId, Long currentUserId) {
        // 공지사항 존재 여부 확인
        noticeDao.selectNoticeById(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException("공지사항을 찾을 수 없습니다."));
        
        List<Comment> comments = commentDao.selectCommentsByNoticeId(noticeId);
        
        List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> {
                    // 댓글 작성자 정보 조회
                    User commentUser = userDao.findById(comment.getUserId()).orElse(null);
                    String userName = commentUser != null ? commentUser.getName() : "알 수 없음";
                    
                    return CommentResponse.builder()
                            .commentId(comment.getCommentId())
                            .noticeId(comment.getNoticeId())
                            .userId(comment.getUserId())
                            .userName(userName)
                            .content(comment.getContent())
                            .createdDate(comment.getCreatedDate())
                            .isOwner(comment.getUserId().equals(currentUserId))
                            .build();
                })
                .collect(Collectors.toList());
        
        return CommentResponse.CommentListResponse.builder()
                .noticeId(noticeId)
                .totalCount(commentResponses.size())
                .comments(commentResponses)
                .build();
    }
    
    /**
     * 댓글 삭제 (본인 또는 관리자 가능)
     */
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentDao.selectCommentById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다."));
        
        // 현재 사용자 정보 조회
        User currentUser = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 본인 또는 관리자 확인
        boolean isOwner = comment.getUserId().equals(userId);
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("댓글을 삭제할 권한이 없습니다.");
        }
        
        commentDao.deleteComment(commentId);
    }
}
