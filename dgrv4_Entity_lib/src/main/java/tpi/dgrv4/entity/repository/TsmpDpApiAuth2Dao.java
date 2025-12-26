package tpi.dgrv4.entity.repository;

import static tpi.dgrv4.common.utils.ServiceUtil.getKeywords;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDpApiAuth2;

@Repository
public interface TsmpDpApiAuth2Dao extends JpaRepository<TsmpDpApiAuth2, Long> {

	public List<TsmpDpApiAuth2> query_dpb0001Service_01(//
			List<String> orgDescList, String applyStatus, String[] words, Long lastId, Integer pageSize);

	public List<TsmpDpApiAuth2> query_dpb0003Service_01(//
			List<String> orgDescList, List<String> applyStatus, String[] words, TsmpDpApiAuth2 lastRecord, Integer pageSize);

	public TsmpDpApiAuth2 findTopByApiAuthIdAndVersion(Long apiAuthId, Long version);

	public List<TsmpDpApiAuth2> findByRefClientIdAndRefApiUid(String refClientId, String refApiUid);

	public List<TsmpDpApiAuth2> findByRefClientIdAndRefApiUidAndApplyStatus(String refClientId, String refApiUid, String applyStatus);

	public List<TsmpDpApiAuth2> findByCreateUser(String createUser);

	/** 模糊搜尋預設方法 */
	public default Page<TsmpDpApiAuth2> findByKeywords(String keywords, Pageable pageable) {
		final String[] words = getKeywords(keywords, " ");
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
	public Page<TsmpDpApiAuth2> findByKeywordSearchContainingIgnoreCase(String key1, Pageable pageable);
	public Page<TsmpDpApiAuth2> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, Pageable pageable);
	public Page<TsmpDpApiAuth2> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, String key3, Pageable pageable);
}
