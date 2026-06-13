package org.hansung.zigma.domain.promise.web.dto;

import org.hansung.zigma.domain.promise.util.CursorUtil;

import java.util.List;

public record PromiseListRes(
        List<PromiseRes> promises,
        String cursor,
        Integer count,
        Boolean hasNext
) {
    public static PromiseListRes of(List<PromiseRes> promises, int pageSize) {
        // 다음 페이지 존재 여부 확인
        boolean hasNext = promises.size() > pageSize;

        // 실제 응답에 포함될 데이터 리스트 (요청한 개수만큼 자르기)
        List<PromiseRes> resultList = hasNext
                ? promises.subList(0, pageSize)
                : promises;

        // 커서 값 결정
        String cursor = null;
        if (!resultList.isEmpty()) {
            PromiseRes lastItem = resultList.getLast();
            cursor = CursorUtil.encodeCursor(lastItem.id(), lastItem.promisedAt());
        }

        return new PromiseListRes(
                resultList,
                cursor,
                resultList.size(),
                hasNext
        );
    }
}
