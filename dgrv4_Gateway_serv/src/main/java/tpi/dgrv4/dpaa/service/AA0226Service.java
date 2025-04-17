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
import tpi.dgrv4.dpaa.vo.AA0226Req;
import tpi.dgrv4.dpaa.vo.AA0226Resp;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpClientGroup;
import tpi.dgrv4.entity.entity.TsmpClientVgroup;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpVgroupGroup;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.entity.repository.TsmpClientVgroupDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpVgroupGroupDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0226Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpClientGroupDao tsmpClientGroupDao;
	private TsmpGroupDao tsmpGroupDao;
	private TsmpClientVgroupDao tsmpClientVgroupDao;
	private TsmpVgroupGroupDao tsmpVgroupGroupDao;
	private TsmpClientDao tsmpClientDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private DgrAuditLogService dgrAuditLogService;
	
	@Autowired
	public AA0226Service(TsmpClientGroupDao tsmpClientGroupDao, TsmpGroupDao tsmpGroupDao,
			TsmpClientVgroupDao tsmpClientVgroupDao, TsmpVgroupGroupDao tsmpVgroupGroupDao, TsmpClientDao tsmpClientDao,
			OauthClientDetailsDao oauthClientDetailsDao, DgrAuditLogService dgrAuditLogService) {
		super();
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpClientVgroupDao = tsmpClientVgroupDao;
		this.tsmpVgroupGroupDao = tsmpVgroupGroupDao;
		this.tsmpClientDao = tsmpClientDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	@Transactional
	public AA0226Resp addClientVGroupByClientId(TsmpAuthorization authorization, AA0226Req req, InnerInvokeParam iip) {
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
				
		AA0226Resp resp = new AA0226Resp();

		try {
			String clientId = req.getClientID();
			List<String> vgroupIdList = req.getVgroupIDList();
			
			if(vgroupIdList != null && vgroupIdList.size() > 0) {
				Set<String> groupIdSet = new HashSet<String>();

				//1.1若AA0226Req.vgroupIDList不為空，則計算AA0226Req.vgroupIDList不重複的數量 ，並執行步驟1.2、1.3。
				vgroupIdList.forEach(x->{
					groupIdSet.add(x);
				} );
				
				//1.2查詢TSMP_CLIENT_GROUP資料表，條件CLIENT_ID = AA0226Req.clientID，將查詢出來的資料再去查詢TSMP_GROUP資料表
				//，條件GROUP_ID=TSMP_CLIENT_GROUP.GROUP_ID，計算(GROUP_ID)不重覆數量。
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
				
				//2.1 新增Tsmp_Client_VGroup資料表，欄位Tsmp_Client_VGroup.client_id=AA0226Req.clientID
				//、Tsmp_Client_VGroup.Vgroup_id=AA0226Req.vgroupIDList每一筆資料。
				vgroupIdList.forEach(x->{
					TsmpClientVgroup clientVgroupVo = new TsmpClientVgroup();
					clientVgroupVo.setClientId(clientId);
					clientVgroupVo.setVgroupId(x);
					getTsmpClientVgroupDao().save(clientVgroupVo);
				} );
				
				//2.2利用AA0226Req.vgroupIDList去查詢Tsmp_VGroup_Group資料表，條件VGROUP_ID關聯AA0226Req.vgroupIDList每一筆資料
				//，找出AA0226Req.vgroupIDList所有對應的GROUP_ID。
				List<String> groupIdList = new ArrayList<>();
				vgroupIdList.forEach(x->{
					List<TsmpVgroupGroup> tsmpVgroupGroupList = getTsmpVgroupGroupDao().findByVgroupId(x);
					tsmpVgroupGroupList.forEach(vggVo ->{
						groupIdList.add(vggVo.getGroupId());
					});
				} );
				
				//2.3查詢TSMP_CLIENT資料表，條件CLIENT_ID = AA0226Req.clientID ，若查不到資料則throw RTN Code 1344。
				//   若有找到資料將TSMP_CLIENT.SECURITY_LEVEL_ID取出。
				TsmpClient tsmpClientVo = getTsmpClientDao().findById(clientId).orElse(null);
				final String securityLevelId;
				if(tsmpClientVo != null) {
					securityLevelId = tsmpClientVo.getSecurityLevelId();
				}else {
					//1344:用戶端不存在
					throw TsmpDpAaRtnCode._1344.throwing();
				}
				
				//2.4查詢TSMP_GROUP資料表，條件GROUP_ID = 步驟2.2的GROUP_ID每一筆資料，若沒有查詢到資料，則throw RTN Code 1410。
				//   若TSMP_GROUP.SECURITY_LEVEL_ID !=TSMP_CLIENT.SECURITY_LEVEL_ID，則throw RTN Code 1365。
				groupIdList.forEach(groupId -> {
					TsmpGroup tsmpGroupVo = getTsmpGroupDao().findById(groupId).orElse(null);
					if(tsmpGroupVo == null) {
						//1410:群組[{{0}}]不存在
						throw TsmpDpAaRtnCode._1410.throwing(groupId);
					}else {
						if(securityLevelId != null && !securityLevelId.equals(tsmpGroupVo.getSecurityLevelId())) {
							//1365:用戶端的SECURITY LEVEL ID與群組的SECURITY LEVEL ID不符合，群組ID為[{{0}}]
							throw TsmpDpAaRtnCode._1365.throwing(tsmpGroupVo.getGroupId());
						}
					}
				});
				
				//3.1新增到tsmp_client_group資料表，欄位CLIENT_ID = AA0226Req.clientID、GROUP_ID = 步驟2.2找到的GROUP_ID每一筆資料。
				groupIdList.forEach(groupId -> {
					TsmpClientGroup clientGroupVo = new TsmpClientGroup();
					clientGroupVo.setClientId(clientId);
					clientGroupVo.setGroupId(groupId);
					getTsmpClientGroupDao().save(clientGroupVo);
				});
				
				//3.2查詢tsmp_client_group，條件CLIENT_ID = AA0226Req.clientID，取得tsmp_client_group.GROUP_ID。
				List<TsmpClientGroup> clientGroupList = getTsmpClientGroupDao().findByClientId(clientId);
				
				//3.3查詢oauth_client_details資料表，條件client_id = AA0226Req.clientID，取得oauth_client_details.scope。
				OauthClientDetails aa0226_oauthClientDetailsVo = getOauthClientDetailsDao().findById(clientId).orElse(null);
				
				//3.4更新oauth_client_details資料表，欄位oauth_client_details.scope = 步驟3.2的tsmp_client_group.GROUP_ID
				//，條件oauth_client_details.client_id = AA0226Req.clientID。備註：scope的資料為5504,5505,5529,5530,2091,T001
				if(aa0226_oauthClientDetailsVo != null) {
					String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, aa0226_oauthClientDetailsVo); //舊資料統一轉成 String
					
					List<String> scopeList = new ArrayList<>();
					clientGroupList.forEach(clientGroupVo ->{
						scopeList.add(clientGroupVo.getGroupId());
					});
					String scope = String.join(",", scopeList);
					aa0226_oauthClientDetailsVo.setScope(scope);
					aa0226_oauthClientDetailsVo = getOauthClientDetailsDao().save(aa0226_oauthClientDetailsVo);
					
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							OauthClientDetails.class.getSimpleName(), TableAct.U.value(), oldRowStr, aa0226_oauthClientDetailsVo);
				}
			}

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException aa0226_e) {
			throw aa0226_e;
		} catch (Exception aa0226_e) {
			this.logger.error(StackTraceUtil.logStackTrace(aa0226_e));
			//1288:新增失敗
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		
		return resp;
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return tsmpClientGroupDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	protected TsmpClientVgroupDao getTsmpClientVgroupDao() {
		return tsmpClientVgroupDao;
	}

	protected TsmpVgroupGroupDao getTsmpVgroupGroupDao() {
		return tsmpVgroupGroupDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}


}
