package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.vo.DPB9906Item;
import tpi.dgrv4.dpaa.vo.DPB9906Req;
import tpi.dgrv4.dpaa.vo.DPB9906Resp;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9906Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpItemsDao tsmpDpItemsDao;

	@Autowired
	public DPB9906Service(TsmpDpItemsDao tsmpDpItemsDao) {
		super();
		this.tsmpDpItemsDao = tsmpDpItemsDao;
	}

	public DPB9906Resp queryTsmpDpItemsDetail(TsmpAuthorization auth, DPB9906Req req) {
		String locale = req.getLocale();
		checkParam(locale, "locale");
		String itemNo = req.getItemNo();
		checkParam(itemNo, "itemNo");
		String subitemNo = req.getSubitemNo();
		checkParam(subitemNo, "subitemNo");
		
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
        TsmpDpItems tsmpDpItems = getTsmpDpItemsDao().findById(id).orElseThrow(TsmpDpAaRtnCode._1298::throwing);
		
		DPB9906Resp resp = new DPB9906Resp();
		resp.setLocale(tsmpDpItems.getLocale());
		resp.setItemId(tsmpDpItems.getItemId().intValue());
		resp.setSortBy(tsmpDpItems.getSortBy());
		resp.setIsDefault(nvl(tsmpDpItems.getIsDefault()));
		resp.setItemNo(tsmpDpItems.getItemNo());
		resp.setItemName(tsmpDpItems.getItemName());
		resp.setSubitemNo(tsmpDpItems.getSubitemNo());
		resp.setSubitemName(tsmpDpItems.getSubitemName());

		String isGetSubitemNameList = req.getGetSubitemNameList();
		if ("Y".equals(isGetSubitemNameList)) {
			List<DPB9906Item> subitemNameList = getSubitemNameList( //
				tsmpDpItems.getItemNo(), tsmpDpItems.getSubitemNo());
			resp.setSubitemNameList(subitemNameList);
		}else {
			List<String> params = getParams(tsmpDpItems);
			resp.setParams(params);
		}
		return resp;
	}

	protected void checkParam(String input, String field) {
		if (!StringUtils.hasText(input)) {
			this.logger.debug(String.format("%s is required", field));
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	protected List<String> getParams(TsmpDpItems tsmpDpItems) {
		return Arrays.asList(new String[] {
			nvl(tsmpDpItems.getParam1()),
			nvl(tsmpDpItems.getParam2()),
			nvl(tsmpDpItems.getParam3()),
			nvl(tsmpDpItems.getParam4()),
			nvl(tsmpDpItems.getParam5())
		});
	}

	protected List<DPB9906Item> getSubitemNameList(String itemNo, String subitemNo) {
		List<TsmpDpItems> tsmpDpItemList = getTsmpDpItemsDao().findByItemNo(itemNo, Sort.by("locale"));
		tsmpDpItemList = tsmpDpItemList.stream() //
			.filter((i) -> subitemNo.equals(i.getSubitemNo())) //
			.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(tsmpDpItemList)) {
			return null;
		}
		
		List<DPB9906Item> subitemNameList = new ArrayList<>();
		List<String> paramsList = new LinkedList<>();
		DPB9906Item dpb9906Item = null;
		for (TsmpDpItems tsmpDpItem : tsmpDpItemList) {
			dpb9906Item = new DPB9906Item();
			dpb9906Item.setVersion(tsmpDpItem.getVersion());
			dpb9906Item.setLocale(tsmpDpItem.getLocale());
			dpb9906Item.setSubitemName(tsmpDpItem.getSubitemName());
			paramsList = getParamsList(tsmpDpItem);
			dpb9906Item.setParams(paramsList);
			subitemNameList.add(dpb9906Item);
		}
		return subitemNameList;
	}

    private List<String> getParamsList(TsmpDpItems tsmpDpItem) {
        List<String> paramsList = new LinkedList<>();
        paramsList.add(Optional.ofNullable(tsmpDpItem.getParam1()).orElse(""));
        paramsList.add(Optional.ofNullable(tsmpDpItem.getParam2()).orElse(""));
        paramsList.add(Optional.ofNullable(tsmpDpItem.getParam3()).orElse(""));
        paramsList.add(Optional.ofNullable(tsmpDpItem.getParam4()).orElse(""));
        paramsList.add(Optional.ofNullable(tsmpDpItem.getParam5()).orElse(""));
        return paramsList;
    }
	
	protected TsmpDpItemsDao getTsmpDpItemsDao() {
		return this.tsmpDpItemsDao;
	}

}
