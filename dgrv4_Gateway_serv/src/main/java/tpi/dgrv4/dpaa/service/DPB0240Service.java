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
import tpi.dgrv4.dpaa.vo.DPB0240Req;
import tpi.dgrv4.dpaa.vo.DPB0240Resp;
import tpi.dgrv4.dpaa.vo.DPB0240RespItem;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoCus;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoCusDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0240Service {

	private DgrGtwIdpInfoCusDao dgrGtwIdpInfoCusDao;
	private TsmpSettingService tsmpSettingService;

	@Autowired
	public DPB0240Service(DgrGtwIdpInfoCusDao dgrGtwIdpInfoCusDao, TsmpSettingService tsmpSettingService) {
		super();
		this.dgrGtwIdpInfoCusDao = dgrGtwIdpInfoCusDao;
		this.tsmpSettingService = tsmpSettingService;
	}

	public DPB0240Resp queryGtwIdPInfoList(TsmpAuthorization authorization, DPB0240Req req) {

		try {
			String gtwIdpInfoCusId = req.getGtwIdpInfoCusId();
			String clientId = req.getClientId();
			String status = req.getStatus();
			String keyword = req.getKeyword();

			Long id = getGtwIdpInfoCusId(gtwIdpInfoCusId);
			String[] keywords = getKeyword(keyword);
			status = getStatus(status);

			if (clientId == null || clientId.isEmpty()) {
				throw TsmpDpAaRtnCode._1350.throwing("clientId");
			}

			List<DgrGtwIdpInfoCus> list = getDgrGtwIdpInfoCusDao()
					.findByGtwIdpInfoCusIdAndClientIdAndStatusOrderByUpdateDateTimeDescGtwIdpInfoCusIdDesc(id, clientId,
							status, keywords, getPageSize());

			return getResp(list);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	private DPB0240Resp getResp(List<DgrGtwIdpInfoCus> list) {
		if (list == null || list.isEmpty()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		List<DPB0240RespItem> items = list.stream().map(entity -> {

			DPB0240RespItem item = new DPB0240RespItem();

			item.setGtwIdpInfoCusId(
					RandomSeqLongUtil.toHexString(entity.getGtwIdpInfoCusId(), RandomLongTypeEnum.YYYYMMDD));
			item.setClientId(entity.getClientId());
			item.setStatus(entity.getStatus());
			item.setCusLoginUrl(entity.getCusLoginUrl());
			item.setCusUserDataUrl(entity.getCusUserDataUrl());
			item.setIconFile(entity.getIconFile());
			item.setPageTitle(entity.getPageTitle());

			return item;

		}).toList();

		return new DPB0240Resp(items);
	}

	private String[] getKeyword(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return new String[0];
		}

		return keyword.trim().split("\\s+");
	}

	private String getStatus(String status) {

		if (!StringUtils.hasText(status)) {
			return null;
		}

		status = status.trim().toUpperCase();

		if ("Y".equals(status) || "N".equals(status)) {
			return status;
		} else {
			return null;
		}
	}

	private Long getGtwIdpInfoCusId(String gtwIdpInfoCusId) {

		if (!StringUtils.hasText(gtwIdpInfoCusId)) {
			return null;
		}

		return isValidLong(gtwIdpInfoCusId) ? Long.parseLong(gtwIdpInfoCusId)
				: RandomSeqLongUtil.toLongValue(gtwIdpInfoCusId);
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

	protected DgrGtwIdpInfoCusDao getDgrGtwIdpInfoCusDao() {
		return dgrGtwIdpInfoCusDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
}
