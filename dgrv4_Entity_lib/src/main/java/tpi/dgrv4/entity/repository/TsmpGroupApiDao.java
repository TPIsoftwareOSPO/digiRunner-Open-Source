package tpi.dgrv4.entity.repository;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpGroupApiId;
import tpi.dgrv4.entity.entity.TsmpGroupApiTopN;
import tpi.dgrv4.entity.vo.AA0237ReqB;

@Repository
@Transactional //因為JPQL原先的設計只能查詢,所以不需要設定 @Transactional,但JPA 1.7以後的版本支持Query命名的方法改為 delete/remove/count, 就需要設定@Transactional
public interface TsmpGroupApiDao extends JpaRepository<TsmpGroupApi, TsmpGroupApiId> {

	public long deleteByGroupId(String groupId);

	public long deleteByGroupIdAndModuleName(String groupId, String moduleName);

	public List<TsmpGroupApi> findByGroupId(String groupId);
	
	public List<TsmpGroupApi> findByGroupIdOrderByModuleName(String groupId);

	public List<TsmpGroupApi> query_aa0237Service(TsmpGroupApi tsmpGroupApi, String vgroupId, AA0237ReqB reqB, String[] words, Integer pageSize);

    public List<String> findUniqueModuleByGroupIdOrderByModuleName(String groupId);
    
    public List<TsmpGroupApi> findByGroupIdAndModuleName(String groupId, String moduleName);
	
    public List<TsmpGroupApi> findByTsmpGroupApiIdAndKeyword(TsmpGroupApi lastTsmpGroupApi, String groupId, String moduleName, String[] keyword, int pageSize);
    
    public List<TsmpGroupApi> findByApiKeyAndModuleName(String apiKey, String moduleName);
    
    public List<TsmpGroupApi> query_aa0320Service(String gId, String moduleName, String apiKey, String[] keywords, int pageSize);

    public long deleteByApiKeyAndModuleName(String apiKey, String moduleName);
    
    public List<TsmpGroupApi> findByModuleName(String moduleName);
    
	public List<TsmpGroupApiTopN> findReferApiBindDpApp(String apiKey, String moduleNmae, String clientId,
			Integer pageSize);
    
    public String findGroupIdByDpAppClientIdWithOutVgroup(String clientId);
    
    public List<TsmpGroupApi> findByDpAppClientIdWithOutVgroup(String clientId);
    
    public List<TsmpGroupApi> findByDpAppClientIdWithOutGroup(String clientId);
    
    public boolean existsByGroupIdAndApiKeyAndModuleName(String groupId, String apiKey, String moduleName);
    
    //若是vgroup回傳才會是單筆
    public TsmpGroupApi findFirstByGroupId(String groupId);
    
    //若是vgroup回傳才會是單筆,因為vgoupId對應groupId不會有重複的apikey+moduleName
    public TsmpGroupApi findFirstByGroupIdInAndApiKeyAndModuleName(List<String> groupIdList, String apiKey, String moduleName);
    
}