package tpi.dgrv4.dpaa.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.constant.TsmpApiSrc;
import tpi.dgrv4.dpaa.vo.AA0322Req;
import tpi.dgrv4.dpaa.vo.AA0322Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.entity.entity.jpql.DgrComposerFlow;
import tpi.dgrv4.entity.repository.DgrComposerFlowDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.gateway.TCP.Packet.UpdateComposerTSPacket;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.CApiKeyService;
import tpi.dgrv4.gateway.util.InnerInvokeParam;

@Service
public class AA0322Service {

	private TPILogger logger = TPILogger.tl;
	
	private CApiKeyService capiKeyService;
	private DgrComposerFlowDao dgrComposerFlowDao;
	private DgrAuditLogService dgrAuditLogService;
	private TsmpApiRegDao tsmpApiRegDao;
	private TsmpApiDao tsmpApiDao;

	@Autowired
	public AA0322Service(CApiKeyService capiKeyService, DgrComposerFlowDao dgrComposerFlowDao,
			DgrAuditLogService dgrAuditLogService, TsmpApiRegDao tsmpApiRegDao, TsmpApiDao tsmpApiDao) {
		super();
		this.capiKeyService = capiKeyService;
		this.dgrComposerFlowDao = dgrComposerFlowDao;
		this.dgrAuditLogService = dgrAuditLogService;
		this.tsmpApiRegDao = tsmpApiRegDao;
		this.tsmpApiDao = tsmpApiDao;
	}

	public AA0322Resp saveComposerFlow(List<AA0322Req> req, ReqHeader reqHeader,
			HttpHeaders headers, InnerInvokeParam iip) {
				
		AA0322Resp resp = new AA0322Resp();

		// 驗證CApiKey
		getCapiKeyService().verifyCApiKey(headers, true, true);

		try {

			
			for (AA0322Req aa0322Req : req) {
				String moduleName = aa0322Req.getModuleName();
				String apiId = aa0322Req.getApiName();
				String flowData = aa0322Req.getData();
				
				updateTsmpApiReq(iip, aa0322Req);

				Optional<DgrComposerFlow> opt_dgrComposerFlow = getDgrComposerFlowDao()
						.findByModuleNameAndApiId(moduleName, apiId);
				
				if (opt_dgrComposerFlow.isPresent()) {
					//寫入 Audit Log M
					String lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_COMPOSER_API.value());
					
					DgrComposerFlow dgrComposerFlow = opt_dgrComposerFlow.get();
					
					String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, dgrComposerFlow); //舊資料統一轉成 String
					
					dgrComposerFlow.setFlowData(flowData.getBytes());
					Date nowTime = new Date(System.currentTimeMillis());
					dgrComposerFlow.setUpdateDateTime(nowTime);					
					getDgrComposerFlowDao().saveAndFlush(dgrComposerFlow);
					
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();	
					getDgrAuditLogService().createAuditLogD(iip, lineNumber,
							DgrComposerFlow.class.getSimpleName(), TableAct.U.value(), oldRowStr, dgrComposerFlow);
					
				} else {
					//寫入 Audit Log M
					String lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_COMPOSER_API.value());
					
					DgrComposerFlow dgrComposerFlow = new DgrComposerFlow();
					dgrComposerFlow.setModuleName(moduleName);
					dgrComposerFlow.setApiId(apiId);
					dgrComposerFlow.setFlowData(flowData.getBytes());
					Date nowTime = new Date(System.currentTimeMillis());
					dgrComposerFlow.setCreateDateTime(nowTime);
					getDgrComposerFlowDao().saveAndFlush(dgrComposerFlow);
					
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();	
					getDgrAuditLogService().createAuditLogD(iip, lineNumber,
							DgrComposerFlow.class.getSimpleName(), TableAct.C.value(), null, dgrComposerFlow);
				}
			}
			
			// 需要判斷TPILogger.lc是否為null，因為在單元測試中，TPILogger.lc是沒有連接會是null。
			if (TPILogger.lc != null) {
				TPILogger.lc.send(new UpdateComposerTSPacket());
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	private void updateTsmpApiReq(InnerInvokeParam iip, AA0322Req aa0322Req) {
		String lineNumber;
		TsmpApiReg tsmpApiReg = checkParams(aa0322Req);
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpApiReg); //舊資料統一轉成 String
		String userName = "SYSTEM";
		tsmpApiReg.setRegStatus("1");	// 註冊狀態: 確認
		tsmpApiReg.setUpdateTime(DateTimeUtil.now());
		tsmpApiReg.setUpdateUser(userName);
		tsmpApiReg = getTsmpApiRegDao().save(tsmpApiReg);
		
		//寫入 Audit Log D
		lineNumber = StackTraceUtil.getLineNumber();	
		getDgrAuditLogService().createAuditLogD(iip, lineNumber,
				TsmpApiReg.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpApiReg);
	}

	protected DgrComposerFlowDao getDgrComposerFlowDao() {
		return dgrComposerFlowDao;
	}

	protected CApiKeyService getCapiKeyService() {
		return capiKeyService;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

	public TsmpApiReg checkParams(AA0322Req req) {	

		
		String apiKey = req.getApiName();
		String moduleName = req.getModuleName();
		if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(moduleName) ) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		String apiSrc = TsmpApiSrc.COMPOSED.value();
		List<TsmpApi> tsmpApiList = getTsmpApiDao().findByApiKeyAndModuleNameAndApiSrc(apiKey, moduleName, apiSrc);
		if (CollectionUtils.isEmpty(tsmpApiList)) {
			throw TsmpDpAaRtnCode._1456.throwing();
		}
		
		TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiKey, moduleName);
		Optional<TsmpApiReg> opt_tsmpApiReg = getTsmpApiRegDao().findById(tsmpApiRegId);
		if (opt_tsmpApiReg.isPresent()==false) {
			throw TsmpDpAaRtnCode.NO_API_INFO.throwing();
		}
		
		return opt_tsmpApiReg.get();
	}
	
	protected TsmpApiRegDao getTsmpApiRegDao() {
		return this.tsmpApiRegDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}
}
