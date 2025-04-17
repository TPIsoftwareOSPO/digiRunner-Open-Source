package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.vo.DPB9907Item;
import tpi.dgrv4.dpaa.vo.DPB9907Req;
import tpi.dgrv4.dpaa.vo.DPB9907Resp;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9907Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpItemsDao tsmpDpItemsDao;

	@Autowired
	public DPB9907Service(TsmpDpItemsDao tsmpDpItemsDao) {
		super();
		this.tsmpDpItemsDao = tsmpDpItemsDao;
	}

	public DPB9907Resp queryItemNameList(TsmpAuthorization auth, DPB9907Req req) {
		String itemNo = req.getItemNo();
		if (!StringUtils.hasLength(itemNo)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		List<TsmpDpItems> tsmpDpItems = getTsmpDpItemsDao().findByItemNo(itemNo, Sort.by("locale"));
		if (CollectionUtils.isEmpty(tsmpDpItems)) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		DPB9907Resp resp = new DPB9907Resp();
		resp.setItemNo(itemNo);
		List<DPB9907Item> dataList = getDataList(tsmpDpItems);
		resp.setDataList(dataList);
		return resp;
	}

	protected List<DPB9907Item> getDataList(List<TsmpDpItems> tsmpDpItems) {
		Map<String, List<TsmpDpItems>> group = tsmpDpItems.stream() //
			.collect(Collectors.groupingBy( //
				(i) -> i.getLocale(), //
				() -> new LinkedHashMap<>(), // 需依 locale 排序
				Collectors.toList()
			));
		
		List<DPB9907Item> dataList = new ArrayList<>();

		DPB9907Item dpb9907Item = null;
		String locale = null;
		List<TsmpDpItems> items = null;
		for (Map.Entry<String, List<TsmpDpItems>> entry : group.entrySet()) {
			locale = entry.getKey();
			items = entry.getValue();
			
			// 同樣語系中, 是否有不同的分類名稱
			Set<String> set = items.stream().map((i) -> i.getItemName()).collect(Collectors.toSet());
			if (set.size() > 1) {
				this.logger.debug(String.format("ItemNo '%s' has more than one itemName with same locale '%s': %s"
						,items.get(0).getItemNo(),locale,items));
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			
			dpb9907Item = new DPB9907Item();
			dpb9907Item.setLocale(locale);
			dpb9907Item.setItemName(entry.getValue().get(0).getItemName());
			dataList.add(dpb9907Item);
		}
		
		return dataList;
	}

	protected TsmpDpItemsDao getTsmpDpItemsDao() {
		return this.tsmpDpItemsDao;
	}

}
