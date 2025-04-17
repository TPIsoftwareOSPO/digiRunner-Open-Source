package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.vo.DPB0103Cron;
import tpi.dgrv4.dpaa.vo.DPB0103Items;
import tpi.dgrv4.dpaa.vo.DPB0103Req;
import tpi.dgrv4.dpaa.vo.DPB0103Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpApptRjob;
import tpi.dgrv4.entity.entity.TsmpDpApptRjobD;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDDao;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDao;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0103Service {

	private TsmpDpApptRjobDao tsmpDpApptRjobDao;
	private TsmpDpApptRjobDDao tsmpDpApptRjobDDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;

	private ObjectMapper om = new ObjectMapper();

	@Autowired
	public DPB0103Service(TsmpDpApptRjobDao tsmpDpApptRjobDao, TsmpDpApptRjobDDao tsmpDpApptRjobDDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpDpApptRjobDao = tsmpDpApptRjobDao;
		this.tsmpDpApptRjobDDao = tsmpDpApptRjobDDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public DPB0103Resp queryRjobByPk(TsmpAuthorization auth, DPB0103Req req, ReqHeader reqHeader) {
		String apptRjobId = req.getApptRjobId();
		if (StringUtils.isEmpty(apptRjobId)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		Optional<TsmpDpApptRjob> opt = getTsmpDpApptRjobDao().findById(apptRjobId);
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		TsmpDpApptRjob rjob = opt.get();
		return getResp(rjob, reqHeader.getLocale());
	}

	private DPB0103Resp getResp(TsmpDpApptRjob rjob, String locale) {
		DPB0103Cron cronJson = getCronJson(rjob.getCronJson());
		String effDateTime = getDateTime(rjob.getEffDateTime());
		String invDateTime = getDateTime(rjob.getInvDateTime());
		List<DPB0103Items> oriDataList = getDataList(rjob.getApptRjobId(), locale);
		
		DPB0103Resp resp = new DPB0103Resp();
		resp.setApptRjobId(rjob.getApptRjobId());
		resp.setLv(rjob.getVersion());
		resp.setRjobName(rjob.getRjobName());
		resp.setRemark(rjob.getRemark());
		resp.setCronJson(cronJson);
		resp.setCronExpression(rjob.getCronExpression());
		resp.setEffDateTime(effDateTime);
		resp.setInvDateTime(invDateTime);
		resp.setStatus(rjob.getStatus());
		resp.setOriDataList(oriDataList);
		return resp;
	}

	private DPB0103Cron getCronJson(String cronJson) {
		try {
			return om.readValue(cronJson, DPB0103Cron.class);
		} catch (Exception e) {
			throw TsmpDpAaRtnCode._1242.throwing();
		}
	}

	private String getDateTime(Long millis) {
		if (millis == null) {
			return new String();
		}
		return DateTimeUtil.dateTimeToString(new Date(millis), DateTimeFormatEnum.西元年月日時分秒_2).orElse(new String());
	}

	private List<DPB0103Items> getDataList(String apptRjobId, String locale) {
		List<DPB0103Items> dataList = new ArrayList<>();
		
		List<TsmpDpApptRjobD> dList = getTsmpDpApptRjobDDao().findByApptRjobIdOrderBySortByAscApptRjobDIdAsc(apptRjobId);
		if (dList == null || dList.isEmpty()) {
			return dataList;
		}
		
		DPB0103Items dpb0103Items = null;
		String refItemName = null;
		String refSubitemName = null;
		for (TsmpDpApptRjobD rjobD : dList) {
			refItemName = getRefItemName(rjobD.getRefItemNo(), locale);
			refSubitemName = getRefSubitemName(rjobD.getRefItemNo(), rjobD.getRefSubitemNo(), locale);
			
			dpb0103Items = new DPB0103Items();
			dpb0103Items.setApptRjobDId(rjobD.getApptRjobDId());
			dpb0103Items.setLv(rjobD.getVersion());
			dpb0103Items.setRefItemNo(rjobD.getRefItemNo());
			dpb0103Items.setRefItemName(refItemName);
			dpb0103Items.setRefSubitemNo(nvl(rjobD.getRefSubitemNo()));
			dpb0103Items.setRefSubitemName(refSubitemName);
			dpb0103Items.setInParams(nvl(rjobD.getInParams()));
			dpb0103Items.setIdentifData(nvl(rjobD.getIdentifData()));
			dpb0103Items.setSortBy(rjobD.getSortBy());
			dataList.add(dpb0103Items);
		}
		
		return dataList;
	}

	private String getRefItemName(String refItemNo, String locale) {
		TsmpDpItems dpb0103_items = getItemsById("SCHED_CATE1", refItemNo, false, locale);
		if (dpb0103_items != null) {
			return dpb0103_items.getSubitemName();
		}
		return new String();
	}

	private String getRefSubitemName(String refItemNo, String refSubitemNo, String locale) {
		if (StringUtils.isEmpty(refSubitemNo)) {
			return new String();
		}
		TsmpDpItems dpb0103_items = getItemsById(refItemNo, refSubitemNo, false, locale);
		if (dpb0103_items != null) {
			return dpb0103_items.getSubitemName();
		}
		return new String();
	}

	private TsmpDpItems getItemsById(String itemNo, String subitemNo, boolean errorWhenNotExists, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems dpb0103_i = getTsmpDpItemsCacheProxy().findById(id);
		if (errorWhenNotExists && dpb0103_i == null) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return dpb0103_i;
	}

	protected TsmpDpApptRjobDao getTsmpDpApptRjobDao() {
		return this.tsmpDpApptRjobDao;
	}

	protected TsmpDpApptRjobDDao getTsmpDpApptRjobDDao() {
		return this.tsmpDpApptRjobDDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

}