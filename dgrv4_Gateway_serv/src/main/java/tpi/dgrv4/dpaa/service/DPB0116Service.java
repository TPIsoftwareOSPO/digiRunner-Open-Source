package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0116Data;
import tpi.dgrv4.dpaa.vo.DPB0116Req;
import tpi.dgrv4.dpaa.vo.DPB0116Resp;
import tpi.dgrv4.dpaa.vo.DPB0116Trunc;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailLog;
import tpi.dgrv4.entity.repository.TsmpDpMailLogDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0116Service {

	private TsmpDpMailLogDao tsmpDpMailLogDao;
	private BcryptParamHelper bcryptParamHelper;
	private ServiceConfig serviceConfig;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;

	private Integer pageSize;
	
	@Autowired
	public DPB0116Service(TsmpDpMailLogDao tsmpDpMailLogDao, BcryptParamHelper bcryptParamHelper,
			ServiceConfig serviceConfig, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpDpMailLogDao = tsmpDpMailLogDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.serviceConfig = serviceConfig;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public DPB0116Resp queryMailLogList(TsmpAuthorization auth, DPB0116Req req, ReqHeader reqHeader) {
		String local = ServiceUtil.getLocale(reqHeader.getLocale());
		// 檢查參數
		checkParams(req, local);

		// query
		Integer pageSize = getPageSize();
		List<TsmpDpMailLog> list = getTsmpDpMailLogDao().queryMailLogList(req.getStartDate(), req.getEndDate(),
				req.getResult(), req.getId(), req.getKeyword(), pageSize);

		// 查無資料
		if (list == null || list.size() == 0) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		// parse data
		DPB0116Resp resp = setResp(list,reqHeader.getLocale());

		return resp;
	}

	private void checkParams(DPB0116Req req, String locale) {
		// id 不檢查
		// Long id=req.getId();

		// 檢查日期格式
		req.setStartDate(req.getStartDate() + " 00:00:00.000");
		req.setEndDate(req.getEndDate() + " 23:59:59.999");
		String startDate = req.getStartDate().replace('/', '-');
		String endDate = req.getEndDate().replace('/', '-');
		Optional<Date> opt_startDate = DateTimeUtil.stringToDateTime(startDate, DateTimeFormatEnum.西元年月日時分秒毫秒);
		Optional<Date> opt_endDate = DateTimeUtil.stringToDateTime(endDate, DateTimeFormatEnum.西元年月日時分秒毫秒);
		if (!opt_startDate.isPresent() || !opt_endDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}

		// 檢查加密資料 result
		try {
			req.setResult(getbcryptParamHelper().decode(req.getResult(), "RESULT_FLAG", locale));
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
	}

	private DPB0116Resp setResp(List<TsmpDpMailLog> list,String locale) {
		DPB0116Resp resp = new DPB0116Resp();
		List<DPB0116Data> listData = new ArrayList<DPB0116Data>();
		DPB0116Data data;
		String subject, txt;
		for (TsmpDpMailLog item : list) {
			data = new DPB0116Data();
			data.setMaillogId(item.getMaillogId());
			data.setRecipients(item.getRecipients());

			txt = item.getTemplateTxt();
			subject= txt.substring(txt.indexOf("##") + 2, txt.lastIndexOf("##")).trim();
			data.setSubject(subject);

			data.setResult(this.getSbNameByItemsSbNo("RESULT_FLAG", item.getResult(), locale));
			data.setCreateDate(item.getCreateDateTime().toString());
			listData.add(data);
		}
		resp.setDataList(listData);
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
		return this.tsmpDpMailLogDao;
	}

	protected BcryptParamHelper getbcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0116");
		return this.pageSize;
	}
	
	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}
}
