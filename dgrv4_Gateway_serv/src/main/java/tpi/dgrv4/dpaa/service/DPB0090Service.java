package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0090Req;
import tpi.dgrv4.dpaa.vo.DPB0090Resp;
import tpi.dgrv4.dpaa.vo.DPB0090RespItem;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.OpenApiKeyService;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0090Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpClientDao tsmpClientDao;
	private TsmpOpenApiKeyDao tsmpOpenApiKeyDao; 
	private ServiceConfig serviceConfig;
	private BcryptParamHelper bcryptParamHelper;
	
	private Integer pageSize;

	@Autowired
	public DPB0090Service(TsmpClientDao tsmpClientDao, TsmpOpenApiKeyDao tsmpOpenApiKeyDao, ServiceConfig serviceConfig,
			BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpOpenApiKeyDao = tsmpOpenApiKeyDao;
		this.serviceConfig = serviceConfig;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	public DPB0090Resp queryOpenApiKeyByClientId(TsmpAuthorization tsmpAuthorization, DPB0090Req req, ReqHeader reqHeader) {
		DPB0090Resp resp = new DPB0090Resp();

		try {
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			String fbTypeEncode = req.getFbTypeEncode();//前後台分類	ex:FRONT , 使用BcryptParam設計, itemNo="FB_FLAG"
			String clientId = null;
			if(fbTypeEncode != null) {// 為前台
				String fbType = getDecode(fbTypeEncode, "FB_FLAG", locale);//解碼
				clientId = tsmpAuthorization.getClientId();//前台: from Token
			}else {// 為後台
				clientId = req.getClientId();//後台: 挑選的 Client Id
			}
			
			//chk param
			if(StringUtils.isEmpty(clientId)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			Optional<TsmpClient> opt = getTsmpClientDao().findById(clientId);
			if (!opt.isPresent()) {
				this.logger.error("用戶不存在: " + clientId);
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			
			TsmpOpenApiKey lastRecord = getLastRecordFromPrevPage(req.getOpenApiKeyId());
			
			List<TsmpOpenApiKey> openApiKeyList = getTsmpOpenApiKeyDao().queryOpenApiKeyByClientId(lastRecord, clientId, getPageSize());
			if (openApiKeyList == null || openApiKeyList.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
 
			List<DPB0090RespItem> itemList = getItemList(openApiKeyList, locale);
			
			resp.setDataList(itemList);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	protected String getDecode(String encode, String itemNo, String locale) {
		String decode = null;
		try {
			decode = getBcryptParamHelper().decode(encode, itemNo, locale);//BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return decode;
	}
	
	private TsmpOpenApiKey getLastRecordFromPrevPage(Long id) {
		if (id != null) {
			Optional<TsmpOpenApiKey> opt = getTsmpOpenApiKeyDao().findById(id);
			return opt.orElseThrow(() -> {return TsmpDpAaRtnCode._1297.throwing();});
		}
		return null;
	}
	
	private List<DPB0090RespItem> getItemList(List<TsmpOpenApiKey> openApiKeyList, String locale) {
		List<DPB0090RespItem> dataList = new ArrayList<DPB0090RespItem>();
		for (TsmpOpenApiKey openApiKey : openApiKeyList) {
			DPB0090RespItem data = getItem(openApiKey, locale);
			dataList.add(data);
		}
		return dataList;
	}
	
	private DPB0090RespItem getItem(TsmpOpenApiKey openApiKey, String locale) {
		DPB0090RespItem item = new DPB0090RespItem();
		item.setOpenApiKeyId(openApiKey.getOpenApiKeyId());
		item.setClientId(openApiKey.getClientId());
		item.setOpenApiKey(openApiKey.getOpenApiKey());
		item.setOpenApiKeyAlias(openApiKey.getOpenApiKeyAlias());
		
		Optional<String> opt_date = DateTimeUtil.dateTimeToString(openApiKey.getCreateDateTime(),
				DateTimeFormatEnum.西元年月日_2);// yyyy/MM/dd
		if (!opt_date.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}		
		item.setCreateDateTime(opt_date.get());
		
		String expiredAtStr = getDateTime(openApiKey.getExpiredAt());
		item.setExpiredAt(expiredAtStr);// 格式: yyyy/MM/dd
		item.setRevokedAt(getDateTime(openApiKey.getRevokedAt()));// 格式: yyyy/MM/dd
		
		String status = openApiKey.getOpenApiKeyStatus();// 狀態,為 0 或 1
		item.setOpenApiKeyStatus(status);
		
		String rolloverFlag = openApiKey.getRolloverFlag();
		String statusName = OpenApiKeyService.getStatusName(expiredAtStr, status, rolloverFlag, locale);
		
		item.setOpenApiKeyStatusName(statusName);
		
		// [歷程]:每個 case 都 = true
		item.setQueryVisiable("Y");
		
		// [異動申請]:按鈕是否顯示, Open Api Key 狀態為啟用 & 不為已展期時, 回傳 = true
		item.setUpdateVisiable("N");
		if("1".equals(status) && !"Y".equals(rolloverFlag)) {
			item.setUpdateVisiable("Y");
		}
		
		//[撤銷申請]:按鈕是否顯示, Open Api Key 狀態為啟用 = true
		item.setRevokeVisiable("N");
		if("1".equals(status)) {
			item.setRevokeVisiable("Y");
		}
		return item;
	}

	private String getDateTime(Long timeInMillis) {
		if(timeInMillis == null) {
			return "";
		}
		Date dt = new Date(timeInMillis);
		String dtStr = DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日_2).orElse(null);// yyyy/MM/dd
		return dtStr;
	}
	
	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}
	
	protected TsmpOpenApiKeyDao getTsmpOpenApiKeyDao() {
		return this.tsmpOpenApiKeyDao;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0090");
		return this.pageSize;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}
}
