package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.vo.DPB0006SearchCriteria;

@Repository
public interface TsmpDpClientextDao extends JpaRepository<TsmpDpClientext, String> {

	public void deleteByClientId(String clientId);
	
	public List<TsmpDpClientext> queryLikeRegStatus(List<String> regStatusList, //
			String[] keywords, String lastId, Integer pageSize);

	public List<TsmpDpClientext> queryLikeRegStatusBetween(DPB0006SearchCriteria cri);

	public List<TsmpDpClientext> query_dpb0006Job(Date expDate);

	public List<TsmpDpClientext> query_dpb0006Job_inconsistentExt();

	/** 檢查用戶的publicFlag與API的publicFlag是否符合權限 */
	public Boolean hasApiAuthority(String clientId, String apiUid);

	public List<TsmpDpClientext> findByCreateUser(String createUser);

	/** 模糊搜尋預設方法 */
	public default Page<TsmpDpClientext> findByKeywords(String keywords, Pageable pageable) {
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
	public Page<TsmpDpClientext> findByKeywordSearchContainingIgnoreCase(String key1, Pageable pageable);
	public Page<TsmpDpClientext> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, Pageable pageable);
	public Page<TsmpDpClientext> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, String key3, Pageable pageable);
	
	public  TsmpDpClientext findByClientId(String clientId);
}