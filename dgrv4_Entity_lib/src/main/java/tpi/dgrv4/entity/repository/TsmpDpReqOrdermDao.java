package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.vo.DPB0067SearchCriteria;

@Repository
public interface TsmpDpReqOrdermDao extends JpaRepository<TsmpDpReqOrderm, Long> {

	public List<TsmpDpReqOrderm> query_dpb0067_queryPersonal(DPB0067SearchCriteria sc);

	public List<TsmpDpReqOrderm> query_dpb0067_queryReviewWork(DPB0067SearchCriteria sc);

	public List<TsmpDpReqOrderm> query_dpb0067_queryReviewHistory(DPB0067SearchCriteria sc);

	public TsmpDpReqOrderm findFirstByReqOrderNo(String reqOrderNo);

	public List<TsmpDpReqOrderm> findByCreateUser(String createUser);

	/** 模糊搜尋預設方法 */
	public default Page<TsmpDpReqOrderm> findByKeywords(String keywords, Pageable pageable) {
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
	public Page<TsmpDpReqOrderm> findByKeywordSearchContainingIgnoreCase(String key1, Pageable pageable);
	public Page<TsmpDpReqOrderm> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, Pageable pageable);
	public Page<TsmpDpReqOrderm> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, String key3, Pageable pageable);
	
	public List<TsmpDpReqOrderm> query_dpb0067_expired(Date queryStartDate);
}
