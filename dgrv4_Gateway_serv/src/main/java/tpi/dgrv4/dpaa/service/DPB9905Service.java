package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;
import static tpi.dgrv4.dpaa.util.ServiceUtil.getLocale;
import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.vo.DPB9905Item;
import tpi.dgrv4.dpaa.vo.DPB9905Req;
import tpi.dgrv4.dpaa.vo.DPB9905Resp;
import tpi.dgrv4.dpaa.vo.DPB9905Subitem;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9905Service {

	private TPILogger logger = TPILogger.tl;

	private ServiceConfig serviceConfig;
	private TsmpDpItemsDao tsmpDpItemsDao;

	private Integer pageSize;

	@Autowired
	public DPB9905Service(ServiceConfig serviceConfig, TsmpDpItemsDao tsmpDpItemsDao) {
		super();
		this.serviceConfig = serviceConfig;
		this.tsmpDpItemsDao = tsmpDpItemsDao;
	}

	public DPB9905Resp queryTsmpDpItemsList(TsmpAuthorization auth, DPB9905Req req) {
		String locale = getLocale(req.getLocale());
		String itemNo = req.getItemNo();
		String[] keywords = getKeywords(req.getKeyword(), " ");
		Integer p_itemOrder = req.getP_itemOrder();
		Integer p_sortBy = req.getP_sortBy();
		
		List<TsmpDpItems> tsmpDpItemsList = getTsmpDpItemsDao().findByLocale(locale, Sort.by("sortBy"));
		if (CollectionUtils.isEmpty(tsmpDpItemsList)) {
			this.logger.debug(String.format("No data found by locale '%s'", locale));
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		List<DPB9905Item> dpb9905ItemList = getDPB9905ItemList(tsmpDpItemsList, itemNo, keywords, p_itemOrder, p_sortBy);
		if (CollectionUtils.isEmpty(dpb9905ItemList)) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		DPB9905Resp resp = new DPB9905Resp();
		resp.setItemList(dpb9905ItemList);
		return resp;
	}

	protected List<DPB9905Item> getDPB9905ItemList(List<TsmpDpItems> tsmpDpItemsList, String itemNo, //
			String[] keywords, Integer p_itemOrder, Integer p_sortBy) {
		
		List<DPB9905Item> dpb9905ItemList = new ArrayList<>();

		TsmpDpItems firstItemOfSubitems = null;
		int lastSortbyOfSubitems = -1;
		int itemOrder = 0;
		List<DPB9905Subitem> dpb9905SubitemList = null;
		Date maxDateTime = null;
		String maxUser = null;
		for (TsmpDpItems tsmpDpItems : tsmpDpItemsList) {
			if (firstItemOfSubitems == null || !firstItemOfSubitems.getItemNo().equals(tsmpDpItems.getItemNo())) {
				// 切換到下一組分類時
				if (firstItemOfSubitems != null) {
					// 統整分類清單：當
					// 1. 超過分頁最大筆數
					// 2. 小於等於前頁最後一筆分類順序
					// 3. 非指定的分類編號
					// 4. 所有子分類皆未符合關鍵字
					// 就不用加入該分類項目
					if ( //
						dpb9905ItemList.size() < getPageSize() && (
							p_itemOrder == null || (
								p_itemOrder != null && itemOrder > p_itemOrder
							)
						) && (
							!StringUtils.hasLength(itemNo) || (
								StringUtils.hasLength(itemNo) && firstItemOfSubitems.getItemNo().equals(itemNo)
							)
						) &&
						!CollectionUtils.isEmpty(dpb9905SubitemList)
					) {
						// 已用數量
						int used = lastSortbyOfSubitems - firstItemOfSubitems.getSortBy() + 1;
						// 保留數量
						int all = tsmpDpItems.getSortBy() - firstItemOfSubitems.getSortBy();
						// 可用數量
						int avail = all - used;
						// 封裝
						DPB9905Item dpb9905Item_1 = new DPB9905Item();
						dpb9905Item_1.setItemOrder(itemOrder);
						dpb9905Item_1.setItemNo(firstItemOfSubitems.getItemNo());
						dpb9905Item_1.setItemName(firstItemOfSubitems.getItemName());
						dpb9905Item_1.setUpdateDateTime(DateTimeUtil.dateTimeToString(maxDateTime, DateTimeFormatEnum.西元年月日時分秒).orElse(null));
						dpb9905Item_1.setUpdateUser(maxUser);
						dpb9905Item_1.setSubitemCount(avail + " / " + used + " / " + all);
						if (StringUtils.hasLength(itemNo)) {
							dpb9905Item_1.setSubitemList(dpb9905SubitemList);
						}
						dpb9905ItemList.add(dpb9905Item_1);
					}
					// 一定要在封裝之後
					itemOrder++;
				}
				firstItemOfSubitems = tsmpDpItems;
				dpb9905SubitemList = new ArrayList<>();	// 重置子分類清單
				maxDateTime = null;
				maxUser = null;
			}

			// 比對建立與更新日期，找出最新異動的日期, 人員
			String user = tsmpDpItems.getCreateUser();
			Date dateTime = tsmpDpItems.getCreateDateTime();
			if (dateTime == null) {
				user = tsmpDpItems.getUpdateUser();
				dateTime = tsmpDpItems.getUpdateDateTime();
			} else {
				Date u = tsmpDpItems.getUpdateDateTime();
				if (u != null && u.compareTo(dateTime) > 0) {
					user = tsmpDpItems.getUpdateUser();
					dateTime = u;
				}
			}
			// 比對同一分類中最新的異動日期, 人員
			if (dateTime != null && (maxDateTime == null || dateTime.compareTo(maxDateTime) > 0)) {
				maxDateTime = dateTime;
				maxUser = user;
			}

			lastSortbyOfSubitems = tsmpDpItems.getSortBy();
			
			// 子分類清單：當
			// 1. 超過分頁最大筆數
			// 2. 小於等於前頁最後一筆子分類序號
			// 4. 未符合關鍵字
			// 就不用加入該子分類項目
			if ( //
				dpb9905SubitemList.size() < getPageSize() && (
					p_sortBy == null || (
						p_sortBy != null && lastSortbyOfSubitems > p_sortBy
					)
				) &&
				isMatchedKeywords(tsmpDpItems, keywords)
			) {
				// 封裝
				DPB9905Subitem dpb9905Subitem = new DPB9905Subitem();
				String subitemOrderStr = String.format("%d (%d)", (lastSortbyOfSubitems - firstItemOfSubitems.getSortBy()), lastSortbyOfSubitems);
				dpb9905Subitem.setSubitemOrder(subitemOrderStr);
				dpb9905Subitem.setSortBy(lastSortbyOfSubitems);
				dpb9905Subitem.setSubitemNo(tsmpDpItems.getSubitemNo());
				dpb9905Subitem.setSubitemName(tsmpDpItems.getSubitemName());
				dpb9905Subitem.setUpdateDateTime(DateTimeUtil.dateTimeToString(dateTime, DateTimeFormatEnum.西元年月日時分秒).orElse(null));
				dpb9905Subitem.setUpdateUser(user);
				dpb9905Subitem.setIsDefault(tsmpDpItems.getIsDefault());
				dpb9905SubitemList.add(dpb9905Subitem);
			}
		}
		// 最後一組分類
		// 統整分類清單：當
		// 1. 超過分頁最大筆數
		// 2. 小於等於前頁最後一筆分類順序
		// 3. 非指定的分類編號
		// 4. 所有子分類皆未符合關鍵字
		// 就不用加入該分類項目
		if (
			dpb9905ItemList.size() < getPageSize() && (
				p_itemOrder == null || (
					p_itemOrder != null && itemOrder > p_itemOrder
				)
			) && (
				!StringUtils.hasLength(itemNo) || (
					StringUtils.hasLength(itemNo) && firstItemOfSubitems.getItemNo().equals(itemNo)
				)
			) &&
			!CollectionUtils.isEmpty(dpb9905SubitemList)
		) {
			// 已用數量
			int used = lastSortbyOfSubitems - firstItemOfSubitems.getSortBy() + 1;
			// 保留數量
			int all = used;
			// 可用數量
			int avail = all - used;
			// 封裝
			DPB9905Item dpb9905Item_2 = new DPB9905Item();
			dpb9905Item_2.setItemOrder(itemOrder);
			dpb9905Item_2.setItemNo(firstItemOfSubitems.getItemNo());
			dpb9905Item_2.setItemName(firstItemOfSubitems.getItemName());
			dpb9905Item_2.setUpdateDateTime(DateTimeUtil.dateTimeToString(maxDateTime, DateTimeFormatEnum.西元年月日時分秒).orElse(null));
			dpb9905Item_2.setUpdateUser(maxUser);
			dpb9905Item_2.setSubitemCount(avail + " / " + used + " / " + all);
			if (StringUtils.hasLength(itemNo)) {
				dpb9905Item_2.setSubitemList(dpb9905SubitemList);
			}
			dpb9905ItemList.add(dpb9905Item_2);
		}

		StringBuffer sb = new StringBuffer();
		sb.append("\n========== ↓ DPB9905 Search Result ↓ ==========");
		dpb9905ItemList.forEach((i) -> {
			sb.append("\n" + i.toString());
		});
		sb.append("\n========== ↑ DPB9905 Search Result ↑ ==========");
		this.logger.debug(sb.toString());

		return dpb9905ItemList;
	}

	protected boolean isMatchedKeywords(TsmpDpItems tsmpDpItems, String[] keywords) {
		if (keywords == null || keywords.length == 0)
			return true;
		boolean isMatched = false;
		for (String keyword : keywords) {
			isMatched = isMatched || (
				nvl(tsmpDpItems.getItemNo()).toUpperCase().contains(keyword.toUpperCase()) ||
				nvl(tsmpDpItems.getItemName()).toUpperCase().contains(keyword.toUpperCase()) ||
				nvl_itemId(tsmpDpItems.getItemId()).toUpperCase().contains(keyword.toUpperCase()) ||
				nvl(tsmpDpItems.getSubitemNo()).toUpperCase().contains(keyword.toUpperCase()) ||
				nvl(tsmpDpItems.getSubitemName()).toUpperCase().contains(keyword.toUpperCase()) ||
				nvl(tsmpDpItems.getParam1()).toUpperCase().contains(keyword.toUpperCase()) ||
				nvl(tsmpDpItems.getParam2()).toUpperCase().contains(keyword.toUpperCase()) ||
				nvl(tsmpDpItems.getParam3()).toUpperCase().contains(keyword.toUpperCase()) ||
				nvl(tsmpDpItems.getParam4()).toUpperCase().contains(keyword.toUpperCase()) ||
				nvl(tsmpDpItems.getParam5()).toUpperCase().contains(keyword.toUpperCase())
			);
		}
		return isMatched;
	}

	private String nvl_itemId(Long itemId) {
		if (itemId == null) itemId = -1L;
		return String.valueOf(itemId);
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected TsmpDpItemsDao getTsmpDpItemsDao() {
		return this.tsmpDpItemsDao;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb9905");
		return this.pageSize;
	}

}
