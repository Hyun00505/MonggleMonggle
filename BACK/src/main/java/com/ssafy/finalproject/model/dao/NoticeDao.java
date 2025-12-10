package com.ssafy.finalproject.model.dao;

import com.ssafy.finalproject.model.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface NoticeDao {

    List<Notice> selectAllNotice();

    Optional<Notice> selectNoticeById(@Param("noticeId") Long noticeId);

    void insertNotice(Notice notice);

    void updateNotice(Notice notice);

    void deleteNotice(@Param("noticeId") Long noticeId);

    void increaseViewCount(@Param("noticeId") Long noticeId);
}

