package com.ssafy.finalproject.service;

import com.ssafy.finalproject.exception.InsufficientCoinException;
import com.ssafy.finalproject.exception.ResourceNotFoundException;
import com.ssafy.finalproject.model.dao.UserDao;
import com.ssafy.finalproject.model.entity.User;
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
    private static final int DREAM_INTERPRETATION_COST = 1;
    private static final int IMAGE_VISUALIZATION_COST = 2;

    private final UserDao userDao;

    /**
     * 꿈 해몽 AI 호출 시 코인 1개 차감.
     */
    public void consumeForDreamInterpretation(Long userId) {
        consume(userId, DREAM_INTERPRETATION_COST);
    }

    /**
     * 꿈 시각화 이미지 호출 시 코인 2개 차감.
     */
    public void consumeForImageVisualization(Long userId) {
        consume(userId, IMAGE_VISUALIZATION_COST);
    }

    /**
     * 꿈 해몽 AI 호출에 필요한 코인이 충분한지 확인.
     */
    @Transactional(readOnly = true)
    public void validateCoinForDreamInterpretation(Long userId) {
        validateCoin(userId, DREAM_INTERPRETATION_COST);
    }

    /**
     * 이미지 시각화 호출에 필요한 코인이 충분한지 확인.
     */
    @Transactional(readOnly = true)
    public void validateCoinForImageVisualization(Long userId) {
        validateCoin(userId, IMAGE_VISUALIZATION_COST);
    }

    /**
     * 사용자의 현재 코인 잔액 조회.
     */
    @Transactional(readOnly = true)
    public int getCurrentCoin(Long userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        
        LocalDate todayKst = LocalDate.now(KST);
        
        // 날짜가 바뀌었으면 5로 간주 (실제 리셋은 consume 시점에 발생)
        if (user.getLastCoinResetAt() == null || 
            user.getLastCoinResetAt().toLocalDate().isBefore(todayKst)) {
            return 5;
        }
        
        return user.getCoin();
    }

    private void validateCoin(Long userId, int requiredCost) {
        int currentCoin = getCurrentCoin(userId);
        if (currentCoin < requiredCost) {
            throw new InsufficientCoinException(
                    String.format("코인이 부족합니다. 필요: %d개, 현재: %d개", requiredCost, currentCoin));
        }
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
            throw new InsufficientCoinException(
                    String.format("코인이 부족합니다. 필요: %d개", cost));
        }
    }
}


