package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpSeqStoreKey;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.constant.AnalyzeClientRelatedDataStatus;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA1120DgrApikeyUseApi;
import tpi.dgrv4.dpaa.vo.AA1120ImportClientRelated;
import tpi.dgrv4.dpaa.vo.AA1121Client;
import tpi.dgrv4.dpaa.vo.AA1121Group;
import tpi.dgrv4.dpaa.vo.AA1121GroupAuth;
import tpi.dgrv4.dpaa.vo.AA1121LackApi;
import tpi.dgrv4.dpaa.vo.AA1121RdbConnection;
import tpi.dgrv4.dpaa.vo.AA1121Resp;
import tpi.dgrv4.dpaa.vo.AA1121SecurityLevel;
import tpi.dgrv4.dpaa.vo.AA1121Vgroup;
import tpi.dgrv4.dpaa.vo.AA1129Req;
import tpi.dgrv4.dpaa.vo.AA1129Resp;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoA;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoJdbc;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoL;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoO;
import tpi.dgrv4.entity.entity.DgrImportClientRelatedTemp;
import tpi.dgrv4.entity.entity.DgrRdbConnection;
import tpi.dgrv4.entity.entity.DgrXApiKey;
import tpi.dgrv4.entity.entity.DgrXApiKeyMap;
import tpi.dgrv4.entity.entity.DpApp;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpClientGroup;
import tpi.dgrv4.entity.entity.TsmpClientVgroup;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.entity.TsmpGroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.entity.TsmpOpenApiKeyMap;
import tpi.dgrv4.entity.entity.TsmpVgroup;
import tpi.dgrv4.entity.entity.TsmpVgroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.TsmpVgroupGroup;
import tpi.dgrv4.entity.entity.jpql.TsmpClientHost;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoADao;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoJdbcDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoLDao;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoODao;
import tpi.dgrv4.entity.repository.DgrImportClientRelatedTempDao;
import tpi.dgrv4.entity.repository.DgrRdbConnectionDao;
import tpi.dgrv4.entity.repository.DgrXApiKeyDao;
import tpi.dgrv4.entity.repository.DgrXApiKeyMapDao;
import tpi.dgrv4.entity.repository.DpAppDao;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.entity.repository.TsmpClientHostDao;
import tpi.dgrv4.entity.repository.TsmpClientVgroupDao;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesMapDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyMapDao;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.entity.repository.TsmpVgroupAuthoritiesMapDao;
import tpi.dgrv4.entity.repository.TsmpVgroupDao;
import tpi.dgrv4.entity.repository.TsmpVgroupGroupDao;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiCacheProxy;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1129Service {

	private TsmpClientDao tsmpClientDao;
	private TsmpClientHostDao tsmpClientHostDao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private TsmpClientVgroupDao tsmpClientVgroupDao;
	private TsmpGroupDao tsmpGroupDao;
	private TsmpClientGroupDao tsmpClientGroupDao;
	private TsmpVgroupGroupDao tsmpVgroupGroupDao;
	private TsmpGroupApiDao tsmpGroupApiDao;
	private DgrXApiKeyDao dgrXApiKeyDao;
	private TsmpVgroupDao tsmpVgroupDao;
	private DgrXApiKeyMapDao dgrXApiKeyMapDao;
	private TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao;
	private TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao;
	private TsmpSecurityLevelDao tsmpSecurityLevelDao;
	private DpAppDao dpAppDao;
	private TsmpOpenApiKeyDao tsmpOpenApiKeyDao;
	private TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao;
	private TsmpVgroupAuthoritiesMapDao tsmpVgroupAuthoritiesMapDao;
	private DgrGtwIdpInfoODao dgrGtwIdpInfoODao;
	private DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao;
	private DgrGtwIdpInfoADao dgrGtwIdpInfoADao;
	private TsmpApiDao tsmpApiDao;
	private DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao;
	private DgrImportClientRelatedTempDao dgrImportClientRelatedTempDao;
	private ObjectMapper objectMapper;
	private SeqStoreService seqStoreService;
	private TsmpApiCacheProxy tsmpApiCacheProxy;
	private DgrRdbConnectionDao dgrRdbConnectionDao;

	@Autowired
	public AA1129Service(TsmpClientDao tsmpClientDao, TsmpClientHostDao tsmpClientHostDao,
			TsmpDpClientextDao tsmpDpClientextDao, OauthClientDetailsDao oauthClientDetailsDao,
			TsmpClientVgroupDao tsmpClientVgroupDao, TsmpGroupDao tsmpGroupDao, TsmpClientGroupDao tsmpClientGroupDao,
			TsmpVgroupGroupDao tsmpVgroupGroupDao, TsmpGroupApiDao tsmpGroupApiDao, DgrXApiKeyDao dgrXApiKeyDao,
			TsmpVgroupDao tsmpVgroupDao, DgrXApiKeyMapDao dgrXApiKeyMapDao,
			TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao, TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao,
			TsmpSecurityLevelDao tsmpSecurityLevelDao, DpAppDao dpAppDao, TsmpOpenApiKeyDao tsmpOpenApiKeyDao,
			TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao, TsmpVgroupAuthoritiesMapDao tsmpVgroupAuthoritiesMapDao,
			DgrGtwIdpInfoODao dgrGtwIdpInfoODao, DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao,
			DgrGtwIdpInfoADao dgrGtwIdpInfoADao, TsmpApiDao tsmpApiDao, DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao,
			DgrImportClientRelatedTempDao dgrImportClientRelatedTempDao, ObjectMapper objectMapper,
			SeqStoreService seqStoreService, TsmpApiCacheProxy tsmpApiCacheProxy,
			DgrRdbConnectionDao dgrRdbConnectionDao) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpClientHostDao = tsmpClientHostDao;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.tsmpClientVgroupDao = tsmpClientVgroupDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.tsmpVgroupGroupDao = tsmpVgroupGroupDao;
		this.tsmpGroupApiDao = tsmpGroupApiDao;
		this.dgrXApiKeyDao = dgrXApiKeyDao;
		this.tsmpVgroupDao = tsmpVgroupDao;
		this.dgrXApiKeyMapDao = dgrXApiKeyMapDao;
		this.tsmpGroupAuthoritiesDao = tsmpGroupAuthoritiesDao;
		this.tsmpGroupAuthoritiesMapDao = tsmpGroupAuthoritiesMapDao;
		this.tsmpSecurityLevelDao = tsmpSecurityLevelDao;
		this.dpAppDao = dpAppDao;
		this.tsmpOpenApiKeyDao = tsmpOpenApiKeyDao;
		this.tsmpOpenApiKeyMapDao = tsmpOpenApiKeyMapDao;
		this.tsmpVgroupAuthoritiesMapDao = tsmpVgroupAuthoritiesMapDao;
		this.dgrGtwIdpInfoODao = dgrGtwIdpInfoODao;
		this.dgrGtwIdpInfoLDao = dgrGtwIdpInfoLDao;
		this.dgrGtwIdpInfoADao = dgrGtwIdpInfoADao;
		this.tsmpApiDao = tsmpApiDao;
		this.dgrGtwIdpInfoJdbcDao = dgrGtwIdpInfoJdbcDao;
		this.dgrImportClientRelatedTempDao = dgrImportClientRelatedTempDao;
		this.objectMapper = objectMapper;
		this.seqStoreService = seqStoreService;
		this.tsmpApiCacheProxy = tsmpApiCacheProxy;
		this.dgrRdbConnectionDao = dgrRdbConnectionDao;
	}

	@Transactional
	public AA1129Resp importClientRelatedConfirm(TsmpAuthorization auth, AA1129Req req) {
		AA1129Resp resp = new AA1129Resp();

		try {
			checkParam(req);
			DgrImportClientRelatedTemp tempVo = getDgrImportClientRelatedTempDao().findById(Long.valueOf(req.getLongId())).orElse(null);
			if(tempVo == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			byte[] arrAnalyze = tempVo.getAnalyzeClientRelated();
			AA1121Resp analyzeVo = getObjectMapper().readValue(arrAnalyze, AA1121Resp.class);
			
			byte[] arrImport = tempVo.getImportClientRelated();
			AA1120ImportClientRelated importVo = getObjectMapper().readValue(arrImport, AA1120ImportClientRelated.class);
		    
			Date nowDate = getNowDate();
			//以下method裡會有ServiceUtil.deepCopy是因為filter出來的資料是傳址的,所以變更值會影響到List,若有其他地方用到,需要deepCopy
			this.handleRdbConnection(analyzeVo, importVo, auth, nowDate);
			this.handleGroupAuth(analyzeVo, importVo);
			this.handleSecurityLevel(analyzeVo, importVo);
			this.handleGroup(analyzeVo, importVo, auth, nowDate);
			this.handleVgroup(analyzeVo, importVo, auth, nowDate);
			this.handleClient(analyzeVo, importVo, auth, nowDate);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	public void handleClient(AA1121Resp analyzeClientRelateVo, AA1120ImportClientRelated importClientRelateVo, TsmpAuthorization auth, Date nowDate) {
		List<AA1121LackApi> lackApiList = analyzeClientRelateVo.getLackApiList();
		List<AA1121Client> analyzeList = analyzeClientRelateVo.getClientList();
		analyzeList = analyzeList.stream().filter(f->AnalyzeClientRelatedDataStatus.A.name().equals(f.getDataStatus())
				   || AnalyzeClientRelatedDataStatus.C.name().equals(f.getDataStatus())).collect(Collectors.toList());
		
		List<TsmpClient> importList = importClientRelateVo.getTsmpClientList();
		for(AA1121Client analyzeVo: analyzeList) {
			TsmpClient clientVo = importList.stream().filter(importVo -> 
			   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).findAny().orElse(null);
			if(clientVo == null) {//理論上一定有值,防例外(EX:有人修改檔案上傳)
				TPILogger.tl.error("import TsmpClient data not found, clientId is "+ analyzeVo.getClientId());
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			//新增
			if(AnalyzeClientRelatedDataStatus.A.name().equals(analyzeVo.getDataStatus())) {
				//TsmpClient
				//密碼錯誤次數變成0
				clientVo.setPwdFailTimes(0);
				clientVo.setCreateTime(nowDate);
				clientVo.setCreateUser(auth.getUserName());
				getTsmpClientDao().save(clientVo);
				
				//TsmpClientHost(不一定有資料)
				List<TsmpClientHost> hostList = importClientRelateVo.getTsmpClientHostList();
				hostList = hostList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				//此動作是新增
				for(TsmpClientHost aHostVo : hostList) {
					Long hostSeq = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_CLIENT_HOST_PK);
					aHostVo.setHostSeq(hostSeq);
					aHostVo.setCreateTime(nowDate);
					getTsmpClientHostDao().save(aHostVo);
				}
				
				//TsmpClientGroup,TsmpClientVgroup(group和vgroup資料一定存在,因為有client在使用是不能刪的)
				List<TsmpClientGroup> clientGroupList = importClientRelateVo.getTsmpClientGroupList();
				clientGroupList = clientGroupList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				List<String> executedVgroupList = new ArrayList<>();
				for(TsmpClientGroup aClientGroupVo : clientGroupList) {//可能不會跑迴圈,因為clientId可以不用有group
					TsmpGroup aImportGroupVo = importClientRelateVo.getTsmpGroupList().stream().filter(importVo -> 
					   importVo.getGroupId().equals(aClientGroupVo.getGroupId())).findAny().orElse(null);
					if(aImportGroupVo == null) {//理論上一定有值,防例外(EX:有人修改檔案上傳)
						TPILogger.tl.error("import TsmpGroup data not found, GroupId is "+ aClientGroupVo.getGroupId());
						throw TsmpDpAaRtnCode._1298.throwing();
					}
					//tsmp_vgroup
					if("1".equals(aImportGroupVo.getVgroupFlag())) {
						//因為在TsmpClientGroup同組vgroup的name是一樣,執行過一次就可以了
						if(executedVgroupList.contains(aImportGroupVo.getVgroupName().toUpperCase())) {
							continue;
						}
						TsmpVgroup dbVgroupVo = this.getTsmpVgroupDao().findFirstByVgroupName(aImportGroupVo.getVgroupName());
						if(dbVgroupVo == null) {//理論上一定有值(在handleVgroup實作了),防例外(EX:有人修改檔案上傳)
							TPILogger.tl.error("db TsmpVgroup data not found, VgroupName is "+ aImportGroupVo.getVgroupName());
							throw TsmpDpAaRtnCode._1298.throwing();
						}
						executedVgroupList.add(aImportGroupVo.getVgroupName().toUpperCase());
						//在handleVgroup已實作vgroupGroup的對應資料了
						List<TsmpVgroupGroup> dbVgroupGroupList = this.getTsmpVgroupGroupDao().findByVgroupId(dbVgroupVo.getVgroupId());
						//TsmpClientGroup, 此動作是新增
						for(TsmpVgroupGroup dbVgroupGroupVo : dbVgroupGroupList) {
							TsmpClientGroup dbClientGroupVo = new TsmpClientGroup();
							dbClientGroupVo.setClientId(aClientGroupVo.getClientId());
							dbClientGroupVo.setGroupId(dbVgroupGroupVo.getGroupId());
							this.getTsmpClientGroupDao().save(dbClientGroupVo);
						}
						
						//TsmpClientVgroup, 此動作是新增
						if(!CollectionUtils.isEmpty(dbVgroupGroupList)) {
							TsmpClientVgroup dbClientVgroupVo = new TsmpClientVgroup();
							dbClientVgroupVo.setClientId(aClientGroupVo.getClientId());
							dbClientVgroupVo.setVgroupId(dbVgroupVo.getVgroupId());
							this.getTsmpClientVgroupDao().save(dbClientVgroupVo);
						}
						
					}else {//tsmp_group
						TsmpGroup dbGroupVo = this.getTsmpGroupDao().findFirstByGroupNameAndVgroupFlag(aImportGroupVo.getGroupName(), aImportGroupVo.getVgroupFlag());
						if(dbGroupVo == null) {//理論上一定有值(在handleGroup實作了),防例外(EX:有人修改檔案上傳)
							TPILogger.tl.error("db TsmpGroup data not found, GroupName is "+ aImportGroupVo.getGroupName()+", VgroupFlag is "+ aImportGroupVo.getVgroupFlag());
							throw TsmpDpAaRtnCode._1298.throwing();
						}
						//TsmpClientGroup, 此動作是新增
						TsmpClientGroup dbClientGroupVo = new TsmpClientGroup();
						dbClientGroupVo.setClientId(aClientGroupVo.getClientId());
						dbClientGroupVo.setGroupId(dbGroupVo.getGroupId());
						this.getTsmpClientGroupDao().save(dbClientGroupVo);
					}					
				}
				
				//處理Vgroup無授權API導致Group無資料的情境,須將Vgroup另外新增
				List<TsmpClientVgroup> clientVgroupList = importClientRelateVo.getTsmpClientVgroupList().stream().filter(clientVgroupVo -> clientVgroupVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				if(null!=clientVgroupList && clientVgroupList.size()>0) {
					for(TsmpClientVgroup clientVgroupVo : clientVgroupList) {
						TsmpVgroup importVgroupVo = importClientRelateVo.getTsmpVgroupList().stream().filter(importVo -> importVo.getVgroupId().equalsIgnoreCase(clientVgroupVo.getVgroupId())).findAny().orElse(null);
						if(importVgroupVo == null) {
							TPILogger.tl.error("import TsmpVgroup data not found, VgroupId is "+ clientVgroupVo.getVgroupId());
							throw TsmpDpAaRtnCode._1298.throwing();
						}
						if(executedVgroupList.contains(importVgroupVo.getVgroupName().toUpperCase())) {
							continue;
						}
						TsmpVgroup dbVgroupVo = this.getTsmpVgroupDao().findFirstByVgroupName(importVgroupVo.getVgroupName());
						if(dbVgroupVo == null) {//理論上一定有值(在handleVgroup實作了),防例外(EX:有人修改檔案上傳)
							TPILogger.tl.error("db TsmpVgroup data not found, VgroupName is "+ importVgroupVo.getVgroupName());
							throw TsmpDpAaRtnCode._1298.throwing();
						}
						TsmpClientVgroup dbClientVgroupVo = new TsmpClientVgroup();
						dbClientVgroupVo.setClientId(clientVgroupVo.getClientId());
						dbClientVgroupVo.setVgroupId(dbVgroupVo.getVgroupId());
						this.getTsmpClientVgroupDao().save(dbClientVgroupVo);
					}
				}			
				
				long aCountGroup = this.getTsmpClientGroupDao().countByClientId(analyzeVo.getClientId());
				if(aCountGroup > 186) {
					throw TsmpDpAaRtnCode._1547.throwing(analyzeVo.getClientId(), "186");
				}
				
				//TsmpDpClientext
				List<TsmpDpClientext> importDpClientextList = importClientRelateVo.getTsmpDpClientextList();
				TsmpDpClientext importDpClientextVo = importDpClientextList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).findAny().orElse(null);
				if(importDpClientextVo == null) {//理論上一定有值,防例外(EX:有人修改檔案上傳)
					TPILogger.tl.error("import TsmpDpClientext data not found, clientId is "+ analyzeVo.getClientId());
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				//此動作是新增
				final Long clientSeqId = getSeqStoreService().nextSequence(//
						TsmpDpSeqStoreKey.TSMP_DP_CLIENTEXT, 2000000000L, 1L);
				importDpClientextVo.setClientSeqId(clientSeqId);
				importDpClientextVo.setCreateDateTime(nowDate);
				importDpClientextVo.setCreateUser(auth.getUserName());
				if(StringUtils.hasText(importDpClientextVo.getResubmitDateTimeForLongTime())) {
					importDpClientextVo.setResubmitDateTime(new Date(Long.valueOf(importDpClientextVo.getResubmitDateTimeForLongTime())));
				}
				getTsmpDpClientextDao().save(importDpClientextVo);
				
				//OauthClientDetails
				List<OauthClientDetails> oauthList = importClientRelateVo.getOauthClientDetailsList();
				OauthClientDetails oauthVo = oauthList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).findAny().orElse(null);
				if(oauthVo == null) {//理論上一定有值,防例外(EX:有人修改檔案上傳)
					TPILogger.tl.error("import OauthClientDetails data not found, clientId is "+ analyzeVo.getClientId());
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				List<TsmpClientGroup> dbClientGroupList = this.getTsmpClientGroupDao().findByClientId(analyzeVo.getClientId());
				List<String> dbClientGroupIdList = dbClientGroupList.stream().map(vo->vo.getGroupId()).collect(Collectors.toList());
				//此動作是新增
				if(dbClientGroupIdList.size() == 0) {
					oauthVo.setScope("select");
				}else {
					oauthVo.setScope(String.join(",", dbClientGroupIdList));
				}
				getOauthClientDetailsDao().save(oauthVo);
				
				//不一定有資料,DgrXApiKey, DgrXApiKeyMap
				List<DgrXApiKey> dgrXList = importClientRelateVo.getDgrXApiKeyList();
				dgrXList = dgrXList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				List<DgrXApiKeyMap> importDgrXMapList = importClientRelateVo.getDgrXApiKeyMapList();
				for(DgrXApiKey aDgrXVo : dgrXList) {
					List<DgrXApiKeyMap> dgrXMapList = importDgrXMapList.stream().filter(filterDgrXMapVo-> 
						filterDgrXMapVo.getRefApiKeyId().equals(aDgrXVo.getApiKeyId())).collect(Collectors.toList());
					if(CollectionUtils.isEmpty(dgrXMapList)) {//理論上一定有值(一定要選group),防例外(EX:有人修改檔案上傳)
						TPILogger.tl.error("import DgrXApiKeyMap data not found, refApiKeyId is "+ aDgrXVo.getApiKeyId());
						throw TsmpDpAaRtnCode._1298.throwing();
					}
					//此動作是新增(無Map也新增,因為可建立後再去刪group,一樣造成master無group)
					aDgrXVo.setApiKeyId(null);
					aDgrXVo.setCreateDateTime(nowDate);
					aDgrXVo.setCreateUser(auth.getUserName());
					this.getDgrXApiKeyDao().save(aDgrXVo);

					for(DgrXApiKeyMap dgrXMapVo : dgrXMapList) {
						TsmpGroup aGroupVo = importClientRelateVo.getTsmpGroupList().stream().filter(filterGroupVo-> 
								filterGroupVo.getGroupId().equals(dgrXMapVo.getGroupId())).findAny().orElse(null);
						if(aGroupVo != null) {//有可能該group已經被刪除了,但groupId資料還是存在DgrXApiKeyMap
							TsmpGroup dbGroupVo = this.getTsmpGroupDao().findFirstByGroupNameAndVgroupFlag(aGroupVo.getGroupName(), aGroupVo.getVgroupFlag());
							
							if(dbGroupVo != null) {//因為Map是有辦法沒group的,加個防範
								DgrXApiKeyMap copyDgrXMapVo = ServiceUtil.deepCopy(dgrXMapVo, DgrXApiKeyMap.class);
								//此動作是新增
								copyDgrXMapVo.setApiKeyMapId(null);
								copyDgrXMapVo.setRefApiKeyId(aDgrXVo.getApiKeyId());
								copyDgrXMapVo.setGroupId(dbGroupVo.getGroupId());
								copyDgrXMapVo.setCreateDateTime(nowDate);
								copyDgrXMapVo.setCreateUser(auth.getUserName());
								this.getDgrXApiKeyMapDao().save(copyDgrXMapVo);
							}else {
								TPILogger.tl.error("no throw, db TsmpGroup data not found, GroupName is "+ aGroupVo.getGroupName()+", VgroupFlag is "+aGroupVo.getVgroupFlag());
							}
						}
						
					}	
				}
				
				//不一定有資料,dpApp
				List<DpApp> dpAppList = importClientRelateVo.getDpAppList();
				DpApp dpAppVo = dpAppList.stream().filter(importVo-> 
								importVo.getClientId().equals(analyzeVo.getClientId())).findAny().orElse(null);
				//此動作是新增
				if(dpAppVo != null) {
					dpAppVo.setDpApplicationId(null);
					dpAppVo.setCreateDateTime(nowDate);
					dpAppVo.setCreateUser(auth.getUserName());
					this.getDpAppDao().save(dpAppVo);
				}
				
				//不一定有資料,TsmpOpenApiKey, TsmpOpenApiKeyMap
				List<TsmpOpenApiKey> openList = importClientRelateVo.getTsmpOpenApiKeyList();
				openList = openList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				List<TsmpOpenApiKeyMap> importOpenMapList = importClientRelateVo.getTsmpOpenApiKeyMapList();
				for(TsmpOpenApiKey aOpenVo : openList) {
					List<TsmpOpenApiKeyMap> openMapList = importOpenMapList.stream().filter(filterOpenMapVo-> 
					  filterOpenMapVo.getRefOpenApiKeyId().equals(aOpenVo.getOpenApiKeyId())).collect(Collectors.toList());
					
					//此動作是新增(無Map也新增,因為可建立後再去刪api,一樣造成master無api)
					aOpenVo.setOpenApiKeyId(null);
					aOpenVo.setCreateDateTime(nowDate);
					aOpenVo.setCreateUser(auth.getUserName());
					TsmpOpenApiKey savedOpenVo = this.getTsmpOpenApiKeyDao().save(aOpenVo);
					for(TsmpOpenApiKeyMap openMapVo : openMapList) {
						AA1120DgrApikeyUseApi aUseApiVo = importClientRelateVo.getDgrApikeyUseApiList().stream().filter(filterUseApiVo-> 
													filterUseApiVo.getApiUid().equals(openMapVo.getRefApiUid())).findAny().orElse(null);
						if(aUseApiVo != null) {//不一定存在,因為該api是可以刪除的,可能在AA1120就沒存放了
							Optional<AA1121LackApi> lackApiOpt = lackApiList.stream().filter(f->f.getModuleName().equalsIgnoreCase(aUseApiVo.getModuleName()) 
									&& f.getApiId().equalsIgnoreCase(aUseApiVo.getApiId())).findAny();
							if(lackApiOpt.isEmpty()) {
								TsmpApiId idVo = new TsmpApiId(aUseApiVo.getApiId(), aUseApiVo.getModuleName());
								TsmpApi dbTsmpApiVo = this.getTsmpApiCacheProxy().findById(idVo).orElse(null);
								if(dbTsmpApiVo != null) {//防範有可能是缺少的API
									TsmpOpenApiKeyMap aCopyOpenMapVo = ServiceUtil.deepCopy(openMapVo, TsmpOpenApiKeyMap.class);
									//此動作是新增
									aCopyOpenMapVo.setOpenApiKeyMapId(null);
									aCopyOpenMapVo.setRefOpenApiKeyId(savedOpenVo.getOpenApiKeyId());
									aCopyOpenMapVo.setRefApiUid(dbTsmpApiVo.getApiUid());
									aCopyOpenMapVo.setCreateDateTime(nowDate);
									aCopyOpenMapVo.setCreateUser(auth.getUserName());
									this.getTsmpOpenApiKeyMapDao().save(aCopyOpenMapVo);
								}else {
									TPILogger.tl.error("no throw, db TsmpApi data not found, ModuleName is "+ aUseApiVo.getModuleName()+", apiId is "+aUseApiVo.getApiId());
								}
							}
						}
						
					}
				}
				
				//不一定有資料,DgrGtwIdpInfoO
				List<DgrGtwIdpInfoO> oList = importClientRelateVo.getDgrGtwIdpInfoOList();
				oList = oList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				for(DgrGtwIdpInfoO oVo : oList) {
					//oVo.setGtwIdpInfoOId(null);
					oVo.setCreateDateTime(nowDate);
					oVo.setCreateUser(auth.getUserName());
					
					this.getDgrGtwIdpInfoODao().save(oVo);
				}
				
				//不一定有資料,DgrGtwIdpInfoL
				List<DgrGtwIdpInfoL> lList = importClientRelateVo.getDgrGtwIdpInfoLList();
				lList = lList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				for(DgrGtwIdpInfoL lVo : lList) {
					//lVo.setGtwIdpInfoLId(null);
					lVo.setCreateDateTime(nowDate);
					lVo.setCreateUser(auth.getUserName());
					
					this.getDgrGtwIdpInfoLDao().save(lVo);
				}
				
				//不一定有資料,DgrGtwIdpInfoA
				List<DgrGtwIdpInfoA> aList = importClientRelateVo.getDgrGtwIdpInfoAList();
				aList = aList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				for(DgrGtwIdpInfoA aVo : aList) {
					//aVo.setGtwIdpInfoAId(null);
					aVo.setCreateDateTime(nowDate);
					aVo.setCreateUser(auth.getUserName());
					
					this.getDgrGtwIdpInfoADao().save(aVo);
				}
				
				//不一定有資料,DgrGtwIdpInfoJdbc
				List<DgrGtwIdpInfoJdbc> jdbcList = importClientRelateVo.getDgrGtwIdpInfoJdbcList();
				jdbcList = jdbcList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				for(DgrGtwIdpInfoJdbc jdbcVo : jdbcList) {
					//不用檢查connectionName,因為一定存在
					//jdbcVo.setGtwIdpInfoJdbcId(null);
					jdbcVo.setCreateDateTime(nowDate);
					jdbcVo.setCreateUser(auth.getUserName());
					
					this.getDgrGtwIdpInfoJdbcDao().save(jdbcVo);
				}
			}else {
				//TsmpClient
				TsmpClient dbClientVo = this.getTsmpClientDao().findById(clientVo.getClientId()).orElse(null);
				if(dbClientVo == null) {//理論上一定有值,因為被分析為重覆資料,防例外(EX:分析完後使用者去db把資料砍了)
					TPILogger.tl.error("db TsmpClient data not found, ClientId is "+ clientVo.getClientId());
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				//密碼已錯誤次數不覆蓋,此動作是更新
				clientVo.setPwdFailTimes(dbClientVo.getPwdFailTimes());
				clientVo.setCreateTime(dbClientVo.getCreateTime());
				clientVo.setCreateUser(dbClientVo.getCreateUser());
				clientVo.setUpdateTime(nowDate);
				clientVo.setUpdateUser(auth.getUserName());
				getTsmpClientDao().save(clientVo);
				
				//TsmpClientHost
				List<TsmpClientHost> hostList = importClientRelateVo.getTsmpClientHostList();
				hostList = hostList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				List<TsmpClientHost> dbHostList =  this.getTsmpClientHostDao().findByClientId(clientVo.getClientId());
				//此動作是更新或新增
				for(TsmpClientHost hostVo : hostList) {
					TsmpClientHost dbHostVo = dbHostList.stream().filter(filterHostVo-> 
												filterHostVo.getHostIp().equalsIgnoreCase(hostVo.getHostIp()) 
												|| filterHostVo.getHostName().equalsIgnoreCase(hostVo.getHostName()))
												.findAny().orElse(null);
					if(dbHostVo != null) {//代表有重覆
						//更新
						hostVo.setHostSeq(dbHostVo.getHostSeq());
						hostVo.setCreateTime(nowDate);
						getTsmpClientHostDao().save(hostVo);
					}else {
						//新增
						Long hostSeq = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_CLIENT_HOST_PK);
						hostVo.setHostSeq(hostSeq);
						hostVo.setCreateTime(nowDate);
						getTsmpClientHostDao().save(hostVo);
					}
				}
				
				//TsmpClientGroup,TsmpClientVgroup(group和vgroup資料一定存在,因為有client在使用中是不能刪的)
				List<TsmpClientGroup> clientGroupList = importClientRelateVo.getTsmpClientGroupList();
				clientGroupList = clientGroupList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				List<String> executedVgroupList = new ArrayList<>();

				for(TsmpClientGroup clientGroupVo : clientGroupList) {
					TsmpGroup importGroupVo = importClientRelateVo.getTsmpGroupList().stream().filter(importVo -> 
					   importVo.getGroupId().equals(clientGroupVo.getGroupId())).findAny().orElse(null);
					if(importGroupVo == null) {
						TPILogger.tl.error("import TsmpGroup data not found, GroupId is "+ clientGroupVo.getGroupId());
						throw TsmpDpAaRtnCode._1298.throwing();
					}
					//tsmp_vgroup
					if("1".equals(importGroupVo.getVgroupFlag())) {
						//因為在TsmpClientGroup同組vgroup的name是一樣,執行過一次就可以了
						if(executedVgroupList.contains(importGroupVo.getVgroupName().toUpperCase())) {
							continue;
						}
						TsmpVgroup dbVgroupVo = this.getTsmpVgroupDao().findFirstByVgroupName(importGroupVo.getVgroupName());
						if(dbVgroupVo == null) {//沒API也可建立主表tsmp_vgroup
							TPILogger.tl.error("db TsmpVGroup data not found, VgroupName is "+ importGroupVo.getVgroupName());
							throw TsmpDpAaRtnCode._1298.throwing();
						}
						executedVgroupList.add(importGroupVo.getVgroupName().toUpperCase());
						List<TsmpVgroupGroup> dbVgroupGroupList = this.getTsmpVgroupGroupDao().findByVgroupId(dbVgroupVo.getVgroupId());
						//TsmpClientGroup, 此動作是新增或更新
						for(TsmpVgroupGroup dbVgroupGroupVo : dbVgroupGroupList) {
							TsmpClientGroup dbClientGroupVo = new TsmpClientGroup();
							dbClientGroupVo.setClientId(clientGroupVo.getClientId());
							dbClientGroupVo.setGroupId(dbVgroupGroupVo.getGroupId());
							this.getTsmpClientGroupDao().save(dbClientGroupVo);
						}
						
						//TsmpClientVgroup, 此動作是新增或更新
						if(!CollectionUtils.isEmpty(dbVgroupGroupList)) {
							TsmpClientVgroup dbClientVgroupVo = new TsmpClientVgroup();
							dbClientVgroupVo.setClientId(clientGroupVo.getClientId());
							dbClientVgroupVo.setVgroupId(dbVgroupVo.getVgroupId());
							this.getTsmpClientVgroupDao().save(dbClientVgroupVo);
						}
						
					}else {//tsmp_group
						TsmpGroup dbGroupVo = this.getTsmpGroupDao().findFirstByGroupNameAndVgroupFlag(importGroupVo.getGroupName(), importGroupVo.getVgroupFlag());
						if(dbGroupVo == null) {//理論上一定有值,因為被分析為重覆資料,防例外(EX:分析完後使用者去db把資料砍了)
							TPILogger.tl.error("db TsmpGroup data not found, GroupName is "+ importGroupVo.getGroupName() +", VgroupFlag is " + importGroupVo.getVgroupFlag());
							throw TsmpDpAaRtnCode._1298.throwing();
						}
						//TsmpClientGroup, 此動作是新增或更新
						TsmpClientGroup dbClientGroupVo = new TsmpClientGroup();
						dbClientGroupVo.setClientId(clientGroupVo.getClientId());
						dbClientGroupVo.setGroupId(dbGroupVo.getGroupId());
						this.getTsmpClientGroupDao().save(dbClientGroupVo);
					}
					
				}
				
				//處理Vgroup無授權API導致Group無資料的情境,須將Vgroup另外新增
				List<TsmpClientVgroup> clientVgroupList = importClientRelateVo.getTsmpClientVgroupList().stream().filter(clientVgroupVo -> clientVgroupVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				if(null!=clientVgroupList && clientVgroupList.size()>0) {
					for(TsmpClientVgroup clientVgroupVo : clientVgroupList) {
						TsmpVgroup importVgroupVo = importClientRelateVo.getTsmpVgroupList().stream().filter(importVo -> importVo.getVgroupId().equalsIgnoreCase(clientVgroupVo.getVgroupId())).findAny().orElse(null);
						if(importVgroupVo == null) {
							TPILogger.tl.error("import TsmpVgroup data not found, VgroupId is "+ clientVgroupVo.getVgroupId());
							throw TsmpDpAaRtnCode._1298.throwing();
						}
						if(executedVgroupList.contains(importVgroupVo.getVgroupName().toUpperCase())) {
							continue;
						}
						TsmpVgroup dbVgroupVo = this.getTsmpVgroupDao().findFirstByVgroupName(importVgroupVo.getVgroupName());
						if(dbVgroupVo == null) {//理論上一定有值(在handleVgroup實作了),防例外(EX:有人修改檔案上傳)
							TPILogger.tl.error("db TsmpVgroup data not found, VgroupName is "+ importVgroupVo.getVgroupName());
							throw TsmpDpAaRtnCode._1298.throwing();
						}
						TsmpClientVgroup dbClientVgroupVo = new TsmpClientVgroup();
						dbClientVgroupVo.setClientId(clientVgroupVo.getClientId());
						dbClientVgroupVo.setVgroupId(dbVgroupVo.getVgroupId());
						this.getTsmpClientVgroupDao().save(dbClientVgroupVo);
					}
				}
				
				long countGroup = this.getTsmpClientGroupDao().countByClientId(analyzeVo.getClientId());
				if(countGroup > 186) {
					throw TsmpDpAaRtnCode._1547.throwing(analyzeVo.getClientId(), "186");
				}
				
				//TsmpDpClientext
				List<TsmpDpClientext> dpClientextList = importClientRelateVo.getTsmpDpClientextList();
				TsmpDpClientext importDpClientextVo = dpClientextList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).findAny().orElse(null);
				if(importDpClientextVo == null) {//理論上一定有值,防例外(EX:有人修改檔案上傳)
					TPILogger.tl.error("import TsmpDpClientext data not found, clientId is "+ analyzeVo.getClientId());
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				TsmpDpClientext dbDpClientextVo = this.getTsmpDpClientextDao().findByClientId(analyzeVo.getClientId());
				if(dbDpClientextVo == null) {//理論上一定有值,因為被分析為重覆資料,防例外(EX:分析完後使用者去db把資料砍了)
					TPILogger.tl.error("db TsmpDpClientext data not found, clientId is "+ analyzeVo.getClientId());
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				//此動作是更新
				importDpClientextVo.setClientSeqId(dbDpClientextVo.getClientSeqId());
				importDpClientextVo.setCreateDateTime(dbDpClientextVo.getCreateDateTime());
				importDpClientextVo.setCreateUser(dbDpClientextVo.getCreateUser());
				importDpClientextVo.setVersion(dbDpClientextVo.getVersion());
				importDpClientextVo.setUpdateDateTime(nowDate);
				importDpClientextVo.setUpdateUser(auth.getUserName());
				if(StringUtils.hasText(importDpClientextVo.getResubmitDateTimeForLongTime())) {
					importDpClientextVo.setResubmitDateTime(new Date(Long.valueOf(importDpClientextVo.getResubmitDateTimeForLongTime())));
				}
				
				getTsmpDpClientextDao().save(importDpClientextVo);
				
				
				//OauthClientDetails
				List<OauthClientDetails> oauthList = importClientRelateVo.getOauthClientDetailsList();
				OauthClientDetails oauthVo = oauthList.stream().filter(importVo -> 
				   importVo.getClientId().equals(analyzeVo.getClientId())).findAny().orElse(null);
				if(oauthVo == null) {//理論上一定有值,防例外(EX:有人修改檔案上傳)
					TPILogger.tl.error("import OauthClientDetails data not found, clientId is "+ analyzeVo.getClientId());
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				
				OauthClientDetails dbOauthVo = this.getOauthClientDetailsDao().findById(analyzeVo.getClientId()).orElse(null);
				if(dbOauthVo == null) {//理論上一定有值,因為被分析為重覆資料,防例外(EX:分析完後使用者去db把資料砍了)
					TPILogger.tl.error("db OauthClientDetails data not found, clientId is "+ analyzeVo.getClientId());
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				
				List<TsmpClientGroup> dbClientGroupList = this.getTsmpClientGroupDao().findByClientId(analyzeVo.getClientId());
				List<String> dbClientGroupIdList = dbClientGroupList.stream().map(vo->vo.getGroupId()).collect(Collectors.toList());
				//此動作是更新
				if(dbClientGroupIdList.size() == 0) {
					oauthVo.setScope("select");
				}else {
					oauthVo.setScope(String.join(",", dbClientGroupIdList));
				}
				//mima不變更
				oauthVo.setClientSecret(dbOauthVo.getClientSecret());
				getOauthClientDetailsDao().save(oauthVo);
				
				//不一定有資料,DgrXApiKey, DgrXApiKeyMap
				List<DgrXApiKey> dgrXList = importClientRelateVo.getDgrXApiKeyList();
				dgrXList = dgrXList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				List<DgrXApiKeyMap> importDgrXMapList = importClientRelateVo.getDgrXApiKeyMapList();
				for(DgrXApiKey dgrXVo : dgrXList) {
					List<DgrXApiKeyMap> dgrXMapList = importDgrXMapList.stream().filter(filterDgrXMapVo-> 
					  						filterDgrXMapVo.getRefApiKeyId().equals(dgrXVo.getApiKeyId())).collect(Collectors.toList());
					if(CollectionUtils.isEmpty(dgrXMapList)) {//因為刪除group並沒連動,它的值會一直存在,所以一定有值
						TPILogger.tl.error("import DgrXApiKeyMap data not found, refApiKeyId is "+ dgrXVo.getApiKeyId());
						throw TsmpDpAaRtnCode._1298.throwing();
					}
					DgrXApiKey dbDgrXVo = this.getDgrXApiKeyDao().findFirstByApiKeyEn(dgrXVo.getApiKeyEn());
					if(dbDgrXVo == null) {
						//此動作是新增(無Map也新增,因為可建立後再去刪group,一樣造成master無group)
						dgrXVo.setApiKeyId(null);
						dgrXVo.setCreateDateTime(nowDate);
						dgrXVo.setCreateUser(auth.getUserName());
						this.getDgrXApiKeyDao().save(dgrXVo);
						
						for(DgrXApiKeyMap dgrXMapVo : dgrXMapList) {
							TsmpGroup groupVo = importClientRelateVo.getTsmpGroupList().stream().filter(filterGroupVo-> 
									filterGroupVo.getGroupId().equals(dgrXMapVo.getGroupId())).findAny().orElse(null);
							if(groupVo != null) {//有可能該group已經被刪除了,但groupId資料還是存在DgrXApiKeyMap
								TsmpGroup dbGroupVo = this.getTsmpGroupDao().findFirstByGroupNameAndVgroupFlag(groupVo.getGroupName(), groupVo.getVgroupFlag());
								
								if(dbGroupVo != null) {//因為Map是有辦法沒group的,加個防範
									DgrXApiKeyMap copyDgrXMapVo = ServiceUtil.deepCopy(dgrXMapVo, DgrXApiKeyMap.class);
									//此動作是新增
									copyDgrXMapVo.setApiKeyMapId(null);
									copyDgrXMapVo.setRefApiKeyId(dgrXVo.getApiKeyId());
									copyDgrXMapVo.setGroupId(dbGroupVo.getGroupId());
									copyDgrXMapVo.setCreateDateTime(nowDate);
									copyDgrXMapVo.setCreateUser(auth.getUserName());
									this.getDgrXApiKeyMapDao().save(copyDgrXMapVo);
								}else {
									TPILogger.tl.error("no throw, db TsmpGroup data not found, GroupName is "+ groupVo.getGroupName()+", VgroupFlag is "+groupVo.getVgroupFlag());
								}
							}
						}
					}else {
						//此動作是更新
						dgrXVo.setApiKeyId(dbDgrXVo.getApiKeyId());
						dgrXVo.setCreateDateTime(dbDgrXVo.getCreateDateTime());
						dgrXVo.setCreateUser(dbDgrXVo.getCreateUser());
						dgrXVo.setVersion(dbDgrXVo.getVersion());
						dgrXVo.setUpdateDateTime(nowDate);
						dgrXVo.setUpdateUser(auth.getUserName());
						this.getDgrXApiKeyDao().save(dgrXVo);
						
						for(DgrXApiKeyMap dgrXMapVo : dgrXMapList) {
							TsmpGroup groupVo = importClientRelateVo.getTsmpGroupList().stream().filter(filterGroupVo-> 
									filterGroupVo.getGroupId().equals(dgrXMapVo.getGroupId())).findAny().orElse(null);
							if(groupVo != null) {//有可能該group已經被刪除了,但groupId資料還是存在DgrXApiKeyMap
								TsmpGroup dbGroupVo = this.getTsmpGroupDao().findFirstByGroupNameAndVgroupFlag(groupVo.getGroupName(), groupVo.getVgroupFlag());
								if(dbGroupVo != null) {//因為Map是有辦法沒group的,加個防範
									DgrXApiKeyMap dbDgrXMapVo = this.getDgrXApiKeyMapDao().findFirstByRefApiKeyIdAndGroupId(dbDgrXVo.getApiKeyId(), dbGroupVo.getGroupId());
									DgrXApiKeyMap copyDgrXMapVo = ServiceUtil.deepCopy(dgrXMapVo, DgrXApiKeyMap.class);
									if(dbDgrXMapVo == null) {
										//此動作是新增
										copyDgrXMapVo.setApiKeyMapId(null);
										copyDgrXMapVo.setRefApiKeyId(dbDgrXVo.getApiKeyId());
										copyDgrXMapVo.setGroupId(dbGroupVo.getGroupId());
										copyDgrXMapVo.setCreateDateTime(nowDate);
										copyDgrXMapVo.setCreateUser(auth.getUserName());
										this.getDgrXApiKeyMapDao().save(copyDgrXMapVo);
									}else {
										//此動作是更新
										copyDgrXMapVo.setApiKeyMapId(dbDgrXMapVo.getApiKeyMapId());
										copyDgrXMapVo.setRefApiKeyId(dbDgrXVo.getApiKeyId());
										copyDgrXMapVo.setGroupId(dbGroupVo.getGroupId());
										copyDgrXMapVo.setCreateDateTime(dbDgrXMapVo.getCreateDateTime());
										copyDgrXMapVo.setCreateUser(dbDgrXMapVo.getCreateUser());
										copyDgrXMapVo.setVersion(dbDgrXMapVo.getVersion());
										copyDgrXMapVo.setUpdateDateTime(nowDate);
										copyDgrXMapVo.setUpdateUser(auth.getUserName());
										this.getDgrXApiKeyMapDao().save(copyDgrXMapVo);
									}
									
								}else {
									TPILogger.tl.error("no throw, db TsmpGroup data not found, GroupName is "+ groupVo.getGroupName()+", VgroupFlag is "+groupVo.getVgroupFlag());
								}
							}
						}
					}
					
				}
				
				//不一定有資料,1個clientId只會有1筆,dpApp
				List<DpApp> dpAppList = importClientRelateVo.getDpAppList();
				DpApp importDpAppVo = dpAppList.stream().filter(importVo-> 
								importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).findAny().orElse(null);
				//此動作是新增或更新
				if(importDpAppVo != null) {
					DpApp dbDpAppVo = this.getDpAppDao().findFirstByClientId(analyzeVo.getClientId());
					if(dbDpAppVo == null) {
						importDpAppVo.setDpApplicationId(null);
						importDpAppVo.setCreateDateTime(nowDate);
						importDpAppVo.setCreateUser(auth.getUserName());
						this.getDpAppDao().save(importDpAppVo);
					}else {
						importDpAppVo.setDpApplicationId(dbDpAppVo.getDpApplicationId());
						importDpAppVo.setCreateDateTime(dbDpAppVo.getCreateDateTime());
						importDpAppVo.setCreateUser(dbDpAppVo.getCreateUser());
						importDpAppVo.setVersion(dbDpAppVo.getVersion());
						importDpAppVo.setUpdateDateTime(nowDate);
						importDpAppVo.setUpdateUser(auth.getUserName());
						this.getDpAppDao().save(importDpAppVo);
					}
					
				}
				
				//不一定會有資料,TsmpOpenApiKey, TsmpOpenApiKeyMap
				List<TsmpOpenApiKey> openList = importClientRelateVo.getTsmpOpenApiKeyList();
				openList = openList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				List<TsmpOpenApiKeyMap> importOpenMapList = importClientRelateVo.getTsmpOpenApiKeyMapList();
				for(TsmpOpenApiKey openVo : openList) {
					List<TsmpOpenApiKeyMap> openMapList = importOpenMapList.stream().filter(filterOpenMapVo-> 
												  filterOpenMapVo.getRefOpenApiKeyId().equals(openVo.getOpenApiKeyId())).collect(Collectors.toList());
					TsmpOpenApiKey dbOpenVo = this.getTsmpOpenApiKeyDao().findFirstByOpenApiKey(openVo.getOpenApiKey());
					if(dbOpenVo == null) {//此動作是新增(無Map也新增,因為可建立後再去刪api,一樣造成master無api)
						openVo.setOpenApiKeyId(null);
						openVo.setCreateDateTime(nowDate);
						openVo.setCreateUser(auth.getUserName());
						TsmpOpenApiKey savedOpenVo = this.getTsmpOpenApiKeyDao().save(openVo);

						//此動作是新增
						for(TsmpOpenApiKeyMap openMapVo : openMapList) {
							AA1120DgrApikeyUseApi useApiVo = importClientRelateVo.getDgrApikeyUseApiList().stream().filter(filterUseApiVo-> 
														filterUseApiVo.getApiUid().equals(openMapVo.getRefApiUid())).findAny().orElse(null);
							if(useApiVo != null) {//因為有可能
								Optional<AA1121LackApi> lackApiOpt = lackApiList.stream().filter(f->f.getModuleName().equalsIgnoreCase(useApiVo.getModuleName()) 
										&& f.getApiId().equalsIgnoreCase(useApiVo.getApiId())).findAny();
								if(lackApiOpt.isEmpty()) {
									TsmpApiId idVo = new TsmpApiId(useApiVo.getApiId(), useApiVo.getModuleName());
									TsmpApi dbTsmpApiVo = this.getTsmpApiCacheProxy().findById(idVo).orElse(null);
									if(dbTsmpApiVo != null) {//有可能是缺少的API
										TsmpOpenApiKeyMap copyOpenMapVo = ServiceUtil.deepCopy(openMapVo, TsmpOpenApiKeyMap.class);
										copyOpenMapVo.setOpenApiKeyMapId(null);
										copyOpenMapVo.setRefOpenApiKeyId(savedOpenVo.getOpenApiKeyId());
										copyOpenMapVo.setRefApiUid(dbTsmpApiVo.getApiUid());
										copyOpenMapVo.setCreateDateTime(nowDate);
										copyOpenMapVo.setCreateUser(auth.getUserName());
										this.getTsmpOpenApiKeyMapDao().save(copyOpenMapVo);
									}else {
										TPILogger.tl.error("no throw, db TsmpApi data not found, ModuleName is "+ useApiVo.getModuleName()+", apiId is "+useApiVo.getApiId());
									}
								}
							}
						}
						
					}else {
						//此動作是更新
						openVo.setOpenApiKeyId(dbOpenVo.getOpenApiKeyId());
						openVo.setCreateDateTime(dbOpenVo.getCreateDateTime());
						openVo.setCreateUser(dbOpenVo.getCreateUser());
						openVo.setVersion(dbOpenVo.getVersion());
						openVo.setUpdateDateTime(nowDate);
						openVo.setUpdateUser(auth.getUserName());
						this.getTsmpOpenApiKeyDao().save(openVo);
						for(TsmpOpenApiKeyMap openMapVo : openMapList) {
							AA1120DgrApikeyUseApi useApiVo = importClientRelateVo.getDgrApikeyUseApiList().stream().filter(filterUseApiVo-> 
														filterUseApiVo.getApiUid().equals(openMapVo.getRefApiUid())).findAny().orElse(null);
							if(useApiVo != null) {//不一定存在,因為該api是可以刪除的,可能在AA1120就沒存放了
								Optional<AA1121LackApi> lackApiOpt = lackApiList.stream().filter(f->f.getModuleName().equalsIgnoreCase(useApiVo.getModuleName()) 
										&& f.getApiId().equalsIgnoreCase(useApiVo.getApiId())).findAny();
								if(lackApiOpt.isEmpty()) {
									TsmpApiId idVo = new TsmpApiId(useApiVo.getApiId(), useApiVo.getModuleName());
									TsmpApi dbTsmpApiVo = this.getTsmpApiCacheProxy().findById(idVo).orElse(null);
									if(dbTsmpApiVo != null) {//有可能是缺少的API
										TsmpOpenApiKeyMap dbOpenMapVo = this.getTsmpOpenApiKeyMapDao().findFirstByRefOpenApiKeyIdAndRefApiUid(dbOpenVo.getOpenApiKeyId(),dbTsmpApiVo.getApiUid());
										TsmpOpenApiKeyMap copyOpenMapVo = ServiceUtil.deepCopy(openMapVo, TsmpOpenApiKeyMap.class);
										if(dbOpenMapVo != null) {
											//此動作是更新
											copyOpenMapVo.setOpenApiKeyMapId(dbOpenMapVo.getOpenApiKeyMapId());
											copyOpenMapVo.setRefOpenApiKeyId(dbOpenVo.getOpenApiKeyId());
											copyOpenMapVo.setRefApiUid(dbTsmpApiVo.getApiUid());
											copyOpenMapVo.setCreateDateTime(dbOpenMapVo.getCreateDateTime());
											copyOpenMapVo.setCreateUser(dbOpenMapVo.getCreateUser());
											copyOpenMapVo.setVersion(dbOpenMapVo.getVersion());
											copyOpenMapVo.setUpdateDateTime(nowDate);
											copyOpenMapVo.setUpdateUser(auth.getUserName());
											this.getTsmpOpenApiKeyMapDao().save(copyOpenMapVo);
										}else {
											//此動作是新增
											copyOpenMapVo.setOpenApiKeyMapId(null);
											copyOpenMapVo.setRefOpenApiKeyId(openVo.getOpenApiKeyId());
											copyOpenMapVo.setRefApiUid(dbTsmpApiVo.getApiUid());
											copyOpenMapVo.setCreateDateTime(nowDate);
											copyOpenMapVo.setCreateUser(auth.getUserName());
											this.getTsmpOpenApiKeyMapDao().save(copyOpenMapVo);
										}
									}else {
										TPILogger.tl.error("no throw, db TsmpApi data not found, ModuleName is "+ useApiVo.getModuleName()+", apiId is "+useApiVo.getApiId());
									}
								}
							}
						}
					}
				}
				
				//不一定會有資料, DgrGtwIdpInfoO, 重複以序號當PK
				List<DgrGtwIdpInfoO> oList = importClientRelateVo.getDgrGtwIdpInfoOList();
				oList = oList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				
				//若client為DP則刪除已存在DB中相同Type的資料
				if(importDpAppVo!=null) {
					List<String> importClientIdpTypeList = oList.stream().map(DgrGtwIdpInfoO::getIdpType).collect(Collectors.toList());
					List<DgrGtwIdpInfoO> dbDgrGtwIdpInfoOList = this.getDgrGtwIdpInfoODao().findByClientIdAndIdpTypeIn(analyzeVo.getClientId(),importClientIdpTypeList);
					this.getDgrGtwIdpInfoODao().deleteAll(dbDgrGtwIdpInfoOList);
				}
				
				for(DgrGtwIdpInfoO oVo : oList) {
					DgrGtwIdpInfoO dbOVo = this.getDgrGtwIdpInfoODao().findById(oVo.getGtwIdpInfoOId()).orElse(null);
					if(dbOVo != null) {//update
						oVo.setCreateDateTime(dbOVo.getCreateDateTime());
						oVo.setCreateUser(dbOVo.getCreateUser());
						oVo.setVersion(dbOVo.getVersion());
						oVo.setUpdateDateTime(nowDate);
						oVo.setUpdateUser(auth.getUserName());
					}else {//insert
						oVo.setCreateDateTime(nowDate);
						oVo.setCreateUser(auth.getUserName());
					}
					this.getDgrGtwIdpInfoODao().save(oVo);
				}
				
				//不一定會有資料, DgrGtwIdpInfoL, 重複以序號當PK
				List<DgrGtwIdpInfoL> lList = importClientRelateVo.getDgrGtwIdpInfoLList();
				lList = lList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				
				//確認匯入的DP Client有資料則,先刪除後新增
				if(importDpAppVo!=null && lList!=null && lList.size()>0) {
					getDgrGtwIdpInfoLDao().deleteByClientId(analyzeVo.getClientId());
				}
				
				for(DgrGtwIdpInfoL lVo : lList) {
					DgrGtwIdpInfoL dbLVo = this.getDgrGtwIdpInfoLDao().findById(lVo.getGtwIdpInfoLId()).orElse(null);
					if(dbLVo != null) {//update
						lVo.setCreateDateTime(dbLVo.getCreateDateTime());
						lVo.setCreateUser(dbLVo.getCreateUser());
						lVo.setVersion(dbLVo.getVersion());
						lVo.setUpdateDateTime(nowDate);
						lVo.setUpdateUser(auth.getUserName());
					}else {//insert
						lVo.setCreateDateTime(nowDate);
						lVo.setCreateUser(auth.getUserName());
					}
					
					this.getDgrGtwIdpInfoLDao().save(lVo);
				}
				
				//不一定會有資料, DgrGtwIdpInfoA, 重複以序號當PK
				List<DgrGtwIdpInfoA> aList = importClientRelateVo.getDgrGtwIdpInfoAList();
				aList = aList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				
				//確認匯入的DP Client有資料則,先刪除後新增
				if(importDpAppVo!=null && aList!=null && aList.size()>0) {
					getDgrGtwIdpInfoADao().deleteByClientId(analyzeVo.getClientId());
				}
				
				for(DgrGtwIdpInfoA aVo : aList) {
					DgrGtwIdpInfoA dbAVo = this.getDgrGtwIdpInfoADao().findById(aVo.getGtwIdpInfoAId()).orElse(null);
					if(dbAVo != null) {//update
						aVo.setCreateDateTime(dbAVo.getCreateDateTime());
						aVo.setCreateUser(dbAVo.getCreateUser());
						aVo.setVersion(dbAVo.getVersion());
						aVo.setUpdateDateTime(nowDate);
						aVo.setUpdateUser(auth.getUserName());
					}else {//insert
						aVo.setCreateDateTime(nowDate);
						aVo.setCreateUser(auth.getUserName());
					}
					
					this.getDgrGtwIdpInfoADao().save(aVo);
				}
				
				//DgrGtwIdpInfoJdbc
				List<DgrGtwIdpInfoJdbc> jdbcList = importClientRelateVo.getDgrGtwIdpInfoJdbcList();
				jdbcList = jdbcList.stream().filter(importVo -> 
				   importVo.getClientId().equalsIgnoreCase(analyzeVo.getClientId())).collect(Collectors.toList());
				
				//確認匯入的DP Client有資料則,先刪除後新增
				if(importDpAppVo!=null && jdbcList!=null && jdbcList.size()>0) {
					getDgrGtwIdpInfoJdbcDao().deleteByClientId(analyzeVo.getClientId());
				}
				
				for(DgrGtwIdpInfoJdbc jdbcVo : jdbcList) {
					DgrGtwIdpInfoJdbc dbJdbcVo = this.getDgrGtwIdpInfoJdbcDao().findById(jdbcVo.getGtwIdpInfoJdbcId()).orElse(null);
					//不用檢查connectionName,因為一定存在
					if(dbJdbcVo != null) {//update
						jdbcVo.setCreateDateTime(dbJdbcVo.getCreateDateTime());
						jdbcVo.setCreateUser(dbJdbcVo.getCreateUser());
						jdbcVo.setVersion(dbJdbcVo.getVersion());
						jdbcVo.setUpdateDateTime(nowDate);
						jdbcVo.setUpdateUser(auth.getUserName());
					}else {//insert
						jdbcVo.setCreateDateTime(nowDate);
						jdbcVo.setCreateUser(auth.getUserName());
					}
					
					this.getDgrGtwIdpInfoJdbcDao().save(jdbcVo);
				}
			}
		}
	}
	
	public void handleVgroup(AA1121Resp analyzeClientRelateVo, AA1120ImportClientRelated importClientRelateVo, TsmpAuthorization auth, Date nowDate) {
		
		List<AA1121LackApi> lackApiList = analyzeClientRelateVo.getLackApiList();
		List<AA1121Vgroup> analyzeList = analyzeClientRelateVo.getVgroupList();
		analyzeList = analyzeList.stream().filter(f->AnalyzeClientRelatedDataStatus.A.name().equals(f.getDataStatus())
				   || AnalyzeClientRelatedDataStatus.C.name().equals(f.getDataStatus())).collect(Collectors.toList());
	
		List<TsmpVgroup> importList = importClientRelateVo.getTsmpVgroupList();
		for(AA1121Vgroup analyzeVo: analyzeList) {
			TsmpVgroup vgroupVo = importList.stream().filter(importVo -> 
            					   importVo.getVgroupName().equals(analyzeVo.getVgroupName())).findAny().orElse(null);
			if(vgroupVo == null) {//理論上一定有值,防例外(EX:有人修改檔案上傳)
				TPILogger.tl.error("import TsmpVgroup data not found, VgroupName is "+ analyzeVo.getVgroupName());
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			vgroupVo = ServiceUtil.deepCopy(vgroupVo, TsmpVgroup.class);
			String importVgroupId = vgroupVo.getVgroupId();
			
			if(AnalyzeClientRelatedDataStatus.A.name().equals(analyzeVo.getDataStatus())) {
				String dbVgroupId = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_VGROUP_PK).toString();
				//TsmpVGroup
				vgroupVo.setVgroupId(dbVgroupId);
				vgroupVo.setCreateTime(nowDate);
				vgroupVo.setCreateUser(auth.getUserName());
				//securityLevelId一定存在,所以不用檢查
				getTsmpVgroupDao().save(vgroupVo);
				
				//不一定有值,TsmpVgroupAuthoritiesMap
				List<TsmpVgroupAuthoritiesMap> vgroupAuthMapList = importClientRelateVo.getTsmpVgroupAuthoritiesMapList();
				vgroupAuthMapList = vgroupAuthMapList.stream().filter(f->f.getVgroupId().equals(importVgroupId)).collect(Collectors.toList());
				//此動作是新增
				for(TsmpVgroupAuthoritiesMap authMapVo : vgroupAuthMapList) {
					TsmpVgroupAuthoritiesMap dbAuthMapVo = new TsmpVgroupAuthoritiesMap();
					dbAuthMapVo.setVgroupAuthoritieId(authMapVo.getVgroupAuthoritieId());
					dbAuthMapVo.setVgroupId(dbVgroupId);
					//vgroupAuthoritieId一定存在, 所以不用檢查
					getTsmpVgroupAuthoritiesMapDao().save(dbAuthMapVo);
				}
				
				//注意:要有API才會有新增
				List<TsmpGroup> aGroupList = importClientRelateVo.getTsmpGroupList();
				aGroupList= aGroupList.stream().filter(f->importVgroupId.equals(f.getVgroupId())).collect(Collectors.toList());
				for(TsmpGroup groupVo : aGroupList) {
					String importGroupId = groupVo.getGroupId();
					//不一定有值,TsmpGroupApi
					List<TsmpGroupApi> groupApiList = importClientRelateVo.getTsmpGroupApiList();
					groupApiList = groupApiList.stream().filter(f->f.getGroupId().equals(importGroupId)).collect(Collectors.toList());
					for(TsmpGroupApi groupApiVo : groupApiList) {
						Optional<AA1121LackApi> lackApiOpt = lackApiList.stream().filter(f->f.getModuleName().equalsIgnoreCase(groupApiVo.getModuleName()) 
																				&& f.getApiId().equalsIgnoreCase(groupApiVo.getApiKey())).findAny();
						//若api有缺少就不新增
						if(lackApiOpt.isEmpty()) {
							groupVo = ServiceUtil.deepCopy(groupVo, TsmpGroup.class);
							//tsmp_group
							String dbGroupId = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_GROUP_PK).toString();
							groupVo.setGroupId(dbGroupId);
							groupVo.setVgroupId(dbVgroupId);
							groupVo.setCreateTime(nowDate);
							groupVo.setCreateUser(auth.getUserName());
							getTsmpGroupDao().save(groupVo);
							
							//TsmpGroupApi
							TsmpGroupApi copyGroupApiVo = ServiceUtil.deepCopy(groupApiVo, TsmpGroupApi.class);
							copyGroupApiVo.setGroupId(dbGroupId);
							copyGroupApiVo.setCreateTime(nowDate);
							getTsmpGroupApiDao().save(copyGroupApiVo);
							
							//不一定有值,TsmpGroupAuthoritiesMap
							List<TsmpGroupAuthoritiesMap> authMapList = importClientRelateVo.getTsmpGroupAuthoritiesMapList();
							authMapList = authMapList.stream().filter(f->f.getGroupId().equals(importGroupId)).collect(Collectors.toList());
							for(TsmpGroupAuthoritiesMap authMapVo : authMapList) {
								TsmpGroupAuthoritiesMap dbAuthMapVo = new TsmpGroupAuthoritiesMap();
								dbAuthMapVo.setGroupId(dbGroupId);
								//groupAuthoritieId一定存在, 所以不用檢查
								dbAuthMapVo.setGroupAuthoritieId(authMapVo.getGroupAuthoritieId());
								getTsmpGroupAuthoritiesMapDao().save(dbAuthMapVo);
							}
							
							//TsmpVgroupGroup
							TsmpVgroupGroup dbVgroupGroupVo = new TsmpVgroupGroup();
							dbVgroupGroupVo.setGroupId(dbGroupId);
							dbVgroupGroupVo.setVgroupId(dbVgroupId);
							dbVgroupGroupVo.setCreateTime(nowDate);
							getTsmpVgroupGroupDao().save(dbVgroupGroupVo);
						}
					}	
				}
			}else {
				//TsmpVgroup
				TsmpVgroup dbVgroupVo = getTsmpVgroupDao().findFirstByVgroupName(vgroupVo.getVgroupName());
				if(dbVgroupVo == null) {//理論上一定有值,因為被分析為重覆資料,防例外(EX:分析完後使用者去db把資料砍了)
					TPILogger.tl.error("db TsmpVgroup data not found, VgroupName is "+ vgroupVo.getVgroupName());
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				dbVgroupVo.setVgroupName(vgroupVo.getVgroupName());
				dbVgroupVo.setVgroupAlias(vgroupVo.getVgroupAlias());
				dbVgroupVo.setVgroupDesc(vgroupVo.getVgroupDesc());
				dbVgroupVo.setVgroupAccess(vgroupVo.getVgroupAccess());
				//securityLevelId一定存在,所以不用檢查
				dbVgroupVo.setSecurityLevelId(vgroupVo.getSecurityLevelId());
				dbVgroupVo.setAllowDays(vgroupVo.getAllowDays());
				dbVgroupVo.setAllowTimes(vgroupVo.getAllowTimes());
				dbVgroupVo.setUpdateTime(nowDate);
				dbVgroupVo.setUpdateUser(auth.getUserName());
				getTsmpVgroupDao().save(dbVgroupVo);
				
				//TsmpVgroupAuthoritiesMap
				List<TsmpVgroupAuthoritiesMap> vgroupAuthMapList = importClientRelateVo.getTsmpVgroupAuthoritiesMapList();
				vgroupAuthMapList = vgroupAuthMapList.stream().filter(f->f.getVgroupId().equals(importVgroupId)).collect(Collectors.toList());
				//此動作是新增或更新
				for(TsmpVgroupAuthoritiesMap authMapVo : vgroupAuthMapList) {
					TsmpVgroupAuthoritiesMap dbAuthMapVo = new TsmpVgroupAuthoritiesMap();
					dbAuthMapVo.setVgroupId(dbVgroupVo.getVgroupId());
					dbAuthMapVo.setVgroupAuthoritieId(authMapVo.getVgroupAuthoritieId());
					//vgroupAuthoritieId一定存在, 所以不用檢查
					getTsmpVgroupAuthoritiesMapDao().save(dbAuthMapVo);
				}
				//注意:要有API才會有新增或更新
				List<TsmpGroup> groupList = importClientRelateVo.getTsmpGroupList();
				groupList = groupList.stream().filter(f->importVgroupId.equals(f.getVgroupId())).collect(Collectors.toList());
				for(TsmpGroup groupVo : groupList) {
					String importGroupId = groupVo.getGroupId();
					//TsmpGroupApi
					List<TsmpGroupApi> groupApiList = importClientRelateVo.getTsmpGroupApiList();
					groupApiList = groupApiList.stream().filter(f->f.getGroupId().equals(importGroupId)).collect(Collectors.toList());
					for(TsmpGroupApi groupApiVo : groupApiList) {
						Optional<AA1121LackApi> lackApiOpt = lackApiList.stream().filter(f->f.getModuleName().equalsIgnoreCase(groupApiVo.getModuleName()) 
																				&& f.getApiId().equalsIgnoreCase(groupApiVo.getApiKey())).findAny();
						//若api沒有缺少就新增或更新
						if(lackApiOpt.isEmpty()) {
							//用vgroupId取得groupId
							List<TsmpVgroupGroup> dbVgroupGroupList = getTsmpVgroupGroupDao().findByVgroupId(dbVgroupVo.getVgroupId());
							List<String> dbGroupIdList = dbVgroupGroupList.stream().map(vo->vo.getGroupId()).collect(Collectors.toList());
								//因為是單筆vgroup的groupId回傳才會是單筆,api存在就更新否則新增
								TsmpGroupApi dbGroupApiVo = getTsmpGroupApiDao().findFirstByGroupIdInAndApiKeyAndModuleName(dbGroupIdList, groupApiVo.getApiKey(),groupApiVo.getModuleName());
								if(dbGroupApiVo != null) {//更新
									//tsmp_group
									TsmpGroup dbGroupVo = getTsmpGroupDao().findById(dbGroupApiVo.getGroupId()).orElse(null);
									if(dbGroupVo == null) {//理論上一定有值,因為被分析為重覆資料,防例外(EX:分析完後使用者去db把資料砍了)
										TPILogger.tl.error("db TsmpGroup data not found, groupId is "+ dbGroupApiVo.getGroupId());
										throw TsmpDpAaRtnCode._1298.throwing();
									}
									dbGroupVo.setGroupName(groupVo.getGroupName());
									dbGroupVo.setGroupAlias(groupVo.getGroupAlias());
									dbGroupVo.setGroupAccess(groupVo.getGroupAccess());
									dbGroupVo.setGroupDesc(groupVo.getGroupDesc());
									//securityLevelId一定存在,所以不用檢查
									dbGroupVo.setSecurityLevelId(groupVo.getSecurityLevelId());
									dbGroupVo.setAllowTimes(groupVo.getAllowTimes());
									dbGroupVo.setAllowDays(groupVo.getAllowDays());
									dbGroupVo.setUpdateTime(nowDate);
									dbGroupVo.setUpdateUser(auth.getUserName());
									getTsmpGroupDao().save(dbGroupVo);
									
									//TsmpGroupApi
									dbGroupApiVo.setCreateTime(nowDate);
									getTsmpGroupApiDao().save(dbGroupApiVo);
									
									//TsmpGroupAuthoritiesMap
									List<TsmpGroupAuthoritiesMap> authMapList = importClientRelateVo.getTsmpGroupAuthoritiesMapList();
									authMapList = authMapList.stream().filter(f->f.getGroupId().equals(importGroupId)).collect(Collectors.toList());
									//此動作可能是新增或更新
									for(TsmpGroupAuthoritiesMap authMapVo : authMapList) {
										TsmpGroupAuthoritiesMap dbAuthMapVo = new TsmpGroupAuthoritiesMap();
										dbAuthMapVo.setGroupId(dbGroupApiVo.getGroupId());
										dbAuthMapVo.setGroupAuthoritieId(authMapVo.getGroupAuthoritieId());
										//groupAuthoritieId一定存在, 所以不用檢查
										getTsmpGroupAuthoritiesMapDao().save(dbAuthMapVo);
									}
									//因為此段是更新,所以TsmpVgroupGroup不動
								}else{//新增
									//tsmp_group
									String dbgroupId = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_GROUP_PK).toString();
									groupVo = ServiceUtil.deepCopy(groupVo, TsmpGroup.class);
									groupVo.setGroupId(dbgroupId);
									groupVo.setVgroupId(dbVgroupVo.getVgroupId());
									groupVo.setCreateTime(nowDate);
									groupVo.setCreateUser(auth.getUserName());
									getTsmpGroupDao().save(groupVo);
									
									//TsmpGroupApi
									TsmpGroupApi copyGroupApiVo = ServiceUtil.deepCopy(groupApiVo, TsmpGroupApi.class);
									copyGroupApiVo.setGroupId(dbgroupId);
									copyGroupApiVo.setCreateTime(nowDate);
									getTsmpGroupApiDao().save(copyGroupApiVo);
									
									//TsmpGroupAuthoritiesMap
									List<TsmpGroupAuthoritiesMap> authMapList = importClientRelateVo.getTsmpGroupAuthoritiesMapList();
									authMapList = authMapList.stream().filter(f->f.getGroupId().equals(importGroupId)).collect(Collectors.toList());
									//此動作可能是新增或更新
									for(TsmpGroupAuthoritiesMap authMapVo : authMapList) {
										TsmpGroupAuthoritiesMap dbAuthMapVo = new TsmpGroupAuthoritiesMap();
										dbAuthMapVo.setGroupId(dbgroupId);
										dbAuthMapVo.setGroupAuthoritieId(authMapVo.getGroupAuthoritieId());
										//groupAuthoritieId一定存在, 所以不用檢查
										getTsmpGroupAuthoritiesMapDao().save(dbAuthMapVo);
									}
									
									//TsmpVgroupGroup
									TsmpVgroupGroup dbVgroupGroupVo = new TsmpVgroupGroup();
									dbVgroupGroupVo.setGroupId(dbgroupId);
									dbVgroupGroupVo.setVgroupId(dbVgroupVo.getVgroupId());
									dbVgroupGroupVo.setCreateTime(nowDate);
									getTsmpVgroupGroupDao().save(dbVgroupGroupVo);
									
								}
							}
						}
					}
				}
		}
	}
	
	public void handleGroup(AA1121Resp analyzeClientRelateVo, AA1120ImportClientRelated importClientRelateVo, TsmpAuthorization auth, Date nowDate) {
		
		List<AA1121LackApi> lackApiList = analyzeClientRelateVo.getLackApiList();
		List<AA1121Group> analyzeList = analyzeClientRelateVo.getGroupList();
		analyzeList = analyzeList.stream().filter(f->AnalyzeClientRelatedDataStatus.A.name().equals(f.getDataStatus())
				   || AnalyzeClientRelatedDataStatus.C.name().equals(f.getDataStatus())).collect(Collectors.toList());
	
		List<TsmpGroup> importList = importClientRelateVo.getTsmpGroupList();	
		for(AA1121Group analyzeVo: analyzeList) {
			//AA1121分析已經只取得VgroupFlag=0的資料了
			TsmpGroup groupVo = importList.stream().filter(importVo -> 
            					   importVo.getGroupName().equalsIgnoreCase(analyzeVo.getGroupName()) 
            					   && "0".equals(importVo.getVgroupFlag())).findAny().orElse(null);
			if(groupVo == null) {//理論上一定有值,防例外(EX:有人修改檔案上傳)
				TPILogger.tl.error("import TsmpGroup data not found, GroupName is "+ analyzeVo.getGroupName());
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			String importGroupId = groupVo.getGroupId();
			
			if(AnalyzeClientRelatedDataStatus.A.name().equals(analyzeVo.getDataStatus())) {
				String dbGroupId = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_GROUP_PK).toString();
				//TsmpGroup
				groupVo = ServiceUtil.deepCopy(groupVo, TsmpGroup.class);
				groupVo.setGroupId(dbGroupId);
				groupVo.setCreateTime(nowDate);
				groupVo.setCreateUser(auth.getUserName());
				//securityLevelId一定存在,所以不用檢查
				getTsmpGroupDao().save(groupVo);
				
				//TsmpGroupAuthoritiesMap
				List<TsmpGroupAuthoritiesMap> authMapList = importClientRelateVo.getTsmpGroupAuthoritiesMapList();
				authMapList = authMapList.stream().filter(f->f.getGroupId().equalsIgnoreCase(importGroupId)).collect(Collectors.toList());
				//此動作是新增
				for(TsmpGroupAuthoritiesMap authMapVo : authMapList) {
					TsmpGroupAuthoritiesMap dbAuthMapVo = new TsmpGroupAuthoritiesMap();
					dbAuthMapVo.setGroupId(dbGroupId);
					dbAuthMapVo.setGroupAuthoritieId(authMapVo.getGroupAuthoritieId());
					//groupAuthoritieId一定存在, 所以不用檢查
					getTsmpGroupAuthoritiesMapDao().save(dbAuthMapVo);
				}
				
				//不一定會有值,TsmpGroupApi
				List<TsmpGroupApi> groupApiList = importClientRelateVo.getTsmpGroupApiList();
				groupApiList = groupApiList.stream().filter(f->f.getGroupId().equals(importGroupId)).collect(Collectors.toList());
				for(TsmpGroupApi groupApiVo : groupApiList) {
					Optional<AA1121LackApi> lackApiOpt = lackApiList.stream().filter(f->f.getModuleName().equalsIgnoreCase(groupApiVo.getModuleName()) 
																			&& f.getApiId().equalsIgnoreCase(groupApiVo.getApiKey())).findAny();
					//若api有缺少就不新增
					if(lackApiOpt.isEmpty()) {
						TsmpGroupApi copyGroupApiVo = ServiceUtil.deepCopy(groupApiVo, TsmpGroupApi.class);
						copyGroupApiVo.setGroupId(dbGroupId);
						copyGroupApiVo.setCreateTime(nowDate);
						getTsmpGroupApiDao().save(copyGroupApiVo);
					}
				}	
				
			}else {
				//TsmpGroup
				TsmpGroup dbGroupVo = getTsmpGroupDao().findFirstByGroupNameAndVgroupFlag(groupVo.getGroupName(), "0");
				if(dbGroupVo == null) {//理論上一定有值,因為被分析為重覆資料,防例外(EX:分析完後使用者去db把資料砍了)
					TPILogger.tl.error("db TsmpGroup data not found, GroupName is "+ groupVo.getGroupName());
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				dbGroupVo.setGroupName(groupVo.getGroupName());
				dbGroupVo.setGroupAlias(groupVo.getGroupAlias());
				dbGroupVo.setGroupDesc(groupVo.getGroupDesc());
				dbGroupVo.setGroupAccess(groupVo.getGroupAccess());
				//securityLevelId一定存在,所以不用檢查
				dbGroupVo.setSecurityLevelId(groupVo.getSecurityLevelId());
				dbGroupVo.setAllowDays(groupVo.getAllowDays());
				dbGroupVo.setAllowTimes(groupVo.getAllowTimes());
				dbGroupVo.setUpdateTime(nowDate);
				dbGroupVo.setUpdateUser(auth.getUserName());
				getTsmpGroupDao().save(dbGroupVo);
				
				//TsmpGroupAuthoritiesMap
				List<TsmpGroupAuthoritiesMap> authMapList = importClientRelateVo.getTsmpGroupAuthoritiesMapList();
				authMapList = authMapList.stream().filter(f->f.getGroupId().equals(importGroupId)).collect(Collectors.toList());
				//此動作可能是新增或更新
				for(TsmpGroupAuthoritiesMap authMapVo : authMapList) {
					TsmpGroupAuthoritiesMap dbAuthMapVo = new TsmpGroupAuthoritiesMap();
					dbAuthMapVo.setGroupId(dbGroupVo.getGroupId());
					dbAuthMapVo.setGroupAuthoritieId(authMapVo.getGroupAuthoritieId());
					//groupAuthoritieId一定存在, 所以不用檢查
					getTsmpGroupAuthoritiesMapDao().save(dbAuthMapVo);
				}
				
				//TsmpGroupApi(不一定有值)
				List<TsmpGroupApi> groupApiList = importClientRelateVo.getTsmpGroupApiList();
				groupApiList = groupApiList.stream().filter(f->f.getGroupId().equals(importGroupId)).collect(Collectors.toList());
				for(TsmpGroupApi groupApiVo : groupApiList) {
					Optional<AA1121LackApi> lackApiOpt = lackApiList.stream().filter(f->f.getModuleName().equalsIgnoreCase(groupApiVo.getModuleName()) 
																			&& f.getApiId().equalsIgnoreCase(groupApiVo.getApiKey())).findAny();
					//若api沒有缺少就新增或更新
					if(lackApiOpt.isEmpty()) {
						TsmpGroupApi copyGroupApiVo = ServiceUtil.deepCopy(groupApiVo, TsmpGroupApi.class);
						copyGroupApiVo.setGroupId(dbGroupVo.getGroupId());
						copyGroupApiVo.setCreateTime(nowDate);
						getTsmpGroupApiDao().save(copyGroupApiVo);
						
					}
				}
			}
			
		}
	}
	
	public void handleSecurityLevel(AA1121Resp analyzeClientRelateVo, AA1120ImportClientRelated importClientRelateVo) {
		
		List<AA1121SecurityLevel> analyzeList = analyzeClientRelateVo.getSecurityLevelList();
		analyzeList = analyzeList.stream().filter(f->AnalyzeClientRelatedDataStatus.A.name().equals(f.getDataStatus())
				   || AnalyzeClientRelatedDataStatus.C.name().equals(f.getDataStatus())).collect(Collectors.toList());
	
		List<TsmpSecurityLevel> importList = importClientRelateVo.getTsmpSecurityLevelList();
		for(AA1121SecurityLevel analyzeVo: analyzeList) {
			TsmpSecurityLevel vo = importList.stream().filter(importVo -> 
            					   importVo.getSecurityLevelId().equalsIgnoreCase(analyzeVo.getSecurityLevelId())).findAny().orElse(null);
			if(vo == null) {//理論上一定有值,防例外(EX:有人修改檔案上傳)
				TPILogger.tl.error("import TsmpSecurityLevel data not found, SecurityLevelId is "+ analyzeVo.getSecurityLevelId());
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			getTsmpSecurityLevelDao().save(vo);
		}
	}
	
	public void handleGroupAuth(AA1121Resp analyzeClientRelateVo, AA1120ImportClientRelated importClientRelateVo) {
		
		List<AA1121GroupAuth> analyzeList = analyzeClientRelateVo.getGroupAuthList();
		analyzeList = analyzeList.stream().filter(f->AnalyzeClientRelatedDataStatus.A.name().equals(f.getDataStatus())
				   || AnalyzeClientRelatedDataStatus.C.name().equals(f.getDataStatus())).collect(Collectors.toList());
	
		List<TsmpGroupAuthorities> importList = importClientRelateVo.getTsmpGroupAuthoritiesList();
		for(AA1121GroupAuth analyzeVo: analyzeList) {
			TsmpGroupAuthorities vo = importList.stream().filter(importVo -> 
            					   importVo.getGroupAuthoritieId().equalsIgnoreCase(analyzeVo.getGroupAuthId())).findAny().orElse(null);
			if(vo == null) {//理論上一定有值,防例外(EX:有人修改檔案上傳)
				TPILogger.tl.error("import TsmpGroupAuthorities data not found, GroupAuthoritieId is "+ analyzeVo.getGroupAuthId());
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			getTsmpGroupAuthoritiesDao().save(vo);
		}
	}
	
	public void handleRdbConnection(AA1121Resp analyzeClientRelateVo, AA1120ImportClientRelated importClientRelateVo, TsmpAuthorization auth, Date nowDate) {
		
		List<AA1121RdbConnection> analyzeList = analyzeClientRelateVo.getRdbConnectionList();
		analyzeList = analyzeList.stream().filter(f->AnalyzeClientRelatedDataStatus.A.name().equals(f.getDataStatus())
				   || AnalyzeClientRelatedDataStatus.C.name().equals(f.getDataStatus())).collect(Collectors.toList());
	
		List<DgrRdbConnection> importList = importClientRelateVo.getDgrRdbConnectionList();
		for(AA1121RdbConnection analyzeVo: analyzeList) {
			DgrRdbConnection rdbVo = importList.stream().filter(importVo -> 
            					   importVo.getConnectionName().equalsIgnoreCase(analyzeVo.getConnectionName())).findAny().orElse(null);
			if(rdbVo == null) {//理論上一定有值,防例外(EX:有人修改檔案上傳)
				TPILogger.tl.error("import DgrRdbConnection data not found, ConnectionName is "+ analyzeVo.getConnectionName());
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			//新增
			if(AnalyzeClientRelatedDataStatus.A.name().equals(analyzeVo.getDataStatus())){
				rdbVo.setCreateDateTime(nowDate);
				rdbVo.setCreateUser(auth.getUserName());
			}else {
				DgrRdbConnection dbRdbVo = this.getDgrRdbConnectionDao().findById(rdbVo.getConnectionName()).orElse(null);
				if(dbRdbVo == null) {//理論上一定有值,因為被分析為重覆資料,防例外(EX:分析完後使用者去db把資料砍了)
					TPILogger.tl.error("db DgrRdbConnection data not found, ConnectionName is "+ rdbVo.getConnectionName());
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				rdbVo.setCreateDateTime(dbRdbVo.getCreateDateTime());
				rdbVo.setCreateUser(dbRdbVo.getCreateUser());
				rdbVo.setVersion(dbRdbVo.getVersion());
				rdbVo.setUpdateDateTime(nowDate);
				rdbVo.setUpdateUser(auth.getUserName());
			}
			getDgrRdbConnectionDao().save(rdbVo);
		}
	}
	
	private void checkParam(AA1129Req req) {
		if(!StringUtils.hasText(req.getLongId())) {
			throw TsmpDpAaRtnCode._1350.throwing("{{longId}}");
		}
	}
	
	protected Date getNowDate() {
		return DateTimeUtil.now();
	}
	
	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}

	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return tsmpDpClientextDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}
	
	protected TsmpClientHostDao getTsmpClientHostDao() {
		return tsmpClientHostDao;
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return tsmpClientGroupDao;
	}

	protected TsmpVgroupDao getTsmpVgroupDao() {
		return tsmpVgroupDao;
	}

	protected TsmpVgroupGroupDao getTsmpVgroupGroupDao() {
		return tsmpVgroupGroupDao;
	}

	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return tsmpGroupApiDao;
	}

	protected DgrXApiKeyDao getDgrXApiKeyDao() {
		return dgrXApiKeyDao;
	}
	
	protected TsmpClientVgroupDao getTsmpClientVgroupDao() {
		return tsmpClientVgroupDao;
	}

	protected DgrXApiKeyMapDao getDgrXApiKeyMapDao() {
		return dgrXApiKeyMapDao;
	}

	protected TsmpGroupAuthoritiesDao getTsmpGroupAuthoritiesDao() {
		return tsmpGroupAuthoritiesDao;
	}

	protected TsmpVgroupAuthoritiesMapDao getTsmpVgroupAuthoritiesMapDao() {
		return tsmpVgroupAuthoritiesMapDao;
	}

	protected TsmpSecurityLevelDao getTsmpSecurityLevelDao() {
		return tsmpSecurityLevelDao;
	}

	protected DpAppDao getDpAppDao() {
		return dpAppDao;
	}
	
	protected TsmpGroupAuthoritiesMapDao getTsmpGroupAuthoritiesMapDao() {
		return tsmpGroupAuthoritiesMapDao;
	}

	protected TsmpOpenApiKeyDao getTsmpOpenApiKeyDao() {
		return tsmpOpenApiKeyDao;
	}

	protected TsmpOpenApiKeyMapDao getTsmpOpenApiKeyMapDao() {
		return tsmpOpenApiKeyMapDao;
	}

	protected DgrGtwIdpInfoODao getDgrGtwIdpInfoODao() {
		return dgrGtwIdpInfoODao;
	}

	protected DgrGtwIdpInfoADao getDgrGtwIdpInfoADao() {
		return dgrGtwIdpInfoADao;
	}

	protected DgrGtwIdpInfoJdbcDao getDgrGtwIdpInfoJdbcDao() {
		return dgrGtwIdpInfoJdbcDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}
	
	protected DgrGtwIdpInfoLDao getDgrGtwIdpInfoLDao() {
		return dgrGtwIdpInfoLDao;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected DgrImportClientRelatedTempDao getDgrImportClientRelatedTempDao() {
		return dgrImportClientRelatedTempDao;
	}

	protected SeqStoreService getSeqStoreService() {
		return seqStoreService;
	}

	protected TsmpApiCacheProxy getTsmpApiCacheProxy() {
		return tsmpApiCacheProxy;
	}

	protected DgrRdbConnectionDao getDgrRdbConnectionDao() {
		return dgrRdbConnectionDao;
	}
	
	
}
