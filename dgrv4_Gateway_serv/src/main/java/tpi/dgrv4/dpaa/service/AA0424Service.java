package tpi.dgrv4.dpaa.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0424ApiData;
import tpi.dgrv4.dpaa.vo.AA0424IpAndSrcUrlList;
import tpi.dgrv4.dpaa.vo.AA0424NewSrcUrlItem;
import tpi.dgrv4.dpaa.vo.AA0424Req;
import tpi.dgrv4.dpaa.vo.AA0424ReqAPIList;
import tpi.dgrv4.dpaa.vo.AA0424ReqSrcUrlList;
import tpi.dgrv4.dpaa.vo.AA0424Resp;
import tpi.dgrv4.dpaa.vo.AA0424SrcUrl;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0424Service {
	private TPILogger logger = TPILogger.tl;
	
	private TsmpApiDao tsmpApiDao;
	private TsmpApiRegDao tsmpApiRegDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private ObjectMapper objectMapper;
	private FileHelper fileHelper;

	@Autowired
	public AA0424Service(TsmpApiDao tsmpApiDao, TsmpApiRegDao tsmpApiRegDao, TsmpDpFileDao tsmpDpFileDao,
			ObjectMapper objectMapper, FileHelper fileHelper) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiRegDao = tsmpApiRegDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.objectMapper = objectMapper;
		this.fileHelper = fileHelper;
	}

	public AA0424Resp temporaryByModifyBatch(TsmpAuthorization authorization, AA0424Req req) {
		AA0424Resp resp = new AA0424Resp();
		try {
			checkParm(req);
			List<AA0424ReqSrcUrlList> reqSrcList = req.getSrcUrlList();

			List<AA0424ApiData> list = new ArrayList<>();
			List<AA0424ReqAPIList> apiList = req.getApiList();

			int i = 0;
			for (AA0424ReqAPIList a : apiList) {
				List<AA0424NewSrcUrlItem> newList ;
				List<AA0424IpAndSrcUrlList> oldList ;
				AA0424ApiData apidata = new AA0424ApiData();
				String apikey = a.getApiKey();
				String moduleName = a.getModuleName();
				TsmpApi api = getTsmpApiDao().findByModuleNameAndApiKey(moduleName, apikey);

				if (api == null) {
					throw TsmpDpAaRtnCode._1400.throwing(apikey);
				}

				TsmpApiReg apireg = getTsmpApiRegDao().findByModuleNameAndApiKey(moduleName, apikey);
				if (apireg == null) {
					throw TsmpDpAaRtnCode._1481.throwing(apikey, moduleName);
				}

				// 取得原資料
				oldList = getSrcUrl(apireg);
				apidata.setOldSrcUrlList(oldList);
				// 這裡取得替換後的資料

				newList = getNewSrcUrl(oldList, reqSrcList);

				apidata.setApiStatus(api.getApiStatus());
				Boolean noOauth = false;
				if ("1".equals(apireg.getNoOauth())) {
					noOauth = true;
				}

				apidata.setNoAuth(noOauth);
				apidata.setApiKey(apikey);
				apidata.setApiName(api.getApiName());
				apidata.setModuleName(moduleName);
				apidata.setSort(i);
				List<String> labelList = new ArrayList<>();
				labelList = getLabelList(api);
				apidata.setLabelList(labelList);

				apidata.setNewSrcUrlList(newList);

				i++;
				list.add(apidata);

			}

			Collections.sort(list, Comparator.comparingInt(AA0424ApiData::getSort));

			String json = getObjectMapper().writeValueAsString(list);
			String fileName = getFileName();
			logger.debug("fileName = " + fileName + " json = "
					+ getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(json));

			Long maxRefid = queryMaxRefid();

			Long refid = maxRefid + 1;
			// 寫入DPFILE
			TsmpDpFile dpfile = getFileHelper().upload(authorization.getUserName(), TsmpDpFileType.API_MODIFY_BATCH,
					refid, fileName, json.getBytes(), "N");
			logger.debug("fileId : " + dpfile.getFileId() + " RefFileCateCode : " + dpfile.getRefFileCateCode()
					+ " refId : " + dpfile.getRefId());
			resp.setTempFileName(fileName);
			resp.setRefId(String.valueOf(refid));
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;

	}

	/***
	 * 取得最後一筆的 refid 以防檔名重複時找不到
	 * 
	 * @return
	 */
	private Long queryMaxRefid() {
		TsmpDpFile dpfile = getTsmpDpFileDao()
				.findTopByRefFileCateCodeOrderByRefIdDesc(TsmpDpFileType.API_MODIFY_BATCH.code());
		if (dpfile == null) {
			return 0l;
		}
		return dpfile.getRefId();
	}

	private List<String> getLabelList(TsmpApi api) {
		String label1 = api.getLabel1();
		String label2 = api.getLabel2();
		String label3 = api.getLabel3();
		String label4 = api.getLabel4();
		String label5 = api.getLabel5();
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

	/***
	 * 取得 srcUrl 的資料
	 * 
	 * @param TsmpApiReg
	 * @return
	 */
	private List<AA0424IpAndSrcUrlList> getSrcUrl(TsmpApiReg r) {
		List<AA0424IpAndSrcUrlList> srcUrlList = new ArrayList<>();
		// 有分流
		if ("Y".equals(r.getRedirectByIp())) {
			String ipSrcUrl1 = r.getIpSrcUrl1();
			if (StringUtils.hasLength(ipSrcUrl1)) {
				AA0424IpAndSrcUrlList srcUrlListItem = setIpAndSrcurl(r.getIpForRedirect1(),
						b64DecodeAndGetTargetSit(ipSrcUrl1));
				srcUrlList.add(srcUrlListItem);
			}

			String ipSrcUrl2 = r.getIpSrcUrl2();
			if (StringUtils.hasLength(ipSrcUrl2)) {
				AA0424IpAndSrcUrlList srcUrlListItem = setIpAndSrcurl(r.getIpForRedirect2(),
						b64DecodeAndGetTargetSit(ipSrcUrl2));
				srcUrlList.add(srcUrlListItem);
			}
			String ipSrcUrl3 = r.getIpSrcUrl3();
			if (StringUtils.hasLength(ipSrcUrl3)) {
				AA0424IpAndSrcUrlList srcUrlListItem = setIpAndSrcurl(r.getIpForRedirect3(),
						b64DecodeAndGetTargetSit(ipSrcUrl3));
				srcUrlList.add(srcUrlListItem);
			}
			String ipSrcUrl4 = r.getIpSrcUrl4();
			if (StringUtils.hasLength(ipSrcUrl4)) {
				AA0424IpAndSrcUrlList srcUrlListItem = setIpAndSrcurl(r.getIpForRedirect4(),
						b64DecodeAndGetTargetSit(ipSrcUrl4));
				srcUrlList.add(srcUrlListItem);
			}
			String ipSrcUrl5 = r.getIpSrcUrl5();
			if (StringUtils.hasLength(ipSrcUrl5)) {
				AA0424IpAndSrcUrlList srcUrlListItem = setIpAndSrcurl(r.getIpForRedirect5(),
						b64DecodeAndGetTargetSit(ipSrcUrl5));
				srcUrlList.add(srcUrlListItem);
			}

		} else { // 無分流 將IP設為 ""
			String srcUrl = r.getSrcUrl();
			AA0424IpAndSrcUrlList srcUrlListItem = setIpAndSrcurl("", b64DecodeAndGetTargetSit(srcUrl));
			srcUrlList.add(srcUrlListItem);
		}
		return srcUrlList;
	}

	/**
	 * 那一大串b64. 分解成
	 * 
	 * @param url
	 * @return
	 */
	private List<AA0424SrcUrl> b64DecodeAndGetTargetSit(String url) {
		List<AA0424SrcUrl> srcUrlAndPercentageList = new ArrayList<>();
		if (url.startsWith("b64.")) {
			String encodeString = url.split("b64.")[1];
			String[] base64String = encodeString.split("\\.");
			for (int i = 0; i < base64String.length; i++) {
				if (i % 2 == 0) {
					AA0424SrcUrl srcUrlAndPercentageItem = new AA0424SrcUrl();

					String plainText = new String(Base64Util.base64URLDecode(base64String[i + 1]));
					srcUrlAndPercentageItem.setPercentage(Integer.valueOf(base64String[i]));
					srcUrlAndPercentageItem.setSrcUrl(plainText);
					srcUrlAndPercentageList.add(srcUrlAndPercentageItem);

				}
			}
		} else {
			AA0424SrcUrl srcUrlAndPercentageItem = new AA0424SrcUrl();

			srcUrlAndPercentageItem.setPercentage(100);
			srcUrlAndPercentageItem.setSrcUrl(url);
			srcUrlAndPercentageList.add(srcUrlAndPercentageItem);

		}

		return srcUrlAndPercentageList;
	}

	private AA0424IpAndSrcUrlList setIpAndSrcurl(String ip, List<AA0424SrcUrl> list) {
		AA0424IpAndSrcUrlList srcUrlListItem = new AA0424IpAndSrcUrlList();
		srcUrlListItem.setIp(ip);
		srcUrlListItem.setSrcUrlList(list);
		return srcUrlListItem;
	}

	/**
	 * 組合更新的資料 真·邏輯 所在
	 * 
	 * @param oldList
	 * @param list
	 * @return
	 */
	private List<AA0424NewSrcUrlItem> getNewSrcUrl(List<AA0424IpAndSrcUrlList> oldList,
			List<AA0424ReqSrcUrlList> list) {

		List<AA0424IpAndSrcUrlList> tempList = new ArrayList<>();
		oldList.forEach(o -> tempList.add(new AA0424IpAndSrcUrlList(o)));

		List<AA0424NewSrcUrlItem> newList = new ArrayList<>();

		for (AA0424IpAndSrcUrlList ipAndSrcUrlList : tempList) {

			List<AA0424SrcUrl> newData = setNewData(ipAndSrcUrlList.getSrcUrlList(), list);
			if (newData == null) {
				return null;
			}

			AA0424NewSrcUrlItem ipAndSrcUrl = new AA0424NewSrcUrlItem();
			ipAndSrcUrl.setSrcUrlList(newData);
			ipAndSrcUrl.setIp(ipAndSrcUrlList.getIp());
			ipAndSrcUrl.setIsSuccess(isSuccess(newData));
			newList.add(ipAndSrcUrl);

		}

		return newList;
	}

	private Boolean isSuccess(List<AA0424SrcUrl> newData) {
		int sum = 0;
		for (AA0424SrcUrl s : newData) {
			sum += s.getPercentage(); // 加總
		}
			return sum == 100;
	}

	private List<AA0424SrcUrl> setNewData(List<AA0424SrcUrl> srcUrl, List<AA0424ReqSrcUrlList> list) {
		List<AA0424SrcUrl> newData = new ArrayList<>();
		srcUrl.forEach(o -> newData.add(new AA0424SrcUrl(o)));
		boolean isOneOfItemContain = false;

		for (AA0424SrcUrl s : newData) { // 先替換%
			for (AA0424ReqSrcUrlList r : list) {
				String target = r.getSrcUrl(); // 目標url
				if (r.getIsPercentage()) {
					if (s.getSrcUrl().contains(target)) {
						s.setPercentage(r.getPercentage());
						isOneOfItemContain = true;
					} else {
						s.setPercentage(s.getPercentage());

					}
				}

			}

		}
		// 分流每個項目中 要有一個有匹對成功才處理，如果此IP分流中沒有符和，則不修改
		if (isOneOfItemContain) {
			for (AA0424SrcUrl s : newData) {
				boolean isContain = false;
				boolean isModify = false;
				for (AA0424ReqSrcUrlList r : list) {
					String target = r.getSrcUrl(); // 目標url
					if (r.getIsPercentage()) {
						isModify = true;

						if (s.getSrcUrl().contains(target)) {
							isContain = true;
						}
					}
				}
				if (!isContain && isModify) {
					// 若是 原本只有一項，則依然強制寫成100，即使她不是輸入的項目
					if (newData.size() == 1) {
						s.setPercentage(100);
					} else {
						s.setPercentage(0);
					}
				}
			}

		}

		for (AA0424SrcUrl s : newData) { // 百分比設定完，就替換URL瞜
			for (AA0424ReqSrcUrlList r : list) {
				String target = r.getSrcUrl(); // 目標url
				if (r.getIsReplace() && s.getSrcUrl().contains(target)) {
					String str = s.getSrcUrl().replace(target, r.getReplaceString());
					s.setSrcUrl(str);
				}
			}

		}

		return newData;
	}

	private void checkParm(AA0424Req req) {
		List<AA0424ReqAPIList> apiList = req.getApiList();
		if (CollectionUtils.isEmpty(apiList)) {
			throw TsmpDpAaRtnCode._1350.throwing("[{{apiList}}]");
		}
		int sum = 0;
		List<AA0424ReqSrcUrlList> srcUrlList = req.getSrcUrlList();
		if (CollectionUtils.isEmpty(srcUrlList)) {
			throw TsmpDpAaRtnCode._1350.throwing("[{{SrcUrlList}}]");
		}

		for (AA0424ReqSrcUrlList l : srcUrlList) {
			checkSrcUrl(l.getSrcUrl());
			sum += checkPercentage(l.getIsPercentage(), l.getPercentage(), sum);
			checkIsReplace(l.getIsReplace());
		}

		if (sum > 0 && sum < 100) {
			throw TsmpDpAaRtnCode._1528.throwing(String.valueOf(sum));
		}
		
	}

	private void checkIsReplace(Boolean isReplace) {
		if (isReplace == null)
			throw TsmpDpAaRtnCode._1350.throwing("[{{isReplace}}]");
		
	}

	private int checkPercentage(Boolean isPercentage, Integer percentage, int sum) {
		if (isPercentage == null)
			throw TsmpDpAaRtnCode._1350.throwing("[{{isPercentage}}]");
		if (isPercentage) {
			if (percentage == null) {
				throw TsmpDpAaRtnCode._1350.throwing("[{{percentage}}]");
			}
			sum += percentage;
			if (sum > 100) {
				throw TsmpDpAaRtnCode._1528.throwing(String.valueOf(sum));
			}

		}
		return sum;
	}

	private void checkSrcUrl(String srcUrl) {
		if (!StringUtils.hasLength(srcUrl)) {
			throw TsmpDpAaRtnCode._1350.throwing("[{{srcUrl}}]");
		}

	}

	public String getFileName() {
		Date now = DateTimeUtil.now();
		String dateTime = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(now);
		return String.format("modifyBatch_%s.json", dateTime);
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return tsmpApiRegDao;
	}
}
