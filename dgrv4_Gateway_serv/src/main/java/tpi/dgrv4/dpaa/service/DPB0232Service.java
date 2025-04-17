package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0232Req;
import tpi.dgrv4.dpaa.vo.DPB0232Resp;
import tpi.dgrv4.dpaa.vo.DPB0232WhitelistItem;
import tpi.dgrv4.entity.entity.DgrBotDetection;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.DgrBotDetectionDao;
import tpi.dgrv4.entity.repository.DgrBotDetectionDao.BotDetectionType;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0232Service {

	private DgrBotDetectionDao dgrBotDetectionDao;
	private TsmpSettingService tsmpSettingService;
	private TsmpSettingDao tsmpSettingDao;

	@Autowired
	public DPB0232Service(DgrBotDetectionDao dgrBotDetectionDao, TsmpSettingService tsmpSettingService,
			TsmpSettingDao tsmpSettingDao) {
		super();
		this.dgrBotDetectionDao = dgrBotDetectionDao;
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpSettingDao = tsmpSettingDao;
	}

	public DPB0232Resp queryBotDetectionList(TsmpAuthorization authorization, DPB0232Req req) {

		DPB0232Resp resp = new DPB0232Resp();

		try {

			checkBotDetection(resp);

			// 從資料庫中查詢所有的白名單資料
			List<DgrBotDetection> list = getDgrBotDetectionDao()
					.findByTypeOrderByCreateDateTimeAscBotDetectionIdAsc(BotDetectionType.WHITELIST.getType());

			// 將查詢結果轉換為回應格式
			return getResp(resp, list);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	private void checkBotDetection(DPB0232Resp resp) {

		String checkBotDetection = null;

		Optional<TsmpSetting> opt = getTsmpSettingDao().findById(getTsmpSettingService().getKey_CHECK_BOT_DETECTION());

		if (opt.isPresent()) {
			TsmpSetting setting = opt.get();
			checkBotDetection = setting.getValue();
		}

		checkBotDetection = getCheckBotDetection(checkBotDetection);
		resp.setStatus(checkBotDetection);
	}

	private String getCheckBotDetection(String checkBotDetection) {

		if (!StringUtils.hasText(checkBotDetection)) {
			TPILogger.tl.debug("CHECK_BOT_DETECTION value is empty or null");
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		String trimmedLowerCase = checkBotDetection.trim().toLowerCase();

		switch (trimmedLowerCase) {
		case "true":
		case "yes":
		case "y":
		case "1":
		case "on":
			return "Y";
		default:
			return "N";
		}
	}

	private DPB0232Resp getResp(DPB0232Resp resp, List<DgrBotDetection> list) {
		// 如果查詢結果為空，拋出查無資料的錯誤
		if (list == null || list.isEmpty()) {
			TPILogger.tl.debug("DgrBotDetection list is empty or null");
			return resp;
		}

		// 將資料庫查詢結果轉換為回應物件
		List<DPB0232WhitelistItem> items = list.stream().map(entity -> {
			DPB0232WhitelistItem item = new DPB0232WhitelistItem();
			item.setId(RandomSeqLongUtil.toHexString(entity.getBotDetectionId(), RandomLongTypeEnum.YYYYMMDD));
			item.setLongId(entity.getBotDetectionId().toString());
			item.setRule(entity.getBotDetectionRule());
			item.setType(entity.getType());
			item.setCreateDateTime(entity.getCreateDateTime());
			item.setCreateUser(entity.getCreateUser());
			item.setUpdateDateTime(entity.getUpdateDateTime());
			item.setUpdateUser(entity.getUpdateUser());
			return item;
		}).toList();

		resp.setDataList(items);
		return resp;
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}

	protected DgrBotDetectionDao getDgrBotDetectionDao() {
		return dgrBotDetectionDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
}
