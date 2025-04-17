package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0190Req;
import tpi.dgrv4.dpaa.vo.DPB0190Resp;
import tpi.dgrv4.dpaa.vo.DPB0190RespItem;
import tpi.dgrv4.entity.entity.DgrRdbConnection;
import tpi.dgrv4.entity.repository.DgrRdbConnectionDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0190Service {
	private TPILogger logger = TPILogger.tl;
	
	private DgrRdbConnectionDao dgrRdbConnectionDao;
    private TsmpSettingService tsmpSettingService;

    @Autowired
	public DPB0190Service(DgrRdbConnectionDao dgrRdbConnectionDao, TsmpSettingService tsmpSettingService) {
		super();
		this.dgrRdbConnectionDao = dgrRdbConnectionDao;
		this.tsmpSettingService = tsmpSettingService;
	}


	public DPB0190Resp queryRdbConnectionInfoList(TsmpAuthorization authorization, DPB0190Req req) {
		DPB0190Resp resp = new DPB0190Resp();
		try {
			List<DPB0190RespItem> items = new ArrayList<>();

			List<DgrRdbConnection> info = getDgrRdbConnectionDao().findAll();

			if (info.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			boolean isDefaultDbEnabled = getTsmpSettingService().getVal_APIM_DEFAULT_DB_ENABLED();
			info.forEach(i -> {
				if(!isDefaultDbEnabled && "APIM-default-DB".equalsIgnoreCase(i.getConnectionName())){
					return;
				}
				DPB0190RespItem item = new DPB0190RespItem();
				item.setConnectionName(i.getConnectionName());
				item.setJdbcUrl(i.getJdbcUrl());
				item.setMaxPoolSize(i.getMaxPoolSize());
				Date updateTime = i.getUpdateDateTime() == null ? i.getCreateDateTime() : i.getUpdateDateTime();

				item.setUpdateDateTime(updateTime);
				String updateUser = !StringUtils.hasLength(i.getUpdateUser()) ? i.getCreateUser() : i.getUpdateUser();
				item.setUpdateUser(updateUser);
				items.add(item);
			});
			
			if (items.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			resp.setInfoList(items);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}
	

	protected DgrRdbConnectionDao getDgrRdbConnectionDao() {
		return dgrRdbConnectionDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	
}
