package com.ssafy.finalproject.model.dao;

import com.ssafy.finalproject.model.entity.NoticeLikes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeLikesDao {

    int countLikesByNoticeId(@Param("noticeId") Long noticeId);

    int checkUserLiked(@Param("noticeId") Long noticeId, @Param("userId") Long userId);

    void insertNoticeLike(NoticeLikes noticeLikes);

    void deleteNoticeLike(@Param("noticeId") Long noticeId, @Param("userId") Long userId);

    List<NoticeLikes> selectLikesByNoticeId(@Param("noticeId") Long noticeId);
}

