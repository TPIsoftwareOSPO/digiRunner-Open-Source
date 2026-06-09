package tpi.dgrv4.dpaa.record;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * 泛型分頁回應。
 *
 * 取代各 API 各自建立的分頁 Resp 類別（如 DPB0310Resp、DPB0317Resp），
 * 統一分頁回應結構，搭配 {@code TsmpBaseResp<PageResp<SomeDto>>} 使用。
 */
public record PageResp<T>(
    List<T> content,
    long totalElements,
    int totalPages,
    int number,
    int size
) {

    /**
     * 從 Spring Data {@link Page} 建構分頁回應。
     *
     * @param page    Spring Data 分頁查詢結果
     * @param content 已轉換為 DTO 的資料清單
     */
    public static <T> PageResp<T> from(Page<?> page, List<T> content) {
        return new PageResp<>(
            content,
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
    }
}
