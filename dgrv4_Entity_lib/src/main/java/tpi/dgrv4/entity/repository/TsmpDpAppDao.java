package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.entity.entity.sql.TsmpDpApp;

@Repository
public interface TsmpDpAppDao extends JpaRepository<TsmpDpApp, Long> {

	public List<TsmpDpApp> queryLikeByDataStatus(List<String> orgDescList //
			, List<String> dataStatusList, String[] words, Long lastId, Integer pageSize);

	public List<TsmpDpApp> findByRefAppCateId(Long refAppCateId);

	public List<TsmpDpApp> findByCreateUser(String createUser);

	/** 模糊搜尋預設方法 */
	public default Page<TsmpDpApp> findByKeywords(String keywords, Pageable pageable) {
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
	public Page<TsmpDpApp> findByKeywordSearchContainingIgnoreCase(String key1, Pageable pageable);
	public Page<TsmpDpApp> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, Pageable pageable);
	public Page<TsmpDpApp> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, String key3, Pageable pageable);
}
