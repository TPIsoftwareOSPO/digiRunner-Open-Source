package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0216Req;
import tpi.dgrv4.dpaa.vo.AA0216Resp;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpClientGroup;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0216Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpClientGroupDao tsmpClientGroupDao;
	private TsmpGroupDao tsmpGroupDao;
	private TsmpClientDao tsmpClientDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private DgrAuditLogService dgrAuditLogService;
	
	@Autowired
	public AA0216Service(TsmpClientGroupDao tsmpClientGroupDao, TsmpGroupDao tsmpGroupDao, TsmpClientDao tsmpClientDao,
			OauthClientDetailsDao oauthClientDetailsDao, DgrAuditLogService dgrAuditLogService) {
		super();
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpClientDao = tsmpClientDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	@Transactional
	public AA0216Resp addClientGroupByClientId(TsmpAuthorization authorization, AA0216Req req, InnerInvokeParam iip) {
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
				
		AA0216Resp resp = new AA0216Resp();

		try {
			String clientId = req.getClientID();
			List<String> groupIdList = req.getGroupIDList();

			if(groupIdList != null && groupIdList.size() > 0) {
				Set<String> groupIdSet = new HashSet<String>();

				//1.1若AA0216Req.groupIDList不為空，則計算AA0216Req.groupIDList不重複的數量 ，並執行步驟1.2、1.3，主要是計算群組不可以超過數量205。
				groupIdList.forEach(x->{
					groupIdSet.add(x);
				} );
				
				//1.2查詢TSMP_CLIENT_GROUP資料表，條件CLIENT_ID = AA0216Req.clientID，
				//將查詢出來的資料(TSMP_CLIENT_GROUP.GROUP_ID)再去查詢TSMP_GROUP資料表，
				//條件GROUP_ID=TSMP_CLIENT_GROUP.GROUP_ID，計算(GROUP_ID)不重覆數量。
				List<TsmpClientGroup> groupList = getTsmpClientGroupDao().findByClientId(clientId);
				for(TsmpClientGroup vo : groupList) {
					TsmpGroup tsmpGroup = getTsmpGroupDao().findById(vo.getGroupId()).orElse(null);
					if(tsmpGroup != null) {
						groupIdSet.add(tsmpGroup.getGroupId());
					}
				}
				
				//1.3 將1.1與1.2的數量加總，若超過205則throw RTN Code 1358。
				if(groupIdSet.size() > 205) {
					//1358:[群組名稱]超過群組選取上限205，請重新選取
					throw TsmpDpAaRtnCode._1358.throwing();
				}
				
				
				//2.1.查詢TSMP_CLIENT資料表，條件CLIENT_ID = AA0216Req.clientID ，若查不到資料則throw RTN Code 1344。
				//有查詢到資料將TSMP_CLIENT.SECURITY_LEVEL_ID取出。
				TsmpClient aa0216_tsmpClientVo = getTsmpClientDao().findById(clientId).orElse(null);
				final String securityLevelId;
				if(aa0216_tsmpClientVo != null) {
					securityLevelId = aa0216_tsmpClientVo.getSecurityLevelId();
				}else {
					//1344:用戶端不存在
					throw TsmpDpAaRtnCode._1344.throwing();
				}
				
				//2.2查詢TSMP_GROUP資料表，條件GROUP_ID = AA0216Req.groupIDList每一筆，若沒有查詢到資料
				//，則throw RTN Code 1410，若TSMP_GROUP.SECURITY_LEVEL_ID !=TSMP_CLIENT.SECURITY_LEVEL_ID，則throw RTN Code 1365。
				groupIdList.forEach(groupId -> {
					TsmpGroup aa0216_tsmpGroupVo = getTsmpGroupDao().findById(groupId).orElse(null);
					if(aa0216_tsmpGroupVo == null) {
						//1410:群組[{{0}}]不存在
						throw TsmpDpAaRtnCode._1410.throwing(groupId);
					}else {
						if(securityLevelId != null && !securityLevelId.equals(aa0216_tsmpGroupVo.getSecurityLevelId())) {
							//1365:用戶端的SECURITY LEVEL ID與群組的SECURITY LEVEL ID不符合，群組ID為[{{0}}]
							throw TsmpDpAaRtnCode._1365.throwing(aa0216_tsmpGroupVo.getGroupId());
						}
					}
				});
				
				//3.1將AA0216Req.groupIDList新增到tsmp_client_group資料表，
				//CLIENT_ID = AA0216Req.clientID、GROUP_ID = AA0216Req.groupIDList每一筆。
				groupIdList.forEach(groupId -> {
					TsmpClientGroup aa0216_clientGroupVo = new TsmpClientGroup();
					aa0216_clientGroupVo.setClientId(clientId);
					aa0216_clientGroupVo.setGroupId(groupId);
					getTsmpClientGroupDao().save(aa0216_clientGroupVo);
				});
				
				//3.2查詢tsmp_client_group，條件CLIENT_ID = AA0216Req.clientID，取得tsmp_client_group.GROUP_ID。
				List<TsmpClientGroup> aa0216_clientGroupList = getTsmpClientGroupDao().findByClientId(clientId);
				
				//3.3更新oauth_client_details資料表，欄位oauth_client_details.scope = 步驟3.2的tsmp_client_group.GROUP_ID
				//，條件oauth_client_details.client_id = AA0216Req.clientID。備註：scope的資料為5504,5505,5529,5530,2091,T001
				OauthClientDetails aa0216_oauthClientDetailsVo = getOauthClientDetailsDao().findById(clientId).orElse(null);
				if(aa0216_oauthClientDetailsVo != null) {
					String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, aa0216_oauthClientDetailsVo); //舊資料統一轉成 String
					
					List<String> scopeList = new ArrayList<>();
					aa0216_clientGroupList.forEach(clientGroupVo ->{
						scopeList.add(clientGroupVo.getGroupId());
					});
					String scope = String.join(",", scopeList);
					aa0216_oauthClientDetailsVo.setScope(scope);
					aa0216_oauthClientDetailsVo =  getOauthClientDetailsDao().save(aa0216_oauthClientDetailsVo);
					
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							OauthClientDetails.class.getSimpleName(), TableAct.U.value(), oldRowStr, aa0216_oauthClientDetailsVo);
				}
			}

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException aa0216_e) {
			throw aa0216_e;
		} catch (Exception aa0216_e) {
			this.logger.error(StackTraceUtil.logStackTrace(aa0216_e));
			//1288:新增失敗
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		
		return resp;
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return tsmpClientGroupDao;
	}
	
	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
}
