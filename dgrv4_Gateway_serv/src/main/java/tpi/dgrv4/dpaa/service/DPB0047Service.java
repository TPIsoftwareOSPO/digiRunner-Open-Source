package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.OAuthUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0047Req;
import tpi.dgrv4.dpaa.vo.DPB0047Resp;
import tpi.dgrv4.dpaa.vo.DPB0047SubItems;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0047Service {
	
	private TPILogger logger = TPILogger.tl;

	private TsmpDpItemsDao tsmpDpItemsDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0047Service(TsmpDpItemsDao tsmpDpItemsDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			ServiceConfig serviceConfig) {
		super();
		this.tsmpDpItemsDao = tsmpDpItemsDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.serviceConfig = serviceConfig;
	}

	public DPB0047Resp querySubItemsByItemNo(TsmpAuthorization tsmpAuthorization, DPB0047Req req, ReqHeader reqHeader) {
		DPB0047Resp resp = new DPB0047Resp();

		try {
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			Long lastId = req.getItemId();
			String encodeItemNo = req.getEncodeItemNo();
			String isDefault = req.getIsDefault();
			
			if (StringUtils.isEmpty(encodeItemNo) || StringUtils.isEmpty(isDefault)) {
				throw TsmpDpAaRtnCode.NO_ITEMS_DATA.throwing();
			}
			
			String ItemNo = getDecoedeItemNo(encodeItemNo, reqHeader.getLocale());//解碼
			
			//2024-06-18 Webber getPageSize()先改為最大,不然下拉選單無法完整查詢
			List<TsmpDpItems> itemsList = getTsmpDpItemsCacheProxy().queryLike(lastId, words, ItemNo, 
					isDefault, Integer.MAX_VALUE, reqHeader.getLocale());
			
			if (itemsList == null || itemsList.size() == 0) {
				throw TsmpDpAaRtnCode.NO_ITEMS_DATA.throwing();
			}
			
			if("Y".equalsIgnoreCase(isDefault)) {//取預設值
				TsmpDpItems item = itemsList.get(0);
				resp.setDefaultVal(item.getSubitemNo());
				
			}else {
				List<DPB0047SubItems> subItemsList = getSubItemsList(itemsList);
				resp.setSubItems(subItemsList);
			}
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private List<DPB0047SubItems> getSubItemsList(List<TsmpDpItems> itemsList) {
		List<DPB0047SubItems> dataList = new ArrayList<DPB0047SubItems>();
		for (TsmpDpItems item : itemsList) {
			DPB0047SubItems vo = getSubItems(item);
			dataList.add(vo);
		}
		return dataList;
	}
	
	private DPB0047SubItems getSubItems(TsmpDpItems item) {
		DPB0047SubItems data = new DPB0047SubItems();
		
		data.setItemId(item.getItemId());
		data.setItemNo(item.getItemNo());
		data.setItemName(item.getItemName());
		data.setSubitemNo(item.getSubitemNo());
		data.setSubitemName(item.getSubitemName());
		data.setSortBy(item.getSortBy());
		data.setIsDefault(item.getIsDefault());
		data.setParam1(item.getParam1());
		data.setParam2(item.getParam2());
		data.setParam3(item.getParam3());

		return data;
	}
	
	/**
	 * 取得所有itemNo在資料表中的順序:
	 * itemNo依 sortBy, itemId排序後,
	 * 依序只取第一次不重複的 itemNo,並給順序值(由0開始)
	 * 
	 * @return
	 */
	public Map<Integer, String> querySortItemNo(String locale) {
		List<TsmpDpItems> itemsList = queryAllOrderBy(locale);
		
		Map<String, String> tempMap = new HashMap<>();
		Map<Integer, String> resultMap = new HashMap<>();//<順序, itemNo>
 
		int index = 0;//順序
		for (TsmpDpItems i : itemsList) {
			String itemNo = i.getItemNo();
			if(tempMap.get(itemNo) == null) {//還沒有排過順序
				resultMap.put(index, itemNo);
				index++;
			}
			tempMap.put(itemNo, "");//表示此itemNo已排過順序
		}
		return resultMap;
	}

	/**
	 * BcryptParam 解碼, for itemNo
	 * 
	 * @param bcryptParamString
	 * @param itemNo
	 * @return
	 * @throws BcryptParamDecodeException
	 */
	public String decode(final String bcryptParamString, final String locale) throws BcryptParamDecodeException {
		if ("".equals(bcryptParamString)) {
			return new String();
		}

		try {
			if (bcryptParamString.indexOf(",") == -1) {
				throw new IllegalArgumentException();
			}
			final String[] bcryptParams = bcryptParamString.split(",");
			final int index = Integer.parseInt(bcryptParams[1]);//itemNo順序

			final Map<Integer, String> itemSortMap = querySortItemNo(locale);
			if ((itemSortMap.size() - 1) < index) {
				throw new NumberFormatException(index + ", data size = " + itemSortMap.size());
			}
			String _itemNo = itemSortMap.get(index);//在資料表中,此順序對映的itemNo

			// 比對 bcrypt 值
			final String bcryptEncodedItemNo = new String(ServiceUtil.base64Decode(bcryptParams[0]));
			final boolean isMatched = OAuthUtil.bCryptPasswordCheck(_itemNo, bcryptEncodedItemNo);
			if (isMatched) {
				return _itemNo;
			} else {
				throw new Exception("Mismatched values.");
			}
		} catch (NumberFormatException e) {
			throw new BcryptParamDecodeException("Invalid index " + e.getLocalizedMessage(), bcryptParamString);
		} catch (IllegalArgumentException e) {
			throw new BcryptParamDecodeException("Invalid parameter format, missing \",\"", bcryptParamString);
		} catch (Exception e) {
			throw new BcryptParamDecodeException(e.getLocalizedMessage(), bcryptParamString);
		}
	}
	
	public String getDecoedeItemNo(String encodeItemNo, String locale) {
		String itemNo = null;
		try {
			itemNo = decode(encodeItemNo, locale);//BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1201.throwing();
		}
		
		return itemNo;
	}
	
	protected List<TsmpDpItems> queryAllOrderBy(String locale){
		Sort sort = Sort.by(Sort.DEFAULT_DIRECTION, "sortBy", "itemId");//排序
		List<TsmpDpItems> itemsList = getTsmpDpItemsCacheProxy().findByLocale(locale, sort);
		return itemsList;
	}
	
	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	protected TsmpDpItemsDao getTsmpDpItemsDao() {
		return this.tsmpDpItemsDao;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0047");
		return this.pageSize;
	}
}
