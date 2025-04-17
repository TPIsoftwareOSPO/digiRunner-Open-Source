package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.vo.DPB9900Item;
import tpi.dgrv4.dpaa.vo.DPB9900Req;
import tpi.dgrv4.dpaa.vo.DPB9900Resp;
import tpi.dgrv4.dpaa.vo.DPB9900Trunc;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.IAllPropertiesService;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9900Service {

	private TPILogger logger = TPILogger.tl;

//	@Autowired(required = false)
	private IAllPropertiesService allPropertiesService;
	
	private TsmpSettingDao tsmpSettingDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;
 
	@Autowired
	public DPB9900Service(@Nullable IAllPropertiesService allPropertiesService, TsmpSettingDao tsmpSettingDao,
			ServiceConfig serviceConfig) {
		super();
		this.allPropertiesService = allPropertiesService;
		this.tsmpSettingDao = tsmpSettingDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB9900Resp queryTsmpSettingList(TsmpAuthorization auth, DPB9900Req req) {
		String lastId = checkLastId(req.getId());
		String[] keywords = getKeywords(req.getKeyword(), " ");
		Integer pageSize = getPageSize();
		List<TsmpSetting> settings = getTsmpSettingDao().query_DPB9900Service_01(lastId, keywords, pageSize);
		if (CollectionUtils.isEmpty(settings)) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		DPB9900Resp resp = new DPB9900Resp();
		List<DPB9900Item> dataList = getDataList(settings);
		resp.setDataList(dataList);
		
		Map<String, String> allProperties = new HashMap<>();
		if (allPropertiesService != null) {
			allProperties = getAllPropertiesService().getAllProperties();
		}
		resp.setAllProperties(allProperties);

		return resp;
	}

	protected String checkLastId(String tsmpSettingId) {
		if (StringUtils.hasLength(tsmpSettingId)) {
			boolean isSetttingExist = getTsmpSettingDao().existsById(tsmpSettingId);
			if (!isSetttingExist) {
				this.logger.error(String.format("TSMP_SETTING.id = [%s] not exist", tsmpSettingId));
				throw TsmpDpAaRtnCode._1298.throwing();
			}
		}
		return tsmpSettingId;
	}

	protected List<DPB9900Item> getDataList(List<TsmpSetting> settings) {
		List<DPB9900Item> dataList = new ArrayList<>();

		DPB9900Item item = null;
		DPB9900Trunc trunc = null;
		for (TsmpSetting setting : settings) {
			item = new DPB9900Item();
			item.setId(setting.getId());
			trunc = getTrunc(setting.getValue(), 20);
			item.setValue(trunc);
			trunc = getTrunc(setting.getMemo(), 25);
			item.setMemo(trunc);
			dataList.add(item);
		}
		
		return dataList;
	}

	protected DPB9900Trunc getTrunc(String input, Integer limit) {
		DPB9900Trunc trunc = new DPB9900Trunc();
		trunc.setVal(input);
		trunc.setT(false);
		if (StringUtils.hasLength(input) && input.length() > limit) {
			trunc.setVal(input.substring(0, limit));
			trunc.setT(true);
			trunc.setOri(input);
		}
		return trunc;
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return this.tsmpSettingDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb9900");
		return this.pageSize;
	}
	
	protected IAllPropertiesService getAllPropertiesService() {
		return allPropertiesService;
	}
}
