package tpi.dgrv4.dpaa.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0062Req;
import tpi.dgrv4.dpaa.vo.DPB0062Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.component.job.appt.TsmpDpApptJobSetter;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0062Service implements TsmpDpApptJobSetter {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private ApptJobDispatcher apptJobDispatcher;
	private TsmpDpApptJobDao tsmpDpApptJobDao;

	@Autowired
	public DPB0062Service(TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, ApptJobDispatcher apptJobDispatcher,
			TsmpDpApptJobDao tsmpDpApptJobDao) {
		super();
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.apptJobDispatcher = apptJobDispatcher;
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
	}

	@Override
	public TsmpDpApptJob set(TsmpDpApptJob job) {
		return getTsmpDpApptJobDao().saveAndFlush(job);
	}

	public DPB0062Resp createOneJob(TsmpAuthorization auth, DPB0062Req req, ReqHeader reqHeader) {
		checkParams(req, reqHeader.getLocale());
		
		TsmpDpApptJob job = generateJobEntity(auth, req);

		TsmpDpApptJobSetter setter = getTsmpDpApptJobSetter(job);
		if (setter == null) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		this.logger.debug("Using TsmpDpApptJobSetter: " + setter.getClass());

		job = setter.set(job);
		if (job == null || job.getApptJobId() == null) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		// refresh memList
		getApptJobDispatcher().resetRefreshSchedule();

		DPB0062Resp resp = new DPB0062Resp();
		resp.setApptJobId(job.getApptJobId());
		return resp;
	}

	protected void checkParams(DPB0062Req req, String locale) {
		final String refItemNo = req.getRefItemNo();
		final String startDateTimeStr = req.getStartDateTime();
		if (StringUtils.isEmpty(refItemNo) ||
			StringUtils.isEmpty(startDateTimeStr)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		final String inParamsEncode = req.getInParams();
		final String identifDataEncode = req.getIdentifData();
		decodeAndSet(inParamsEncode, (inParams) -> {req.setInParams(inParams);});
		decodeAndSet(identifDataEncode, (identifData) -> {req.setIdentifData(identifData);});
		
		Optional<Date> opt_sdt = DateTimeUtil.stringToDateTime(startDateTimeStr, DateTimeFormatEnum.西元年月日時分_2);
		if (!opt_sdt.isPresent()) {
			this.logger.error(String.format("DateTimeFormat should be %s but %s", DateTimeFormatEnum.西元年月日時分_2.value(), startDateTimeStr));
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		
		final String refSubitemNo = req.getRefSubitemNo();
		/*
		if (!StringUtils.isEmpty(refSubitemNo)) {
			TsmpDpItemsId id = new TsmpDpItemsId(refItemNo, refSubitemNo);
			Optional<TsmpDpItems> opt = getTsmpDpItemsDao().findById(id);
			if (!opt.isPresent()) {
				this.logger.error("TsmpDpItems({}, {}) does not exist!", refItemNo, refSubitemNo);
				throw TsmpDpAaRtnCode._1299.throwing();
			}
		} else {
			List<TsmpDpItems> dpItems = getTsmpDpItemsDao().findByItemNo(refItemNo);
			if (dpItems == null || dpItems.isEmpty()) {
				this.logger.error("TsmpDpItems with itemNo '{}' does not exist!", refItemNo);
				throw TsmpDpAaRtnCode._1299.throwing();
			}
		}
		*/
		checkDpItems(refItemNo, refSubitemNo, locale);
	}

	private void decodeAndSet(String encodeStr, Consumer<String> consumer) {
		String decodeStr = new String();
		if (!StringUtils.isEmpty(encodeStr)) {
			byte[] data = null;
			try {
				data = ServiceUtil.base64Decode(encodeStr);
			} catch (Exception e) {
				this.logger.error("base64Decode error!");
				throw TsmpDpAaRtnCode._1299.throwing();
			}
			if (data == null || data.length == 0) {
				this.logger.error("decode data is empty!");
				throw TsmpDpAaRtnCode._1296.throwing();
			} else {
				// 解密後放回去, 方便後續使用
				decodeStr = new String(data, StandardCharsets.UTF_8);
			}
		}
		consumer.accept(decodeStr);
	}

	protected void checkDpItems(String refItemNo, String refSubitemNo, String locale) {
		// 檢查大分類是否存在
		TsmpDpItemsId id = new TsmpDpItemsId("SCHED_CATE1", refItemNo, locale);
		TsmpDpItems item = getTsmpDpItemsCacheProxy().findById(id);
		if (item == null) {
			this.logger.error(String.format("TsmpDpItems(%s, %s) does not exist!", "SCHED_CATE1", refItemNo));
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		
		boolean hasSubtype = !"-1".equals(item.getParam1());
		if (StringUtils.isEmpty(refSubitemNo)) {
			if (hasSubtype) {
				this.logger.error("RefSubitemNo cannot be empty! itemNo=" + refItemNo);
				throw TsmpDpAaRtnCode._1296.throwing();
			}
		} else {
			if (!hasSubtype) {
				this.logger.error("Specifying subitemNo is forbidden! itemNo=" + refItemNo);
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			// 檢查子項目是否存在
			id = new TsmpDpItemsId(refItemNo, refSubitemNo, locale);
			item = getTsmpDpItemsCacheProxy().findById(id);
			if (item == null) {
				this.logger.error(String.format("TsmpDpItems(%s, %s) does not exist!", refItemNo, refSubitemNo));
				throw TsmpDpAaRtnCode._1299.throwing();
			}
		}
	}

	private TsmpDpApptJob generateJobEntity(TsmpAuthorization auth, DPB0062Req req) {
		final String refItemNo = req.getRefItemNo();
		final String refSubitemNo = emptyStrToNull(req.getRefSubitemNo());
		final String inParams = req.getInParams();
		final String identifData = req.getIdentifData();
		final String sdtStr = req.getStartDateTime();
		final Date startDateTime = DateTimeUtil.stringToDateTime(sdtStr, DateTimeFormatEnum.西元年月日時分_2).orElse(null);
		
		TsmpDpApptJob j = new TsmpDpApptJob();
		j.setRefItemNo(refItemNo);
		j.setRefSubitemNo(refSubitemNo);
		j.setInParams(inParams);
		j.setIdentifData(identifData);
		j.setStartDateTime(startDateTime);
		j.setCreateDateTime(DateTimeUtil.now());
		j.setCreateUser(auth.getUserName());
		return j;
	}

	protected TsmpDpApptJobSetter getTsmpDpApptJobSetter(TsmpDpApptJob job) {
		String beanName = getApptJobDispatcher().getBeanName(job);
		try {
			return getApptJobDispatcher().getBeanByName(beanName, job);
		} catch (Exception e) {
			return this;
		}
	}

	private String emptyStrToNull(String input) {
		if (StringUtils.isEmpty(input)) {
			return null;
		}
		return input;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected ApptJobDispatcher getApptJobDispatcher() {
		return this.apptJobDispatcher;
	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}

}
