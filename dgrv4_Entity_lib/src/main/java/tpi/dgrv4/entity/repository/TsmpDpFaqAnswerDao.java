package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.entity.entity.sql.TsmpDpFaqAnswer;

@Repository
public interface TsmpDpFaqAnswerDao extends JpaRepository<TsmpDpFaqAnswer, Long> {

	public List<TsmpDpFaqAnswer> findByRefQuestionId(Long refQuestionId);

	public List<TsmpDpFaqAnswer> findByCreateUser(String createUser);

	/** 模糊搜尋預設方法 */
	public default Page<TsmpDpFaqAnswer> findByKeywords(String keywords, Pageable pageable) {
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
	public Page<TsmpDpFaqAnswer> findByKeywordSearchContainingIgnoreCase(String key1, Pageable pageable);
	public Page<TsmpDpFaqAnswer> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, Pageable pageable);
	public Page<TsmpDpFaqAnswer> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, String key3, Pageable pageable);
}
