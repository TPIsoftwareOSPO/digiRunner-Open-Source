package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.RegexpConstant;
import tpi.dgrv4.common.constant.SafeHttpMethod;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpPublicFlag;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.DgrProtocol;
import tpi.dgrv4.dpaa.component.job.NoticeClearCacheEventsJob;
import tpi.dgrv4.dpaa.constant.TsmpApiSrc;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0311Func;
import tpi.dgrv4.dpaa.vo.AA0311RedirectByIpData;
import tpi.dgrv4.dpaa.vo.AA0311Req;
import tpi.dgrv4.dpaa.vo.AA0311Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.DgrWebhookApiMap;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.jpql.TsmpRegModule;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.DgrWebhookApiMapDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.entity.repository.TsmpRegHostDao;
import tpi.dgrv4.entity.repository.TsmpRegModuleDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.CommForwardProcService;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Service
public class AA0311Service {

	private TPILogger logger = TPILogger.tl;

	private final BcryptParamHelper bcryptParamHelper;
	private final JobHelper jobHelper;
	private final TsmpUserDao tsmpUserDao;
	private final TsmpRegHostDao tsmpRegHostDao;
	private final TsmpRegModuleDao tsmpRegModuleDao;
	private final TsmpApiDao tsmpApiDao;
	private final TsmpApiRegDao tsmpApiRegDao;
	private final ObjectMapper objectMapper;
	private final ApplicationContext ctx;
	private final DgrAuditLogService dgrAuditLogService;
	private final TsmpSettingService tsmpSettingService;
	private final CommForwardProcService commForwardProcService;
	private final DgrAcIdpUserDao dgrAcIdpUserDao;
	private final DgrWebhookApiMapDao dgrWebhookApiMapDao;
	private final DgrWebhookNotifyDao dgrWebhookNotifyDao;
	
	@Transactional
	public AA0311Resp registerAPI(TsmpAuthorization auth, AA0311Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		if (TsmpApiSrc.COMPOSED.value().equals(req.getApiSrc())) {
			getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_COMPOSER_API.value());
			
		} else if(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc())){
			getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_REGISTER_API.value());
		}
		
		Integer type = null;
		if (TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc())){
			type = req.getType();
			if(type == null) {
				type = getTsmpSettingService().getVal_DGR_PATHS_COMPATIBILITY();
				req.setType(type);
			}
			
			if(type.intValue() != 0 && type.intValue() != 1) {
				logger.info("type = " + type);
				throw TsmpDpAaRtnCode._1297.throwing();
			}
		}
		
		String userName = auth.getUserName();
		String userNameForQuery = auth.getUserNameForQuery();
		String idPType = auth.getIdpType();
		String orgId = auth.getOrgId();
		String locale = ServiceUtil.getLocale(reqHeader.getLocale());

		checkParams(userName, orgId, req, locale, idPType, userNameForQuery);
		LinkedList<LinkedList<String[]>> srcUrlDataList = processingValAndCheck(req);
		LinkedList<String> resultSrcUrl = new LinkedList<>();
		srcUrlDataList.forEach(x-> resultSrcUrl.add(getResultSrcUrl(x)));
		try {
			// 新增 TSMP_API
			TsmpApi tsmpApi = saveTsmpApi(userName, orgId, req, iip, resultSrcUrl);
			
			// 新增 TSMP_API_REG
			String apiUUID = tsmpApi.getApiUid();
			saveTsmpApiReg(userName, orgId, req, apiUUID, iip, resultSrcUrl);
			
			clearAPICache();
			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.API.value());
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			if (ServiceUtil.isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			}
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return new AA0311Resp();
	}
	
	protected LinkedList<String[]> checkSrcUrl(String srcUrl) {
		boolean isThrowing = true;
		LinkedList<String[]> dataList = (LinkedList<String[]>) getCommForwardProcService().getSrcUrlToTargetUrlAndProbabilityList(srcUrl, isThrowing);
		return dataList;
	}
	
	protected LinkedList<LinkedList<String[]>> processingValAndCheck(AA0311Req req) {
		String srcUrl = req.getSrcUrl();
		LinkedList<String[]> srcUrlDataList = null;
		LinkedList<LinkedList<String[]>> redirectSrcUrlDataList = new LinkedList<>();
		Boolean redirectByIp = req.getRedirectByIp() ;
		if (redirectByIp == null) {
			redirectByIp = false;
		}
		if (!redirectByIp) {
			srcUrlDataList = checkSrcUrl(srcUrl);
			srcUrlDataList = processingVal(req, srcUrlDataList);
			redirectSrcUrlDataList.add(srcUrlDataList);
		} else {
			
			List<AA0311RedirectByIpData> redirectByIpDataList = req.getRedirectByIpDataList();
//			srcUrlDataList = checkSrcUrl(redirectByIpDataList.get(0).getIpSrcUrl());
//			srcUrlDataList = new LinkedList<>();
			for (AA0311RedirectByIpData aa0313RedirectByIpData : redirectByIpDataList) {
				if (aa0313RedirectByIpData != null) {
					srcUrlDataList = checkSrcUrl(aa0313RedirectByIpData.getIpSrcUrl());
					redirectSrcUrlDataList.add(processingVal(req, srcUrlDataList));
				}
			}
		}
		return redirectSrcUrlDataList;
	}
	
	protected LinkedList<String[]> processingVal(AA0311Req req, LinkedList<String[]> srcUrlDataList){
		//dgrc
				if (TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
					String apiName = req.getApiName();
					String moduleName = req.getModuleName();
					String apiId = req.getApiId();
					
					if(!StringUtils.hasText(apiName)) {
						throw TsmpDpAaRtnCode._1350.throwing("{{apiName}}");
					}
					
					if(apiName.length() > 255) {
						throw TsmpDpAaRtnCode._1351.throwing("{{apiName}}", "255", String.valueOf(apiName.length()));
					}
					
					//開頭要有/
					if(!"/".equals(moduleName.substring(0, 1))) {
						moduleName = "/" + moduleName;
					}
					
					//開頭要有/
					if(!"/".equals(apiId.substring(0, 1))) {
						apiId = "/" + apiId;
					}
					
					//結尾不要有/
					if(moduleName.length() > 1 && "/".equals(moduleName.substring(moduleName.length() - 1))) {
						moduleName = moduleName.substring(0, moduleName.length() - 1);
					}
					
					//結尾不要有/
					if(apiId.length() > 1 && "/".equals(apiId.substring(apiId.length() - 1))) {
						apiId = apiId.substring(0, apiId.length() - 1);
					}
					
					//取第1節
					String[] arrMoudleName = moduleName.split("/");
					if(arrMoudleName.length > 1) {
						moduleName = "/" + arrMoudleName[1];
					}
					
					//{x}轉{p}
					moduleName = ServiceUtil.replaceParameterToP(moduleName);
					apiId = ServiceUtil.replaceParameterToP(apiId);
					
					req.setModuleName(moduleName);
					req.setApiId(apiId);
					
					srcUrlDataList = checkAndReplaceSrcUrl(apiId, srcUrlDataList);
					
				}else {//tsmpc
					if(!ServiceUtil.checkDataByPattern(req.getModuleName(),"[a-zA-Z0-9_-]*")) {
						throw TsmpDpAaRtnCode._1352.throwing("{{moudleName}}");
					}
					
					if(!ServiceUtil.checkDataByPattern(req.getApiId(),"[a-zA-Z0-9_-]*")) {
						throw TsmpDpAaRtnCode._1352.throwing("{{apiId}}");
					}
				}
				
				if(req.getModuleName().length() > 50) {
					if (TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
						throw TsmpDpAaRtnCode._1351.throwing("{{proxyPath}}", "50", String.valueOf(req.getModuleName().length()));
					}else {
						throw TsmpDpAaRtnCode._1351.throwing("{{moduleName}}", "50", String.valueOf(req.getModuleName().length()));
					}
				}
				
				if(req.getApiId().length() > 255) {
					if (TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
						throw TsmpDpAaRtnCode._1351.throwing("{{proxyPath}}", "255", String.valueOf(req.getApiId().length()));
					}else {
						throw TsmpDpAaRtnCode._1351.throwing("{{apiId}}", "255", String.valueOf(req.getApiId().length()));
					}
				}
				
				checkRegModule(req.getModuleName());
				
				checkAPI(req.getModuleName(), req.getApiId());
				
				return srcUrlDataList;
	}
	
	/**
	 *  dgrc時,檢查目標URL
	 */
	protected LinkedList<String[]> checkAndReplaceSrcUrl(String apiId, LinkedList<String[]> srcUrlDataList) {
		for (String[] srcUrlData : srcUrlDataList) {
			String targetUrl = srcUrlData[1];// 目標URL
			
			// 結尾不要有/
//			int length = targetUrl.length();
//			if("/".equals(targetUrl.substring(length - 1))) {
//				targetUrl = targetUrl.substring(0, length - 1);
//			}
			
			// 將{X}轉成{p},呼叫時fromIndex固定帶0
			targetUrl = ServiceUtil.replaceParameterToP(targetUrl);
			
			// 檢查targetUrl的{p}和apiId的{p},個數是否一致
			checkNumberForP(apiId, targetUrl);
			srcUrlData[1] = targetUrl;
		}
		return srcUrlDataList;
	}
	
	/**
	 * dgrc時,檢查 targetUrl 的 {p} 和 apiId 的 {p}, 個數是否一致
	 */
	protected void checkNumberForP(String proxyPath, String targetUrl) {
		int proxyPathNum = this.appearNumber(proxyPath, "\\{p\\}");
		int targetUrlNum = this.appearNumber(targetUrl, "\\{p\\}");
		if(proxyPathNum != targetUrlNum) {
			throw TsmpDpAaRtnCode._1524.throwing(String.valueOf(proxyPathNum), String.valueOf(targetUrlNum));
		}
	}

	/**
	 * 取得要存入DB 的 src_url 值, <br>
	 * 1.若 targeUrl 只有1筆, srcUrl 為 targeUrl，省略機率 <br>
	 * 2.若 targeUrl 大於1筆, srcUrl 為 "b64." + probability 1 + "." + Base64URL Encode(targeUrl 1) + "." <br>
	 * + probability 2 + "." + Base64URL Encode(targeUrl 2) + ...... 依此類推
	 */
	protected String getResultSrcUrl(LinkedList<String[]> srcUrlDataList) {
		if (CollectionUtils.isEmpty(srcUrlDataList)) {
			return null;
		}

		// 1.若 targeUrl 只有1筆, srcUrl 為 targeUrl，省略機率
		if (srcUrlDataList.size() == 1 ) {

			String[] srcUrlData = srcUrlDataList.get(0);
				return srcUrlData[1];// 目標URL
		}

		// 2.若 targeUrl 大於1筆,
		// srcUrl 為 "b64." + probability 1 + "." + Base64URL Encode(targeUrl 1) + "."
		// + probability 2 + "." + Base64URL Encode(targeUrl 2) + ...... 依此類推
		String srcUrl = "";
		for (String[] srcUrlData : srcUrlDataList) {
			String probability = srcUrlData[0];// 機率
			String targetUrl = srcUrlData[1];// 目標URL
			String targetUrlEnc = Base64Util.base64URLEncode(targetUrl.getBytes());
			if (StringUtils.hasLength(srcUrl)) {
				srcUrl += ".";
			}
			srcUrl += probability + "." + targetUrlEnc;
		}

		if (StringUtils.hasLength(srcUrl)) {
			srcUrl = "b64." + srcUrl;// 為"b64."開頭
		}

		return srcUrl;
	}
	
	private int appearNumber(String srcText, String findText) { 
		  int count = 0; 
		  Pattern p = Pattern.compile(findText); 
		  Matcher m = p.matcher(srcText); 
		  while (m.find()) { 
		    count++; 
		  } 
		  return count; 
	} 

	protected void checkParams(String userName, String orgId, AA0311Req req, String locale, String idPType,
			String userNameForQuery) {
		if (!StringUtils.hasText(req.getModuleName())) {
			if (TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
				throw TsmpDpAaRtnCode._1350.throwing("{{proxyPath}}");
			} else {
				throw TsmpDpAaRtnCode._1350.throwing("{{moduleName}}");
			}
		}

		if (!StringUtils.hasText(req.getApiId())) {
			if (TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
				throw TsmpDpAaRtnCode._1350.throwing("{{proxyPath}}");
			} else {
				throw TsmpDpAaRtnCode._1350.throwing("{{apiId}}");
			}
		}

		if (!StringUtils.hasLength(userName)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}

		if (!StringUtils.hasLength(orgId)) {
			throw TsmpDpAaRtnCode._1273.throwing();
		}

		Boolean urlRID = req.getUrlRID();
		if (urlRID == null) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		Boolean noOAuth = req.getNoOAuth();
		if (noOAuth == null) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		AA0311Func funFlag = req.getFunFlag();
		if ((funFlag == null) || (funFlag.getTokenPayload() == null)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		List<String> aa0311_methods = req.getMethods();
		if (CollectionUtils.isEmpty(aa0311_methods)) {
			throw TsmpDpAaRtnCode._1350.throwing("{{methods}}");
		}

		Set<String> aa0311_set = new HashSet<>();
		for (String method : aa0311_methods) {
			try{
				if (!aa0311_set.contains(method) && (SafeHttpMethod.safeValueOf(method, true) != null)) {
					aa0311_set.add(method);
				}else {
					this.logger.debug("Invalid methods: " + aa0311_methods);
					throw TsmpDpAaRtnCode._1290.throwing();
				}
			}catch (IllegalArgumentException e){
				this.logger.debug("Invalid methods: " + aa0311_methods);
				throw TsmpDpAaRtnCode._1290.throwing();
			}
		}

		List<String> consumes = checkRepeatValues("consumes", req.getConsumes());
		req.setConsumes(consumes);

		List<String> produces = checkRepeatValues("produces", req.getProduces());
		req.setProduces(produces);

		List<String> headers = checkRepeatValues("headers", req.getHeaders());
		req.setHeaders(headers);

		List<String> params = checkRepeatValues("params", req.getParams());
		req.setParams(params);

		checkUserExists(userNameForQuery, idPType);

		checkBcryptParams(req, locale);

		Boolean isRedirectByIp = req.getRedirectByIp();
		if (TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc())) {
			if (isRedirectByIp == null) {
				throw TsmpDpAaRtnCode._1350.throwing("{{redirectByIp}}");
			}

			if (isRedirectByIp) {
				List<AA0311RedirectByIpData> redirectByIpDataList = req.getRedirectByIpDataList();
				if (redirectByIpDataList == null || redirectByIpDataList.size() == 0) {
					throw TsmpDpAaRtnCode._1350.throwing("{{redircetByIpDataList}}");
				}
				if (redirectByIpDataList.size() > 5) {
					throw TsmpDpAaRtnCode._1407.throwing("{{redirectByIpDataList}}", "5",
							String.valueOf(redirectByIpDataList.size()));
				}
				int i= 0;
				if (redirectByIpDataList.size()>0) {
					for (AA0311RedirectByIpData aa0311RedirectByIpData : redirectByIpDataList) {
						String ip = aa0311RedirectByIpData.getIpForRedirect();
						if (!StringUtils.hasLength(ip)) {
							throw TsmpDpAaRtnCode._1350.throwing("{{ipForRedirect}}");
						}
						if (!ip.matches(RegexpConstant.IP_CIDR_FQDN)){
							throw  TsmpDpAaRtnCode._1352.throwing("{{ipForRedirect}}");
						}
						if ((ip.contains("0.0.0.0/0") || ip.contains("::/0")) && i != redirectByIpDataList.size() - 1) {
							throw TsmpDpAaRtnCode._1559.throwing("If you want to allow all IPs, please set it as the last item.");
						}

						if (!StringUtils.hasLength(aa0311RedirectByIpData.getIpSrcUrl())) {
							throw TsmpDpAaRtnCode._1350.throwing("{{ipSrcUrl}}");
						}
						i++;
					}
				}
			}

			String bodyMaskPolicy = req.getBodyMaskPolicy();
			if (!StringUtils.hasLength(bodyMaskPolicy)) {
				throw TsmpDpAaRtnCode._1350.throwing("{{bodyMaskPolicy}}");
			}
			
			if (!bodyMaskPolicy.equals("0")) {
				if (!StringUtils.hasLength(req.getBodyMaskKeyword())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{bodyMaskKeyword}}");
				}
				if (!StringUtils.hasLength(req.getBodyMaskPolicySymbol())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{bodyMaskPolicySymbol}}");
				}
				if (req.getBodyMaskPolicySymbol().length() > 10) {
					throw TsmpDpAaRtnCode._1351.throwing("{{bodyMaskPolicySymbol}}", String.valueOf(10),
							String.valueOf(req.getBodyMaskPolicySymbol().length()));
				}

				if (req.getBodyMaskPolicyNum() <= 0) {
					throw TsmpDpAaRtnCode._1355.throwing("{{bodyMaskPolicyNum}}", String.valueOf(1),
							String.valueOf(req.getBodyMaskPolicyNum()));
				}
				if (req.getBodyMaskPolicyNum() > 9999) {
					throw TsmpDpAaRtnCode._1355.throwing("{{bodyMaskPolicyNum}}", String.valueOf(9999),
							String.valueOf(req.getBodyMaskPolicyNum()));
				}
			}
			String headerMaskPolicy = req.getHeaderMaskPolicy();
			if (!StringUtils.hasLength(headerMaskPolicy)) {
				throw TsmpDpAaRtnCode._1350.throwing("{{headerMaskPolicy}}");
			}

			if (!headerMaskPolicy.equals("0")) {
				if (!StringUtils.hasLength(req.getHeaderMaskKey())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{headerMaskKey}}");
				}
				if (!StringUtils.hasLength(req.getHeaderMaskPolicySymbol())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{headerMaskPolicySymbol}}");
				}
				if (req.getHeaderMaskPolicySymbol().length() > 10) {
					throw TsmpDpAaRtnCode._1351.throwing("{{headerMaskPolicySymbol}}", String.valueOf(10),
							String.valueOf(req.getHeaderMaskPolicySymbol().length()));
				}
				if (req.getHeaderMaskPolicyNum() <= 0) {
					throw TsmpDpAaRtnCode._1355.throwing("{{headerMaskPolicyNum}}", String.valueOf(1),
							String.valueOf(req.getHeaderMaskPolicyNum()));
				}
				if (req.getHeaderMaskPolicyNum() > 9999) {
					throw TsmpDpAaRtnCode._1355.throwing("{{headerMaskPolicyNum}}", String.valueOf(9999),
							String.valueOf(req.getHeaderMaskPolicyNum()));
				}
			}
		}
		
		if (TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
			// 當為 註冊 且 dgrc 時, 必須有值
			// 失敗判定策略
			if (!StringUtils.hasLength(req.getFailDiscoveryPolicy())) {
				throw TsmpDpAaRtnCode._1350.throwing("{{failDiscoveryPolicy}}");
			}

			// 失敗處置策略
			if (!StringUtils.hasLength(req.getFailHandlePolicy())) {
				throw TsmpDpAaRtnCode._1350.throwing("{{failHandlePolicy}}");
			}
		}
		
		List<String> labelList = req.getLabelList();
		if (labelList != null) {
			if (labelList.size() > 5)
				throw TsmpDpAaRtnCode._1407.throwing("{{labelList}}", "5", String.valueOf(labelList.size()));
			if (labelList.size() > 0) {
				labelList.forEach(l -> {
					if (l.length() > 20) {
						throw TsmpDpAaRtnCode._1351.throwing("{{labelList}}", "20", String.valueOf(l.length()));
					}
				});
			}
		}
	}

	protected void checkRegModule(String moduleName) {
		List<TsmpRegModule> mList = getTsmpRegModuleDao().findByModuleName(moduleName);
		if (!CollectionUtils.isEmpty(mList)) {
			throw TsmpDpAaRtnCode._1426.throwing();
		}
	}

	protected void checkUserExists(String userNameForQuery, String idPType) {
		if (StringUtils.hasLength(idPType)) {// 以 IdP 登入 AC
			DgrAcIdpUser dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(userNameForQuery, idPType);
			if (dgrAcIdpUser == null) {
				//Table 查不到 user
				TPILogger.tl.debug("Table [DGR_AC_IDP_USER] can not find user, user_name: " + userNameForQuery + ", idp_type: " + idPType);
				throw TsmpDpAaRtnCode._1231.throwing();
			}
			
		} else {//以 AC 登入
			TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userNameForQuery);
			//Table 查不到 user
			TPILogger.tl.debug("Table [TSMP_USER] can not find user, user_name: " + userNameForQuery);
			if (tsmpUser == null) {
				throw TsmpDpAaRtnCode._1231.throwing();
			}
		}
	}

	protected void checkBcryptParams(AA0311Req req, String locale) {
		String dataFormat = req.getDataFormat();
		String jweFlag = req.getJweFlag();
		String jweFlagResp = req.getJweFlagResp();
		
		if (!StringUtils.hasLength(dataFormat) || !StringUtils.hasLength(jweFlag) || !StringUtils.hasLength(jweFlagResp)) {
			this.logger.debug(String.format("dataFormat: %s, jweFlag: %s, jweFlagResp: %s", dataFormat, jweFlag, jweFlagResp));
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		try {
			dataFormat = getBcryptParamHelper().decode(dataFormat, "API_DATA_FORMAT", locale);
			jweFlag =  getBcryptParamHelper().decode(jweFlag, "API_JWT_FLAG", locale);
			jweFlagResp =  getBcryptParamHelper().decode(jweFlagResp, "API_JWT_FLAG", locale);

			req.setDataFormat(dataFormat);
			req.setJweFlag(jweFlag);
			req.setJweFlagResp(jweFlagResp);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
	}

	protected List<String> checkRepeatValues(String fieldName, List<String> values) {
		Set<String> aa0311_set = new HashSet<>();
		if (!CollectionUtils.isEmpty(values)) {
			values.forEach((val) -> {
				if (!aa0311_set.contains(val)) {
					aa0311_set.add(val);
				} else {
					throw TsmpDpAaRtnCode._1284.throwing("{{" + fieldName + "}}");
				}
			});
		}
		return new ArrayList<>(aa0311_set);
	}

	protected void checkAPI(String moduleName, String apiKey) {
		// 找出已存在的 TSMP_API
		TsmpApiId tsmpApiId = new TsmpApiId(apiKey, moduleName);
		Optional<TsmpApi> opt_api = getTsmpApiDao().findById(tsmpApiId);
		if (opt_api.isPresent()) {
			throw TsmpDpAaRtnCode._1353.throwing("API", String.format("%s-%s", moduleName, apiKey));
		}
		
		// 找出已存在的 TSMP_API_REG
		TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiKey, moduleName);
		Optional<TsmpApiReg> opt_reg = getTsmpApiRegDao().findById(tsmpApiRegId);
		if (opt_reg.isPresent()) {
			throw TsmpDpAaRtnCode._1353.throwing("API", String.format("%s-%s", moduleName, apiKey));
		}
	}

	protected TsmpApi saveTsmpApi(String userName, String orgId, AA0311Req req, InnerInvokeParam iip, LinkedList<String> resultSrcUrl) {
		TsmpApi tsmpApi = new TsmpApi();
		tsmpApi.setApiKey(req.getApiId());
		tsmpApi.setModuleName(req.getModuleName());
		if (TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
			tsmpApi.setApiName(req.getApiName());
		}else {
			tsmpApi.setApiName(req.getApiId());
		}
		tsmpApi.setApiStatus("2");	// Disabled
		tsmpApi.setApiSrc(req.getApiSrc());
		tsmpApi.setApiDesc(req.getApiDesc());
		tsmpApi.setCreateTime(DateTimeUtil.now());
		tsmpApi.setCreateUser(userName);
		tsmpApi.setOrgId(orgId);
		tsmpApi.setPublicFlag(TsmpDpPublicFlag.PRIVATE.value());
		String protocol = req.getProtocol();
		Boolean isRedirectByIp = req.getRedirectByIp();
		if (isRedirectByIp == null) {
			isRedirectByIp = false;
		}
		if (!isRedirectByIp) {
			String srcUrl = resultSrcUrl.get(0);
			if (StringUtils.hasLength(protocol)) {
				// 若srcUrl值不是"b64.", 也不是"http"開頭, 才需要加協定
				int index = srcUrl.indexOf("b64.");
				int index2 = srcUrl.indexOf("http");
				var dgrProto = DgrProtocol.parse(srcUrl);
				if (index != 0 && index2 != 0 && !dgrProto.valid()) {
					// not "b64.", not "http", not dgr protocol
					srcUrl = String.format("%s://%s", protocol,srcUrl );
				}
			}

			tsmpApi.setSrcUrl(srcUrl);
		}
	
		tsmpApi.setApiUid(UUID.randomUUID().toString().toUpperCase());
		tsmpApi.setDataFormat(req.getDataFormat());
		tsmpApi.setJewFlag(req.getJweFlag());
		tsmpApi.setJewFlagResp(req.getJweFlagResp());
		
		List<String> labList = req.getLabelList();
		String[] label = new String[5];

		if (labList != null && labList.size() > 0) {
			for (int i = 0; i < labList.size(); i++) {
				label[i] = (labList.get(i).toLowerCase());

			}
		}
		tsmpApi.setLabel1(label[0]);
		tsmpApi.setLabel2(label[1]);
		tsmpApi.setLabel3(label[2]);
		tsmpApi.setLabel4(label[3]);
		tsmpApi.setLabel5(label[4]);

		tsmpApi = getTsmpApiDao().saveAndFlush(tsmpApi);
		
		if (TsmpApiSrc.COMPOSED.value().equals(req.getApiSrc()) 
				|| TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc())) {
			//寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					TsmpApi.class.getSimpleName(), TableAct.C.value(), null, tsmpApi);
		}
		
		return tsmpApi;
	}

	protected TsmpApiReg saveTsmpApiReg(String userName, String orgId, AA0311Req req, //
			String apiUUID, InnerInvokeParam iip, LinkedList<String> resultSrcUrl) {
		TsmpApiReg tsmpApiReg = new TsmpApiReg();
		tsmpApiReg.setApiKey(req.getApiId());
		String moduleName = req.getModuleName();
		tsmpApiReg.setModuleName(moduleName);
		String protocol = req.getProtocol();
		Boolean isRedirectByIp = req.getRedirectByIp();
		if (isRedirectByIp == null) {
			isRedirectByIp = false;
		}
		if (!isRedirectByIp) {
			String srcUrl = resultSrcUrl.get(0);
			if (StringUtils.hasLength(protocol)) {
				// 若srcUrl值不是"b64.", 也不是"http"開頭, 才需要加協定
				int index = srcUrl.indexOf("b64.");
				int index2 = srcUrl.indexOf("http");
				var dgrProto = DgrProtocol.parse(srcUrl);
				if (index != 0 && index2 != 0 && !dgrProto.valid()) {
					// not "b64.", not "http", not dgr protocol
					srcUrl = String.format("%s://%s", protocol, srcUrl);

				}
			}
			tsmpApiReg.setSrcUrl(srcUrl);
		}
	
		tsmpApiReg.setUrlRid(req.getUrlRID() ? "1" : "0");
		String regStatus = null;
		if (TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc())) {
			regStatus = "1";
			apiUUID = null;
		} else if (TsmpApiSrc.COMPOSED.value().equals(req.getApiSrc())) {
			regStatus = "0";
		}
		tsmpApiReg.setRegStatus(regStatus);
		tsmpApiReg.setApiUuid(apiUUID);
		if (TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
			tsmpApiReg.setPathOfJson("[" + req.getApiId() + "]");
		} else {
			tsmpApiReg.setPathOfJson("[/" + moduleName + "/" + req.getApiId() + "]");
		}

		try {
			tsmpApiReg.setMethodOfJson(getObjectMapper().writeValueAsString(req.getMethods()));
			tsmpApiReg.setParamsOfJson(getObjectMapper().writeValueAsString(req.getParams()));
			tsmpApiReg.setHeadersOfJson(getObjectMapper().writeValueAsString(req.getHeaders()));
			tsmpApiReg.setConsumesOfJson(getObjectMapper().writeValueAsString(req.getConsumes()));
			tsmpApiReg.setProducesOfJson(getObjectMapper().writeValueAsString(req.getProduces()));
		} catch (Exception e) {
			this.logger.debug(String.format("Convert json error: %s", StackTraceUtil.logStackTrace(e)));
		}
		
		tsmpApiReg.setCreateTime(DateTimeUtil.now());
		tsmpApiReg.setCreateUser(userName);
		tsmpApiReg.setNoOauth(req.getNoOAuth() ? "1" : "0");
		tsmpApiReg.setFunFlag(req.getFunFlag().convert());
		if (TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc())) {
		
			tsmpApiReg.setRedirectByIp(isRedirectByIp ? "Y" : "N");
			if (isRedirectByIp) {
				List<AA0311RedirectByIpData> redirectByIpDataList = req.getRedirectByIpDataList();

				AA0311RedirectByIpData ipList1 = redirectByIpDataList.get(0);
				if (ipList1 != null) {
					tsmpApiReg.setIpForRedirect1(ipList1.getIpForRedirect());
					tsmpApiReg.setIpSrcUrl1(resultSrcUrl.get(0));
				}
				if (redirectByIpDataList.size() > 1) {
					AA0311RedirectByIpData ipList2 = redirectByIpDataList.get(1);
					if (ipList2 != null) {
						tsmpApiReg.setIpForRedirect2(ipList2.getIpForRedirect());
						tsmpApiReg.setIpSrcUrl2(resultSrcUrl.get(1));
					}
				}
				if (redirectByIpDataList.size() > 2) {
					AA0311RedirectByIpData ipList3 = redirectByIpDataList.get(2);
					if (ipList3 != null) {
						tsmpApiReg.setIpForRedirect3(ipList3.getIpForRedirect());
						tsmpApiReg.setIpSrcUrl3(resultSrcUrl.get(2));
					}
				}
				if (redirectByIpDataList.size() > 3) {
					AA0311RedirectByIpData ipList4 = redirectByIpDataList.get(3);
					if (ipList4 != null) {
						tsmpApiReg.setIpForRedirect4(ipList4.getIpForRedirect());
						tsmpApiReg.setIpSrcUrl4(resultSrcUrl.get(3));
					}
				}
				if (redirectByIpDataList.size() > 4) {
					AA0311RedirectByIpData ipList5 = redirectByIpDataList.get(4);
					if (ipList5 != null) {
						tsmpApiReg.setIpForRedirect5(ipList5.getIpForRedirect());
						tsmpApiReg.setIpSrcUrl5(resultSrcUrl.get(4));
					}
				}
			}
			
			String headerMaskPolicy = req.getHeaderMaskPolicy();
			tsmpApiReg.setHeaderMaskPolicy(headerMaskPolicy);
			tsmpApiReg.setHeaderMaskKey(req.getHeaderMaskKey());
			tsmpApiReg.setHeaderMaskPolicyNum(req.getHeaderMaskPolicyNum());
			tsmpApiReg.setHeaderMaskPolicySymbol(req.getHeaderMaskPolicySymbol());

			tsmpApiReg.setBodyMaskKeyword(req.getBodyMaskKeyword());
			tsmpApiReg.setBodyMaskPolicy(req.getBodyMaskPolicy());
			tsmpApiReg.setBodyMaskPolicyNum(req.getBodyMaskPolicyNum());
			tsmpApiReg.setBodyMaskPolicySymbol(req.getBodyMaskPolicySymbol());
			
		} else {
			tsmpApiReg.setRedirectByIp("N");
			tsmpApiReg.setHeaderMaskPolicy("0");
			tsmpApiReg.setBodyMaskPolicy("0");
		}
		
		// 失敗判定策略 & 失敗處置策略
		String failDiscoveryPolicy = req.getFailDiscoveryPolicy();
		String failHandlePolicy = req.getFailHandlePolicy();
		tsmpApiReg.setFailDiscoveryPolicy(nvl(failDiscoveryPolicy, "0"));
		tsmpApiReg.setFailHandlePolicy(nvl(failHandlePolicy, "0"));
		
		tsmpApiReg = getTsmpApiRegDao().saveAndFlush(tsmpApiReg);
		
		// 處理API webhook通知Map
		// Process API webhook notification mapping
		Optional.ofNullable(req.getNotifyNameList()).ifPresent(lst -> {
		    for (String notifyName : lst) {
		    	var n = getDgrWebhookNotifyDao().findFirstByNotifyName(notifyName);
		    	if(n.isPresent()) {
		    		DgrWebhookApiMap m = new DgrWebhookApiMap();
			    	m.setApiKey(req.getApiId());
			    	m.setModuleName(req.getModuleName());
			    	m.setWebhookNotifyId(n.get().getWebhookNotifyId());
			    	m.setCreateDateTime(DateTimeUtil.now());
			    	m.setCreateUser(userName);
			    	getDgrWebhookApiMapDao().save(m);
		    	}
		    }
		});
		
		if (TsmpApiSrc.COMPOSED.value().equals(req.getApiSrc())
				|| TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc())) {
			// 寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, TsmpApiReg.class.getSimpleName(),
					TableAct.C.value(), null, tsmpApiReg);
		}

		return tsmpApiReg;
	}

	protected void clearAPICache() {
		NoticeClearCacheEventsJob job = getNoticeClearCacheEventsJob("TSMP_API", "TSMP_API_REG");
		getJobHelper().add(job);
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

	protected TsmpRegHostDao getTsmpRegHostDao() {
		return this.tsmpRegHostDao;
	}

	protected TsmpRegModuleDao getTsmpRegModuleDao() {
		return this.tsmpRegModuleDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return this.tsmpApiRegDao;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}
	
	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected NoticeClearCacheEventsJob getNoticeClearCacheEventsJob(String tableName1, String tableName2) {
		Integer aa0311_action = 2;
		List<String> aa0311_tableNameList = new ArrayList<>();
		aa0311_tableNameList.add(tableName1);
		aa0311_tableNameList.add(tableName2);
		return (NoticeClearCacheEventsJob) getCtx().getBean("noticeClearCacheEventsJob", aa0311_action, null, aa0311_tableNameList);
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}
	
	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}
}