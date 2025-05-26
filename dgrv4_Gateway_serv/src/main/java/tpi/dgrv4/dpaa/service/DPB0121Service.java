package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.vo.DPB0121Resp;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class DPB0121Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpSettingService tsmpSettingService;

	@Autowired
	public DPB0121Service(TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
	}

	public DPB0121Resp queryRefreshMemListUrls(String scheme) {
		DPB0121Resp resp = new DPB0121Resp();
		resp.setRefreshMemListUrls(new ArrayList<>());
		resp.setRedoUrls(new ArrayList<>());
		
		// 是否有客製包存在
		boolean isCusModuleExists = getTsmpSettingService().getVal_CUS_MODULE_EXIST();
		if (!isCusModuleExists) {
			return resp;
		}
		
		// 連續查詢，查到爆出 NO_ITEMS_DATA 錯誤為止
		int index = 1;
		boolean isCusFuncEnable = false;
		String cusModuleName = null;
		boolean b = true;
		while (b) {
			try {
				isCusFuncEnable = getTsmpSettingService().getVal_CUS_FUNC_ENABLE(index);
				// 客製包的介接功能是否啟用
				if (isCusFuncEnable) {
					cusModuleName = getTsmpSettingService().getVal_CUS_MODULE_NAME(index);
					if (!ObjectUtils.isEmpty(cusModuleName)) {
						String refreshMemListUrl = String.format("%s://{{ip}}:{{port}}/%s/v3/refreshMemList", //
							scheme, cusModuleName);
						String redoUrl = String.format("%s://{{ip}}:{{port}}/%s/11/DPB0059", //
								scheme, cusModuleName);
						resp.getRefreshMemListUrls().add(refreshMemListUrl);
						resp.getRedoUrls().add(redoUrl);
					}
				}
				index++;
				b = index < 100;
			} catch (TsmpDpAaException e) {
				this.logger.debug(String.format("CUS_MODULE_NAME%d 或 CUS_FUNC_ENABLE%d 不存在", index, index));
				break;
			}
		}
		
		return resp;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}

}
