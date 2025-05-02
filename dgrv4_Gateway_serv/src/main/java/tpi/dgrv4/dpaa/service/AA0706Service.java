package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertDispatcherIfs;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0706AlertSetting;
import tpi.dgrv4.dpaa.vo.AA0706Req;
import tpi.dgrv4.dpaa.vo.AA0706Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.jpql.TsmpAlert;
import tpi.dgrv4.entity.repository.TsmpAlertDao;
import tpi.dgrv4.entity.repository.TsmpRoleAlertDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0706Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpRoleDao tsmpRoleDao;
	private TsmpAlertDao tsmpAlertDao;
	private TsmpRoleAlertDao tsmpRoleAlertDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private ServiceConfig serviceConfig;
	private BcryptParamHelper bcryptParamHelper;
	private DpaaAlertDispatcherIfs dpaaAlertDispatcher;

	private Integer pageSize;

	@Autowired
	public AA0706Service(TsmpRoleDao tsmpRoleDao, TsmpAlertDao tsmpAlertDao, TsmpRoleAlertDao tsmpRoleAlertDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, ServiceConfig serviceConfig,
			BcryptParamHelper bcryptParamHelper, DpaaAlertDispatcherIfs dpaaAlertDispatcher) {
		super();
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpAlertDao = tsmpAlertDao;
		this.tsmpRoleAlertDao = tsmpRoleAlertDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.serviceConfig = serviceConfig;
		this.bcryptParamHelper = bcryptParamHelper;
		this.dpaaAlertDispatcher = dpaaAlertDispatcher;
	}

	public AA0706Resp queryAlarmSettings(TsmpAuthorization authorization, AA0706Req req, ReqHeader reqHeader) {
		AA0706Resp resp = new AA0706Resp();
		try {
			Long lastAlertId = StringUtils.isEmpty(req.getLastAlertId()) ? null : Long.valueOf(req.getLastAlertId());
			String local = ServiceUtil.getLocale(reqHeader.getLocale());
			String decodeAlertEnabled = getValueByBcryptParamHelper(req.getAlertEnabled(), "ENABLE_FLAG", local);
			Boolean alertEnabled = "-1".equals(decodeAlertEnabled) ? null : "0".equals(decodeAlertEnabled) ? false : true;
			String roleName = req.getRoleName();
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");

			List<TsmpAlert> alertList = getTsmpAlertDao().queryByAA0706Service(lastAlertId, alertEnabled, roleName, words, getPageSize());
			if (alertList.size() == 0) {
				// 1298:查無資料
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			// 查詢到的TSMP_ALERT資料放進AA0706Resp.alertSettingList
			List<AA0706AlertSetting> alertSettingList = new ArrayList<>();
			alertList.forEach(vo -> {
				AA0706AlertSetting setVo = new AA0706AlertSetting();
				setVo.setAlertEnabled(this.getSbNameByItemsSbNo("ENABLE_FLAG", vo.getAlertEnabled() ? "1" : "0",
						reqHeader.getLocale()));
				setVo.setAlertID(vo.getAlertId().toString());
				setVo.setAlertName(vo.getAlertName());
				setVo.setAlertSys(vo.getAlertSys());
				setVo.setAlertType(vo.getAlertType());
				alertSettingList.add(setVo);
			});

			resp.setAlertSettingList(alertSettingList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			// 1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		} finally {
			// 2022.07.13 Whether query successfully or not, always synchronize.
			syncAlert();
		}
		return resp;
	}

	protected String getValueByBcryptParamHelper(String encodeValue, String itemNo, String locale) {
		String value = null;
		try {
			value = getBcryptParamHelper().decode(encodeValue, itemNo, BcryptFieldValueEnum.SUBITEM_NO, locale);// BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return value;
	}
	
	private String getSbNameByItemsSbNo(String itemNo, String subitemNo, String locale) {
		String subitemName = "";
		TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale(itemNo, subitemNo, locale);
		if (dpItem == null) {
			return null;
		}
		subitemName = dpItem.getSubitemName();

		return subitemName;
	}

	protected void syncAlert() {
		// 使用 execute(...) , 異常會被吞掉
		// 使用 submit(...).get() , 會拋出 ExecutionException
		// 離開 try block 時，會自動呼叫 executor.shutdown()
		try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
		    executor.submit(() -> getDpaaAlertDispatcherIfs().syncAlert()).get();
		} catch (InterruptedException | ExecutionException e) {
		    TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		    Thread.currentThread().interrupt();
		}
	}

	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}

	protected TsmpAlertDao getTsmpAlertDao() {
		return tsmpAlertDao;
	}

	protected TsmpRoleAlertDao getTsmpRoleAlertDao() {
		return tsmpRoleAlertDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0706");
		return this.pageSize;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected DpaaAlertDispatcherIfs getDpaaAlertDispatcherIfs() {
		return this.dpaaAlertDispatcher;
	}

}
