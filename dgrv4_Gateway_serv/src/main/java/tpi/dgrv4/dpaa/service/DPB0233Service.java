package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.CheckmarxCommUtils;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0233Req;
import tpi.dgrv4.dpaa.vo.DPB0233Resp;
import tpi.dgrv4.dpaa.vo.DPB0233WhitelistItem;
import tpi.dgrv4.entity.entity.DgrBotDetection;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.DgrBotDetectionDao;
import tpi.dgrv4.entity.repository.DgrBotDetectionDao.BotDetectionType;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.TCP.Packet.BotDetectionUpdatePacket;
import tpi.dgrv4.gateway.component.BotDetectionRuleValidator;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0233Service {

	private DgrBotDetectionDao dgrBotDetectionDao;
	private TsmpSettingDao tsmpSettingDao;
	private TsmpSettingService tsmpSettingService;
	private BotDetectionRuleValidator validator;
	private DaoGenericCacheService daoGenericCacheService;

	@Autowired
	public DPB0233Service(DgrBotDetectionDao dgrBotDetectionDao, TsmpSettingDao tsmpSettingDao,
			TsmpSettingService tsmpSettingService, BotDetectionRuleValidator validator,
			DaoGenericCacheService daoGenericCacheService) {
		super();
		this.dgrBotDetectionDao = dgrBotDetectionDao;
		this.tsmpSettingDao = tsmpSettingDao;
		this.tsmpSettingService = tsmpSettingService;
		this.validator = validator;
		this.daoGenericCacheService = daoGenericCacheService;
	}

	@Transactional
	public DPB0233Resp createAndUpdateBotDetectionList(TsmpAuthorization authorization, DPB0233Req req) {
		try {

			String username = authorization.getUserName();
			// 檢查請求資料的合法性
			check0233(req);
			// 更新機器人偵測狀態並取得是否啟用
			updateCheckBotDetection(req);
			// 更新白名單資料並取得規則樣式列表
			updateWhitelistDataList(req, username);

			clearAndNotify();

			return new DPB0233Resp();

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();
		}
	}

	protected void clearAndNotify() {
		getDaoGenericCacheService().clearAndNotify();
		BotDetectionUpdatePacket packet = new BotDetectionUpdatePacket();
		TPILogger.lc.send(packet);
	}

	protected void reloadValidator(List<Pattern> patterns, boolean isValidationEnabled) {
		validator.reloadAsync(patterns, isValidationEnabled);
	}

	private List<Pattern> updateWhitelistDataList(DPB0233Req req, String username) {

		// 刪除舊的白名單資料
		deleOldWhitelistData(req);
		// 更新新的白名單資料
		return updateWhitelistData(req, username);
	}

	private List<Pattern> updateWhitelistData(DPB0233Req req, String username) {
		List<DPB0233WhitelistItem> whiteList = req.getWhitelistDataList();
		List<Pattern> patterns = new ArrayList<>();

		for (DPB0233WhitelistItem item : whiteList) {
			String id = item.getId();
			String rule = item.getRule();

			// 驗證規則的正則表達式是否有效
			Pattern opt = isValidRegex(rule).orElse(null);
			if (opt != null) {
				patterns.add(opt);
			}


			Long botDetectionId = getBotDetectionId(id);
			Optional<DgrBotDetection> dgrBotDetectionOpt = getDgrBotDetectionDao().findByBotDetectionId(botDetectionId);

			DgrBotDetection dgrBotDetection = null;
			if (dgrBotDetectionOpt.isPresent()) {
				// 更新現有記錄
				dgrBotDetection = dgrBotDetectionOpt.get();
				dgrBotDetection.setBotDetectionRule(rule);
				dgrBotDetection.setUpdateUser(username);
				dgrBotDetection.setUpdateDateTime(DateTimeUtil.now());
			} else {
				// 建立新記錄
				dgrBotDetection = new DgrBotDetection();
				dgrBotDetection.setCreateUser(username);
				dgrBotDetection.setBotDetectionRule(rule);
				dgrBotDetection.setType(BotDetectionType.WHITELIST.getType());
			}

			getDgrBotDetectionDao().save(dgrBotDetection);
		}

		return patterns;
	}

	private Optional<Pattern> isValidRegex(String regex) {
		if (regex == null || regex.trim().isEmpty()) {
			throw TsmpDpAaRtnCode._1350.throwing("{{rule}}");
		}

		try {
			//checkmarx, ReDoS From Regex Injection
			return CheckmarxCommUtils.sanitizeForCheckmarxRegex(regex);
		} catch (PatternSyntaxException e) {
			throw TsmpDpAaRtnCode._2007.throwing();
		}
	}

	private void deleOldWhitelistData(DPB0233Req req) {

		List<DPB0233WhitelistItem> whiteList = req.getWhitelistDataList();

		// 取得現有的白名單 ID 列表
		List<Long> oldList = getDgrBotDetectionDao().findBotDetectionIdByTypeOrderByCreateDateTimeAscBotDetectionIdAsc(
				BotDetectionType.WHITELIST.getType());

		// 取得新的白名單 ID 列表
		List<Long> newList = getNewList(whiteList);
		// 找出需要刪除的項目（在舊列表中但不在新列表中的項目）
		List<Long> result = removeCommonElements(oldList, newList);

		getDgrBotDetectionDao().deleteByBotDetectionIdIn(result);
	}

	private List<Long> getNewList(List<DPB0233WhitelistItem> whiteList) {
		return whiteList.stream().map(DPB0233WhitelistItem::getId).filter(Objects::nonNull).flatMap(id -> {
			try {
				Long botDetectionId = getBotDetectionId(id);
				return Stream.of(botDetectionId);
			} catch (NumberFormatException e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				throw TsmpDpAaRtnCode._2007.throwing();
			}
		}).filter(Objects::nonNull).toList();
	}

	private List<Long> removeCommonElements(List<Long> oldList, List<Long> newList) {
		Set<Long> newSet = new HashSet<>(newList);
		return oldList.stream().filter(item -> !newSet.contains(item)).toList();
	}

	private Long getBotDetectionId(String id) {

		if (!StringUtils.hasText(id)) {
			return null;
		}

		return isValidLong(id) ? Long.parseLong(id) : RandomSeqLongUtil.toLongValue(id);
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

	private boolean updateCheckBotDetection(DPB0233Req req) {
		String status = req.getStatus();
		boolean result = false;

		Optional<TsmpSetting> opt = getTsmpSettingDao().findById(getTsmpSettingService().getKey_CHECK_BOT_DETECTION());

		if (opt.isEmpty()) {
			TPILogger.tl.debug("CHECK_BOT_DETECTION value is empty or null");
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		TsmpSetting setting = opt.get();

		if ("Y".equalsIgnoreCase(status)) {
			setting.setValue("true");
			result = true;
		} else {
			setting.setValue("false");
			result = false;
		}

		getTsmpSettingDao().save(setting);
		return result;
	}

	private void check0233(DPB0233Req req) {

		String status = req.getStatus();
		if (!StringUtils.hasText(status)) {
			throw TsmpDpAaRtnCode._2025.throwing("status");
		}

		List<DPB0233WhitelistItem> whitelistDataList = req.getWhitelistDataList();
		if ("Y".equalsIgnoreCase(status) && (whitelistDataList == null || whitelistDataList.isEmpty())) {
			throw TsmpDpAaRtnCode._2025.throwing("Allow List Data");
		}

		if (whitelistDataList == null) {
			req.setWhitelistDataList(Collections.emptyList());
		}
	}

	protected DgrBotDetectionDao getDgrBotDetectionDao() {
		return dgrBotDetectionDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}

	protected DaoGenericCacheService getDaoGenericCacheService() {
		return daoGenericCacheService;
	}
}
