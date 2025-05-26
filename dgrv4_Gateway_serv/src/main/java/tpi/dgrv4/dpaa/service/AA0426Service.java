package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.component.validator.ReqConstraintsCache;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0425RespIpAndSrcUrlList;
import tpi.dgrv4.dpaa.vo.AA0425RespItem;
import tpi.dgrv4.dpaa.vo.AA0425RespNewSrcUrlItem;
import tpi.dgrv4.dpaa.vo.AA0425RespPercentageAndSrcUrl;
import tpi.dgrv4.dpaa.vo.AA0426Req;
import tpi.dgrv4.dpaa.vo.AA0426Resp;
import tpi.dgrv4.dpaa.vo.AA0426RespItem;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0426Service {
	private TPILogger logger = TPILogger.tl;
	
	private ObjectMapper objectMapper;
	private FileHelper fileHelper;
	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpApiRegDao tsmpApiRegDao;

	@Autowired
	public AA0426Service(ObjectMapper objectMapper, FileHelper fileHelper, TsmpDpFileDao tsmpDpFileDao,
			TsmpApiDao tsmpApiDao, TsmpApiRegDao tsmpApiRegDao) {
		super();
		this.objectMapper = objectMapper;
		this.fileHelper = fileHelper;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiRegDao = tsmpApiRegDao;
	}

	public AA0426Resp batchModify(TsmpAuthorization authorization, AA0426Req req) {
		AA0426Resp resp = new AA0426Resp();
		try {

			String fileName = req.getTempFileName();
			if (!StringUtils.hasLength(fileName)) {
				throw TsmpDpAaRtnCode._1350.throwing("fileName");
			}
			String refId = req.getRefId();
			if (!StringUtils.hasLength(refId)) {
				throw TsmpDpAaRtnCode._1350.throwing("refId");
			}
			Long refIdL = Long.valueOf(refId);

			List<TsmpDpFile> fileList = getTsmpDpFileDao()
					.findByRefFileCateCodeAndRefIdAndFileName(TsmpDpFileType.API_MODIFY_BATCH.code(), refIdL, fileName);
			if (CollectionUtils.isEmpty(fileList)) {
				throw TsmpDpAaRtnCode.NO_FILE.throwing();
			}
			byte[] fileContent = null;
			try {
				fileContent = getFileHelper().download(fileList.get(0));
			} catch (Exception e) {
				this.logger.debug(String.format("File download error: %s", StackTraceUtil.logStackTrace(e)));
			}
			if (fileContent == null || fileContent.length == 0) {
				throw TsmpDpAaRtnCode._1233.throwing();
			}

			List<AA0425RespItem> list = getObjectMapper().readValue(fileContent,
					new TypeReference<List<AA0425RespItem>>() {
					});
			List<AA0426RespItem> errApiIds = new ArrayList<>();
		
			List<TsmpApi> apis = new ArrayList<>();
			List<TsmpApiReg> regs = new ArrayList<>();
			for (AA0425RespItem aa0425RespItem : list) {
				String apiKey = aa0425RespItem.getApiKey();
				String moduleName = aa0425RespItem.getModuleName();
				TsmpApi api = getTsmpApiDao().findByModuleNameAndApiKey(moduleName, apiKey);
				TsmpApiReg apireg = getTsmpApiRegDao().findByModuleNameAndApiKey(moduleName, apiKey);
				if (api == null || apireg == null) {
					errApiIds.add(new AA0426RespItem(apiKey, moduleName));

				} else {
					// 檢查有沒有 isScuess 是 false的
					Boolean isScuess = checkStatus(aa0425RespItem.getNewSrcUrlList());

					if (Boolean.FALSE.equals(isScuess)) {
						throw TsmpDpAaRtnCode._2026.throwing("Data is incorrect. Percentage is not equal to 100.");
					}
					
					// 更新TsmpApiReg
					apireg = setApiReg(aa0425RespItem, apireg, authorization);
					// 更新TsmpApi
					api = setApi(aa0425RespItem, api, apireg, authorization );

					regs.add(apireg);
					apis.add(api);
				}

			}
			getTsmpApiDao().saveAll(apis);
			getTsmpApiRegDao().saveAll(regs);

			if (errApiIds != null && errApiIds.size() > 0) {
				String result = errApiIds.stream().map((id) -> "[ " + id.getApiKey() + ", " + id.getModuleName() + " ]")
						.collect(Collectors.joining(", "));
				logger.error("Batch modify failed : API data missing. APIs: " + result);
				resp.setErrMsg("Batch modify failed : API data missing.");
				resp.setErrList(errApiIds);

			}

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.API.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	private Boolean checkStatus(List<AA0425RespNewSrcUrlItem> newSrcUrlList) {

		for (AA0425RespNewSrcUrlItem aa0425RespIpAndSrcUrlList : newSrcUrlList) {
			Boolean isScuess = aa0425RespIpAndSrcUrlList.getIsSuccess();
			if (Boolean.FALSE.equals(isScuess)) {
				return false;
			}
		}
		return true;
	}

	private TsmpApiReg setApiReg(AA0425RespItem aa0425RespItem, TsmpApiReg apireg, TsmpAuthorization authorization) {
		List<AA0425RespNewSrcUrlItem> newSrcList = aa0425RespItem.getNewSrcUrlList();
		Map<String, String> map = new HashMap<>();

		map.put(apireg.getIpForRedirect1(), apireg.getIpSrcUrl1());
		map.put(apireg.getIpForRedirect2(), apireg.getIpSrcUrl2());
		map.put(apireg.getIpForRedirect3(), apireg.getIpSrcUrl3());
		map.put(apireg.getIpForRedirect4(), apireg.getIpSrcUrl4());
		map.put(apireg.getIpForRedirect5(), apireg.getIpSrcUrl5());

		map.remove(null);

		if ("Y".equals(apireg.getRedirectByIp())) { // 依照分流flag判斷塞入的欄位
			for (AA0425RespNewSrcUrlItem aa0425RespIpAndSrcUrlList : newSrcList) {

				String ip = aa0425RespIpAndSrcUrlList.getIp();

				String src = srcUrlToBase64(aa0425RespIpAndSrcUrlList.getSrcUrlList());
				map.put(ip, src);
			}
		} else {
			apireg.setSrcUrl(srcUrlToBase64(newSrcList.get(0).getSrcUrlList()));
		}
		// 按照IP塞入新值
		apireg.setIpSrcUrl1(map.get(apireg.getIpForRedirect1()));
		apireg.setIpSrcUrl2(map.get(apireg.getIpForRedirect2()));
		apireg.setIpSrcUrl3(map.get(apireg.getIpForRedirect3()));
		apireg.setIpSrcUrl4(map.get(apireg.getIpForRedirect4()));
		apireg.setIpSrcUrl5(map.get(apireg.getIpForRedirect5()));
		
		apireg.setUpdateUser(authorization.getUserName());
		apireg.setUpdateTime(DateTimeUtil.now());
		return apireg;
	}

	private TsmpApi setApi(AA0425RespItem aa0425RespItem, TsmpApi api, TsmpApiReg apireg, TsmpAuthorization authorization) {
		api.setSrcUrl(apireg.getSrcUrl());
		api.setUpdateUser(authorization.getUserName());
		api.setUpdateTime(DateTimeUtil.now());
		return api;
	}

	/***
	 * 若是多條 要 b64.趴數.base64URL(URL).趴數2.base64URL(URL2) 一條就直接存明文
	 * 
	 * @param srcUrlList
	 * @return
	 */
	private String srcUrlToBase64(List<AA0425RespPercentageAndSrcUrl> srcUrlList) {
		String srcurl = "";
		if (srcUrlList.size() > 1) {
			srcurl = "";
			List<String> str = new ArrayList<>();
			for (AA0425RespPercentageAndSrcUrl aa0425RespPercentageAndSrcUrl : srcUrlList) {

				Integer percentage = aa0425RespPercentageAndSrcUrl.getPercentage();
				String src = aa0425RespPercentageAndSrcUrl.getSrcUrl();
				str.add(String.valueOf(percentage));
				str.add(Base64Util.base64URLEncode(src.getBytes()));
			}
			srcurl = "b64." + String.join(".", str);

		} else {
			srcurl = srcUrlList.get(0).getSrcUrl();
		}
		return srcurl;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return tsmpDpFileDao;
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
