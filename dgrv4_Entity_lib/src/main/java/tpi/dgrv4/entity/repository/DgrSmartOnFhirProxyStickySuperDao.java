package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxySticky;

public interface DgrSmartOnFhirProxyStickySuperDao extends SuperDao<DgrSmartOnFhirProxySticky> {

	/**
	 * 根據篩選條件查詢 Sticky 列表（分頁）
	 *
	 * @param sofProxyId          Proxy ID（精確匹配，可選）
	 * @param sofProxyDiversionId Diversion ID（精確匹配，可選）
	 * @param sofProxyStickyType  Sticky Type（精確匹配，可選）
	 * @param keywords            關鍵字列表（模糊查詢 type, typeId, path，OR 邏輯）
	 * @param pageable            分頁參數
	 * @return 分頁結果
	 */
	Page<DgrSmartOnFhirProxySticky> findByFiltersWithPagination(
			Long sofProxyId,
			Long sofProxyDiversionId,
			String sofProxyStickyType,
			List<String> keywords,
			Pageable pageable);
}
