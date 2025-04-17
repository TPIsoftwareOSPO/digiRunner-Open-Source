package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Arrays;
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
import tpi.dgrv4.dpaa.vo.AA0227Req;
import tpi.dgrv4.dpaa.vo.AA0227Resp;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClientGroupId;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0227Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpClientGroupDao tsmpClientGroupDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private DgrAuditLogService dgrAuditLogService;
	
	@Autowired
	public AA0227Service(TsmpClientGroupDao tsmpClientGroupDao, OauthClientDetailsDao oauthClientDetailsDao,
			DgrAuditLogService dgrAuditLogService) {
		super();
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	@Transactional
	public AA0227Resp deleteClientGroupByClientId(TsmpAuthorization authorization, AA0227Req req, InnerInvokeParam iip) {
				
		AA0227Resp resp = new AA0227Resp();

		try {
			String clientId = req.getClientID();
			String groupId = req.getGroupID();

			//1.刪除TSMP_CLIENT_GROUP資料表，條件CLIENT_ID = AA0213Req.clientID AND GROUP_ID = AA0213Req.groupID。
			TsmpClientGroupId id = new TsmpClientGroupId();
			id.setClientId(clientId);
			id.setGroupId(groupId);
			if(getTsmpClientGroupDao().existsById(id)) {
				getTsmpClientGroupDao().deleteById(id);
			}
			
			
			//2.查詢oauth_client_details資料表，條件client_id = AA0213Req.clientID，將得到oauth_client_details.scope。
			OauthClientDetails oauthVo = getOauthClientDetailsDao().findById(clientId).orElse(null);
			if(oauthVo != null) {
				String scope = oauthVo.getScope();
				if(!StringUtils.isEmpty(scope)) {
					//寫入 Audit Log M
					String lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
					
					String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, oauthVo); //舊資料統一轉成 String
					
					//3.從oauth_client_details.scope中移除AA0213Req.groupID，在將oauth_client_details.scope更新回oauth_client_details.scope。
					String[] arrScope = scope.split(",");
					List<String> scopeList = new ArrayList<>(Arrays.asList(arrScope));
					scopeList.remove(groupId);
					scope = String.join(",", scopeList);
					oauthVo.setScope(scope);
					oauthVo = getOauthClientDetailsDao().save(oauthVo);
					
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							OauthClientDetails.class.getSimpleName(), TableAct.U.value(), oldRowStr, oauthVo);
					
				}
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

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return tsmpClientGroupDao;
	}

	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	

}
