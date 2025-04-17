package tpi.dgrv4.dpaa.service;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.TimeZoneUtil;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0204HostReq;
import tpi.dgrv4.dpaa.vo.AA0204Req;
import tpi.dgrv4.dpaa.vo.AA0204Resp;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpClientGroup;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.entity.jpql.TsmpClientHost;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.entity.repository.TsmpClientHostDao;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0204Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpClientDao tsmpClientDao;
	private TsmpClientGroupDao tsmpClientGroupDao;
	private TsmpGroupDao tsmpGroupDao;
	private TsmpSettingDao tsmpSettingDao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	private TsmpClientHostDao tsmpClientHostDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private BcryptParamHelper bcryptParamHelper;
	private SeqStoreService seqStoreService;
	private DgrAuditLogService dgrAuditLogService;
	
	@Autowired
	public AA0204Service(TsmpClientDao tsmpClientDao, TsmpClientGroupDao tsmpClientGroupDao, TsmpGroupDao tsmpGroupDao,
			TsmpSettingDao tsmpSettingDao, TsmpDpClientextDao tsmpDpClientextDao, TsmpClientHostDao tsmpClientHostDao,
			OauthClientDetailsDao oauthClientDetailsDao, BcryptParamHelper bcryptParamHelper,
			SeqStoreService seqStoreService, DgrAuditLogService dgrAuditLogService) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpSettingDao = tsmpSettingDao;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.tsmpClientHostDao = tsmpClientHostDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.seqStoreService = seqStoreService;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	@Transactional
	public AA0204Resp updateClient(TsmpAuthorization authorization, AA0204Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
		
		AA0204Resp resp = new AA0204Resp();

		try {
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			String cPriority = req.getcPriority();
			String newTps = req.getNewTps();
			String newCPriority = req.getNewCPriority();
			String apiQuota = req.getApiQuota();
			String newApiQuota = req.getNewApiQuota();
			String clientId = req.getClientID();
			String clientAlias = req.getClientAlias();
			String newOwner = req.getNewOwner();
			String newClientAlias = req.getNewClientAlias();
			String newEncodePublicFlag = req.getNewEncodePublicFlag();
			String newClientName = req.getNewClientName();
			String emails = req.getEmails();
			String newEmails = req.getNewEmails();
			String owner = req.getOwner();
			String clientName = req.getClientName();
			String newSignupNum = req.getNewSignupNum();
			String tps = req.getTps();
			List<String> newGroupIDList = req.getNewGroupIDList();
			String encodePublicFlag = req.getEncodePublicFlag();
			List<AA0204HostReq> hostList = req.getHostList();
			String signupNum = req.getSignupNum();
			List<AA0204HostReq> newHostList = req.getNewHostList();
			List<String> groupIDList = req.getGroupIDList();
			String remark = req.getRemark();
			String newRemark = req.getNewRemark();
			String clientStartDate = req.getClientStartDate();
			String newClientStartDate = req.getNewClientStartDate();
			String clientEndDate = req.getClientEndDate();
			String newClientEndDate = req.getNewClientEndDate();
			String clientStartTimePerDay = req.getClientStartTimePerDay();
			String newClientStartTimePerDay = req.getNewClientStartTimePerDay();
			String clientEndTimePerDay = req.getClientEndTimePerDay();
			String newClientEndTimePerDay = req.getNewClientEndTimePerDay();
			String timeZone = req.getTimeZone();
			String newTimeZone = req.getNewTimeZone();
		
			checkParam(req);
			
			//2.查詢tsmp_client資料表，條件CLIENT_ID =AA0204Req.clientID AND CLIENT_NAME =AA0204Req.clientName，若找不到資料資則throw RTN Code 1344。
			TsmpClient tsmpClient = getTsmpClientDao().findFirstByClientIdAndClientName(clientId, clientName);		
			if(tsmpClient == null) {
				//1344:用戶端不存在
				throw TsmpDpAaRtnCode._1344.throwing();
			}
			
			//3.若AA0204Req.clientName和AA0204Req.newClientName不相同，則查詢tsmp_client資料表，條件CLIENT_NAME = AA0204Req.newClientName，若有查詢到資料則throw RTN Code 1340。
			if(!newClientName.equals(clientName)) {
				List<TsmpClient> clientList = getTsmpClientDao().findByClientName(newClientName);
				if(clientList.size() > 0) {
					//1340:用戶端代號已存在
					throw TsmpDpAaRtnCode._1340.throwing();
				}
			}

			//4.若AA0204Req.clientAlias和AA0204Req.newClientAlias不相同，則查詢tsmp_client資料表，條件CLIENT_ALIAS = AA0204Req.newClientAlias，若有查詢到資料則throw RTN Code 1341。
			if(newClientAlias != null && !newClientAlias.equals(clientAlias)) {
				List<TsmpClient>clientList = getTsmpClientDao().findByClientAlias(newClientAlias);
				if(clientList.size() > 0) {
					//1341:用戶端名稱已存在
					throw TsmpDpAaRtnCode._1341.throwing();
				}
			}
			List<TsmpClientGroup> tsmpClientGroupEntityList_VGroup = new ArrayList<>();
			if(newGroupIDList != null) {
				Set<String> groupIdSet = new HashSet<String>();

				//5.1若AA0204Req.newGroupIDList不為空，則計算AA0204Req.newGroupIDList不重複的數量 ，並執行步驟5.2、5.3。
				newGroupIDList.forEach(x->{
					groupIdSet.add(x);
				} );
				
				//5.2查詢TSMP_CLIENT_GROUP資料表，條件CLIENT_ID = AA0204Req.clientID，將查詢出來的資料再去查詢TSMP_GROUP資料表
				//，條件GROUP_ID=TSMP_CLIENT_GROUP.GROUP_ID AND VGROUP_FLAG = "1"，計算(GROUP_ID)不重覆數量。
				List<TsmpClientGroup> groupList = getTsmpClientGroupDao().findByClientId(clientId);
				for(TsmpClientGroup vo : groupList) {
					TsmpGroup tsmpGroup = getTsmpGroupDao().findFirstByGroupIdAndVgroupFlag(vo.getGroupId(), "1");
					if(tsmpGroup != null) {
						if(newGroupIDList.size() > 0) {
							groupIdSet.add(tsmpGroup.getGroupId());
						}
						//10.1 若 AA0204Req.newGroupIDList不為Null，查詢TSMP_CLIENT_GROUP、TSMP_GROUP並JOIN以TSMP_CLIENT_GROUP為查詢出來資料，條件VGROUP_FLAG = "1" 、CLIENT_ID = AA0204Req.clientID，將查詢到資料存進"變數tsmpClientGroupEntityList_VGroup"
						tsmpClientGroupEntityList_VGroup.add(vo);
					}
				}
				
				//5.3 將5.1與5.2的數量加總，若超過205則throw RTN Code 1358。
				if(groupIdSet.size() > 205) {
					//1358:[群組名稱]超過群組選取上限205，請重新選取
					throw TsmpDpAaRtnCode._1358.throwing();
				}
				
			}
			
			//6.將AA0204Req資料更新到tsmp_client資料表，UPDATE_USER與UPDATE_TIME欄位記得更新。
			if((newApiQuota != null && !newApiQuota.equals(apiQuota)) || (newClientAlias != null && !newClientAlias.equals(clientAlias))
				|| !newClientName.equals(clientName)
				|| (newCPriority != null && !newCPriority.equals(cPriority))
				|| (newEmails != null && !newEmails.equals(emails)) || !newOwner.equals(owner)
				|| (newSignupNum != null && !newSignupNum.equals(signupNum))
				|| (newClientStartDate != null && !newClientStartDate.equals(clientStartDate))
				|| (newClientEndDate != null && !newClientEndDate.equals(clientEndDate))
				|| (newClientStartTimePerDay != null && !newClientStartTimePerDay.equals(clientStartTimePerDay))
				|| (newClientEndTimePerDay != null && !newClientEndTimePerDay.equals(clientEndTimePerDay))
				|| (newTimeZone != null && !newTimeZone.equals(timeZone))
				|| (newTps != null && !newTps.equals(tps))
				|| (newRemark!=null && !newRemark.equals(remark))) {
				
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpClient); //舊資料統一轉成 String
				
				tsmpClient.setApiQuota(StringUtils.isEmpty(newApiQuota) ? 0 : Integer.valueOf(newApiQuota));
				tsmpClient.setClientAlias(newClientAlias);
				tsmpClient.setClientName(newClientName);
				tsmpClient.setcPriority(StringUtils.isEmpty(newCPriority) ? 0 : Integer.valueOf(newCPriority));
				tsmpClient.setEmails(newEmails);
				tsmpClient.setOwner(newOwner);
				tsmpClient.setSignupNum(newSignupNum);
				tsmpClient.setTps(StringUtils.isEmpty(newTps) ? 0 : Integer.valueOf(newTps));
				tsmpClient.setUpdateTime(DateTimeUtil.now());
				tsmpClient.setUpdateUser(authorization.getUserName());
				tsmpClient.setRemark(newRemark);
				tsmpClient.setStartDate(StringUtils.hasText(newClientStartDate) ? TimeZoneUtil.anyISO86012long(newClientStartDate) : null);
				tsmpClient.setEndDate(StringUtils.hasText(newClientEndDate) ? TimeZoneUtil.anyISO86012long(newClientEndDate) : null);
				tsmpClient.setStartTimePerDay(StringUtils.hasText(newClientStartTimePerDay) ? TimeZoneUtil.anyISO86012long(newClientStartTimePerDay) : null);
				tsmpClient.setEndTimePerDay(StringUtils.hasText(newClientEndTimePerDay) ? TimeZoneUtil.anyISO86012long(newClientEndTimePerDay) : null);
				tsmpClient.setTimeZone(StringUtils.hasText(newTimeZone) ? newTimeZone : null);
				
				tsmpClient = getTsmpClientDao().save(tsmpClient);
				
				//寫入 Audit Log D
				lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
						TsmpClient.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpClient);
			}
			
			//7.查詢TSMP_SETTING資料表，條件 ID = "TSMP_AC_CONF"，將查詢出來的值轉換成JSON物件，取出dp欄位的值。若dp欄位的值為0則執行步驟8。
//			int dp = this.getDp();
//			if(dp == 0) {
//				TsmpDpClientext dpClientext = getTsmpDpClientextDao().findById(clientId).orElse(null);
//				if(dpClientext != null) {
//					//8.更新TSMP_DP_CLIENTEXT資料表，public_flag = (AA0204Req.encodePublicFlag == null :"2"?AA0204Req.encodePublicFlag)。
//					String newDecodePublicFlag = getValueByBcryptParamHelper(newEncodePublicFlag, "API_AUTHORITY", reqHeader.getLocale());
//					String decodePublicFlag = getValueByBcryptParamHelper(encodePublicFlag, "API_AUTHORITY", reqHeader.getLocale());
//					if(!newDecodePublicFlag.equals(decodePublicFlag)) {
//						//String publicFlag = newEncodePublicFlag == null ? "2" : newDecodePublicFlag;
//						dpClientext.setPublicFlag(newDecodePublicFlag);
//						dpClientext.setUpdateDateTime(DateTimeUtil.now());
//						dpClientext.setUpdateUser(authorization.getUserName());
//						getTsmpDpClientextDao().save(dpClientext);
//					}
//				}
//			}
			
			TsmpDpClientext dpClientext = getTsmpDpClientextDao().findById(clientId).orElse(null);
			if(dpClientext != null) {
				//8.更新TSMP_DP_CLIENTEXT資料表，public_flag = (AA0204Req.encodePublicFlag == null :"2"?AA0204Req.encodePublicFlag)。
				String newDecodePublicFlag = getValueByBcryptParamHelper(newEncodePublicFlag, "API_AUTHORITY", locale);
				String decodePublicFlag = getValueByBcryptParamHelper(encodePublicFlag, "API_AUTHORITY", locale);
				if(!newDecodePublicFlag.equals(decodePublicFlag)) {
					String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, dpClientext); //舊資料統一轉成 String
					
					//String publicFlag = newEncodePublicFlag == null ? "2" : newDecodePublicFlag;
					dpClientext.setPublicFlag(newDecodePublicFlag);
					dpClientext.setUpdateDateTime(DateTimeUtil.now());
					dpClientext.setUpdateUser(authorization.getUserName());
					dpClientext = getTsmpDpClientextDao().save(dpClientext);
					
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							TsmpDpClientext.class.getSimpleName(), TableAct.U.value(), oldRowStr, dpClientext);
				}
			}
			
			
			//9.將AA0204Req.newHostList更新到tsmp_client_host資料表，HOST_SEQ = 取"SEQ_TSMP_CLIENT_HOST_PK"
			//、CLIENT_ID = AA0204Req.clientID 、TSMP_CLIENT_HOST.CREATE_TIME= 現在時間
			//、TSMP_CLIENT_HOST.HOST_IP = AA0204Req.newHostList.hostIP、TSMP_CLIENT_HOST.HOST_NAME = AA0204Req.newHostList.hostName。
			if(isHostListDiffer(newHostList, hostList)) {
				List<TsmpClientHost> delHostList = getTsmpClientHostDao().findByClientId(clientId);
				for(TsmpClientHost vo : delHostList) {
					String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, vo); //舊資料統一轉成 String
					
					getTsmpClientHostDao().delete(vo);
					
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							TsmpClientHost.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
				}
				if(newHostList != null && newHostList.size() > 0) {
					for(AA0204HostReq vo : newHostList) {
						TsmpClientHost hostVo = new TsmpClientHost();
						hostVo.setClientId(clientId);
						hostVo.setCreateTime(DateTimeUtil.now());
						hostVo.setHostIp(vo.getHostIP());
						hostVo.setHostName(vo.getHostName());
						hostVo.setHostSeq(this.getHostSeq());
						hostVo = getTsmpClientHostDao().save(hostVo);
						
						//寫入 Audit Log D
						lineNumber = StackTraceUtil.getLineNumber();
						getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
								TsmpClientHost.class.getSimpleName(), TableAct.C.value(), null, hostVo);
					}
				}
			}
			
			//10.1 若 AA0204Req.newGroupIDList不為Null，查詢TSMP_CLIENT_GROUP、TSMP_GROUP並JOIN以TSMP_CLIENT_GROUP為查詢出來資料
			//，條件VGROUP_FLAG = "1" 、CLIENT_ID = AA0204Req.clientID，將查詢到資料存進"變數tsmpClientGroupEntityList_VGroup"。
			//刪除TSMP_CLIENT_GROUP資料，條件CLIENT_ID = AA0204Req.clientID。執行步驟10.2
			if(newGroupIDList != null && this.isGroupIDListDiffer(newGroupIDList, groupIDList)) {
				this.getTsmpClientGroupDao().deleteByClientId(clientId);
				//10.2若 AA0204Req.newGroupIDList.size() != 0，則執行10.3、10.4、10.5、10.6反之執行10.7。
				if(newGroupIDList.size() > 0) {
					//10.3查詢TSMP_GROUP資料表，條件GROUP_ID =  AA0204Req.newGroupIDList每一筆，若沒有查詢到資料，則throw RTN Code 1410。
					newGroupIDList.forEach(groupId->{
						TsmpGroup tsmpGroup = getTsmpGroupDao().findById(groupId).orElse(null);
						if(tsmpGroup == null) {
							//1410:群組[{{0}}]不存在
							throw TsmpDpAaRtnCode._1410.throwing(groupId);
						}
					});
					//10.4將AA0204Req.newGroupIDList新增到tsmp_client_group資料表，CLIENT_ID = AA0204Req.clientID、GROUP_ID = AA0204Req.newGroupIDList每一筆。
					for(String groupId : newGroupIDList) {
						TsmpClientGroup clientGroup = new TsmpClientGroup();
						clientGroup.setClientId(clientId);
						clientGroup.setGroupId(groupId);
						getTsmpClientGroupDao().save(clientGroup);
					}
					
					//10.5將"變數tsmpClientGroupEntityList_VGroup"新增到tsmp_client_group資料表
					tsmpClientGroupEntityList_VGroup.forEach(vo ->{
						getTsmpClientGroupDao().save(vo);
					});
					
					//10.6更新oauth_client_details資料表，scope = AA0204Req.newGroupIDList的groupId
					//    與"變數tsmpClientGroupEntityList_VGroup"的groupId，條件client_id = AA0204Req.clientID。備註：scope的資料為5504,5505,5529,5530,2091,T001
					StringJoiner sjScope = new StringJoiner(",");
					newGroupIDList.forEach(groupId -> {
						sjScope.add(groupId);
					});
					tsmpClientGroupEntityList_VGroup.forEach(vo ->{
						sjScope.add(vo.getGroupId());
					});
					OauthClientDetails oauthClientDetails = getOauthClientDetailsDao().findById(clientId).orElse(null);
					if(oauthClientDetails != null) {
						String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, oauthClientDetails); //舊資料統一轉成 String
						
						oauthClientDetails.setScope(sjScope.toString());
						oauthClientDetails = getOauthClientDetailsDao().save(oauthClientDetails);
						
						//寫入 Audit Log D
						lineNumber = StackTraceUtil.getLineNumber();
						getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
								OauthClientDetails.class.getSimpleName(), TableAct.U.value(), oldRowStr, oauthClientDetails);
					}
					
				}else {
					//10.7若"變數tsmpClientGroupEntityList_VGroup"不為空新增到tsmp_client_group資料表。執行步驟10.8，反之則執行10.9
					if(tsmpClientGroupEntityList_VGroup.size() > 0) {
						tsmpClientGroupEntityList_VGroup.forEach(vo ->{
							getTsmpClientGroupDao().save(vo);
						});
						
						//10.8 更新oauth_client_details資料表，scope = "變數tsmpClientGroupEntityList_VGroup"的groupId
						//    ，條件client_id = AA0204Req.clientID。備註：scope的資料為5504,5505,5529,5530,2091,T001
						StringJoiner aa0204_sjScope = new StringJoiner(",");
						tsmpClientGroupEntityList_VGroup.forEach(vo ->{
							aa0204_sjScope.add(vo.getGroupId());
						});
						OauthClientDetails aa0204_oauthClientDetails = getOauthClientDetailsDao().findById(clientId).orElse(null);
						if(aa0204_oauthClientDetails != null) {
							String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, aa0204_oauthClientDetails); //舊資料統一轉成 String
							
							aa0204_oauthClientDetails.setScope(aa0204_sjScope.toString());
							aa0204_oauthClientDetails = getOauthClientDetailsDao().save(aa0204_oauthClientDetails);
							
							//寫入 Audit Log D
							lineNumber = StackTraceUtil.getLineNumber();
							getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
									OauthClientDetails.class.getSimpleName(), TableAct.U.value(), oldRowStr, aa0204_oauthClientDetails);
						}
					}else {
						//10.9 更新oauth_client_details資料表，scope = ""，條件client_id = AA0216Req.clientID。
						OauthClientDetails oauthClientDetails = getOauthClientDetailsDao().findById(clientId).orElse(null);
						if(oauthClientDetails != null) {
							String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, oauthClientDetails); //舊資料統一轉成 String
							
							oauthClientDetails.setScope("");
							getOauthClientDetailsDao().save(oauthClientDetails);
							
							//寫入 Audit Log D
							lineNumber = StackTraceUtil.getLineNumber();
							getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
									OauthClientDetails.class.getSimpleName(), TableAct.U.value(), oldRowStr, oauthClientDetails);
						}
					}
				}
			}
			
			//11.1.查詢tsmp_group資料表，條件GROUP_NAME = AA0217Req.clientName，若有資料則進行步驟11.2。
			if(!newClientName.equals(clientName)) {
				TsmpGroup tsmpGroup = getTsmpGroupDao().findFirstByGroupName(clientName);
				//11.2.更新tsmp_group資料表，欄位GROUP_NAME = AA0217Req.newClientName，條件GROUP_NAME = AA0217Req.clientName。
				if(tsmpGroup != null) {
					tsmpGroup.setGroupName(newClientName);
					tsmpGroup.setUpdateTime(DateTimeUtil.now());
					tsmpGroup.setUpdateUser(authorization.getUserName());
					getTsmpGroupDao().save(tsmpGroup);
				}
			}
			
			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp;
	}
	
	private void checkParam(AA0204Req req) {
		String clientId = req.getClientID();
		String cPriority = req.getcPriority();
		List<AA0204HostReq> newHostList = req.getNewHostList();
		String apiQuota = req.getApiQuota();
		String newApiQuota = req.getNewApiQuota();
		
		String newClientAlias = req.getNewClientAlias();
		String clientName = req.getClientName();
		String newClientName = req.getNewClientName();
		String emails = req.getEmails();
		String newEmails = req.getNewEmails();
		String clientAlias = req.getClientAlias();
		String owner = req.getOwner();
		String tps = req.getTps();
		String newOwner = req.getNewOwner();
		String signupNum = req.getSignupNum();
		String newSignupNum = req.getNewSignupNum();
		String newEncodePublicFlag = req.getNewEncodePublicFlag();
		String newTps = req.getNewTps();
		String encodePublicFlag = req.getEncodePublicFlag();
		List<AA0204HostReq> hostList = req.getHostList();
		List<String> groupIDList = req.getGroupIDList();
		String newCPriority = req.getNewCPriority();
		List<String> newGroupIDList = req.getNewGroupIDList();
		String newClientStartDate = req.getNewClientStartDate();
		String newClientEndDate = req.getNewClientEndDate();
		String newClientStartTimePerDay = req.getNewClientStartTimePerDay();
		String newClientEndTimePerDay = req.getNewClientEndTimePerDay();
		String newTimeZone = req.getNewTimeZone();
		
		if(StringUtils.isEmpty(clientId)) {
			//1343:用戶端帳號:必填參數
			throw TsmpDpAaRtnCode._1343.throwing();
		}
		
		if(StringUtils.isEmpty(newClientName)) {
			//1324:用戶端代號:必填參數
			throw TsmpDpAaRtnCode._1324.throwing();
		}
		
		if(StringUtils.isEmpty(newOwner)) {
			//1334:擁有者:必填參數
			throw TsmpDpAaRtnCode._1334.throwing();
		}
		
		if(newHostList != null && newHostList.size() > 0) {
			newHostList.forEach(x->{
				if((!StringUtils.isEmpty(x.getHostIP()) && StringUtils.isEmpty(x.getHostName()))
						|| (StringUtils.isEmpty(x.getHostIP()) && StringUtils.isEmpty(x.getHostName()))) {
					//1345:主機名稱:必填參數
					throw TsmpDpAaRtnCode._1345.throwing();
				}
				
				if(StringUtils.isEmpty(x.getHostIP()) && !StringUtils.isEmpty(x.getHostName())) {
					//1346:主機IP:必填參數
					throw TsmpDpAaRtnCode._1346.throwing();
				}
				
				if(x.getHostName().length() > 30) {
					//1347:主機名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
					throw TsmpDpAaRtnCode._1347.throwing("30", String.valueOf(x.getHostName().length()));
				}
				
				if(x.getHostIP().length() > 255) {
					//1348:主機IP:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
					throw TsmpDpAaRtnCode._1348.throwing("25550", String.valueOf(x.getHostIP().length()));
				}
				
				if(!ServiceUtil.checkDataByPattern(x.getHostIP(),"^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])$")
						&& !ServiceUtil.checkDataByPattern(x.getHostIP(),"^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$")
						&& !ServiceUtil.checkDataByPattern(x.getHostIP(),"^((?:[0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4})*)?)::((?:([0-9A-Fa-f]{1,4}:)*[0-9A-Fa-f]{1,4})?)$")
						&& !ServiceUtil.checkDataByPattern(x.getHostIP(),"^(::(?:[0-9A-Fa-f]{1,4})(?::[0-9A-Fa-f]{1,4}){5})|((?:[0-9A-Fa-f]{1,4})(?::[0-9A-Fa-f]{1,4}){5}::)$")
						&& !ServiceUtil.checkDataByPattern(x.getHostIP(),"^(([a-zA-Z0-9][-a-zA-Z0-9]*\\.)+[a-zA-Z]{2,}(:\\d+)?)$")
) {
					//1349:主機IP:格式錯誤
					throw TsmpDpAaRtnCode._1349.throwing();
				}
			});
		}
		
		//檢查主機清單是否有重複相同資料
		if(newHostList != null && newHostList.size() > 1) {
			for(int i=0 ; i < newHostList.size() ; i++) {
				for(int j=0 ; j < newHostList.size() ; j++) {
					if(i == j) {
						continue;
					}else if(newHostList.get(i).getHostName().equalsIgnoreCase(newHostList.get(j).getHostName())) {
						//1423:主機名稱{{0}}有重複相同資料
						throw TsmpDpAaRtnCode._1423.throwing(newHostList.get(i).getHostName());
					}else if(newHostList.get(i).getHostIP().equalsIgnoreCase(newHostList.get(j).getHostIP())) {
						//1425:主機IP{{0}}有重複相同資料
						throw TsmpDpAaRtnCode._1425.throwing(newHostList.get(i).getHostIP());
					}
				}
			}
		}
		
		if(newClientAlias != null && newClientAlias.length() > 50) {
			//1326:用戶端名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1326.throwing("50", String.valueOf(newClientAlias.length()));
		}
		
		if(newClientName.length() > 50) {
			//1323:用戶端代號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1323.throwing("50", String.valueOf(newClientName.length()));
		}
		
		if (!ServiceUtil.isNumericOrAlphabetic(newClientName)) {
			//1325:用戶端代號:只能輸入英文字母(a~z,A~Z)及數字且不含空白
			throw TsmpDpAaRtnCode._1325.throwing();
		}
		
		if(!StringUtils.isEmpty(newSignupNum) && newSignupNum.length() > 100) {
			//1328:簽呈編號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1328.throwing("100", String.valueOf(newSignupNum.length()));
		}
		
		if(!StringUtils.isEmpty(newEmails) && newEmails.length() > 500) {
			//1331:電子郵件帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1331.throwing("500", String.valueOf(newEmails.length()));
		}
		
		if(!StringUtils.isEmpty(newEmails) && !ServiceUtil.checkEmail(newEmails)) {
			//1332:電子郵件帳號:只能為Email格式
			throw TsmpDpAaRtnCode._1332.throwing();
		}
		
		if(newOwner.length() > 50) {
			//1333:擁有者:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1333.throwing("50", String.valueOf(newOwner.length()));
		}

		if (StringUtils.hasText(newClientStartDate)) {
			try {
				TimeZoneUtil.anyISO86012long(newClientStartDate);
			} catch (DateTimeParseException e) {
				// 1337:開始日期:只能輸入日期格式
				throw TsmpDpAaRtnCode._1337.throwing();
			}
		}
		
		if (StringUtils.hasText(newClientEndDate)) {
			try {
				TimeZoneUtil.anyISO86012long(newClientEndDate);
			} catch (DateTimeParseException e) {
				// 1338:到期日期:只能輸入日期格式
				throw TsmpDpAaRtnCode._1338.throwing();
			}
		}
		
		if (StringUtils.hasText(newClientStartTimePerDay)) {
			try {
				TimeZoneUtil.anyISO86012long(newClientStartTimePerDay);
			} catch (Exception e) {
				// 1339:服務時間:只能輸入時間格式
				throw TsmpDpAaRtnCode._1339.throwing();
			}
		}
		
		if (StringUtils.hasText(newClientEndTimePerDay)) {
			try {
				TimeZoneUtil.anyISO86012long(newClientEndTimePerDay);
			} catch (Exception e) {
				// 1339:服務時間:只能輸入時間格式
				throw TsmpDpAaRtnCode._1339.throwing();
			}
		}
		
		//有開始時間就要有到期時間
		if (StringUtils.hasText(newClientStartTimePerDay) ^ StringUtils.hasText(newClientEndTimePerDay)) {
			//1369:開始時間與到期時間必須填寫
			throw TsmpDpAaRtnCode._1369.throwing();
		}
		
		//有開始日期就要有到期日期
		if (StringUtils.hasText(newClientStartDate) ^ StringUtils.hasText(newClientEndDate)) {
			//1366:開始日期與到期日期必須填寫
			throw TsmpDpAaRtnCode._1366.throwing();
		}
		
		// 到期日期不可小於開始日期
		if (StringUtils.hasText(newClientStartDate) && StringUtils.hasText(newClientEndDate)) {
			long cSD = TimeZoneUtil.anyISO86012long(newClientStartDate);
			long cED = TimeZoneUtil.anyISO86012long(newClientEndDate);
			if (cSD < 0 || cED < 0 || cED - cSD < 0) {
				// 1368:到期日期不可小於開始日期
				throw TsmpDpAaRtnCode._1368.throwing();
			}
		}
		
		// 到期時間不可小於開始時間
		if (StringUtils.hasText(newClientStartTimePerDay) && StringUtils.hasText(newClientEndTimePerDay)) {
			long cSTPD = TimeZoneUtil.anyISO86012long(newClientStartTimePerDay);
			long cETPD = TimeZoneUtil.anyISO86012long(newClientEndTimePerDay);
			if (cSTPD < 0 || cETPD < 0 || cETPD - cSTPD < 0) {
				// 1367:到期時間不可小於開始時間
				throw TsmpDpAaRtnCode._1367.throwing();
			}
		}
	
		// 有開始日期與到期日期，時區不得為空
		if (StringUtils.hasText(newClientStartDate) && StringUtils.hasText(newClientEndDate) && !StringUtils.hasText(newTimeZone)) {
			// 1523 時區必須填寫
			throw TsmpDpAaRtnCode._1523.throwing();
		}
		
		// 有開始時間與到期時間，時區不得為空
		if (StringUtils.hasText(newClientStartTimePerDay) && StringUtils.hasText(newClientEndTimePerDay) && !StringUtils.hasText(newTimeZone)) {
			// 1523 時區必須填寫
			throw TsmpDpAaRtnCode._1523.throwing();
		}
	}
	
	private int getDp() {
		int dp = -1;
		Optional<TsmpSetting> settingent = getTsmpSettingDao().findById(TsmpSettingDao.Key.TSMP_AC_CONF);
		if (settingent.isPresent()) {
			String settingValue = settingent.get().getValue();
			try {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode root = mapper.readTree(settingValue);
				JsonNode dpNode = root.findValue("dp");	
				dp = Integer.parseInt(dpNode.asText());
			}  catch (IOException e) {
				this.logger.error(StackTraceUtil.logStackTrace(e));
				throw TsmpDpAaRtnCode._1297.throwing();
			}
		}
		return dp;
	}
	
	private boolean isHostListDiffer(List<AA0204HostReq> newHostList, List<AA0204HostReq> hostList) {
		if(newHostList != null && hostList == null) {
			return true;
		}else if(newHostList != null && hostList != null){
			if(newHostList.size() != hostList.size()) {
				return true;
			}else if(!newHostList.containsAll(hostList)) {
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}	
	}
	
	private boolean isGroupIDListDiffer(List<String> newGroupIDList, List<String> groupIDList) {
		if(newGroupIDList != null && groupIDList == null) {
			return true;
		}else if(newGroupIDList != null && groupIDList != null){
			if(newGroupIDList.size() != groupIDList.size()) {
				return true;
			}else if(!newGroupIDList.containsAll(groupIDList)) {
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}	
	}
	
	protected Long getHostSeq() throws Exception {
		
		Long hostSeq = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_CLIENT_HOST_PK);
		if (hostSeq == null) {
			logger.debug("hostSeq=" + hostSeq);
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return hostSeq;
	}
	
	protected String getValueByBcryptParamHelper(String encodeValue, String itemNo, String locale) {
		String value = null;
		try {
			value = getBcryptParamHelper().decode(encodeValue, itemNo, BcryptFieldValueEnum.SUBITEM_NO, locale);// BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return value;
	}
	
	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return tsmpClientGroupDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}
	
	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return tsmpDpClientextDao;
	}
	
	protected TsmpClientHostDao getTsmpClientHostDao() {
		return tsmpClientHostDao;
	}

	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}
	
	protected SeqStoreService getSeqStoreService() {
		return this.seqStoreService;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
}
