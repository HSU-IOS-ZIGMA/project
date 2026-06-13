package org.hansung.zigma.domain.promise.util;

import org.hansung.zigma.domain.promise.exception.PromiseInvalidPromiseTimeException;
import org.hansung.zigma.domain.promise.exception.PromiseInvalidTimeSequenceException;
import org.hansung.zigma.domain.promise.exception.PromiseInvalidVotingTimeException;

import java.time.LocalDateTime;

public class Validator {
    /**
     * 약속 관련 날짜 유효성 검사
     * 1. 오늘 < 약속 날짜
     * 2. 오늘 < 투표 종료 날짜
     * 3. 투표 종료 날짜 < 약속 날짜
     */
    public static void validatePromiseDates(LocalDateTime promisedAt, LocalDateTime endAt, LocalDateTime now) {
        // 약속 날짜가 오늘보다 미래인지 확인
        if (promisedAt.isBefore(now)) {
            throw new PromiseInvalidPromiseTimeException();
        }

        // 투표 종료 시간이 설정된 경우 추가 검증
        if (endAt != null) {
            // 투표 종료가 오늘보다 미래인지 확인
            if (endAt.isBefore(now)) {
                throw new PromiseInvalidVotingTimeException();
            }

            // 투표 종료가 약속 날짜보다 과거인지 확인
            if (endAt.isAfter(promisedAt)) {
                throw new PromiseInvalidTimeSequenceException();
            }
        }
    }
}
