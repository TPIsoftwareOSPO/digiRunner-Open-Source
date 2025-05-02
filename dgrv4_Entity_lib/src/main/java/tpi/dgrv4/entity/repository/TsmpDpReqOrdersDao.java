package tpi.dgrv4.entity.repository;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrders;

@Repository
@Transactional //因為JPQL原先的設計只能查詢,所以不需要設定 @Transactional,但JPA 1.7以後的版本支持Query命名的方法改為 delete/remove/count, 就需要設定@Transactional
public interface TsmpDpReqOrdersDao extends JpaRepository<TsmpDpReqOrders, Long> {

	public List<TsmpDpReqOrders> findByReqOrdermIdAndStatusOrderByLayerAscReqOrdersIdAsc(Long reqOrdermId, String status);

	public TsmpDpReqOrders queryCurrentStatus(Long reqOrdermId);

	public TsmpDpReqOrders queryNextCheckPoint(Long reqOrdermId);

	public Boolean isEndable(Long reqOrdermId);

	public Boolean wasAbleToSign(Long reqOrdermId, String userName);

	public List<TsmpDpReqOrders> query_AA0303Service_01(String apiUid);

	public List<TsmpDpReqOrders> findByCreateUser(String createUser);

	public long deleteByReqOrdermId(Long reqOrdermId);

	/** 模糊搜尋預設方法 */
	public default Page<TsmpDpReqOrders> findByKeywords(String keywords, Pageable pageable) {
		final String[] words = ServiceUtil.getKeywords(keywords, " ");
		if (words == null || words.length < 1) {
			return null;
		}
		if (words.length == 1) {
			return findByKeywordSearchContainingIgnoreCase(words[0], pageable);
		} else if (words.length == 2) {
			return findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(words[0], words[1], pageable);
		} else {
			return findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(words[0], words[1], words[2], pageable);
		}
	};
	public Page<TsmpDpReqOrders> findByKeywordSearchContainingIgnoreCase(String key1, Pageable pageable);
	public Page<TsmpDpReqOrders> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, Pageable pageable);
	public Page<TsmpDpReqOrders> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, String key3, Pageable pageable);
}
