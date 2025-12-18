package com.ssafy.finalproject.model.dao;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.finalproject.model.entity.Comment;

@Mapper
public interface CommentDao {

    List<Comment> selectCommentsByNoticeId(@Param("noticeId") Long noticeId);

    Optional<Comment> selectCommentById(@Param("commentId") Long commentId);

    void insertComment(Comment comment);

    void deleteComment(@Param("commentId") Long commentId);
}

