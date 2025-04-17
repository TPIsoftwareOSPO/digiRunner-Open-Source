package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0230Req;
import tpi.dgrv4.dpaa.vo.AA0230Resp;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClientGroupId;
import tpi.dgrv4.entity.entity.TsmpClientVgroupId;
import tpi.dgrv4.entity.entity.TsmpVgroupGroup;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.entity.repository.TsmpClientVgroupDao;
import tpi.dgrv4.entity.repository.TsmpVgroupGroupDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0230Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpClientVgroupDao tsmpClientVgroupDao;
	private TsmpClientGroupDao tsmpClientGroupDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private TsmpVgroupGroupDao tsmpVgroupGroupDao;
	private DgrAuditLogService dgrAuditLogService;
	
	@Autowired
	public AA0230Service(TsmpClientVgroupDao tsmpClientVgroupDao, TsmpClientGroupDao tsmpClientGroupDao,
			OauthClientDetailsDao oauthClientDetailsDao, TsmpVgroupGroupDao tsmpVgroupGroupDao,
			DgrAuditLogService dgrAuditLogService) {
		super();
		this.tsmpClientVgroupDao = tsmpClientVgroupDao;
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.tsmpVgroupGroupDao = tsmpVgroupGroupDao;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	@Transactional
	public AA0230Resp deleteClientVGroupByClientId(TsmpAuthorization authorization, AA0230Req req, InnerInvokeParam iip) {
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
		
		AA0230Resp resp = new AA0230Resp();

		try {
			String clientId = req.getClientID();
			String vgroupId = req.getVgroupID();
			
			//1.刪除TSMP_CLIENT_VGROUP資料表，條件CLIENT_ID = AA0230Req.clientID AND VGROUP_ID = AA0230Req.vgroupID。
			TsmpClientVgroupId vId = new TsmpClientVgroupId();
			vId.setClientId(clientId);
			vId.setVgroupId(vgroupId);
			getTsmpClientVgroupDao().deleteById(vId);
			
			//2.查詢Tsmp_VGroup_Group資料表，條件VGROUP_ID = AA0230Req.vgroupID，找出Tsmp_VGroup_Group.GROUP_ID。
			List<TsmpVgroupGroup> tsmpVgroupGroupList = getTsmpVgroupGroupDao().findByVgroupId(vgroupId);
			
			//3.刪除tsmp_client_group資料表，條件CLIENT_ID = AA0230Req.clientID AND GROUP_ID = 步驟2找到的Tsmp_VGroup_Group.GROUP_ID。
			tsmpVgroupGroupList.forEach(vo ->{
				TsmpClientGroupId id = new TsmpClientGroupId();
				id.setClientId(clientId);
				id.setGroupId(vo.getGroupId());
				getTsmpClientGroupDao().deleteById(id);
			});
			
			
			// 4.查詢oauth_client_details資料表，條件client_id = AA0230Req.clientID，將得到oauth_client_details.scope。
			OauthClientDetails oauthClientDetailsVo = getOauthClientDetailsDao().findById(clientId).orElse(null);
			
			//5.從oauth_client_details.scope中移除步驟2找到的Tsmp_VGroup_Group.GROUP_ID，在將oauth_client_details.scope更新回oauth_client_details.scope。
			if(oauthClientDetailsVo != null) {
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, oauthClientDetailsVo); //舊資料統一轉成 String
				
				List<String> groupIdList = new ArrayList<>();
				tsmpVgroupGroupList.forEach(vo ->{
					groupIdList.add(vo.getGroupId());
				});
				String scope = getNewScope(oauthClientDetailsVo.getScope(), groupIdList);
				oauthClientDetailsVo.setScope(scope);
				oauthClientDetailsVo = getOauthClientDetailsDao().save(oauthClientDetailsVo);
				
				//寫入 Audit Log D
				lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
						OauthClientDetails.class.getSimpleName(), TableAct.U.value(), oldRowStr, oauthClientDetailsVo);
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1287:刪除失敗
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		return resp;
	}
	
	private String getNewScope(String scope, List<String> groupIdList) {
		if(StringUtils.isEmpty(scope) || groupIdList == null || groupIdList.size() == 0) {
			return scope;
		}
		String[] arrScope = scope.split(",");
		//scopeList裝載非移除(groupIdList)的資料
		List<String> scopeList = new ArrayList<>();
		for(int i =0 ; i < arrScope.length ; i++) {
			if(!groupIdList.contains(arrScope[i])) {
				scopeList.add(arrScope[i]);
			}
		}
		
		return String.join(",", scopeList);
	}
	


	protected TsmpClientVgroupDao getTsmpClientVgroupDao() {
		return tsmpClientVgroupDao;
	}

	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}

	protected TsmpVgroupGroupDao getTsmpVgroupGroupDao() {
		return tsmpVgroupGroupDao;
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return tsmpClientGroupDao;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

}
