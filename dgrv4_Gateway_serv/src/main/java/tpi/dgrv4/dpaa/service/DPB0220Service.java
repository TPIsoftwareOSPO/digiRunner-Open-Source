package tpi.dgrv4.dpaa.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0220Req;
import tpi.dgrv4.dpaa.vo.DPB0220Resp;
import tpi.dgrv4.dpaa.vo.DPB0220RespItem;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoCus;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoCusDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0220Service {

	private DgrAcIdpInfoCusDao dgrAcIdpInfoCusDao;
	private TsmpSettingService tsmpSettingService;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0220Service(DgrAcIdpInfoCusDao dgrAcIdpInfoCusDao, TsmpSettingService tsmpSettingService) {
		super();
		this.dgrAcIdpInfoCusDao = dgrAcIdpInfoCusDao;
		this.tsmpSettingService = tsmpSettingService;
	}

	public DPB0220Resp queryIdPInfoList_cus(TsmpAuthorization authorization, DPB0220Req req) {

		try {

			String acIdpInfoCusId = req.getCusId();
			String cusStatus = req.getCusStatus();
			String keyword = req.getKeyword();

			Long id = getAcIdpInfoCusId(acIdpInfoCusId);
			cusStatus = getCusStatus(cusStatus);
			String[] keywords = getKeyword(keyword);

			List<DgrAcIdpInfoCus> list = getDgrAcIdpInfoCusDao()
					.findByAcIdpInfoCusIdAndAcIdpInfoCusNameAndCusStatusOrderByUpdateDateTimeDescAcIdpInfoCusIdDesc( //
							id, cusStatus, keywords, getPageSize());

			return getResp(list);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	private DPB0220Resp getResp(List<DgrAcIdpInfoCus> list) {
		if (list == null || list.isEmpty()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		List<DPB0220RespItem> items = list.stream().map(entity -> {

			DPB0220RespItem item = new DPB0220RespItem();

			item.setCusId(RandomSeqLongUtil.toHexString(entity.getAcIdpInfoCusId(), RandomLongTypeEnum.YYYYMMDD));
			item.setCusName(entity.getAcIdpInfoCusName());
			item.setCusStatus(entity.getCusStatus());
			item.setCusLoginUrl(entity.getCusLoginUrl());
			item.setCusBackendLoginUrl(entity.getCusBackendLoginUrl());
			item.setCusUserDataUrl(entity.getCusUserDataUrl());

			return item;

		}).toList();

		return new DPB0220Resp(items);
	}

	private String[] getKeyword(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return new String[0];
		}

		return keyword.trim().split("\\s+");
	}

	private String getCusStatus(String cusStatus) {

		if (!StringUtils.hasText(cusStatus)) {
			return null;
		}

		cusStatus = cusStatus.trim().toUpperCase();

		if ("Y".equals(cusStatus) || "N".equals(cusStatus)) {
			return cusStatus;
		} else {
			return null;
		}
	}

	private Long getAcIdpInfoCusId(String acIdpInfoCusId) {

		if (!StringUtils.hasText(acIdpInfoCusId)) {
			return null;
		}

		return isValidLong(acIdpInfoCusId) ? Long.parseLong(acIdpInfoCusId)
				: RandomSeqLongUtil.toLongValue(acIdpInfoCusId);
	}

	private boolean isValidLong(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}

		try {
			Long.parseLong(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	protected Integer getPageSize() {
		return getTsmpSettingService().getVal_DEFAULT_PAGE_SIZE();
	}

	protected DgrAcIdpInfoCusDao getDgrAcIdpInfoCusDao() {
		return dgrAcIdpInfoCusDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

}
