package tpi.dgrv4.dpaa.service;


import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.List;

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
import tpi.dgrv4.dpaa.vo.AA0801Req;
import tpi.dgrv4.dpaa.vo.AA0801Resp;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.jpql.TsmpRegHost;
import tpi.dgrv4.entity.repository.TsmpRegHostDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0801Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpRegHostDao tsmpRegHostDao;
	private SeqStoreService seqStoreService;
	private BcryptParamHelper bcryptParamHelper;
	
	@Autowired
	public AA0801Service(TsmpRegHostDao tsmpRegHostDao, SeqStoreService seqStoreService,
			BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpRegHostDao = tsmpRegHostDao;
		this.seqStoreService = seqStoreService;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	@Transactional
	public AA0801Resp addRegHost(TsmpAuthorization authorization, AA0801Req req, ReqHeader reqHeader) {
		AA0801Resp resp = new AA0801Resp();
		String hostId = "";
		try {
			String regHost = nvl(req.getRegHost());
			List<TsmpRegHost> regHostList = getTsmpRegHostDao().findByReghost(regHost);
			if(regHostList != null && regHostList.size() > 0) {
				throw TsmpDpAaRtnCode._1419.throwing();		//1419:主機名稱已存在
			}
			
			hostId = updateTable(authorization, req, regHost, reqHeader.getLocale());	
			resp.setRegHostID(hostId);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();		//1288:新增失敗
		}
		return resp;
	}
	
	private String updateTable(TsmpAuthorization authorization, AA0801Req req, String regHost, String locale) throws Exception {
		String regHostId = "";
		String enabled = getDecodeEnableFlag(nvl(req.getEnabled()), locale);	//啟用心跳
	
		regHostId = getRegHostId();
		TsmpRegHost tsmpRegHost = new TsmpRegHost();
		tsmpRegHost.setCreateUser(nvl(authorization.getUserName()));
		tsmpRegHost.setCreateTime(DateTimeUtil.now());
		tsmpRegHost.setUpdateUser(nvl(authorization.getUserName()));
		tsmpRegHost.setUpdateTime(DateTimeUtil.now());
		tsmpRegHost.setEnabled(enabled);
		tsmpRegHost.setReghostId(regHostId);
		tsmpRegHost.setReghost(req.getRegHost());
		tsmpRegHost.setClientid(req.getClientID());
		tsmpRegHost.setMemo(nvl(req.getMemo()));

		tsmpRegHost.setReghostStatus("S");		//entity預設"S"
		getTsmpRegHostDao().saveAndFlush(tsmpRegHost);
		
		return regHostId;
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
	 * 取得註冊主機ID
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getRegHostId() throws Exception {
		String hostId = "";
		
		Long hostID = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_CLIENT_HOST_PK);
		hostId = hostID+"";
		if (hostID != null) {
			hostId = hostID.toString();
		}
		if(StringUtils.isEmpty(hostId)) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return hostId;
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
