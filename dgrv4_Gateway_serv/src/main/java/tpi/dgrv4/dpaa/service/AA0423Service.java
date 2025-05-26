package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0423Req;
import tpi.dgrv4.dpaa.vo.AA0423Resp;
import tpi.dgrv4.dpaa.vo.AA0423RespItem;
import tpi.dgrv4.dpaa.vo.AA0423RespSrcUrlAndPercentageItem;
import tpi.dgrv4.dpaa.vo.AA0423RespSrcUrlListItem;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AA0423Service {
	private TPILogger logger = TPILogger.tl;
	
	private TsmpApiDao tsmpApiDao;
	private TsmpApiRegDao tsmpApiRegDao;

	@Autowired
	public AA0423Service(TsmpApiDao tsmpApiDao, TsmpApiRegDao tsmpApiRegDao) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiRegDao = tsmpApiRegDao;
	}

	public AA0423Resp queryAPIListBySrcUrlOrLabel(AA0423Req req) {
		AA0423Resp resp = new AA0423Resp();
		try {
			List<AA0423RespItem> dataList ;
			List<String> labeList = req.getLabelList();
			String targetSite = req.getTargetSite();
			//若不是依標籤搜尋就通通用目標站台搜尋
			if (!CollectionUtils.isEmpty(labeList)) {
				List<TsmpApi> apis = getTsmpApiDao().query_AA0423Service(labeList);
				dataList = setData(apis);

			} else {//預設 空
				if (targetSite == null) {
					targetSite = "";
				}

				dataList = queryByTargetSit(targetSite);
			}
			Collections.sort(dataList, Comparator.comparing(AA0423RespItem::getApiKey));
			resp.setDataList(dataList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private List<AA0423RespItem> setData(List<TsmpApi> apis) {
		List<AA0423RespItem> dataList = new ArrayList<>();
		apis.forEach(a -> {
			String apikey = a.getApiKey();
			String moduleName = a.getModuleName();
			TsmpApiReg r = getTsmpApiRegDao().findById(new TsmpApiRegId(apikey, moduleName)).orElse(null);
			if (r == null) {
				throw TsmpDpAaRtnCode._1481.throwing(apikey, moduleName);
			}
			AA0423RespItem item = new AA0423RespItem();
			item.setApiKey(apikey);
			item.setModuleName(moduleName);
			item.setApiName(a.getApiName());
			item.setApiStatus(a.getApiStatus());
			item.setLabelList(getLabelList(a));
			Boolean noOauth = false;
			if ("1".equals(r.getNoOauth())) {
				noOauth = true;
			}

			item.setNoOauth(noOauth);
			List<AA0423RespSrcUrlListItem> srcUrlList = getSrcUrl(r);

			item.setSrcUrlList(srcUrlList);
			dataList.add(item);

		});

		return dataList;
	}

	private List<AA0423RespItem> queryByTargetSit(String targetSite) {
		List<AA0423RespItem> dataList = new ArrayList<>();

		List<TsmpApiReg> apiRegList = getTsmpApiRegDao().query_AA0423Service();
		if (apiRegList == null) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		apiRegList.forEach(r -> {
			AA0423RespItem item = new AA0423RespItem();

			String apikey = r.getApiKey();
			String moduleName = r.getModuleName();
			TsmpApiId apiId = new TsmpApiId(apikey, moduleName);
			TsmpApi tsmpApi = getTsmpApiDao().findById(apiId).orElse(null);
			if (tsmpApi == null) {
				throw TsmpDpAaRtnCode._1481.throwing(apikey, moduleName);
			}

			List<AA0423RespSrcUrlListItem> srcUrlList = getSrcUrl(r);

			List<String> labeList = getLabelList(tsmpApi);
			item.setLabelList(labeList);
			item.setApiKey(apikey);
			item.setApiName(tsmpApi.getApiName());
			item.setApiStatus(tsmpApi.getApiStatus());
			item.setModuleName(moduleName);
			Boolean noOauth = false;
			if ("1".equals(r.getNoOauth())) {
				noOauth = true;
			}

			item.setNoOauth(noOauth);
			item.setSrcUrlList(srcUrlList);

			dataList.add(item);
		});
		Set<AA0423RespItem> set = new HashSet<>();
		dataList.forEach(data -> {
			data.getSrcUrlList().forEach(sL -> {
				sL.getSrcUrlAndPercentageList().stream().filter(s -> s.getSrcUrl().contains(targetSite))
						.forEach(e -> set.add(data));

			});

		});
		return new ArrayList<>(set);

	}

	/***
	 * 取得 srcUrl 的資料
	 * 
	 * @param TsmpApiReg
	 * @return
	 */
	private List<AA0423RespSrcUrlListItem> getSrcUrl(TsmpApiReg r) {
		List<AA0423RespSrcUrlListItem> srcUrlList = new ArrayList<>();
		try {

			// 有分流
			if ("Y".equals(r.getRedirectByIp())) {
				String ipSrcUrl1 = r.getIpSrcUrl1();
				if (StringUtils.hasLength(ipSrcUrl1)) {
					AA0423RespSrcUrlListItem srcUrlListItem = setIpAndSrcurl(r.getIpForRedirect1(),
							b64DecodeAndGetTargetSite(ipSrcUrl1));
					srcUrlList.add(srcUrlListItem);
				}

				String ipSrcUrl2 = r.getIpSrcUrl2();
				if (StringUtils.hasLength(ipSrcUrl2)) {
					AA0423RespSrcUrlListItem srcUrlListItem = setIpAndSrcurl(r.getIpForRedirect2(),
							b64DecodeAndGetTargetSite(ipSrcUrl2));
					srcUrlList.add(srcUrlListItem);
				}
				String ipSrcUrl3 = r.getIpSrcUrl3();
				if (StringUtils.hasLength(ipSrcUrl3)) {
					AA0423RespSrcUrlListItem srcUrlListItem = setIpAndSrcurl(r.getIpForRedirect3(),
							b64DecodeAndGetTargetSite(ipSrcUrl3));
					srcUrlList.add(srcUrlListItem);
				}
				String ipSrcUrl4 = r.getIpSrcUrl4();
				if (StringUtils.hasLength(ipSrcUrl4)) {
					AA0423RespSrcUrlListItem srcUrlListItem = setIpAndSrcurl(r.getIpForRedirect4(),
							b64DecodeAndGetTargetSite(ipSrcUrl4));
					srcUrlList.add(srcUrlListItem);
				}
				String ipSrcUrl5 = r.getIpSrcUrl5();
				if (StringUtils.hasLength(ipSrcUrl5)) {
					AA0423RespSrcUrlListItem srcUrlListItem = setIpAndSrcurl(r.getIpForRedirect5(),
							b64DecodeAndGetTargetSite(ipSrcUrl5));
					srcUrlList.add(srcUrlListItem);
				}

			} else { // 無分流 將IP設為 ""
				String srcUrl = r.getSrcUrl();
				AA0423RespSrcUrlListItem srcUrlListItem = setIpAndSrcurl("", b64DecodeAndGetTargetSite(srcUrl));
				srcUrlList.add(srcUrlListItem);
			}
		} catch (Exception e) {
			throw TsmpDpAaRtnCode._1452.throwing("url decode", r.getApiKey(), r.getModuleName(), "url decode error .");
		}
		return srcUrlList;
	}

	private List<String> getLabelList(TsmpApi tsmpApi) {
		String label1 = tsmpApi.getLabel1();
		String label2 = tsmpApi.getLabel2();
		String label3 = tsmpApi.getLabel3();
		String label4 = tsmpApi.getLabel4();
		String label5 = tsmpApi.getLabel5();
		Set<String> set = new HashSet<>();
		set.add(label1);
		set.add(label2);
		set.add(label3);
		set.add(label4);
		set.add(label5);
		set.removeIf(value -> !StringUtils.hasLength(value));
		List<String> labeList = new ArrayList<>();
		set.forEach(s -> labeList.add(s.toLowerCase()));
		return labeList;
	}

	/**
	 * 懶的寫好多次就給他拉出來
	 * 
	 * @param ip
	 * @param list
	 * @return
	 */
	private AA0423RespSrcUrlListItem setIpAndSrcurl(String ip, List<AA0423RespSrcUrlAndPercentageItem> list) {
		AA0423RespSrcUrlListItem srcUrlListItem = new AA0423RespSrcUrlListItem();
		srcUrlListItem.setIp(ip);
		srcUrlListItem.setSrcUrlAndPercentageList(list);
		return srcUrlListItem;
	}

	/**
	 * base64解碼 若沒有"b64."開頭代表只有一條 ，直接將百分比設為100
	 * 
	 * @param url
	 * @return
	 */
	private List<AA0423RespSrcUrlAndPercentageItem> b64DecodeAndGetTargetSite(String url) throws Exception {
		List<AA0423RespSrcUrlAndPercentageItem> srcUrlAndPercentageList = new ArrayList<>();
		if (url.startsWith("b64.")) {
			String encodeString = url.split("b64.")[1];
			String[] base64String = encodeString.split("\\.");
			for (int i = 0; i < base64String.length; i++) {
				if (i % 2 == 0) {
					AA0423RespSrcUrlAndPercentageItem srcUrlAndPercentageItem = new AA0423RespSrcUrlAndPercentageItem();
					String plainText = "";

					plainText = new String(Base64Util.base64URLDecode(base64String[i + 1]));

					srcUrlAndPercentageItem.setPercentage(base64String[i]);
					srcUrlAndPercentageItem.setSrcUrl(plainText);
					srcUrlAndPercentageList.add(srcUrlAndPercentageItem);

				}
			}
		} else {
			AA0423RespSrcUrlAndPercentageItem srcUrlAndPercentageItem = new AA0423RespSrcUrlAndPercentageItem();

			srcUrlAndPercentageItem.setPercentage("100");
			srcUrlAndPercentageItem.setSrcUrl(url);
			srcUrlAndPercentageList.add(srcUrlAndPercentageItem);

		}

		return srcUrlAndPercentageList;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return tsmpApiRegDao;
	}
}
