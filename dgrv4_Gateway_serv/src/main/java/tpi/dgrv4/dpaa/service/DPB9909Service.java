package tpi.dgrv4.dpaa.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB9909Item;
import tpi.dgrv4.dpaa.vo.DPB9909Req;
import tpi.dgrv4.dpaa.vo.DPB9909Resp;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9909Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpItemsDao tsmpDpItemsDao;

	@Autowired
	public DPB9909Service(TsmpDpItemsDao tsmpDpItemsDao) {
		super();
		this.tsmpDpItemsDao = tsmpDpItemsDao;
	}

	@Transactional
	public DPB9909Resp updateTsmpDpItemsDetail(TsmpAuthorization auth, DPB9909Req req) {
		String userName = auth.getUserName();
		checkParams(userName, req);
		
		String itemNo = req.getItemNo();
		String oriSubitemNo = req.getOriSubitemNo();
		List<TsmpDpItems> tsmpDpItemsList = getTsmpDpItemsDao().findByItemNo(itemNo, Sort.by("locale"));
		Map<Boolean, List<TsmpDpItems>> ynMap = tsmpDpItemsList.stream() //
			.collect(Collectors.partitioningBy((i) -> oriSubitemNo.equals(i.getSubitemNo())));
		tsmpDpItemsList = ynMap.get(Boolean.TRUE);
		if (CollectionUtils.isEmpty(tsmpDpItemsList)) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		try {
			Map<String, DPB9909Item> locales = combineLocales(req.getSubitemNameList());
			
			String subitemNo = req.getSubitemNo();
			if (subitemNo.equals(oriSubitemNo)) {
				doUpdate(req, locales, tsmpDpItemsList, userName);
			} else {
				doCreate(req, locales, tsmpDpItemsList, userName);
			}
			
			// 2021.10.12, 如果此項子分類為預設, 則相同分類底下的其他子分類應清除預設
			setOtherSubitemsNotDefault(req, ynMap.get(Boolean.FALSE), userName);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();
		}
		
		return new DPB9909Resp();
	}

	protected void checkParams(String userName, DPB9909Req req) {
		if (!StringUtils.hasLength(userName)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}

		String oriSubitemNo = req.getOriSubitemNo();
		if (!StringUtils.hasLength(oriSubitemNo)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		List<DPB9909Item> subitemNameList = req.getSubitemNameList();
		List<String> params = new LinkedList<>();
		for (DPB9909Item dgl9909Item : subitemNameList) {
			params = dgl9909Item.getParams();
			if (params.size() < 5) {
	            throw TsmpDpAaRtnCode._2009.throwing("5");
			}
		}
		
		// 非 "V" 則轉為 null
		String isDefault = req.getIsDefault();
		if (!"V".equals(isDefault)) {
			req.setIsDefault(null);
		}
		
		// 某參數無值則設為 null
		for (int i = 0; i < params.size(); i++) {
			if (!StringUtils.hasLength(params.get(i))) {
				params.set(i, null);
			}
		}
		
		// 每個  locale 只能出現 1 次
		Set<String> locales = new HashSet<>();
		for (DPB9909Item dgl9909Item : req.getSubitemNameList()) {
			Long version = dgl9909Item.getVersion();
			String locale = dgl9909Item.getLocale();
			String subitemName = dgl9909Item.getSubitemName();
			if (!(StringUtils.hasLength(locale) && StringUtils.hasLength(subitemName) && version != null)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			if (locales.contains(locale)) {
				throw TsmpDpAaRtnCode._1284.throwing("{{locale}}");
			}
			locales.add(locale);
		}
	}

	protected Map<String, DPB9909Item> combineLocales(List<DPB9909Item> subitemNameList) {
		Map<String, DPB9909Item> locales = new HashMap<>();
		for (DPB9909Item dgl9909Item : subitemNameList) {
			locales.put(dgl9909Item.getLocale(), dgl9909Item);
		}
		return locales;
	}

	protected void doUpdate(DPB9909Req req, Map<String, DPB9909Item> locales, //
			List<TsmpDpItems> tsmpDpItems, String userName) {

		TsmpDpItems copiedEntity = null;
		for (TsmpDpItems entity : tsmpDpItems) {
			copiedEntity = ServiceUtil.deepCopy(entity, TsmpDpItems.class);
			
			String locale = entity.getLocale();
			DPB9909Item dgl9909Item = locales.get(locale);
			String newSubitemName = Optional.ofNullable(dgl9909Item) //
				.map((i) -> i.getSubitemName()) //
				.orElse(null);
			if (StringUtils.hasLength(newSubitemName)) {
				String params0 = null;
				String params1 = null;
				String params2 = null;
				String params3 = null;
				String params4 = null;
				Long version = null;
				
				if(dgl9909Item != null) {
					params0 = dgl9909Item.getParams().get(0);
					params1 = dgl9909Item.getParams().get(1);
					params2 = dgl9909Item.getParams().get(2);
					params3 = dgl9909Item.getParams().get(3);
					params4 = dgl9909Item.getParams().get(4);
					version = dgl9909Item.getVersion();
				}
				
				copiedEntity.setIsDefault(req.getIsDefault());
				copiedEntity.setSubitemName(newSubitemName);
				copiedEntity.setParam1(params0);
				copiedEntity.setParam2(params1);
				copiedEntity.setParam3(params2);
				copiedEntity.setParam4(params3);
				copiedEntity.setParam5(params4);
				copiedEntity.setUpdateDateTime(DateTimeUtil.now());
				copiedEntity.setUpdateUser(userName);
				copiedEntity.setVersion(version);
				
				// 樂觀鎖
				try {
					copiedEntity = getTsmpDpItemsDao().save(copiedEntity);
				} catch (ObjectOptimisticLockingFailureException e) {
					throw TsmpDpAaRtnCode.ERROR_DATA_EDITED.throwing();
				}
			}
		}
	}

	protected void doCreate(DPB9909Req req, Map<String, DPB9909Item> locales, //
			List<TsmpDpItems> tsmpDpItems, String userName) {
		
		for (TsmpDpItems oriEntity : tsmpDpItems) {
			String locale = oriEntity.getLocale();
			DPB9909Item dgl9909Item = locales.get(locale);
			String newSubitemName = Optional.ofNullable(dgl9909Item) //
				.map((i) -> i.getSubitemName()) //
				.orElse(null);
			if (StringUtils.hasLength(newSubitemName)) {
				String params0 = null;
				String params1 = null;
				String params2 = null;
				String params3 = null;
				String params4 = null;
				
				if(dgl9909Item != null) {
					params0 = dgl9909Item.getParams().get(0);
					params1 = dgl9909Item.getParams().get(1);
					params2 = dgl9909Item.getParams().get(2);
					params3 = dgl9909Item.getParams().get(3);
					params4 = dgl9909Item.getParams().get(4);
				}
				
				TsmpDpItems newEntity = new TsmpDpItems();
				newEntity.setItemId(oriEntity.getItemId());
				newEntity.setItemNo(oriEntity.getItemNo());
				newEntity.setItemName(oriEntity.getItemName());
				newEntity.setSubitemNo(req.getSubitemNo());
				newEntity.setSubitemName(newSubitemName);
				newEntity.setSortBy(oriEntity.getSortBy());
				newEntity.setIsDefault(req.getIsDefault());
				newEntity.setParam1(params0);
				newEntity.setParam2(params1);
				newEntity.setParam3(params2);
				newEntity.setParam4(params3);
				newEntity.setParam5(params4);
				newEntity.setLocale(locale);
				newEntity.setCreateDateTime(oriEntity.getCreateDateTime());
				newEntity.setCreateUser(oriEntity.getCreateUser());
				newEntity.setUpdateDateTime(DateTimeUtil.now());
				newEntity.setUpdateUser(userName);
				newEntity.setVersion(1L);
				newEntity = getTsmpDpItemsDao().save(newEntity);
				
				getTsmpDpItemsDao().delete(oriEntity);
			} else {
				this.logger.error(String.format("New locale was found: %s", locale));
				throw TsmpDpAaRtnCode._1297.throwing();
			}
		}
	}

	protected void setOtherSubitemsNotDefault(DPB9909Req req, List<TsmpDpItems> tsmpDpItemsList, //
			String userName) {
		String isDefault = req.getIsDefault();
		if (!"V".equals(isDefault) || CollectionUtils.isEmpty(tsmpDpItemsList)) {
			return;
		}
		tsmpDpItemsList.forEach(i -> {
			i.setIsDefault(null);
			i.setUpdateDateTime(DateTimeUtil.now());
			i.setUpdateUser(userName);
		});
		getTsmpDpItemsDao().saveAll(tsmpDpItemsList);
	}

	protected TsmpDpItemsDao getTsmpDpItemsDao() {
		return this.tsmpDpItemsDao;
	}

}