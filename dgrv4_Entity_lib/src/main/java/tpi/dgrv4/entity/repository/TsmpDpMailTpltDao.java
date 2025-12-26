package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;

@Repository
public interface TsmpDpMailTpltDao extends JpaRepository<TsmpDpMailTplt, Long> {

	public List<TsmpDpMailTplt> findByCode(String code);

	public List<TsmpDpMailTplt> findByCodeContainingOrderByMailtpltIdAsc(String code);

	public List<TsmpDpMailTplt> findByCodeContainingIgnoreCase(String code);

	public List<TsmpDpMailTplt> findByCreateUser(String createUser);
	
	public List<TsmpDpMailTplt> findAllByOrderByMailtpltIdAsc();

	/** 模糊搜尋預設方法 */
	public default Page<TsmpDpMailTplt> findByKeywords(String keywords, Pageable pageable) {
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
	public Page<TsmpDpMailTplt> findByKeywordSearchContainingIgnoreCase(String key1, Pageable pageable);
	public Page<TsmpDpMailTplt> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, Pageable pageable);
	public Page<TsmpDpMailTplt> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, String key3, Pageable pageable);
}
