package tpi.dgrv4.dpaa.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.TimeZoneUtil;
import tpi.dgrv4.common.constant.*;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.component.job.AA0201Job;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredMailJob;
import tpi.dgrv4.dpaa.util.OAuthUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0201HostReq;
import tpi.dgrv4.dpaa.vo.AA0201Req;
import tpi.dgrv4.dpaa.vo.AA0201Resp;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.entity.entity.jpql.TsmpClientHost;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.repository.*;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AA0201Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpClientDao tsmpClientDao;
	private TsmpClientHostDao tsmpClientHostDao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private TsmpGroupDao tsmpGroupDao;
	private SeqStoreService seqStoreService;
	private BcryptParamHelper bcryptParamHelper;
	private TsmpSettingDao tsmpSettingDao;
	private ApplicationContext ctx;
	private JobHelper jobHelper;
	private TsmpClientGroupDao tsmpClientGroupDao;
	private ServiceConfig serviceConfig;
	private DgrAuditLogService dgrAuditLogService;
	private TsmpDpMailTpltDao tsmpDpMailTpltDao;
	private TsmpSettingService tsmpSettingService;
	
	private String sendTime;
	 
	@Autowired
	public AA0201Service(TsmpClientDao tsmpClientDao, TsmpClientHostDao tsmpClientHostDao,
			TsmpDpClientextDao tsmpDpClientextDao, OauthClientDetailsDao oauthClientDetailsDao,
			TsmpGroupDao tsmpGroupDao, SeqStoreService seqStoreService, BcryptParamHelper bcryptParamHelper,
			TsmpSettingDao tsmpSettingDao, ApplicationContext ctx, JobHelper jobHelper,
			TsmpClientGroupDao tsmpClientGroupDao, ServiceConfig serviceConfig, DgrAuditLogService dgrAuditLogService,
			TsmpDpMailTpltDao tsmpDpMailTpltDao, TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpClientHostDao = tsmpClientHostDao;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.seqStoreService = seqStoreService;
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpSettingDao = tsmpSettingDao;
		this.ctx = ctx;
		this.jobHelper = jobHelper;
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.serviceConfig = serviceConfig;
		this.dgrAuditLogService = dgrAuditLogService;
		this.tsmpDpMailTpltDao = tsmpDpMailTpltDao;
		this.tsmpSettingService = tsmpSettingService;
	}

	@PostConstruct
	public void init() {
	}

	@Transactional
	public AA0201Resp addClient(TsmpAuthorization authorization, AA0201Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_CLIENT.value());
		
		AA0201Resp resp = new AA0201Resp();

		try {
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			String clientName = req.getClientName();
			String clientAlias = req.getClientAlias();
			String signupNum = req.getSignupNum();
			String owner = req.getOwner();
			String clientBlock = req.getClientBlock();
			String clientID = req.getClientID();
			String emails = req.getEmails();
			String tps = req.getTps();
			String encodeStatus = req.getEncodeStatus();
			String publicFlag = req.getPublicFlag();
			List<AA0201HostReq> hostList = req.getHostList();
			String apiQuota = req.getApiQuota();
			String clientEndDate = req.getClientEndDate();
			String cPriority = req.getcPriority();
			List<String> groupIDList = req.getGroupIDList();
			String remark = req.getRemark();
			String clientStartDate = req.getClientStartDate();
			String timeZone = req.getTimeZone();
			String clientStartTimePerDay = req.getClientStartTimePerDay();
			String clientEndTimePerDay = req.getClientEndTimePerDay();
			
			checkParam(req);
			
			//5.AA0201Req.clientID = (取AA0201Req.clientName的Byte，在用Base64編譯成String)
			if(StringUtils.isEmpty(clientID)) {
				clientID = Base64Util.base64Encode(clientName.getBytes());
			}
			
			//6.查詢TSMP_CLIENT資料表，條件CLIENT_ID = AA0201Req.clientID，若查到資料則throw RTN Code 1342。
			TsmpClient tsmpClient = getTsmpClientDao().findById(clientID).orElse(null);
			if(tsmpClient != null) {
				//1342:用戶端帳號已存在
				throw TsmpDpAaRtnCode._1342.throwing();
			}
			
			if(groupIDList != null && groupIDList.size() > 0) {
				Set<String> groupIdSet = new HashSet<String>();

				//6.1若AA0201Req.groupIDList不為空，則計算AA0201Req.groupIDList不重複的數量 ，並執行步驟6.2、6.3、6.4。
				groupIDList.forEach(x->{
					groupIdSet.add(x);
				} );
				
				//6.2查詢TSMP_CLIENT_GROUP資料表，條件CLIENT_ID = AA0201Req.clientID，將查詢出來的資料再去查詢TSMP_GROUP資料表
				//  ，條件GROUP_ID=TSMP_CLIENT_GROUP.GROUP_ID AND VGROUP_FLAG = "1"，計算(GROUP_ID)不重覆數量。
				List<TsmpClientGroup> groupList = getTsmpClientGroupDao().findByClientId(clientID);
				for(TsmpClientGroup vo : groupList) {
					TsmpGroup tsmpGroup = getTsmpGroupDao().findFirstByGroupIdAndVgroupFlag(vo.getGroupId(), "1");
					if(tsmpGroup != null) {
						groupIdSet.add(tsmpGroup.getGroupId());
					}
				}
				
				//6.3 將6.1與6.2的數量加總，若超過205則throw RTN Code 1358。
				if(groupIdSet.size() > 205) {
					//1358:[群組名稱]超過群組選取上限205，請重新選取
					throw TsmpDpAaRtnCode._1358.throwing();
				}
				
				//6.4 查詢TSMP_GROUP資料表，條件GROUP_ID = AA0201Req.groupIDList每一筆
				//   ，查詢出來的TSMP_GROUP.SECURITY_LEVEL_ID若不為空，則throw RTN Code 1359。
				groupIDList.forEach(groupId->{
					TsmpGroup tsmpGroup = getTsmpGroupDao().findById(groupId).orElse(null);
					if(tsmpGroup != null) {
						if(!"".equals(tsmpGroup.getSecurityLevelId())) {
							//1359:建立新Client Group時，必須是SYSTEM Group
							throw TsmpDpAaRtnCode._1359.throwing();
						}
					}else {
						//6.5 查詢TSMP_GROUP資料表，條件GROUP_ID =  AA0201Req.groupIDList每一筆，若沒有查詢到資料，則throw RTN Code 1410。
						////1410:群組[{{0}}]不存在
						throw TsmpDpAaRtnCode._1410.throwing(groupId);
					}
				});
				
			}
			
			encodeStatus = getValueByBcryptParamHelper(encodeStatus, "ENABLE_FLAG", BcryptFieldValueEnum.PARAM1, locale);
			publicFlag = getValueByBcryptParamHelper(publicFlag, "API_AUTHORITY", BcryptFieldValueEnum.SUBITEM_NO, locale);
			
			/*
			 * 只要有異動到 Client pwd (OAUTH_CLIENT_DETAILS.client_secret) 這個欄位,	
			 * 都要將 Client pwd 經過 SHA512URL base64 hash 的值,更新至 TSMP_CLIENT.client_secret	
			 */
			String clientPwd = new String(ServiceUtil.base64Decode(clientBlock));//Client PW,經過 Base64 Decode 的值
			String sha512Pwd = ServiceUtil.getSHA512ToBase64(clientPwd);
			
			//7.將AA0201Req欄位新增到TSMP_CLIENT資料表，有些欄位是固定值TSMP_CLIENT.SECURITY_LEVEL_ID= "SYSTEM"
			// 、TSMP_CLIENT.PWD_FAIL_TIMES = 0、TSMP_CLIENT.FAIL_TRESHHOLD = 3、TSMP_CLIENT.CREATE_TIME、TSMP_CLIENT.CREATE_USER、TSMP_CLIENT.UPDATE_TIME、TSMP_CLIENT.UPDATE_USER。
			tsmpClient = new TsmpClient();
			tsmpClient.setApiQuota(StringUtils.isEmpty(apiQuota) ? 0 : Integer.valueOf(apiQuota));
			tsmpClient.setClientAlias(clientAlias);
			tsmpClient.setClientId(clientID);
			tsmpClient.setClientName(clientName);
			tsmpClient.setClientStatus(encodeStatus);
			tsmpClient.setcPriority(StringUtils.isEmpty(cPriority) ? 5 : Integer.valueOf(cPriority));
			tsmpClient.setCreateTime(DateTimeUtil.now());
			tsmpClient.setCreateUser(authorization.getUserName());
			tsmpClient.setEmails(emails);
			tsmpClient.setOwner(owner);
			tsmpClient.setSecurityLevelId("SYSTEM");
			tsmpClient.setSignupNum(signupNum);
			tsmpClient.setTps(StringUtils.isEmpty(tps) ? 10 : Integer.valueOf(tps));
			tsmpClient.setPwdFailTimes(0);
			tsmpClient.setFailTreshhold(3);
			tsmpClient.setClientSecret(sha512Pwd);
			tsmpClient.setRemark(remark);
			tsmpClient.setStartDate(
					StringUtils.hasText(clientStartDate) ? TimeZoneUtil.anyISO86012long(clientStartDate) : null);
			tsmpClient.setEndDate(
					StringUtils.hasText(clientEndDate) ? TimeZoneUtil.anyISO86012long(clientEndDate) : null);
			tsmpClient.setStartTimePerDay(
					StringUtils.hasText(clientStartTimePerDay) ? TimeZoneUtil.anyISO86012long(clientStartTimePerDay) : null);
			tsmpClient.setEndTimePerDay(
					StringUtils.hasText(clientEndTimePerDay) ? TimeZoneUtil.anyISO86012long(clientEndTimePerDay) : null);
			tsmpClient.setTimeZone(StringUtils.hasText(timeZone) ? timeZone : null);
			tsmpClient = getTsmpClientDao().save(tsmpClient);
			//寫入 Audit Log D
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber,
					TsmpClient.class.getSimpleName(), TableAct.C.value(), null, tsmpClient);
			
			//8.將AA0201Req.hostList欄位新增到TSMP_CLIENT_HOST資料表，TSMP_CLIENT_HOST .HOST_SEQ = 取"SEQ_TSMP_CLIENT_HOST_PK"
			//、TSMP_CLIENT_HOST.CLIENT_ID = TSMP_CLIENT.CLIENT_ID、TSMP_CLIENT_HOST.HOST_IP = AA0201Req.hostList.hostIP
			//、TSMP_CLIENT_HOST.HOST_NAME = AA0201Req.hostList.hostName、TSMP_CLIENT_HOST.CREATE_TIME= TSMP_CLIENT.CREATE_TIME
			if(hostList != null && hostList.size() > 0) {
				for(AA0201HostReq vo : hostList) {
					TsmpClientHost hostVo = new TsmpClientHost();
					hostVo.setClientId(tsmpClient.getClientId());
					hostVo.setCreateTime(tsmpClient.getCreateTime());
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
			
			//9.1將AA0201Req.groupIDList新增到TSMP_CLIENT_GROUP資料表，欄位CLIENT_ID = TSMP_CLIENT.CLIENT_ID 、GROUP_ID = AA0201Req.groupIDList每一筆。
			if(groupIDList != null && groupIDList.size() > 0) {
				for(String groupId : groupIDList) {
					TsmpClientGroup clientGroup = new TsmpClientGroup();
					clientGroup.setClientId(tsmpClient.getClientId());
					clientGroup.setGroupId(groupId);
					getTsmpClientGroupDao().save(clientGroup);
				}
			}
			


			//11.查詢SEQ_STORE，條件sequence_name = "TSMP_DP_CLIENTEXT"，取得SEQ_STORE.next_val +1 當作是CLIENT_SEQ_ID 
			//，再後更新回SEQ_STORE資料表。此功能已經有實作請洽Kim兄。
			final Long clientSeqId = getSeqStoreService().nextSequence(//
					TsmpDpSeqStoreKey.TSMP_DP_CLIENTEXT, 2000000000L, 1L);
			
			//10.新增到TSMP_DP_CLIENTEXT資料表，TSMP_DP_CLIENTEXT.CONTENT_TXT="後台申請"、TSMP_DP_CLIENTEXT.REG_STATUS="2"
			//、TSMP_DP_CLIENTEXT.PUBLIC_FLAG=AA0201Req.publicFlag、TSMP_DP_CLIENTEXT.CLIENT_ID = TSMP_CLIENT.CLIENT_ID、TSMP_DP_CLIENTEXT.CLIENT_SEQ_ID = 步驟 11 。
			TsmpDpClientext dpClientextVo = new TsmpDpClientext();
			dpClientextVo.setClientId(tsmpClient.getClientId());
			dpClientextVo.setClientSeqId(clientSeqId);
			dpClientextVo.setContentTxt("後台申請");
			dpClientextVo.setPublicFlag(publicFlag);
			dpClientextVo.setRegStatus("2");
			dpClientextVo = getTsmpDpClientextDao().save(dpClientextVo);
			//寫入 Audit Log D
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber,
					TsmpDpClientext.class.getSimpleName(), TableAct.C.value(), null, dpClientextVo);

			
			//12.新增OAUTH_CLIENT_DETAILS資料表，OAUTH_CLIENT_DETAILS.CLIENT_ID = TSMP_CLIENT.CLIENT_ID
			//、OAUTH_CLIENT_DETAILS.RESOURCE_IDS = ("adminAPI"用Base64編碼(YWRtaW5BUEk))
			//、OAUTH_CLIENT_DETAILS.CLIENT_SECRET = AA0201Req.clientBlock(請看加密流程說明，可以請教Mavis)
			//、OAUTH_CLIENT_DETAILS.SCOPE="select"
			//、OAUTH_CLIENT_DETAILS.AUTHORIZED_GRANT_TYPES="client_credentials,refresh_token,password,authorization_code"
			//、OAUTH_CLIENT_DETAILS.AUTHORITIES="client"、OAUTH_CLIENT_DETAILS.ADDITIONAL_INFORMATION="{}"。
			OauthClientDetails oauthVo = new OauthClientDetails();
			oauthVo.setClientId(tsmpClient.getClientId());
			oauthVo.setResourceIds("YWRtaW5BUEk");
			String bcryptEncode = OAuthUtil.bCryptEncode(clientBlock);
			oauthVo.setClientSecret(bcryptEncode);
			oauthVo.setScope("select");
			oauthVo.setAuthorizedGrantTypes("client_credentials,refresh_token,password,authorization_code");
			oauthVo.setAuthorities("client");
			oauthVo.setAdditionalInformation("{}");
			oauthVo = this.getOauthClientDetailsDao().save(oauthVo);
			//寫入 Audit Log D
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber,
					OauthClientDetails.class.getSimpleName(), TableAct.C.value(), null, oauthVo);
			
			if(!StringUtils.isEmpty(emails)) {
				sendEmail(req, authorization, tsmpClient);
				
				// 刪除過期的 Mail log
				deleteExpiredMail();
			}
			
			resp.setClientID(clientID);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		
		return resp;
	}
	
	private void checkParam(AA0201Req req) {
		String clientID = req.getClientID();
		String clientName = req.getClientName();
		String clientAlias = req.getClientAlias();
		String signupNum = req.getSignupNum();
		String clientBlock = req.getClientBlock();
		String emails = req.getEmails();
		String tps = req.getTps();
		String encodeStatus = req.getEncodeStatus();
		String publicFlag = req.getPublicFlag();
		List<AA0201HostReq> hostList = req.getHostList();;
		String apiQuota = req.getApiQuota();
		String owner = req.getOwner();
		String cPriority = req.getcPriority();
		List<String> groupIDList = req.getGroupIDList();
		String clientStartDate = req.getClientStartDate();
		String clientEndDate = req.getClientEndDate();
		String clientStartTimePerDay = req.getClientStartTimePerDay();
		String clientEndTimePerDay = req.getClientEndTimePerDay();
		String timeZone = req.getTimeZone();
		
		if(StringUtils.isEmpty(clientName)) {
			//1324:用戶端代號:必填參數
			throw TsmpDpAaRtnCode._1324.throwing();
		}
		
		if(StringUtils.isEmpty(clientAlias)) {
			//1327:用戶端名稱:必填參數
			throw TsmpDpAaRtnCode._1327.throwing();
		}
		
		if(StringUtils.isEmpty(clientBlock)) {
			//1330:密碼:必填參數
			throw TsmpDpAaRtnCode._1330.throwing();
		}
		
		if(StringUtils.isEmpty(owner)) {
			//1334:擁有者:必填參數
			throw TsmpDpAaRtnCode._1334.throwing();
		}
		
		if(StringUtils.isEmpty(encodeStatus)) {
			//1335:狀態:必填參數
			throw TsmpDpAaRtnCode._1335.throwing();
		}
		
		if(StringUtils.isEmpty(publicFlag)) {
			//1336:開放狀態:必填參數
			throw TsmpDpAaRtnCode._1336.throwing();
		}
		
		if(hostList != null && hostList.size() > 0) {
			hostList.forEach(x->{
				
				if(StringUtils.isEmpty(x.getHostIP()) && !StringUtils.isEmpty(x.getHostName())) {
					//1346:主機IP:必填參數
					throw TsmpDpAaRtnCode._1346.throwing();
				}
				
				if((!StringUtils.isEmpty(x.getHostIP()) && StringUtils.isEmpty(x.getHostName()))
						|| (StringUtils.isEmpty(x.getHostIP()) && StringUtils.isEmpty(x.getHostName()))) {
					//1345:主機名稱:必填參數
					throw TsmpDpAaRtnCode._1345.throwing();
				}
				
				if(x.getHostIP().length() > 255) {
					//1348:主機IP:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
					throw TsmpDpAaRtnCode._1348.throwing("255", String.valueOf(x.getHostIP().length()));
				}
				
				if(x.getHostName().length() > 30) {
					//1347:主機名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
					throw TsmpDpAaRtnCode._1347.throwing("30", String.valueOf(x.getHostName().length()));
				}
				
				if(!ServiceUtil.checkDataByPattern(x.getHostIP(),"^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])$")
						&& !ServiceUtil.checkDataByPattern(x.getHostIP(),"^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$")
						&& !ServiceUtil.checkDataByPattern(x.getHostIP(),"^((?:[0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4})*)?)::((?:([0-9A-Fa-f]{1,4}:)*[0-9A-Fa-f]{1,4})?)$")
						&& !ServiceUtil.checkDataByPattern(x.getHostIP(),"^(::(?:[0-9A-Fa-f]{1,4})(?::[0-9A-Fa-f]{1,4}){5})|((?:[0-9A-Fa-f]{1,4})(?::[0-9A-Fa-f]{1,4}){5}::)$") 
						&& !ServiceUtil.checkDataByPattern(x.getHostIP(),"^(([a-zA-Z0-9][-a-zA-Z0-9]*\\.)+[a-zA-Z]{2,}(:\\d+)?)$")){

					//1349:主機IP:格式錯誤
					throw TsmpDpAaRtnCode._1349.throwing();
				}
			});
		}
		//檢查主機清單是否有重複相同資料
		if(hostList != null && hostList.size() > 1) {
			for(int i=0 ; i < hostList.size() ; i++) {
				for(int j=0 ; j < hostList.size() ; j++) {
					if(i == j) {
						continue;
					}else if(hostList.get(i).getHostName().equalsIgnoreCase(hostList.get(j).getHostName())) {
						//1423:主機名稱{{0}}有重複相同資料
						throw TsmpDpAaRtnCode._1423.throwing(hostList.get(i).getHostName());
					}else if(hostList.get(i).getHostIP().equalsIgnoreCase(hostList.get(j).getHostIP())) {
						//1425:主機IP{{0}}有重複相同資料
						throw TsmpDpAaRtnCode._1425.throwing(hostList.get(i).getHostIP());
					}
				}
			}
		}
		
		
		if(!StringUtils.isEmpty(clientID) && clientID.length() > 40) {
			//1322:用戶端帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1322.throwing("40", String.valueOf(clientID.length()));
		}
		
		if(clientName.length() > 50) {
			//1323:用戶端代號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1323.throwing("50", String.valueOf(clientName.length()));
		}
		
		if (!ServiceUtil.isNumericOrAlphabetic(clientName)) {
			//1325:用戶端代號:只能輸入英文字母(a~z,A~Z)及數字且不含空白
			throw TsmpDpAaRtnCode._1325.throwing();
		}
		
		if(clientAlias.length() > 50) {
			//1326:用戶端名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1326.throwing("50", String.valueOf(clientAlias.length()));
		}
		
		if(!StringUtils.isEmpty(signupNum) && signupNum.length() > 100) {
			//1328:簽呈編號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1328.throwing("100", String.valueOf(signupNum.length()));
		}
		
		if(clientBlock.length() > 256) {
			//1329:密碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1329.throwing("256", String.valueOf(clientBlock.length()));
		}
		
		if(!StringUtils.isEmpty(emails) && emails.length() > 500) {
			//1331:電子郵件帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1331.throwing("500", String.valueOf(emails.length()));
		}
		
		if(!StringUtils.isEmpty(emails) && !ServiceUtil.checkEmail(emails)) {
			//1332:電子郵件帳號:只能為Email格式
			throw TsmpDpAaRtnCode._1332.throwing();
		}
		
		if(owner.length() > 50) {
			//1333:擁有者:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1333.throwing("50", String.valueOf(owner.length()));
		}
		
		if (StringUtils.hasText(clientStartDate)) {
			try {
				TimeZoneUtil.anyISO86012long(clientStartDate);
			} catch (DateTimeParseException e) {
				// 1337:開始日期:只能輸入日期格式
				throw TsmpDpAaRtnCode._1337.throwing();
			}
		}

		if (StringUtils.hasText(clientEndDate)) {
			try {
				TimeZoneUtil.anyISO86012long(clientEndDate);
			} catch (DateTimeParseException e) {
				// 1338:到期日期:只能輸入日期格式
				throw TsmpDpAaRtnCode._1338.throwing();
			}
		}
		
		if (StringUtils.hasText(clientStartTimePerDay)) {
			try {
				TimeZoneUtil.anyISO86012long(clientStartTimePerDay);
			} catch (DateTimeParseException e) {
				// 1339:服務時間:只能輸入時間格式
				throw TsmpDpAaRtnCode._1339.throwing();
			}
		}

		if (StringUtils.hasText(clientEndTimePerDay)) {
			try {
				TimeZoneUtil.anyISO86012long(clientEndTimePerDay);
			} catch (DateTimeParseException e) {
				// 1339:服務時間:只能輸入時間格式
				throw TsmpDpAaRtnCode._1339.throwing();
			}
		}
		
		//有開始時間就要有到期時間
		if (StringUtils.hasText(clientStartTimePerDay) ^ StringUtils.hasText(clientEndTimePerDay)) {
			//1369:開始時間與到期時間必須填寫
			throw TsmpDpAaRtnCode._1369.throwing();
		}

		//有開始日期就要有到期日期
		if (StringUtils.hasText(clientStartDate) ^ StringUtils.hasText(clientEndDate)) {
			//1366:開始日期與到期日期必須填寫
			throw TsmpDpAaRtnCode._1366.throwing();
		}
		
		// 到期日期不可小於開始日期
		if (StringUtils.hasText(clientStartDate) && StringUtils.hasText(clientEndDate)) {
			long cSD = TimeZoneUtil.anyISO86012long(clientStartDate);
			long cED = TimeZoneUtil.anyISO86012long(clientEndDate);
			if (cSD < 0 || cED < 0 || cED - cSD < 0) {
				// 1368:到期日期不可小於開始日期
				throw TsmpDpAaRtnCode._1368.throwing();
			}
		}

		// 到期時間不可小於開始時間
		if (StringUtils.hasText(clientStartTimePerDay) && StringUtils.hasText(clientEndTimePerDay)) {
			long cSTPD = TimeZoneUtil.anyISO86012long(clientStartTimePerDay);
			long cETPD = TimeZoneUtil.anyISO86012long(clientEndTimePerDay);
			if (cSTPD < 0 || cETPD < 0 || cETPD - cSTPD < 0) {
				// 1367:到期時間不可小於開始時間
				throw TsmpDpAaRtnCode._1367.throwing();
			}
		}

		// 有開始日期與到期日期，時區不得為空
		if (StringUtils.hasText(clientStartDate) && StringUtils.hasText(clientEndDate) && !StringUtils.hasText(timeZone)) {
			// 1523 時區必須填寫
			throw TsmpDpAaRtnCode._1523.throwing();
		}
		
		// 有開始時間與到期時間，時區不得為空
		if (StringUtils.hasText(clientStartTimePerDay) && StringUtils.hasText(clientEndTimePerDay) && !StringUtils.hasText(timeZone)) {
			// 1523 時區必須填寫
			throw TsmpDpAaRtnCode._1523.throwing();
		}
		
		//2.查詢TSMP_CLIENT資料表，條件CLIENT_NAME = AA0201Req.clientName，若查到資料則throw RTN Code 1340。
		List<TsmpClient> clientList = getTsmpClientDao().findByClientName(clientName);
		if(clientList.size() > 0) {
			//1340:用戶端代號已存在
			throw TsmpDpAaRtnCode._1340.throwing();
		}
		
		//3.查詢TSMP_CLIENT資料表，條件CLIENT_ALIAS = AA0201Req.clientAlias，若查到資料則throw RTN Code 1341。
		clientList = getTsmpClientDao().findByClientAlias(clientAlias);
		if(clientList.size() > 0) {
			//1341:用戶端名稱已存在
			throw TsmpDpAaRtnCode._1341.throwing();
		}
	}
	
	protected String getValueByBcryptParamHelper(String encodeValue, String itemNo, BcryptFieldValueEnum fieldValue, String locale) {
		String value = null;
		try {
			value = getBcryptParamHelper().decode(encodeValue, itemNo , fieldValue, locale);// BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return value;
	}
	
	protected Long getHostSeq() throws Exception {
		
		Long hostSeq = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_CLIENT_HOST_PK);
		if (hostSeq == null) {
			logger.debug("hostSeq=" + hostSeq);
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return hostSeq;
	}
	
	public AA0201Job sendEmail(AA0201Req req, TsmpAuthorization auth, TsmpClient tsmpClient) {
		List<TsmpMailEvent> mailEvents = new ArrayList<>();
			TsmpMailEvent mailEvent = getTsmpMailEvent(req, auth, tsmpClient);
			if (mailEvent != null) {
				mailEvents.add(mailEvent);
			}
			
		AA0201Job job = getAA0201Job(auth, mailEvents, getSendTime());
		getJobHelper().add(job);
			
		return job;
	}
	
	protected DeleteExpiredMailJob deleteExpiredMail() {
		DeleteExpiredMailJob job = (DeleteExpiredMailJob) getCtx().getBean("deleteExpiredMailJob");
		getJobHelper().add(job);
		return job;
	}
	
	private TsmpMailEvent getTsmpMailEvent( AA0201Req req , TsmpAuthorization authorization, TsmpClient tsmpClient) {
		String aa0201_clientId = authorization.getClientId();
		String aa0201_recipients = req.getEmails();
		
		if (aa0201_recipients == null || aa0201_recipients.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty emails!", aa0201_clientId));
			return null;
		}

		String aa0201_subject = getTemplate("subject.add-client");
		String aa0201_body = getTemplate("body.add-client");
		
		if (aa0201_subject == null || aa0201_body == null) {
			return null;
		}

		Map<String, String> aa0201_subjectParams = getSubjectParams();
		if (aa0201_subjectParams == null || aa0201_subjectParams.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty subject params!", aa0201_clientId));
			return null;
		}

		Map<String, String> aa0201_bodyParams = getBodyParams(req, tsmpClient);
		if (aa0201_bodyParams == null || aa0201_bodyParams.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty body params!", aa0201_clientId));
			return null;
		}

		final String aa0201_title = MailHelper.buildContent(aa0201_subject, aa0201_subjectParams);
		final String aa0201_content = MailHelper.buildContent(aa0201_body, aa0201_bodyParams);
		this.logger.debug("Email title = " + aa0201_title);
		this.logger.debug("Email content = " + aa0201_content);
		
		return new TsmpMailEventBuilder() //
		.setSubject(aa0201_title)
		.setContent(aa0201_content)
		.setRecipients(aa0201_recipients)
		.setCreateUser(authorization.getUserName())
		.setRefCode("body.add-client")
		.build();
	}

	private String getTemplate(String code) {
		List<TsmpDpMailTplt> aa0201_list = getTsmpDpMailTpltDao().findByCode(code);
		if (aa0201_list != null && !aa0201_list.isEmpty()) {
			return aa0201_list.get(0).getTemplateTxt();
		}
		return null;
	}
	
	private Map<String, String> getSubjectParams() {
		Map<String, String> aa0201_emailParams = new HashMap<>();
		aa0201_emailParams.put("projectName", TsmpDpModule.DP.getChiDesc());
		return aa0201_emailParams;
	}
	
	private Map<String, String> getBodyParams(AA0201Req req, TsmpClient tsmpClient) {

		Map<String, String> emailParams = new HashMap<>();
		
		String now = "";
		Optional<String> opt = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日_2);
		if (opt.isPresent()) {
			now = opt.get();
		}
		
		String clientID = tsmpClient.getClientId();
		String client = tsmpClient.getClientName();
		String newBlock = new String(ServiceUtil.base64Decode(req.getClientBlock()));
		String base64Block = req.getClientBlock();
		String hosts = req.getHostList() == null || StringUtils.isEmpty(req.getHostList().stream().map(AA0201HostReq::getHostIP).collect(Collectors.joining("/")))
				       ?"(n/a)":req.getHostList().stream().map(AA0201HostReq::getHostIP).collect(Collectors.joining("/"));
		String clientDate = StringUtils.isEmpty(req.getClientStartDate()) ? "(no limit)"
				: longDateTimeToStringDateTime(req.getClientStartDate(),req.getTimeZone(),DateTimeFormatEnum.西元年月日_2) + 
				"~" + longDateTimeToStringDateTime(req.getClientEndDate(),req.getTimeZone(),DateTimeFormatEnum.西元年月日_2);
		String serviceTime = StringUtils.isEmpty(req.getClientStartTimePerDay()) ? "(no limit)"
				: longDateTimeToStringDateTime(req.getClientStartTimePerDay(),req.getTimeZone(),DateTimeFormatEnum.時分秒) + 
				"~" + longDateTimeToStringDateTime(req.getClientEndTimePerDay(),req.getTimeZone(),DateTimeFormatEnum.時分秒);
		String quota = req.getApiQuota()== null || "0".equals(req.getApiQuota()) ? "(0: no limit)": req.getApiQuota();
		String tps = req.getTps() == null ? "10" : req.getTps();
		String date = now;
		
		emailParams.put("clientID", clientID);
		emailParams.put("client", client);
		emailParams.put("newBlock", newBlock);
		emailParams.put("base64Block", base64Block);
		emailParams.put("hosts", hosts);
		emailParams.put("clientDate", clientDate);
		emailParams.put("serviceTime", serviceTime);
		emailParams.put("quota", quota);
		emailParams.put("tps", tps);
		emailParams.put("date", date);
		
		return emailParams;
	}
	
	private String longDateTimeToStringDateTime(String datetime, String timezone, DateTimeFormatEnum dateTimeFormatEnum) {
		if (datetime == null || !StringUtils.hasText(timezone)) {
			return "";
		}
		Date date = new Date(TimeZoneUtil.anyISO86012long(datetime));
		return DateTimeUtil.dateTimeToString(date, dateTimeFormatEnum, timezone).orElse("");
	}	

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected TsmpClientHostDao getTsmpClientHostDao() {
		return tsmpClientHostDao;
	}
	
	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}
	
	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return tsmpDpClientextDao;
	}
	
	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}
	
	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return tsmpClientGroupDao;
	}
	
	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}
	
	protected ApplicationContext getCtx() {
		return ctx;
	}

	protected JobHelper getJobHelper() {
		return jobHelper;
	}
	
	protected TsmpDpMailTpltDao getTsmpDpMailTpltDao() {
		return this.tsmpDpMailTpltDao;
	}

	protected String getSendTime() {
		this.sendTime = this.getTsmpSettingService().getVal_MAIL_SEND_TIME();//多久後寄發Email(ms)
		return this.sendTime;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}
	
	protected AA0201Job getAA0201Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return (AA0201Job) getCtx().getBean("aa0201Job", auth, mailEvents, getSendTime());
	}
	
	protected SeqStoreService getSeqStoreService() {
		return this.seqStoreService;
	}
	protected ServiceConfig getServiceConfig() {
		return serviceConfig;
	}
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	protected TsmpSettingService getTsmpSettingService(){
		return this.tsmpSettingService;
	}
}
