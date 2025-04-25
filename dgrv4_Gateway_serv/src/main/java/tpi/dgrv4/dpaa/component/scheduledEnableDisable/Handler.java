package tpi.dgrv4.dpaa.component.scheduledEnableDisable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.constant.TsmpDpItem;
import tpi.dgrv4.dpaa.util.TimestampConverterUtil;
import tpi.dgrv4.dpaa.vo.AA0303Item;
import tpi.dgrv4.entity.entity.ITsmpRtnCode;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

public abstract class Handler {

	private EnableAlgorithm enableAlgorithm;
	private DisableAlgorithm disableAlgorithm;
	private TsmpDpItemsDao tsmpDpItemsDao;
	private TsmpRtnCodeDao tsmpRtnCodeDao;

	@Autowired
	public void setHandler(EnableAlgorithm enableAlgorithm, DisableAlgorithm disableAlgorithm,
			TsmpDpItemsDao tsmpDpItemsDao, TsmpRtnCodeDao tsmpRtnCodeDao) {
		this.enableAlgorithm = enableAlgorithm;
		this.disableAlgorithm = disableAlgorithm;
		this.tsmpDpItemsDao = tsmpDpItemsDao;
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
	}

	protected AA0303Item getAA0303Item(TsmpApi api, String locale) {

		AA0303Item resp = new AA0303Item();

		String apiKey = api.getApiKey();
		String moduleName = api.getModuleName();
		String apiStatus = api.getApiStatus();
		Long enableScheduledDate = api.getEnableScheduledDate();
		Long disableScheduledDate = api.getDisableScheduledDate();

		TsmpDpItem tsmpDpItem = TsmpDpItem.getByItemNoAndParam1( //
				TsmpDpItem.ENABLE_FLAG, apiStatus);

		TsmpDpItems item = new TsmpDpItems();
		item.setSubitemNo(apiStatus);
		item.setSubitemName("");
		if (tsmpDpItem != null) {
			item = getTsmpDpItemsByItemNoAndSubitemNoAndLocale( //
					TsmpDpItem.ENABLE_FLAG, tsmpDpItem.getSubitemNo(), locale);
		}

		resp.setApiKey(apiKey);
		resp.setModuleName(moduleName);
		resp.setEnableScheduledDate(enableScheduledDate);
		resp.setDisableScheduledDate(disableScheduledDate);
		resp.setApiStatus(item.getParam1());
		resp.setApiStatusName(item.getSubitemName());

		return resp;
	}

	protected TsmpDpItems getTsmpDpItemsByItemNoAndSubitemNoAndLocale(TsmpDpItem itemEnum, String subitemNo,
			String locale) {

		TsmpDpItems item = getTsmpDpItemsDao().findByItemNoAndSubitemNoAndLocale(itemEnum.getItemNo(), subitemNo,
				locale);

		if (item == null && !LocaleType.EN_US.equals(locale))
			item = getTsmpDpItemsDao().findByItemNoAndSubitemNoAndLocale(itemEnum.getItemNo(), subitemNo,
					LocaleType.EN_US);

		if (item == null) {
			TPILogger.tl.debugDelay2sec("The result of searching TsmpDpItems's " + itemEnum.getItemNo() + " is null.");
			TPILogger.tl.debugDelay2sec("subitemNo is " + subitemNo + ";" + "locale is " + locale);
		}

		return item;
	}

	protected String getErrMsg(TsmpDpAaRtnCode tsmpDpAaRtnCode, String locale) {

		if (tsmpDpAaRtnCode == null) {
			return null;
		}

		Optional<ITsmpRtnCode> opt = getTsmpRtnCodeDao().findByTsmpRtnCodeAndLocale(tsmpDpAaRtnCode.getCode(), locale);
		if (opt.isEmpty()) {
			opt = getTsmpRtnCodeDao().findByTsmpRtnCodeAndLocale(tsmpDpAaRtnCode.getCode(), LocaleType.EN_US);
		}
		return opt.isPresent() ? opt.get().getTsmpRtnMsg() : null;
	}

	protected List<TsmpApi> getApiList(String name, ApiListAndAlgorithmClassifier classifier) {

		Map<String, List<TsmpApi>> map = classifier.getApiListMap();

		return map.computeIfAbsent(name, k -> new ArrayList<>());
	}

	/**
	 * 比較兩個長整型數字（代表時間戳）轉換成只含日期後的大小。
	 *
	 * @param first  第一個長整型數字
	 * @param second 第二個長整型數字
	 * @return 如果第一個時間戳代表的日期大於等於第二個，則返回 true；否則返回 false。
	 */
	protected boolean isFirstNumberGreaterThanOrEqualSecondNumber(long first, long second) {

		// 將第一個時間戳轉換成只含日期的時間戳
		first = TimestampConverterUtil.getDateOnlyTimestamp(first);
		// 將第二個時間戳轉換成只含日期的時間戳
		second = TimestampConverterUtil.getDateOnlyTimestamp(second);

		return first >= second;
	}

	public EnableAlgorithm getEnableAlgorithm() {
		return enableAlgorithm;
	}

	public void setEnableAlgorithm(EnableAlgorithm enableAlgorithm) {
		this.enableAlgorithm = enableAlgorithm;
	}

	public TsmpDpItemsDao getTsmpDpItemsDao() {
		return tsmpDpItemsDao;
	}

	public void setTsmpDpItemsDao(TsmpDpItemsDao tsmpDpItemsDao) {
		this.tsmpDpItemsDao = tsmpDpItemsDao;
	}

	public TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return tsmpRtnCodeDao;
	}

	public void setTsmpRtnCodeDao(TsmpRtnCodeDao tsmpRtnCodeDao) {
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
	}

	public DisableAlgorithm getDisableAlgorithm() {
		return disableAlgorithm;
	}

	public void setDisableAlgorithm(DisableAlgorithm disableAlgorithm) {
		this.disableAlgorithm = disableAlgorithm;
	}

}
