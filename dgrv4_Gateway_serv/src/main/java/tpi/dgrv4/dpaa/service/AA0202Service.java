package tpi.dgrv4.dpaa.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0202List;
import tpi.dgrv4.dpaa.vo.AA0202Req;
import tpi.dgrv4.dpaa.vo.AA0202Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AA0202Service {
	
	private TPILogger logger = TPILogger.tl;

	private TsmpClientDao tsmpClientDao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	private TsmpSecurityLevelDao securityLVDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private TsmpSettingDao tsmpSettingDao;
	private BcryptParamHelper bcryptParamHelper;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public AA0202Service(TsmpClientDao tsmpClientDao, TsmpDpClientextDao tsmpDpClientextDao,
			TsmpSecurityLevelDao securityLVDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			TsmpSettingDao tsmpSettingDao, BcryptParamHelper bcryptParamHelper, ServiceConfig serviceConfig) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.securityLVDao = securityLVDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpSettingDao = tsmpSettingDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.serviceConfig = serviceConfig;
	}

	public AA0202Resp queryClientList(AA0202Req req, ReqHeader reqHeader) {
		AA0202Resp resp = null;

		try {
			String clientId = req.getClientId();
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			String groupID = req.getGroupID();
			String encodeStatus = req.getEncodeStatus();

			if (StringUtils.isEmpty(encodeStatus)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			String local = ServiceUtil.getLocale(reqHeader.getLocale());

			//解密成功後，再找Param1的值
			String status = getStatusByBcryptParamHelper(encodeStatus, local);
			status = getItemsParam1("ENABLE_FLAG", status, local);
			
			resp = queryClientList(clientId, words, groupID, status, local);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private AA0202Resp queryClientList(String clientId, String[] words, String groupID, String status, String locale) {

		List<TsmpClient> dataList = getTsmpClientDao().findByClientIdAndKeywordAndGroupIdAndStatus(clientId, words,
				groupID, status, getPageSize());

		if (dataList == null || dataList.size() == 0) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		List<AA0202List> clientInfoList = getClientInfoList(dataList, locale);

		AA0202Resp resp = new AA0202Resp();
		resp.setClientInfoList(clientInfoList);

		return resp;
	}

	private List<AA0202List> getClientInfoList(List<TsmpClient> dataList, String locale) {

		List<AA0202List> aa0202List = new ArrayList<AA0202List>();

//		int dp = getDp();

		dataList.forEach(data -> {

			AA0202List aa0202 = new AA0202List();
			aa0202.setClientAlias(data.getClientAlias());// 用戶端帳號
			aa0202.setClientId(data.getClientId());// 用戶端代號
			aa0202.setClientName(data.getClientName());// 用戶端名稱
			aa0202.setSecurityLevelId(data.getSecurityLevelId());// 安全等級Id
			
			//parser
			if (data.getSecurityLevelId()!=null) {
				Optional<TsmpSecurityLevel> securityLV = getSecurityLVDao().findById(data.getSecurityLevelId());
				if (securityLV.isPresent()) {
					aa0202.setSecurityLevelName(securityLV.get().getSecurityLevelName());// 安全等級名稱
				}	
			}

			//parser
//			if (dp == 0) {
//				Optional<TsmpDpClientext> tsmpDpClientext = getTsmpDpClientextDao().findById(data.getClientId());
//				if (tsmpDpClientext.isPresent()) {
//					aa0202.setPublicFlag(tsmpDpClientext.get().getPublicFlag());// 入口網開放狀態代碼
//				}
//			} else {
//				aa0202.setPublicFlag("2");// 入口網開放狀態代碼
//			}
			
			//parser			
			Optional<TsmpDpClientext> tsmpDpClientext = getTsmpDpClientextDao().findById(data.getClientId());
			TsmpDpClientext tsmpDpClientext1 = new TsmpDpClientext();
			tsmpDpClientext1.setPublicFlag("2");
			aa0202.setPublicFlag(tsmpDpClientext.orElse(tsmpDpClientext1).getPublicFlag());	

			

			aa0202.setPublicFlagName(getSubitemName("API_AUTHORITY", aa0202.getPublicFlag(), locale));
			aa0202.setStatus(data.getClientStatus());// 狀態代碼
			aa0202.setStatusName(getSubitemNameByParam1("ENABLE_FLAG", data.getClientStatus(), locale));// 狀態名稱

			aa0202List.add(aa0202);

		});

		return aa0202List;
	}

	protected String getStatusByBcryptParamHelper(String encodeStatus, String locale) {
		String status = null;
		try {
			status = getBcryptParamHelper().decode(encodeStatus, "ENABLE_FLAG", locale);// BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return status;
	}

	private String getSubitemName(String itemNo, String subitemNo, String locale) {
		
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findById(id);
		
		if (vo == null) {
			return null;
		}
		
		String subitemName = vo.getSubitemName();
		return subitemName;
	}
	
	private String getSubitemNameByParam1(String itemNo, String param1, String locale) {
		TsmpDpItems n = getTsmpDpItemsCacheProxy().findByItemNoAndParam1AndLocale(itemNo, param1, locale);
		if (n==null) {
			return null;
		}
		String subitemName = n.getSubitemName();
		return subitemName;
	}
	
	private String getItemsParam1(String itemNo, String subitemNo, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findById(id);
		
		if (vo == null) {
			return null;
		}
		
		String param1 = vo.getParam1();
		return param1;
	}

	private int getDp() {
		int aa0202_dp = 0;
		Optional<TsmpSetting> aa0202_settingent = getTsmpSettingDao().findById(TsmpSettingDao.Key.TSMP_AC_CONF);
		if (aa0202_settingent.isPresent()) {
			String aa0202_settingValue = aa0202_settingent.get().getValue();
			try {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode root = mapper.readTree(aa0202_settingValue);
				JsonNode dpNode = root.findValue("dp");	
				aa0202_dp = Integer.parseInt(dpNode.asText());
			}  catch (IOException aa0202_e) {
				this.logger.error(StackTraceUtil.logStackTrace(aa0202_e));
				throw TsmpDpAaRtnCode._1297.throwing();
			}
		}
		return aa0202_dp;
	}

	public BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0202");
		return this.pageSize;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return tsmpDpClientextDao;
	}

	protected TsmpSecurityLevelDao getSecurityLVDao() {
		return securityLVDao;
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}
	
	

}
