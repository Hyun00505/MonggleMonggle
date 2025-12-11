package com.ssafy.finalproject.service;

import com.ssafy.finalproject.exception.ForbiddenException;
import com.ssafy.finalproject.exception.ResourceNotFoundException;
import com.ssafy.finalproject.model.dao.UserDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional
public class CoinService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final UserDao userDao;

    /**
     * 꿈 해몽 AI 호출 시 코인 1개 차감.
     */
    public void consumeForDreamInterpretation(Long userId) {
        consume(userId, 1);
    }

    /**
     * 꿈 시각화 이미지 호출 시 코인 2개 차감.
     */
    public void consumeForImageVisualization(Long userId) {
        consume(userId, 2);
    }

    private void consume(Long userId, int cost) {
        // 사용자 존재 여부 확인 (Soft delete 고려)
        userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        LocalDate todayKst = LocalDate.now(KST);

        // 날짜가 바뀌었으면 하루치 코인을 다시 지급
        userDao.resetDailyCoin(userId, todayKst);

        // 코인 차감 (잔액이 충분할 때만 성공)
        int updatedRows = userDao.consumeCoin(userId, cost);
        if (updatedRows == 0) {
            throw new ForbiddenException("오늘 사용 가능 횟수를 초과했습니다.");
        }
    }
}


