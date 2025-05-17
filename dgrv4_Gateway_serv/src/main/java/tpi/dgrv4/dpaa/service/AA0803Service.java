package tpi.dgrv4.dpaa.service;


import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.AA0803Req;
import tpi.dgrv4.dpaa.vo.AA0803Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.jpql.TsmpRegHost;
import tpi.dgrv4.entity.repository.TsmpRegHostDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0803Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpRegHostDao tsmpRegHostDao;
	private SeqStoreService seqStoreService;
	private BcryptParamHelper bcryptParamHelper;

	@Autowired
	public AA0803Service(TsmpRegHostDao tsmpRegHostDao, SeqStoreService seqStoreService,
			BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpRegHostDao = tsmpRegHostDao;
		this.seqStoreService = seqStoreService;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	@Transactional
	public AA0803Resp updateRegHost(TsmpAuthorization authorization, AA0803Req req, ReqHeader reqHeader) {
		AA0803Resp resp = new AA0803Resp();
		try {
			String regHostId = nvl(req.getRegHostID());
			String regHost = nvl(req.getRegHost());
			TsmpRegHost regHos = getTsmpRegHostDao().findByReghostId(regHostId);
			if(regHos == null ) {
				throw TsmpDpAaRtnCode._1420.throwing();		//1420:主機不存在
			}
			
			updateTable(authorization, req, regHostId, regHost, reqHeader.getLocale());	
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();		//1286:更新失敗
		}
		return resp;
	}
	
	private void updateTable(TsmpAuthorization authorization, AA0803Req req
			, String regHostId, String regHost, String locale) throws Exception {
		String enabled = getDecodeEnableFlag(nvl(req.getEnabled()), locale);	//啟用心跳
	
		TsmpRegHost tsmpRegHost = getTsmpRegHostDao().findFirstByReghostIdAndReghost(regHostId, regHost);
		if(tsmpRegHost != null) {
			tsmpRegHost.setUpdateUser(nvl(authorization.getUserName()));
			tsmpRegHost.setUpdateTime(DateTimeUtil.now());
			
			String regHostStatus = nvl(req.getRegHostStatus());
			if(!StringUtils.isEmpty(regHostStatus)) {
				regHostStatus = getDecodeHostStatus(regHostStatus, locale);
			}
			tsmpRegHost.setReghostStatus(regHostStatus);
			
			//如果AA0801Req.regHostStatus == 'A'，則存入"現在時間"，反之則不用存入
			if("A".contentEquals(regHostStatus)) {
				tsmpRegHost.setHeartbeat(DateTimeUtil.now());
			}
			
			tsmpRegHost.setEnabled(enabled);
			tsmpRegHost.setClientid(req.getClientID());
			tsmpRegHost.setMemo(nvl(req.getMemo()));
			
			
			getTsmpRegHostDao().saveAndFlush(tsmpRegHost);
		}else {
			throw TsmpDpAaRtnCode._1286.throwing();		//1286:更新失敗
		}
		
	}
	
	/**
	 * 使用BcryptParam, 
	 * ITEM_NO='ENABLE_FLAG' , DB儲存值對應代碼如下:
	 * DB值 (PARAM2) = 中文說明; 
	 * 1=Y, 2=N
	 * @param encodeStatus
	 * @return
	 */
	protected String getDecodeEnableFlag(String encodeStatus, String locale) {
		String status = null;
		try {
			status = getBcryptParamHelper().decode(encodeStatus, "ENABLE_FLAG", BcryptFieldValueEnum.PARAM2, locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return status;
	}
	
	/**
	 * 使用BcryptParam, 
	 * ITEM_NO='HOST_STATUS' , DB儲存值對應代碼如下:
	 * DB值 (SUBITEM_NO) = 中文說明; 
	 * A=啟動, S=停止
	 * @param status
	 * @return
	 */
	private String getDecodeHostStatus(String status, String locale) {
		String decodeHostStatus = null;
		try {
			decodeHostStatus = getBcryptParamHelper().decode(status, "HOST_STATUS", locale);
		} catch (BcryptParamDecodeException e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return decodeHostStatus;
	}
	
	protected TsmpRegHostDao getTsmpRegHostDao() {
		return this.tsmpRegHostDao;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}
	
	protected SeqStoreService getSeqStoreService() {
		return this.seqStoreService;
	}
	
}
