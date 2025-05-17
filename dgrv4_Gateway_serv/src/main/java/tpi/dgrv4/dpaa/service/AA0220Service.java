package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import tpi.dgrv4.dpaa.vo.AA0220Req;
import tpi.dgrv4.dpaa.vo.AA0220Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0220Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpClientDao tsmpClientDao;
	private BcryptParamHelper bcryptParamHelper;
	private DgrAuditLogService dgrAuditLogService;
	
	@Autowired
	public AA0220Service(TsmpClientDao tsmpClientDao, BcryptParamHelper bcryptParamHelper,
			DgrAuditLogService dgrAuditLogService) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.dgrAuditLogService = dgrAuditLogService;
	}


	public AA0220Resp updateStatusSettingByClient(TsmpAuthorization authorization, AA0220Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
		
		AA0220Resp resp = new AA0220Resp();

		try {
			String clientId = req.getClientID();
			String encodeStatus = req.getEncodeStatus();
			Integer resetFailLoginTreshhold = req.getResetFailLoginTreshhold();
			String resetPwdFailTimes = req.getResetPwdFailTimes();
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			//1.查詢TSMP_CLIENT資料表，條件CLIENT_ID = AA0220Req.clientID，若查詢不到資料則throw RTN Code 1344。
			TsmpClient tsmpClientVo = getTsmpClientDao().findById(clientId).orElse(null);
			if(tsmpClientVo == null) {
				//1344:用戶端不存在
				throw TsmpDpAaRtnCode._1344.throwing();
			}
			
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpClientVo); //舊資料統一轉成 String
			
			//2.更新TSMP_CLIENT資料表，欄位 FAIL_TRESHHOLD = AA0220Req.resetFailLoginTreshhold、 
			//CLIENT_STATUS = AA0220Req.encodeStatus(解密後)、UPDATE_TIME = 現在時間、UPDATE_USER = 登入者，條件CLIENT_ID = AA0220Req.clientID。
			String status = this.getValueByBcryptParamHelper(encodeStatus, "ENABLE_FLAG", BcryptFieldValueEnum.PARAM1, locale);
			tsmpClientVo.setFailTreshhold(resetFailLoginTreshhold);
			tsmpClientVo.setClientStatus(status);
			tsmpClientVo.setUpdateUser(authorization.getUserName());
			tsmpClientVo.setUpdateTime(DateTimeUtil.now());
			
			//3.若AA0220Req.resetPwdFailTimes為true，則更新TSMP_CLIENT資料表，欄位 PWD_FAIL_TIMES = 0、UPDATE_TIME = 現在時間、UPDATE_USER = 登入者，條件CLIENT_ID = AA0220Req.clientID。
			if("true".equalsIgnoreCase(resetPwdFailTimes)) {
				tsmpClientVo.setPwdFailTimes(0);
			}
			
			tsmpClientVo = getTsmpClientDao().save(tsmpClientVo);
			
			//寫入 Audit Log D
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					TsmpClient.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpClientVo);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1286:更新失敗
			throw TsmpDpAaRtnCode._1286.throwing();
		}
		return resp;
	}
	
	
	private String getValueByBcryptParamHelper(String encodeValue, String itemNo, BcryptFieldValueEnum fieldValue, String locale) {
		String value = null;
		try {
			value = getBcryptParamHelper().decode(encodeValue, itemNo, fieldValue, locale);// BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return value;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}


	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

}
