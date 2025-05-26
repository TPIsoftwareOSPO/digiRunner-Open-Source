package tpi.dgrv4.entity.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.entity.entity.TsmpDpFile;

@Repository
@Transactional //因為JPQL原先的設計只能查詢,所以不需要設定 @Transactional,但JPA 1.7以後的版本支持Query命名的方法改為 delete/remove/count, 就需要設定@Transactional
public interface TsmpDpFileDao extends JpaRepository<TsmpDpFile, Long> {

	public List<TsmpDpFile> findByRefFileCateCodeAndRefId(String refFileCateCode, Long refId);

	public List<TsmpDpFile> findByRefFileCateCodeAndRefIdAndFileName(String refFileCateCode, Long refId, String fileName);

	public List<TsmpDpFile> findByFilePathAndFileName(String filePath, String fileName);

	public List<TsmpDpFile> findByCreateUser(String createUser);
	
	public List<TsmpDpFile> queryByRefFileCateCodeAndRefId(String refFileCateCode ,Long refId);
	
	public List<TsmpDpFile> findByFileIdIn(Collection<Long> ids);
	
	public long deleteByRefFileCateCodeAndRefIdAndFileName(String refFileCateCode, Long refId, String fileName);
	
	public long deleteByRefFileCateCodeAndRefId(String refFileCateCode, Long refId);
	
	public long deleteByfileName(String fileName);

	public List<TsmpDpFile> findByFileName(String fileName);
	
	public List<TsmpDpFile> findByFileNameAndRefIdAndRefFileCateCode(String fileName, Long refId ,String refFileCateCode);
	
	public long countByFileNameAndRefIdAndRefFileCateCodeAndFileIdNot(String fileName, Long refId ,String refFileCateCode,Long fileId);
	
	public List<TsmpDpFile> findByIsTmpfile(String isTmpfile);

	public List<TsmpDpFile> findByFileIdAndIsTmpfile(Long fileId,String isTmpfile);

	public List<TsmpDpFile> query_DPB0061Service_01(String refFileCateCode, String fileNameSuffix);
	
	public List<TsmpDpFile> query_DPB9915Service_01(Long lastId, Date lastDateTime, Date startDate, Date endDate, //
			ArrayList<Long> fileIds, List<String> fileNames,String refFileCateCode, Long refId, String isTempFile, Integer pageSize);
	
	public List<Integer> query_DPB9915Service_02(Long fileId);

	/** 模糊搜尋預設方法 */
	public default Page<TsmpDpFile> findByKeywords(String keywords, Pageable pageable) {
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
	public Page<TsmpDpFile> findByKeywordSearchContainingIgnoreCase(String key1, Pageable pageable);
	public Page<TsmpDpFile> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, Pageable pageable);
	public Page<TsmpDpFile> findByKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCaseOrKeywordSearchContainingIgnoreCase(String key1, String key2, String key3, Pageable pageable);

	public TsmpDpFile findTopByRefFileCateCodeOrderByRefIdDesc(String refFileCateCode);
	
	public List<TsmpDpFile> findByRefFileCateCodeAndCreateDateTimeLessThan(String refFileCateCode, Date expDate);
}
