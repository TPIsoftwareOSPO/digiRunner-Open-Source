package tpi.dgrv4.entity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxySticky;

@Repository
public interface DgrSmartOnFhirProxyStickyDao extends JpaRepository<DgrSmartOnFhirProxySticky, Long>, DgrSmartOnFhirProxyStickySuperDao {

	/**
	 * 根據 Proxy ID 和 Hashcode 查詢 Sticky 紀錄
	 * 用於 Sticky Session 機制，檢查是否已有資源的後端對應紀錄
	 *
	 * @param sofProxyId Proxy ID
	 * @param hashcode 資源的 hashcode (SHA256 of sofProxyId + type)
	 * @return Sticky 紀錄
	 */
	Optional<DgrSmartOnFhirProxySticky> findBySofProxyIdAndSofProxyStickyHashcode(
		Long sofProxyId,
		String hashcode
	);

	/**
	 * 統計各 Diversion 綁定的 Resource Type 數量
	 * 用於智能分配演算法，計算各後端的負載
	 *
	 * @param sofProxyId Proxy ID
	 * @return List of [diversionId, count] pairs
	 */
	@Query("SELECT s.sofProxyDiversionId, COUNT(s) FROM DgrSmartOnFhirProxySticky s " +
		   "WHERE s.sofProxyId = :sofProxyId GROUP BY s.sofProxyDiversionId")
	List<Object[]> countByDiversion(@Param("sofProxyId") Long sofProxyId);

	/**
	 * 刪除指定 Diversion 的所有 Sticky 紀錄
	 * 用於刪除 Backend 時清理關聯的綁定紀錄
	 *
	 * @param diversionId Diversion ID
	 */
	@Modifying
	@Query("DELETE FROM DgrSmartOnFhirProxySticky s WHERE s.sofProxyDiversionId = :diversionId")
	void deleteByDiversionId(@Param("diversionId") Long diversionId);

	/**
	 * 查詢指定 Diversion 綁定的所有 Resource Types
	 * 用於刪除 Backend 前預覽影響範圍
	 *
	 * @param diversionId Diversion ID
	 * @return Sticky 紀錄列表
	 */
	List<DgrSmartOnFhirProxySticky> findBySofProxyDiversionId(Long diversionId);

	/**
	 * 根據 Proxy ID 查詢所有 Sticky 紀錄
	 *
	 * @param sofProxyId Proxy ID
	 * @return Sticky 紀錄列表
	 */
	List<DgrSmartOnFhirProxySticky> findBySofProxyId(Long sofProxyId);

	/**
	 * 刪除指定 Proxy 的所有 Sticky 紀錄
	 * 用於刪除 Proxy 時的級聯清理
	 *
	 * @param sofProxyId Proxy ID
	 */
	@Modifying
	@Query("DELETE FROM DgrSmartOnFhirProxySticky s WHERE s.sofProxyId = :sofProxyId")
	void deleteByProxyId(@Param("sofProxyId") Long sofProxyId);

	/**
	 * 根據多個 Proxy ID 批次查詢所有 Sticky 紀錄
	 * 用於匯出時批次查詢
	 *
	 * @param sofProxyIds Proxy ID 列表
	 * @return Sticky 紀錄列表
	 */
	List<DgrSmartOnFhirProxySticky> findBySofProxyIdIn(List<Long> sofProxyIds);

}
