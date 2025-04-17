package tpi.dgrv4.dpaa.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.DPB0117Req;
import tpi.dgrv4.dpaa.vo.DPB0117Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailLog;
import tpi.dgrv4.entity.repository.TsmpDpMailLogDao;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0117Service {

	private TsmpDpMailLogDao tsmpDpMailLogDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;

	@Autowired
	public DPB0117Service(TsmpDpMailLogDao tsmpDpMailLogDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpDpMailLogDao = tsmpDpMailLogDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public DPB0117Resp queryMailLogDetail(TsmpAuthorization auth, DPB0117Req req, ReqHeader reqHeader) {
		// 檢查參數
		checkParams(req);

		List<TsmpDpMailLog> logList = getTsmpDpMailLogDao().findByMaillogIdAndRecipients(req.getId(),
				req.getRecipients());

		// 查無資料
		if (logList == null || logList.size() == 0) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		// parse data
		TsmpDpMailLog log = logList.get(0);
		DPB0117Resp resp = setResp(log, reqHeader.getLocale());

		return resp;
	}

	private void checkParams(DPB0117Req req) {
		Long id = req.getId();
		String recipients = req.getRecipients();
		if (id == null || recipients == null) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	private DPB0117Resp setResp(TsmpDpMailLog log, String locale) {
		DPB0117Resp resp = new DPB0117Resp();
		resp.setMailLogId(log.getMaillogId());
		resp.setRecipients(log.getRecipients());
		String txt = log.getTemplateTxt();
		String subject = txt.substring(txt.indexOf("##") + 2, txt.lastIndexOf("##"));
		String content = txt.substring(txt.indexOf("##,") + 3);
		resp.setSubject(subject.trim());
		resp.setContent(content);
		String logResult = log.getResult();
		String result = this.getSbNameByItemsSbNo("RESULT_FLAG", logResult, locale);
		resp.setResult(result);
		if ("0".equals(logResult)) {
			resp.setErrorMsg(log.getStackTrace());
		}
		
		resp.setRefCode(log.getRefCode());
		resp.setCreateDate(log.getCreateDateTime().toString());
		resp.setCreateUser(log.getCreateUser());
		return resp;
	}
	
	private String getSbNameByItemsSbNo(String itemNo, String subitemNo, String locale) {
		String subitemName = "";
		TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale(itemNo, subitemNo, locale);
		if (dpItem == null) {
		    return null;
		}
		subitemName = dpItem.getSubitemName();
		  
		return subitemName;
	}

	protected TsmpDpMailLogDao getTsmpDpMailLogDao() {
		return tsmpDpMailLogDao;
	}
	
	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}
}
