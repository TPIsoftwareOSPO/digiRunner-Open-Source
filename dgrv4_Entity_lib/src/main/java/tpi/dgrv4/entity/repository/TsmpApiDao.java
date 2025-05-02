package tpi.dgrv4.entity.repository;

import java.util.AbstractMap;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.vo.AA0301SearchCriteria;
import tpi.dgrv4.entity.vo.DPB0018SearchCriteria;

@Repository
public interface TsmpApiDao extends JpaRepository<TsmpApi, TsmpApiId> {

	public List<TsmpApi> findByCreateUser(String createUser);

	@Query("select new tpi.dgrv4.entity.entity.TsmpApi(t.apiKey, t.moduleName, t.srcUrl) from TsmpApi t")
	public List<TsmpApi> queryAll_APIkeyAndModuleNameAndSrcUrl();

	public List<TsmpApi> query_dpb0075Service(TsmpApiId lastId, List<String> orgDescList, String dpStatus,
			String[] words, int pageSize);

	public List<TsmpApi> query_aa0321Service(TsmpApiId lastId, List<String> orgDescList, String[] words,
			List<String> apiUidList, int pageSize);

	public List<TsmpApi> findByApiUid(String apiUid);

	public List<TsmpApi> query_dpb0018Service(DPB0018SearchCriteria cri);

	public List<TsmpApi> query_dpb0072Service(Date sdt, Date edt, String[] words, TsmpApi lastRecord, Integer pageSize,
			List<String> orgDescList, String orgFlag);

	// DPB0091, DPF0048 & DPB0092, DPF0049
	public List<TsmpApi> queryByOpenApiKeyId(TsmpApi lastRecord, Integer pageSize, Long openApiKeyId);

	// DPB0093, DPF0046
	public List<TsmpApi> queryApiLikeList(TsmpApiId lastId, String[] words, int pageSize);

	public List<TsmpApi> findAllByModuleNameAndApiStatus(String moduleName, String apiStatus);

	public List<TsmpApi> findByOrgId(String orgId);

	// AA0228
	public List<TsmpApi> queryByTsmpGroupAPiGroupId(String groupId);

	public List<TsmpApi> findByAA0229Service(String vgroupId);

	// AA0233
	public List<String> queryModuleByClinetOrgId(String lastModuleNameRecord, String[] keywords, List<String> orgList,
			List<String> selectedModuleNameList, int pageSize);

	public TsmpApi findByModuleNameAndApiKey(String moduleName, String apiKey);

	public List<TsmpApi> queryByAA0234Service(TsmpApi lastTsmpApi, String[] keywords, String moduleName,
			List<String> orgList, List<String> selectedApiKeyList, int pageSize);

	public List<TsmpApi> query_AA0301Service(AA0301SearchCriteria cri);

	public List<TsmpApi> findByApiKeyAndModuleNameAndApiSrc(String apiKey, String moduleName, String apiSrc);

	public List<TsmpApi> findByModuleName(String moduleName);

	public List<TsmpApi> findByModuleNameAndApiStatusNot(String moduleName, String apiStatus);

	public List<TsmpApi> query_AA0405Service(String moduleName, List<String> orgList);

	public Long countByApiSrcAndApiStatus(String apiSrc, String apiStatus);

	public List<TsmpApi> findTop5ByReleaseTimeDesc();

	public List<TsmpApi> findByModuleNameAndApiKeyAndApiNameAndApiDescAndApiStatusAndPublicFlag( //
			String moduleName, String apiKey, String apiStatus, String publicFlag, String[] words, Integer pageSize);

	public List<String> query_AA0427Lable1(List<String> apiSrc);

	public List<String> query_AA0427Lable2(List<String> apiSrc);

	public List<String> query_AA0427Lable3(List<String> apiSrc);

	public List<String> query_AA0427Lable4(List<String> apiSrc);

	public List<String> query_AA0427Lable5(List<String> apiSrc);

	public List<TsmpApi> query_AA0428Service(AA0301SearchCriteria cri);
	
	int deleteNonSpecifiedContent(List<AbstractMap.SimpleEntry<String, String>> list);

	public List<TsmpApi> query_AA0423Service(List<String> labelList);

	List<TsmpApi> findByScheduledRemovalDateLessThanEqualAndScheduledRemovalDateNot(Long scheduledRemovalDate,
			Long excludedValue);

	List<TsmpApi> findByEnableScheduledDateLessThanEqualAndEnableScheduledDateNot(Long enableScheduledDate,
			Long excludedValue);

	List<TsmpApi> findByDisableScheduledDateLessThanEqualAndDisableScheduledDateNot(Long disableScheduledDate,
			Long excludedValue);

	List<TsmpApi> findByScheduledLaunchDateLessThanEqualAndScheduledLaunchDateNot(Long scheduledLaunchDate,
			Long excludedValue);
}
