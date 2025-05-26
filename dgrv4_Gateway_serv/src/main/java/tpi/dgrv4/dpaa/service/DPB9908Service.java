package tpi.dgrv4.dpaa.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9908Item;
import tpi.dgrv4.dpaa.vo.DPB9908Req;
import tpi.dgrv4.dpaa.vo.DPB9908Resp;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9908Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpItemsDao tsmpDpItemsDao;

	@Autowired
	public DPB9908Service(TsmpDpItemsDao tsmpDpItemsDao) {
		super();
		this.tsmpDpItemsDao = tsmpDpItemsDao;
	}

	@Transactional
	public DPB9908Resp updateItemNameList(TsmpAuthorization auth, DPB9908Req req) {
		String userName = auth.getUserName();
		checkParams(userName, req);
		
		String oriItemNo = req.getOriItemNo();
		List<TsmpDpItems> tsmpDpItems = getTsmpDpItemsDao().findByItemNo(oriItemNo, Sort.by("locale"));
		if (CollectionUtils.isEmpty(tsmpDpItems)) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		try {
			Map<String, String> locales = req.getDataList().stream() //
				.collect(Collectors.toMap( //
					(dgl9908Item) -> dgl9908Item.getLocale(), //
					(dgl9908Item) -> dgl9908Item.getItemName()
				));
			
			String itemNo = req.getItemNo();
			if (itemNo.equals(oriItemNo)) {
				doUpdate(itemNo, userName, locales, tsmpDpItems);
			} else {
				doCreate(itemNo, userName, locales, tsmpDpItems);
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();
		}
		
		return new DPB9908Resp();
	}

	protected void checkParams(String userName, DPB9908Req req) {
		if (!StringUtils.hasLength(userName)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}
		
		String oriItemNo = req.getOriItemNo();
		if (!StringUtils.hasLength(oriItemNo)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		Set<String> locales = new HashSet<>();
		for(DPB9908Item dgl9908Item : req.getDataList()) {
			String locale = dgl9908Item.getLocale();
			String itemName = dgl9908Item.getItemName();
			if (!(StringUtils.hasLength(locale) && StringUtils.hasLength(itemName))) {
				this.logger.error("locale and itemName is not nullable");
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			if (locales.contains(locale)) {
				throw TsmpDpAaRtnCode._1284.throwing("{{locale}}");
			}
			locales.add(locale);
		}
	}

	protected void doUpdate(String itemNo, String userName, //
		Map<String, String> locales, List<TsmpDpItems> tsmpDpItems) {

		tsmpDpItems.forEach((tsmpDpItem) -> {
			String locale = tsmpDpItem.getLocale();
			String newItemName = locales.get(locale);
			if (StringUtils.hasLength(newItemName)) {
				tsmpDpItem.setItemName(newItemName);
				tsmpDpItem.setUpdateDateTime(DateTimeUtil.now());
				tsmpDpItem.setUpdateUser(userName);
				tsmpDpItem = getTsmpDpItemsDao().save(tsmpDpItem);
			}
		});
	}

	protected void doCreate(String itemNo, String userName, //
		Map<String, String> locales, List<TsmpDpItems> tsmpDpItems) {
		
		for (TsmpDpItems oriTsmpDpItem : tsmpDpItems) {
			String locale = oriTsmpDpItem.getLocale();
			String newItemName = locales.get(locale);
			if (StringUtils.hasLength(newItemName)) {
				TsmpDpItems newTsmpDpItem = new TsmpDpItems();
				newTsmpDpItem.setItemId(oriTsmpDpItem.getItemId());
				newTsmpDpItem.setItemNo(itemNo);
				newTsmpDpItem.setItemName(newItemName);
				newTsmpDpItem.setSubitemNo(oriTsmpDpItem.getSubitemNo());
				newTsmpDpItem.setSubitemName(oriTsmpDpItem.getSubitemName());
				newTsmpDpItem.setSortBy(oriTsmpDpItem.getSortBy());
				newTsmpDpItem.setIsDefault(oriTsmpDpItem.getIsDefault());
				newTsmpDpItem.setParam1(oriTsmpDpItem.getParam1());
				newTsmpDpItem.setParam2(oriTsmpDpItem.getParam2());
				newTsmpDpItem.setParam3(oriTsmpDpItem.getParam3());
				newTsmpDpItem.setParam4(oriTsmpDpItem.getParam4());
				newTsmpDpItem.setParam5(oriTsmpDpItem.getParam5());
				newTsmpDpItem.setLocale(locale);
				newTsmpDpItem.setCreateDateTime(oriTsmpDpItem.getCreateDateTime());
				newTsmpDpItem.setCreateUser(oriTsmpDpItem.getCreateUser());
				newTsmpDpItem.setUpdateDateTime(DateTimeUtil.now());
				newTsmpDpItem.setUpdateUser(userName);
				newTsmpDpItem.setVersion(1L);
				newTsmpDpItem = getTsmpDpItemsDao().save(newTsmpDpItem);
				
				getTsmpDpItemsDao().delete(oriTsmpDpItem);
			} else {
				this.logger.error(String.format("New locale was found: %s", locale));
				throw TsmpDpAaRtnCode._1297.throwing();
			}
		}
	}

	protected TsmpDpItemsDao getTsmpDpItemsDao() {
		return this.tsmpDpItemsDao;
	}

}
