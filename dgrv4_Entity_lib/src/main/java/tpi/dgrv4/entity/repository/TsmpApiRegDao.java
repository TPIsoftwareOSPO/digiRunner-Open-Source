package tpi.dgrv4.entity.repository;

import java.util.AbstractMap;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;

public interface TsmpApiRegDao extends JpaRepository<TsmpApiReg, TsmpApiRegId> {

	public List<TsmpApiReg> findByApiKeyAndModuleNameAndApiUuid(String apiKey, String moduleName, String apiUUID);

	public List<TsmpApiReg> findByCreateUser(String createUser);

	public List<TsmpApiReg> findByReghostId(String reghostId);

	TsmpApiReg findByModuleNameAndApiKey(String moduleName, String apiKey);

	List<String> query_AA0429SrcUrl();

	List<String> query_AA0429IpSrcUrl1();

	List<String> query_AA0429IpSrcUrl2();

	List<String> query_AA0429IpSrcUrl3();

	List<String> query_AA0429IpSrcUrl4();

	List<String> query_AA0429IpSrcUrl5();

	int deleteNonSpecifiedContent(List<AbstractMap.SimpleEntry<String, String>> list);

	public List<TsmpApiReg> query_AA0423Service();

}