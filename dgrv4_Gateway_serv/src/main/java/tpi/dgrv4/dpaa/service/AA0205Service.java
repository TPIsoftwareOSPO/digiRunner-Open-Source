package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0205Req;
import tpi.dgrv4.dpaa.vo.AA0205Resp;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.jpql.TsmpClientHost;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.entity.repository.TsmpClientHostDao;
import tpi.dgrv4.entity.repository.TsmpClientVgroupDao;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0205Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpClientDao tsmpClientDao;
	private TsmpClientHostDao tsmpClientHostDao;
	private TsmpClientGroupDao tsmpClientGroupDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	private TsmpClientVgroupDao tsmpClientVgroupDao;
	private DgrAuditLogService dgrAuditLogService;
	private TsmpSettingService tsmpSettingService;

	@Autowired
	public AA0205Service(TsmpClientDao tsmpClientDao, TsmpClientHostDao tsmpClientHostDao,
			TsmpClientGroupDao tsmpClientGroupDao, OauthClientDetailsDao oauthClientDetailsDao,
			TsmpDpClientextDao tsmpDpClientextDao, TsmpClientVgroupDao tsmpClientVgroupDao,
			DgrAuditLogService dgrAuditLogService, TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpClientHostDao = tsmpClientHostDao;
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.tsmpClientVgroupDao = tsmpClientVgroupDao;
		this.dgrAuditLogService = dgrAuditLogService;
		this.tsmpSettingService = tsmpSettingService;
	}

	@Transactional
	public AA0205Resp deleteClientByClientId(TsmpAuthorization auth, AA0205Req req, InnerInvokeParam iip) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.DELETE_CLIENT.value());
		
		AA0205Resp resp = new AA0205Resp();
		try {
			
			boolean settingFlag = getTsmpSettingService().getVal_DEFAULT_DATA_CHANGE_ENABLED();
			if(("YWRtaW5Db25zb2xl".equals(req.getClientID()) || "DpClient".equals(req.getClientID())) && !settingFlag) {
				throw TsmpDpAaRtnCode._1548.throwing();
			}				
			
			Optional<TsmpClient> opt_tsmpClient = getTsmpClientDao().findById(req.getClientID());

			if (opt_tsmpClient.isPresent() == false) {
				// 用戶端不存在
				throw TsmpDpAaRtnCode._1344.throwing();
			}

			// 刪除Client
			TsmpClient clientVo = opt_tsmpClient.get();
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, clientVo); //舊資料統一轉成 String
			
			getTsmpClientDao().delete(clientVo);
			
			//寫入 Audit Log D
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					TsmpClient.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
			
			String clientId = opt_tsmpClient.get().getClientId();

			// 刪除Clinet Host
			List<TsmpClientHost> hostList = getTsmpClientHostDao().findByClientId(clientId);
			for(TsmpClientHost hostVo : hostList) {
				oldRowStr = getDgrAuditLogService().writeValueAsString(iip, hostVo); //舊資料統一轉成 String
				
				getTsmpClientHostDao().delete(hostVo);
				
				//寫入 Audit Log D
				lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
						TsmpClientHost.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
			}

			// 刪除Client與Group關係
			getTsmpClientGroupDao().deleteByClientId(clientId);

			// 刪除Client的oauth
			if (getOauthClientDetailsDao().existsById(clientId)) {
				OauthClientDetails oauthVo = getOauthClientDetailsDao().findById(clientId).orElse(null);
				if(oauthVo != null) {
					oldRowStr = getDgrAuditLogService().writeValueAsString(iip, oauthVo); //舊資料統一轉成 String
					
					getOauthClientDetailsDao().delete(oauthVo);
					
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							OauthClientDetails.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
				}
			}
			
			// 刪除Clinet
			if (getTsmpDpClientextDao().existsById(clientId)) {
				TsmpDpClientext clientextVo = getTsmpDpClientextDao().findById(clientId).orElse(null);
				if(clientextVo != null) {
					oldRowStr = getDgrAuditLogService().writeValueAsString(iip, clientextVo); //舊資料統一轉成 String
					
					getTsmpDpClientextDao().delete(clientextVo);
					
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							TsmpDpClientext.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
				}
			}

			// 刪除Client與虛擬Group關係
			getTsmpClientVgroupDao().deleteByClientId(opt_tsmpClient.get().getClientId());

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}

		return resp;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected TsmpClientHostDao getTsmpClientHostDao() {
		return tsmpClientHostDao;
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return tsmpClientGroupDao;
	}

	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}

	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return tsmpDpClientextDao;
	}

	protected TsmpClientVgroupDao getTsmpClientVgroupDao() {
		return tsmpClientVgroupDao;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
}
