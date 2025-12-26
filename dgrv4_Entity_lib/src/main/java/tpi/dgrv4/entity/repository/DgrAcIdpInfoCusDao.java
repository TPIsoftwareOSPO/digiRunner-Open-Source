package tpi.dgrv4.entity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrAcIdpInfoCus;

@Repository
public interface DgrAcIdpInfoCusDao extends JpaRepository<DgrAcIdpInfoCus, Long>, DgrAcIdpInfoCusSuperDao {

	List<DgrAcIdpInfoCus> findAllByOrderByUpdateDateTimeDescAcIdpInfoCusIdDesc();

	/**
	 * 根據狀態查詢最新的一筆記錄
	 * 
	 * @param cusStatus 狀態
	 * @return 最新的一筆記錄，如果不存在則返回空
	 */
	Optional<DgrAcIdpInfoCus> findFirstByCusStatusOrderByUpdateDateTimeDescAcIdpInfoCusIdDesc(String cusStatus);

	/**
	 * 根據狀態查詢最新的多筆記錄
	 * 
	 * @param cusStatus 狀態
	 * @return 按更新時間和ID降序排列的記錄列表
	 */
	List<DgrAcIdpInfoCus> findByCusStatusOrderByUpdateDateTimeDescAcIdpInfoCusIdDesc(String cusStatus);

	/**
	 * 根據ID查詢記錄
	 * 
	 * @param acIdpInfoCusId ID
	 * @return 對應的記錄，如果不存在則返回空
	 */
	Optional<DgrAcIdpInfoCus> findByAcIdpInfoCusId(Long acIdpInfoCusId);

	/**
	 * 根據狀態查詢記錄
	 * 
	 * @param cusStatus 狀態（Y:啟用, N:停用）
	 * @return 符合狀態的記錄列表
	 */
	List<DgrAcIdpInfoCus> findByCusStatus(String cusStatus);

	List<DgrAcIdpInfoCus> findByAcIdpInfoCusIdAndAcIdpInfoCusNameAndCusStatusOrderByUpdateDateTimeDescAcIdpInfoCusIdDesc(
			Long acIdpInfoCusId, String cusStatus, String[] words, Integer pageSize);
}
