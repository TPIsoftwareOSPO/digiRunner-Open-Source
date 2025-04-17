package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0097Item;
import tpi.dgrv4.dpaa.vo.DPB0097Req;
import tpi.dgrv4.dpaa.vo.DPB0097Resp;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;


@Service
public class DPB0097Service {

	private TsmpRtnCodeDao tsmpRtnCodeDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0097Service(TsmpRtnCodeDao tsmpRtnCodeDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
		this.serviceConfig = serviceConfig;
	}

	/**
	 * 可依關鍵字(不分大小寫)模糊搜尋出API回覆代碼清單。				
	 * 模糊搜尋欄位: 回覆代碼、語言地區、顯示的回覆訊息、說明				
	 * 排序欄位: 回覆代碼 asc, 語言地區 asc				
	 * 連帶查詢: 如果使用者只輸入中文訊息的關鍵字，則須連帶其他語言相同代碼的資料一起帶出。				
	 * 
	 * @param tsmpAuthorization
	 * @param req
	 * @return
	 */
	public DPB0097Resp queryApiRtnCodeList(TsmpAuthorization tsmpAuthorization, DPB0097Req req) {
		DPB0097Resp resp = new DPB0097Resp();

		try {
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			String rtnCode = req.getTsmpRtnCode();
			String locale = req.getLocale();
				
			resp = queryApiRtnCodeList(rtnCode, locale, words);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	public DPB0097Resp queryApiRtnCodeList(String rtnCode, String locale, String[] words) throws Exception {
		List<TsmpRtnCode> retCode = getTsmpRtnCodeDao().query_dpb0097service_01(rtnCode, locale, words, getPageSize());
		
		if (retCode == null || retCode.size() == 0) {
			throw TsmpDpAaRtnCode._1298.throwing();	//查無資料
		}

		DPB0097Resp resp = new DPB0097Resp();
		List<DPB0097Item> dataList = getDPB0097ItemList(retCode);
		resp.setDataList(dataList);
		
		return resp;
	}
	
	private List<DPB0097Item> getDPB0097ItemList(List<TsmpRtnCode> retCode) throws Exception {
		List<DPB0097Item> dataList = new ArrayList<DPB0097Item>();
		for (TsmpRtnCode tsmpRtnCode : retCode) {
			DPB0097Item vo = getDPB0097Item(tsmpRtnCode);
			dataList.add(vo);
		}
		return dataList;
	}

	private DPB0097Item getDPB0097Item(TsmpRtnCode tsmpRtnCode) throws Exception {
		DPB0097Item data = new DPB0097Item();
		boolean isMsgTruncated = false;
		boolean isDescTruncated = false;
		String msg = ServiceUtil.nvl(tsmpRtnCode.getTsmpRtnMsg());
		String desc = ServiceUtil.nvl(tsmpRtnCode.getTsmpRtnDesc());
		String truncatedMsg = msg;
		String truncatedDesc = desc;
		
		if(msg.length() > 30) {
			truncatedMsg = msg.substring(0, 30)+"...";
			isMsgTruncated = true;
		}
		if(desc.length() > 30) {
			truncatedDesc = desc.substring(0, 30)+"...";
			isDescTruncated = true;
		}
		
		data.setTsmpRtnCode(tsmpRtnCode.getTsmpRtnCode());	//回覆代碼
		data.setLocale(tsmpRtnCode.getLocale());			//語言地區
		data.setTsmpRtnMsg(truncatedMsg);					//截斷後的"顯示的回覆訊息"
		data.setOriTsmpRtnMsg(msg);							//完整的"顯示的回覆訊息
		data.setIsMsgTruncated(isMsgTruncated);				//顯示的回覆訊息是否被截斷
		data.setTsmpRtnDesc(truncatedDesc);					//截斷後的"說明"
		data.setOriTsmpRtnDesc(desc);						// 完整的"說明"
		data.setIsDescTruncated(isDescTruncated);				//說明是否被截斷
		
		return data;
	}

	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return this.tsmpRtnCodeDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0097");
		return this.pageSize;
	}
	
}
