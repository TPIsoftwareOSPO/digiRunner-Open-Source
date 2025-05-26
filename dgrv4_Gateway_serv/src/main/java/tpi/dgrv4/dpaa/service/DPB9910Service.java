package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9910Item;
import tpi.dgrv4.dpaa.vo.DPB9910Req;
import tpi.dgrv4.dpaa.vo.DPB9910Resp;
import tpi.dgrv4.dpaa.vo.DPB9910Trunc;
import tpi.dgrv4.entity.entity.jpql.CusSetting;
import tpi.dgrv4.entity.repository.CusSettingDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9910Service {

	private TPILogger logger = TPILogger.tl;

	private CusSettingDao cusSettingDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB9910Service(CusSettingDao cusSettingDao, ServiceConfig serviceConfig) {
		super();
		this.cusSettingDao = cusSettingDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB9910Resp queryCusSettingList(TsmpAuthorization auth, DPB9910Req req) {
		try {
			Integer lastSortBy = req.getSortBy();
			String[] keywords = getKeywords(req.getKeyword(), " ");
			List<CusSetting> list = getCusSettingDao().queryDPB9910Service(lastSortBy, keywords, getPageSize());
			if (CollectionUtils.isEmpty(list)) {
				//查無資料
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			DPB9910Resp resp = new DPB9910Resp();
			List<DPB9910Item> itemList = getItemList(list);
			resp.setItemList(itemList);
			return resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	private List<DPB9910Item> getItemList(List<CusSetting> list) {
		List<DPB9910Item> itemList = new ArrayList<>();

		DPB9910Item item = null;
		DPB9910Trunc trunc = null;
		for (CusSetting vo : list) {
			item = new DPB9910Item();
			item.setCusSettingId(vo.getCusSettingId());
			item.setSettingNo(vo.getSettingNo());
			trunc = this.getTrunc(vo.getSettingName(), 20);
			item.setSettingName(trunc);
			item.setSubsettingNo(vo.getSubsettingNo());
			trunc = this.getTrunc(vo.getSubsettingName(), 20);
			item.setSubsettingName(trunc);
			item.setSortBy(vo.getSortBy());
			itemList.add(item);
		}
		
		return itemList;
	}

	private DPB9910Trunc getTrunc(String input, Integer limit) {
		DPB9910Trunc trunc = new DPB9910Trunc();
		trunc.setVal(input);
		trunc.setT(false);
		if (StringUtils.hasLength(input) && input.length() > limit) {
			trunc.setVal(input.substring(0, limit));
			trunc.setT(true);
			trunc.setOri(input);
		}
		return trunc;
	}

	protected CusSettingDao getCusSettingDao() {
		return cusSettingDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb9910");
		return this.pageSize;
	}

}
