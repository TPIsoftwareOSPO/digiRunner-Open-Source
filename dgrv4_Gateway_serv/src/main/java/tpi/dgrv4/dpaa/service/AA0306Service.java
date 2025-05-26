package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.constant.TsmpDpItem;
import tpi.dgrv4.dpaa.vo.AA0306ItemReq;
import tpi.dgrv4.dpaa.vo.AA0306ItemResp;
import tpi.dgrv4.dpaa.vo.AA0306Req;
import tpi.dgrv4.dpaa.vo.AA0306Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.ITsmpRtnCode;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0306Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpApiDao tsmpApiDao;
	private TsmpRtnCodeDao tsmpRtnCodeDao;
	private BcryptParamHelper bcryptParamHelper;

	@Autowired
	public AA0306Service(TsmpApiDao tsmpApiDao, TsmpRtnCodeDao tsmpRtnCodeDao, BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	/**
	 * 取消已排程的日期，根據請求中的撤銷標誌進行相應操作。
	 *
	 * @param auth      TsmpAuthorization 物件，包含授權資訊。
	 * @param req       AA0306Req 物件，包含 API 列表和撤銷標誌。
	 * @param reqHeader 請求標頭，包含地區資訊。
	 * @return AA0306Resp 物件，包含更新後的 API 列表。
	 */
	@Transactional
	public AA0306Resp cancelScheduledDate(TsmpAuthorization auth, AA0306Req req, ReqHeader reqHeader) {

		// 建立回應物件
		AA0306Resp resp = new AA0306Resp();

		try {

			logger.debug("Starting cancelScheduledDate process.");

			// 檢查請求參數
			check(req);
			logger.debug("Request parameters checked successfully.");

			// 取得地區資訊
			String locale = reqHeader.getLocale();
			// 解碼撤銷標誌
			String revokeFlag = getRevokeFlag(req.getRevokeFlag(), locale);
			logger.debug("Revoke flag decoded: " + revokeFlag);

			// 如果撤銷標誌不存在於預期的項目中，返回空的 API 列表
			if (!TsmpDpItem.existsSubitemNoInItemNo(TsmpDpItem.DGR_API_REVOKE_TYPE, revokeFlag)) {
				logger.warn("Revoke flag not found in expected items. RevokeFlag: " + revokeFlag);
				resp.setApiList(new ArrayList<>());
				return resp;
			}

			// 取得對應的撤銷演算法
			BiFunction<List<TsmpApi>, Map<String, List<TsmpApi>>, Map<String, List<TsmpApi>>> algorithm = getRevokeAlgorithm(
					revokeFlag);

			Map<String, List<TsmpApi>> apiMap = new HashMap<>();

			// 取得對應的 TsmpApi 列表
			List<TsmpApi> ls = getTsmpApis(req.getApiList());
			logger.debug("TsmpApi list retrieved: " + ls.size() + " items.");

			// 應用撤銷演算法
			apiMap = algorithm.apply(ls, apiMap);
			logger.debug("Revoke algorithm applied.");

			// 將 TsmpApi 列表轉換為 AA0306ItemResp 列表
			List<AA0306ItemResp> respList = getRespItem(apiMap, locale);
			// 設定回應物件的 API 列表
			resp.setApiList(respList);
			logger.debug("Response API list set.");

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Unexpected exception occurred: " + StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		// 返回回應物件
		return resp;
	}

	private List<AA0306ItemResp> getRespItem(Map<String, List<TsmpApi>> map, String locale) {

		// 建立回應列表
		List<AA0306ItemResp> ls = new ArrayList<>();

		// 遍歷 Map 的每個 entry
		for (Map.Entry<String, List<TsmpApi>> entry : map.entrySet()) {
			// 獲取鍵和值
			String key = entry.getKey();
			List<TsmpApi> value = entry.getValue();

			// 根據鍵和值以及 locale 獲取回應列表
			List<AA0306ItemResp> resp = getResp(key, value, locale);

			// 將回應列表添加到總回應列表中
			ls.addAll(resp);
		}

		return ls;
	}

	private List<AA0306ItemResp> getResp(String key, List<TsmpApi> value, String locale) {

		// 如果鍵為成功代碼，返回成功的回應列表
		if (TsmpDpAaRtnCode.SUCCESS.getCode().equals(key)) {
			return getResp(true, null, locale, value);
		}

		// 如果鍵為 _1555 代碼，返回對應的失敗回應列表
		if (TsmpDpAaRtnCode._1555.getCode().equals(key)) {
			return getResp(false, TsmpDpAaRtnCode._1555, locale, value);
		}

		// 如果鍵為 _1556 代碼，返回對應的失敗回應列表
		if (TsmpDpAaRtnCode._1556.getCode().equals(key)) {
			return getResp(false, TsmpDpAaRtnCode._1556, locale, value);
		}

		// 如果鍵不符合以上任何條件，返回空列表
		return Collections.emptyList();
	}

	private List<AA0306ItemResp> getResp(boolean isSuccess, TsmpDpAaRtnCode tsmpDpAaRtnCode, String locale,
			List<TsmpApi> value) {

		return value.stream().map(api -> {

			String apiKey = api.getApiKey();
			String moduleName = api.getModuleName();

			AA0306ItemResp aa0306ItemResp = new AA0306ItemResp();

			Long enableScheduledDate = api.getEnableScheduledDate();
			Long disableScheduledDate = api.getDisableScheduledDate();

			aa0306ItemResp.setApiKey(apiKey);
			aa0306ItemResp.setModuleName(moduleName);

			aa0306ItemResp.setEnableScheduledDate(enableScheduledDate);
			aa0306ItemResp.setDisableScheduledDate(disableScheduledDate);

			aa0306ItemResp.setProcessResult(isSuccess);
			aa0306ItemResp.setErrMsg(getErrMsg(tsmpDpAaRtnCode, locale));

			return aa0306ItemResp;

		}).collect(Collectors.toList());
	}

	private String getErrMsg(TsmpDpAaRtnCode tsmpDpAaRtnCode, String locale) {

		if (tsmpDpAaRtnCode == null) {
			return null;
		}

		Optional<ITsmpRtnCode> opt = getTsmpRtnCodeDao().findByTsmpRtnCodeAndLocale(tsmpDpAaRtnCode.getCode(), locale);
		if (opt.isEmpty()) {
			opt = getTsmpRtnCodeDao().findByTsmpRtnCodeAndLocale(tsmpDpAaRtnCode.getCode(), LocaleType.EN_US);
		}
		return opt.isPresent() ? opt.get().getTsmpRtnMsg() : null;
	}

	/**
	 * 根據給定的 API 列表，取得對應的 TsmpApi 列表。
	 *
	 * @param apiList API 列表。
	 * @return 對應的 TsmpApi 列表。
	 */
	private List<TsmpApi> getTsmpApis(List<AA0306ItemReq> apiList) {
		// 建立 id 的列表
		List<TsmpApiId> ids = apiList.stream().map(item -> new TsmpApiId(item.getApiKey(), item.getModuleName()))
				.collect(Collectors.toList());

		// 根據 TsmpApiId 列表查找並返回對應的 TsmpApi 列表
		return getTsmpApiDao().findAllById(ids);
	}

	/**
	 * 根據給定的撤銷標誌，取得對應的撤銷演算法。
	 *
	 * @param revokeFlag 撤銷標誌。
	 * @return 對應的撤銷演算法。
	 * @throws TsmpDpAaRtnCode._1297 如果撤銷標誌不符合預期的值。
	 */
	private BiFunction<List<TsmpApi>, Map<String, List<TsmpApi>>, Map<String, List<TsmpApi>>> getRevokeAlgorithm(
			String revokeFlag) {
		// 如果撤銷標誌為 REVOKE_ENABLE_DISABLE，返回對應的演算法
		if (TsmpDpItem.isEqualSubitemNo(TsmpDpItem.REVOKE_ENABLE_DISABLE, revokeFlag)) {
			return getRevokeAllAlgorithm();
		}

		// 如果撤銷標誌為 REVOKE_ENABLE，返回對應的演算法
		if (TsmpDpItem.isEqualSubitemNo(TsmpDpItem.REVOKE_ENABLE, revokeFlag)) {
			return getRevokeEnableAlgorithm();
		}

		// 如果撤銷標誌為 REVOKE_DISABLE，返回對應的演算法
		if (TsmpDpItem.isEqualSubitemNo(TsmpDpItem.REVOKE_DISABLE, revokeFlag)) {
			return getRevokeDisableAlgorithm();
		}

		// 如果撤銷標誌不符合預期的值，記錄錯誤並拋出異常
		logger.error("The value of revokeFlag is not as expected. RevokeFlag: " + revokeFlag);
		throw TsmpDpAaRtnCode._1297.throwing();
	}

	private BiFunction<List<TsmpApi>, Map<String, List<TsmpApi>>, Map<String, List<TsmpApi>>> getRevokeDisableAlgorithm() {
		return (list, map) -> {
			// 收集需要批次處理的 TsmpApi 物件
			List<TsmpApi> apisToRevoke = new ArrayList<>();

			list.forEach(api -> {
				if ("1".equalsIgnoreCase(api.getApiStatus()) && api.getEnableScheduledDate() != 0) {
					throw TsmpDpAaRtnCode._1555.throwing();
//					setApiToMap(TsmpDpAaRtnCode._1555, api, map);
				} else {
					api.setDisableScheduledDate(0L);
					api.setUpdateTime(DateTimeUtil.now());
					apisToRevoke.add(api); // 將需要處理的 api 加入清單
				}
			});

			getTsmpApiDao().saveAll(apisToRevoke);

			// 將批次處理後的結果更新到 map 中
			apisToRevoke.forEach(api -> setApiToMap(TsmpDpAaRtnCode.SUCCESS, api, map));

			return map; // 返回更新後的 Map
		};
	}

	private BiFunction<List<TsmpApi>, Map<String, List<TsmpApi>>, Map<String, List<TsmpApi>>> getRevokeEnableAlgorithm() {
		return (list, map) -> {
			// 收集需要批次處理的 TsmpApi 物件
			List<TsmpApi> apisToRevoke = new ArrayList<>();

			list.forEach(api -> {
				if ("2".equalsIgnoreCase(api.getApiStatus()) && api.getDisableScheduledDate() != 0) {
					throw TsmpDpAaRtnCode._1556.throwing();
//					setApiToMap(TsmpDpAaRtnCode._1556, api, map);
				} else {
					api.setEnableScheduledDate(0L);
					api.setUpdateTime(DateTimeUtil.now());
					apisToRevoke.add(api); // 將需要處理的 api 加入清單
				}
			});

			getTsmpApiDao().saveAll(apisToRevoke);

			// 將批次處理後的結果更新到 map 中
			apisToRevoke.forEach(api -> setApiToMap(TsmpDpAaRtnCode.SUCCESS, api, map));

			return map; // 返回更新後的 Map
		};
	}

	private BiFunction<List<TsmpApi>, Map<String, List<TsmpApi>>, Map<String, List<TsmpApi>>> getRevokeAllAlgorithm() {

		return (list, map) -> {
			// 收集需要批次處理的 TsmpApi 物件
			List<TsmpApi> apisToRevoke = new ArrayList<>();

			list.forEach(api -> {

				api.setEnableScheduledDate(0L);
				api.setDisableScheduledDate(0L);
				api.setUpdateTime(DateTimeUtil.now());
				apisToRevoke.add(api); // 將需要處理的 api 加入清單

			});

			getTsmpApiDao().saveAll(apisToRevoke);

			// 將批次處理後的結果更新到 map 中
			apisToRevoke.forEach(api -> setApiToMap(TsmpDpAaRtnCode.SUCCESS, api, map));

			return map; // 返回更新後的 Map
		};

	}

	/**
	 * 根據給定的撤銷標誌和地區，解碼並返回新的撤銷標誌。
	 *
	 * @param revokeFlag 原始的撤銷標誌。
	 * @param locale     地區資訊。
	 * @return 解碼後的撤銷標誌。
	 * @throws TsmpDpAaRtnCode._1299 如果解碼過程中發生錯誤。
	 */
	private String getRevokeFlag(String revokeFlag, String locale) {
		try {
			// 解碼撤銷標誌
			return getBcryptParamHelper().decode(revokeFlag, TsmpDpItem.DGR_API_REVOKE_TYPE.getItemNo(), locale);
		} catch (BcryptParamDecodeException e) {
			// 如果解碼過程中發生錯誤，拋出指定的異常
			throw TsmpDpAaRtnCode._1299.throwing();
		}
	}

	/**
	 * 檢查 AA0306Req 請求物件的有效性
	 */
	private void check(AA0306Req req) {
		// 檢查 API 列表
		checkApiList(req.getApiList());
		// 檢查撤銷標誌
		checkRevokeFlag(req.getRevokeFlag());
	}

	/**
	 * 檢查 API 列表的有效性
	 */
	private void checkApiList(List<AA0306ItemReq> apiList) {
		// 如果 API 列表不為空，則逐一檢查每個項目
		if (apiList != null) {
			apiList.forEach(aa0306ItemReq -> {
				// 檢查 API Key 的有效性
				checkApiKey(aa0306ItemReq.getApiKey());
				// 檢查模組名稱的有效性
				checkModuleName(aa0306ItemReq.getModuleName());
			});
		}
	}

	/**
	 * 檢查 API Key 的有效性
	 */
	private void checkApiKey(String apiKey) {
		// 如果 API Key 為空或僅包含空白字符，則拋出異常
		if (!StringUtils.hasText(apiKey)) {
			throw TsmpDpAaRtnCode._1395.throwing();
		}
	}

	/**
	 * 檢查模組名稱的有效性
	 */
	private void checkModuleName(String moduleName) {
		// 如果模組名稱為空或僅包含空白字符，則拋出異常
		if (!StringUtils.hasText(moduleName)) {
			throw TsmpDpAaRtnCode._1393.throwing();
		}
	}

	/**
	 * 檢查撤銷標誌的有效性
	 */
	private void checkRevokeFlag(String revokeFlag) {
		// 如果撤銷標誌為空或僅包含空白字符，則拋出異常
		if (!StringUtils.hasText(revokeFlag)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return this.tsmpRtnCodeDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	private Map<String, List<TsmpApi>> setApiToMap(TsmpDpAaRtnCode code, TsmpApi api, Map<String, List<TsmpApi>> map) {

		List<TsmpApi> ls = map.computeIfAbsent(code.getCode(), k -> new ArrayList<>());

		ls.add(api);

		map.put(code.getCode(), ls);

		return map;
	}

}
