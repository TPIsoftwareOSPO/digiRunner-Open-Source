package tpi.dgrv4.dpaa.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0217Req;
import tpi.dgrv4.dpaa.vo.AA0217Resp;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpClientGroup;
import tpi.dgrv4.entity.entity.TsmpClientVgroup;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.entity.repository.TsmpClientVgroupDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.entity.repository.TsmpVgroupDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0217Service {

	private TsmpSecurityLevelDao securityLVDao;
	private TsmpClientDao tsmpClientDao;
	private TsmpGroupDao tsmpGroup;
	private TsmpVgroupDao tsmpVgroup;
	private TsmpClientGroupDao tsmpClientGroup;
	private TsmpClientVgroupDao tsmpClientVgroupDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private DgrAuditLogService dgrAuditLogService;

	private TPILogger logger = TPILogger.tl;
	
	@Autowired
	public AA0217Service(TsmpSecurityLevelDao securityLVDao, TsmpClientDao tsmpClientDao, TsmpGroupDao tsmpGroup,
			TsmpVgroupDao tsmpVgroup, TsmpClientGroupDao tsmpClientGroup, TsmpClientVgroupDao tsmpClientVgroupDao,
			OauthClientDetailsDao oauthClientDetailsDao, DgrAuditLogService dgrAuditLogService) {
		super();
		this.securityLVDao = securityLVDao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpGroup = tsmpGroup;
		this.tsmpVgroup = tsmpVgroup;
		this.tsmpClientGroup = tsmpClientGroup;
		this.tsmpClientVgroupDao = tsmpClientVgroupDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	@Transactional
	public AA0217Resp updateSecurityLVByClient(TsmpAuthorization auth, AA0217Req req, InnerInvokeParam iip) {
		AA0217Resp resp = new AA0217Resp();

		try {

			checkParams(req);

			updateAndDeleteTable(auth, req, iip);

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

	/**
	 * 檢查邏輯
	 * 
	 * @param req
	 * @throws Exception
	 */
	private void checkParams(AA0217Req req) throws Exception {
		// 查詢TSMP_SECURITY_LEVEL資料表，條件 SECURITY_LEVEL_ID = AA0217Req.newSecurityID，若查詢不到資料，則throw RTN Code 1364。
		String newSecurity = req.getNewSecurityID();
		
		// 1364:安全等級不存在
		Optional<TsmpSecurityLevel> optSLV = getSecurityLVDao().findById(newSecurity);
		if(!optSLV.isPresent()) {
			throw TsmpDpAaRtnCode._1364.throwing();
		}
	}
	
	private void updateAndDeleteTable(TsmpAuthorization auth, AA0217Req req, InnerInvokeParam iip) {
		String security = ServiceUtil.nvl(req.getSecurityID());
		String newSecurity = ServiceUtil.nvl(req.getNewSecurityID());
		
		if(!security.equalsIgnoreCase(newSecurity)) {
			//寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
			
			// 更新TSMP_CLIENT資料表
			updateTsmpClient(req, iip);
			
			// 更新tsmp_group資料表
			updateTsmpGroup(auth, req);
			
			// 刪除Tsmp_Client_Group資料表
			deleteTsmpClientGroup(req);
			
			// 刪除TSMP_CLIENT_VGROUP資料表
			deleteTsmpClientVGroup(req);
			
			// 更新oauth_client_details資料表
			updateOauthDlientDetails(auth, req, iip);
		}
	}
	
	/**
	 * 更新TSMP_CLIENT資料表，欄位SECURITY_LEVEL_ID = AA0217Req.newSecurityID ，條件CLIENT_ID = AA0217Req.clientID。
	 * 
	 * @param req
	 */
	private void updateTsmpClient(AA0217Req req, InnerInvokeParam iip) {
		String newSecurityId = req.getNewSecurityID();
		String cliectId = req.getClientID();
		
		Optional<TsmpClient> optTsmpClient = getTsmpClientDao().findById(cliectId);
		if(optTsmpClient.isPresent()) {
			TsmpClient tsmpClient = optTsmpClient.get();
			
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpClient); //舊資料統一轉成 String
			
			tsmpClient.setSecurityLevelId(newSecurityId);
			tsmpClient = getTsmpClientDao().saveAndFlush(tsmpClient);
			
			//寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					TsmpClient.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpClient);
		}
		
	}
	
	/**
	 * 查詢tsmp_group資料表，條件
	 * 1.GROUP_NAME = AA0217Req.clientName
	 * 2.GROUP_NAME = tsmp_group.GROUP_NAME
	 * 更新tsmp_group資料表，欄位SECURITY_LEVEL_ID = AA0217Req.newSecurityID、UPDATE_TIME = 現在時間、UPDATE_USER = 登入者
	 * 
	 * @param auth
	 * @param req
	 */
	private void updateTsmpGroup(TsmpAuthorization auth, AA0217Req req) {

		String newSecurityId = req.getNewSecurityID();
		String cliectName = req.getClientName();
		String userName = auth.getUserName();
		
		List<TsmpGroup> tsmpGroups = getTsmpGroupDao().findByGroupName(cliectName);
		Date date = DateTimeUtil.now();
		tsmpGroups.forEach((tsmpGroup) ->{
			
			tsmpGroup.setSecurityLevelId(newSecurityId);
			tsmpGroup.setUpdateTime(date);
			tsmpGroup.setUpdateUser(userName);
			
			getTsmpGroupDao().saveAndFlush(tsmpGroup);
			
		});
		
		
	}
	
	/**
	 * 刪除Tsmp_Client_Group資料表，
	 * 條件 
	 * 1.CLIENT_ID = AA0217Req.clientID 
	 * 2.GROUP_ID NOT IN (SELECT GROUP_ID FROM TSMP_GROUP WHERE SECURITY_LEVEL_ID = AA0217Req.securityID )。
	 * 
	 * @param req
	 */
	private void deleteTsmpClientGroup(AA0217Req req) {
		String clientId = req.getClientID();
		String newSecurityLV = req.getNewSecurityID();
		List<TsmpClientGroup> clientGroup = getTsmpClientGroupDao().queryGroupIdNotInTsmpGroupBySecurityLV(clientId, newSecurityLV);
		if(clientGroup != null && clientGroup.size() > 0) {
			clientGroup.forEach((group) ->{
				getTsmpClientGroupDao().delete(group);
			});
		}
	}

	/**
	 * 刪除TSMP_CLIENT_VGROUP資料表，條件
	 * 1.CLIENT_ID = AA0217Req.clientID 
	 * 2.VGROUP_ID NOT IN (SELECT VGROUP_ID FROM TSMP_VGROUP WHERE SECURITY_LEVEL_ID = AA0217Req.securityID )。
	 * 
	 * @param req
	 */
	private void deleteTsmpClientVGroup(AA0217Req req) {
		String clientId = req.getClientID();
		String newSecurityLV = req.getNewSecurityID();
		List<TsmpClientVgroup> clientVGroup = getTsmpClientVgroupDao().queryGroupIdNotInTsmpVGroupBySecurityLV(clientId, newSecurityLV);
		
		if(clientVGroup != null && clientVGroup.size() > 0) {
			clientVGroup.forEach((group) ->{
				getTsmpClientVgroupDao().delete(group);
			});
		}
	}
	
	/**
	 * 查詢tsmp_client_group資料表，條件CLIENT_ID = AA0217Req.clientID
	 * 更新oauth_client_details資料表，scope = tsmp_client_group.GROUP_ID，
	 * 條件client_id = AA0204Req.clientID。
	 * 備註：scope的資料為5504,5505,5529,5530,2091,T001
	 * 
	 * @param auth
	 * @param req
	 */
	private void updateOauthDlientDetails(TsmpAuthorization auth, AA0217Req req, InnerInvokeParam iip) {
		String clientId = req.getClientID();
		List<TsmpClientGroup> clientGroup = getTsmpClientGroupDao().findByClientId(clientId);
		StringBuffer sb = new StringBuffer();
		String scope = "";
		clientGroup.forEach((group) ->{
			sb.append(group.getGroupId()+",");
		});
		if(!"".equals(sb.toString())) {
			scope = sb.toString().substring(0, sb.toString().length()-1);
		}
		
		if(!"".equals(scope)) {
			Optional<OauthClientDetails> optOcd = getOauthClientDetailsDao().findById(clientId);
			if(optOcd.isPresent()) {
				OauthClientDetails ocd = optOcd.get();
				
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, ocd); //舊資料統一轉成 String
				
				ocd.setScope(scope);
				
				getOauthClientDetailsDao().saveAndFlush(ocd);
				
				//寫入 Audit Log D
				String lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
						OauthClientDetails.class.getSimpleName(), TableAct.U.value(), oldRowStr, ocd);
				
			}else {
				logger.debug("AA0217Service ( updateOauthDlientDetails ) clientId = "+clientId +" not existed !");
				throw TsmpDpAaRtnCode._1297.throwing();
			}
		}
		
	}

	protected TsmpSecurityLevelDao getSecurityLVDao() {
		return securityLVDao;
	}
	
	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroup;
	}

	protected TsmpVgroupDao getTsmpVgroupDao() {
		return tsmpVgroup;
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return tsmpClientGroup;
	}

	protected TsmpClientVgroupDao getTsmpClientVgroupDao() {
		return tsmpClientVgroupDao;
	}

	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

}
