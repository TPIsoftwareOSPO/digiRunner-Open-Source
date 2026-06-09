package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxy;

public interface DgrSmartOnFhirProxySuperDao extends SuperDao<DgrSmartOnFhirProxy> {

    /**
     * 根據篩選條件查詢 SmartOnFhirProxy 列表（分頁）
     * 
     * @param keywords         關鍵字列表（模糊查詢 ID/名稱/備註/Diversion URL，OR 邏輯）
     * @param sofProxyStatus   狀態（精確匹配）
     * @param pageable         分頁參數
     * @return 分頁結果
     */
    Page<DgrSmartOnFhirProxy> findByFiltersWithPagination(
            List<String> keywords,
            String sofProxyStatus,
            Pageable pageable);

    /**
     * 根據篩選條件查詢 SmartOnFhirProxy 列表（不分頁，用於匯出）
     * 
     * @param keywords         關鍵字列表（模糊查詢 ID/名稱/備註/Diversion URL，OR 邏輯）
     * @param sofProxyStatus   狀態（精確匹配）
     * @return 符合條件的所有 Proxy
     */
    List<DgrSmartOnFhirProxy> findByFiltersForExport(
            List<String> keywords,
            String sofProxyStatus);
}
