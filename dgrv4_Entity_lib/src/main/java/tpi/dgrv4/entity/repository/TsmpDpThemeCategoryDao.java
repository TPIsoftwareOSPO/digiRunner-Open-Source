package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;

@Repository
public interface TsmpDpThemeCategoryDao extends JpaRepository<TsmpDpThemeCategory, Long> {

	public List<TsmpDpThemeCategory> query_dpb0020Service(List<String> orgDescList //
			, String dataStatus, String[] words, TsmpDpThemeCategory lastRecord, Integer pageSize);

	public List<TsmpDpThemeCategory> query_dpb0055Service(List<String> orgDescList //
			, String[] words, TsmpDpThemeCategory lastRecord, Integer pageSize);

	public List<TsmpDpThemeCategory> query_dpb0076Service(List<String> orgDescList, String[] words, //
			Long lastId, Integer pageSize);

	public long countByApiThemeName(String themeName);
	
	public List<TsmpDpThemeCategory> findByCreateUser(String createUser);

	/** 模糊搜尋預設方法 */
	public default Page<TsmpDpThemeCategory> findByKeywords(String keywords, Pageable pageable) {
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
	public Page<TsmpDpThemeCategory> findByKeywordSearchContainingIgnoreCase(String key1, Pageable pageable);
	public Page<TsmpDpThemeCategory> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, Pageable pageable);
	public Page<TsmpDpThemeCategory> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, String key3, Pageable pageable);
}
