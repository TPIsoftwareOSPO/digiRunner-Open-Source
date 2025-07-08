package tpi.dgrv4.dpaa.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpRjobStatus;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0101Cron;
import tpi.dgrv4.dpaa.vo.DPB0105Items;
import tpi.dgrv4.dpaa.vo.DPB0105Req;
import tpi.dgrv4.dpaa.vo.DPB0105Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpApptRjob;
import tpi.dgrv4.entity.entity.TsmpDpApptRjobD;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDDao;
import tpi.dgrv4.entity.repository.TsmpDpApptRjobDao;
import tpi.dgrv4.gateway.component.job.appt.ApptRjobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.util.*;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

@Service
@Transactional
public class DPB0105Service {

	private TPILogger logger = TPILogger.tl;

	private DPB0101Service dpb0101Service;
	private TsmpDpApptRjobDao tsmpDpApptRjobDao;
	private TsmpDpApptRjobDDao tsmpDpApptRjobDDao;
	private BcryptParamHelper bcryptParamHelper;
	private ApptRjobDispatcher apptRjobDispatcher;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;

	@Autowired
	public DPB0105Service(DPB0101Service dpb0101Service, TsmpDpApptRjobDao tsmpDpApptRjobDao,
			TsmpDpApptRjobDDao tsmpDpApptRjobDDao, BcryptParamHelper bcryptParamHelper,
			ApptRjobDispatcher apptRjobDispatcher, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.dpb0101Service = dpb0101Service;
		this.tsmpDpApptRjobDao = tsmpDpApptRjobDao;
		this.tsmpDpApptRjobDDao = tsmpDpApptRjobDDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.apptRjobDispatcher = apptRjobDispatcher;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public DPB0105Resp updateRjob(TsmpAuthorization auth, DPB0105Req req, ReqHeader reqHeader) {
		checkParams(auth, req);
		String locale = ServiceUtil.getLocale(reqHeader.getLocale());
		DPB0105Resp resp = null;
		String act = req.getAct();
		String updateUser = auth.getUserName();
		switch(act) {
			case "U":
				resp = doUpdate(req, updateUser, locale);
				break;
			case "P":
				resp = doPause(req, updateUser, locale);
				break;
			case "A":
				resp = doActivate(req, updateUser, locale);
				break;
			case "I":
				resp = doIgnore(req, updateUser, locale);
				break;
			case "S":
				resp = doSuspend(req, updateUser, locale);
				break;
			default:
				// 不會跑到這來
				break;
		};
		return resp;
	}

	private void checkParams(TsmpAuthorization auth, DPB0105Req req) {
		String userName = auth.getUserName();
		String act = req.getAct();
		String apptRjobId = req.getApptRjobId();
		Long lv = req.getLv();
		if (StringUtils.isEmpty(userName) ||
			StringUtils.isEmpty(act) ||
			StringUtils.isEmpty(apptRjobId) ||
			lv == null) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		act = act.toUpperCase();
		req.setAct(act);
		if (!("U".equals(act) || "P".equals(act) || "A".equals(act) || "I".equals(act) || "S".equals(act))) {
			this.logger.debug("未知的動作: " + act);
			throw TsmpDpAaRtnCode._1290.throwing();
		}
	}

	// 更新
	private DPB0105Resp doUpdate(DPB0105Req req, String updateUser, String locale) {
		if (StringUtils.isEmpty(req.getRjobName()) ||
			req.getCronJson() == null ||
			req.getOriDataList() == null || req.getOriDataList().isEmpty() ||
			req.getNewDataList() == null || req.getNewDataList().isEmpty()) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		// 檢查週期排程檔是否存在
		String apptRjobId = req.getApptRjobId();
		Optional<TsmpDpApptRjob> opt = getTsmpDpApptRjobDao().findById(apptRjobId);
		if (!opt.isPresent()) {
			this.logger.debug("週期排程檔ID不存在: " + apptRjobId);
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		// 更新明細
		updateRjobDList(req, updateUser, locale);

		// 更新主檔
		TsmpDpApptRjob rjob = updateRjob(opt.get(), req, updateUser, locale);

		return createResp(rjob, locale);
	}

	private void updateRjobDList(DPB0105Req req, String userName, String locale) {
		String apptRjobId = req.getApptRjobId();
		List<DPB0105Items> oriList = req.getOriDataList();
		List<DPB0105Items> newList = req.getNewDataList();
		// 檢查新的清單
		checkRjobDList(newList, locale);
		
		deleteOld(oriList, newList);
		insertOrUpdateNew(apptRjobId, oriList, newList, userName);
	}

	private void checkRjobDList(List<DPB0105Items> rjobDList, String locale) {
		String refItemNo = null;
		String refSubitemNo = null;
		Integer sortBy = null;
		Set<Integer> sorts = new HashSet<>();	// 用來檢查是否有重複的[執行順序]
		for (DPB0105Items item : rjobDList) {
			refItemNo = item.getRefItemNo();
			refSubitemNo = item.getRefSubitemNo();
			sortBy = item.getSortBy();
			if (
				StringUtils.isEmpty(refItemNo) ||
				sortBy == null ||
				sortBy < 0
			) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			if (sorts.contains(sortBy)) {
				throw TsmpDpAaRtnCode._1284.throwing("SORT_BY");
			} else {
				sorts.add(sortBy);
			}
			
			try {
				refItemNo = getBcryptParamHelper().decode(refItemNo, "SCHED_CATE1", locale);
				item.setRefItemNo(refItemNo);
			} catch (BcryptParamDecodeException e) {
				throw TsmpDpAaRtnCode._1299.throwing();
			}
			
			if (!StringUtils.isEmpty(refSubitemNo)) {
				try {
					refSubitemNo = getBcryptParamHelper().decode(refSubitemNo, refItemNo, locale);
					item.setRefSubitemNo(refSubitemNo);
				} catch (BcryptParamDecodeException e) {
					throw TsmpDpAaRtnCode._1299.throwing();
				}
			}
			
			checkSchedCate(refItemNo, refSubitemNo, locale);
		}
	}

	private void checkSchedCate(String itemNo, String subitemNo, String locale) {
		TsmpDpItems items = getItemsById("SCHED_CATE1", itemNo, false, locale);
		if (items == null) {
			throw TsmpDpAaRtnCode.NO_ITEMS_DATA.throwing();
		}
		if (!StringUtils.isEmpty(subitemNo)) {
			String param1 = items.getParam1();
			if ("-1".equals(param1)) {
				// 不應該有子項目
				throw TsmpDpAaRtnCode._1290.throwing();
			}
			items = getItemsById(itemNo, subitemNo, false, locale);
			if (items == null) {
				throw TsmpDpAaRtnCode.NO_ITEMS_DATA.throwing();
			}
		}
	}

	private void deleteOld(List<DPB0105Items> oriList, List<DPB0105Items> newList) {
		Long apptRjobDId = null;
		boolean isInNewList = false;
		for (DPB0105Items oldD : oriList) {
			apptRjobDId = oldD.getApptRjobDId();
			for (DPB0105Items newD : newList) {
				if (apptRjobDId.equals(newD.getApptRjobDId()) && oldD.getLv().equals(newD.getLv())) {
					isInNewList = true;
					break;
				}
			}
			if (!isInNewList && getTsmpDpApptRjobDDao().existsById(apptRjobDId)) {
				getTsmpDpApptRjobDDao().deleteById(apptRjobDId);
			}
		}
	}

	private void insertOrUpdateNew(String apptRjobId, //
			List<DPB0105Items> oriList, List<DPB0105Items> newList, String userName) {
		Long apptRjobDId = null;
		Long lv = null;
		TsmpDpApptRjobD rjobD = null;
		boolean isInOriList = false;
		for (DPB0105Items newD : newList) {
			apptRjobDId = newD.getApptRjobDId();
			lv = newD.getLv();
			
			for (DPB0105Items oldD : oriList) {
				if (
					apptRjobDId != null && lv != null &&
					apptRjobDId.equals(oldD.getApptRjobDId()) && 
					lv.equals(oldD.getLv())
				) {
					isInOriList = true;
					break;
				}
			}

			if (isInOriList) {
				// 更新
				Optional<TsmpDpApptRjobD> opt = getTsmpDpApptRjobDDao().findById(newD.getApptRjobDId());
				if (!opt.isPresent()) {
					this.logger.debug("找不到週期排程工作項目: " + newD.getApptRjobDId());
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				rjobD = ServiceUtil.deepCopy(opt.get(), TsmpDpApptRjobD.class);
				rjobD.setRefItemNo(newD.getRefItemNo());
				rjobD.setRefSubitemNo(newD.getRefSubitemNo());
				rjobD.setInParams(newD.getInParams());
				rjobD.setIdentifData(newD.getIdentifData());
				rjobD.setSortBy(newD.getSortBy());
				rjobD.setUpdateDateTime(DateTimeUtil.now());
				rjobD.setUpdateUser(userName);
				try {
					rjobD = getTsmpDpApptRjobDDao().save(rjobD);
				} catch (ObjectOptimisticLockingFailureException e) {
					this.logger.debug("週期排程工作項目版本不同: " + rjobD.getVersion());
					throw TsmpDpAaRtnCode.ERROR_DATA_EDITED.throwing();
				}
			} else {
				// 新增
				rjobD = new TsmpDpApptRjobD();
				rjobD.setApptRjobId(apptRjobId);
				rjobD.setRefItemNo(newD.getRefItemNo());
				rjobD.setRefSubitemNo(newD.getRefSubitemNo());
				rjobD.setInParams(newD.getInParams());
				rjobD.setIdentifData(newD.getIdentifData());
				rjobD.setSortBy(newD.getSortBy());
				rjobD.setCreateDateTime(DateTimeUtil.now());
				rjobD.setCreateUser(userName);
				rjobD = getTsmpDpApptRjobDDao().save(rjobD);
			}
		}
	}

	private TsmpDpApptRjob updateRjob(TsmpDpApptRjob rjob, DPB0105Req req, String updateUser, String locale) {
		// 交給 RjobHelper.update() 驗證日期合法性
		Long effDateTime = getDateInMillis(req.getEffDateTime());
		Long invDateTime = getDateInMillis(req.getInvDateTime());
		this.logger.debug(String.format("eff ~ inv = %s ~ %s", req.getEffDateTime(), req.getInvDateTime()));

		Map<String, String> checkCronResult = getDPB0101Service().checkCron(req.getCronJson(), locale);
		this.logger.debug("cron expression = " + checkCronResult.get("cronExpression"));
		this.logger.debug("cron desc = " + checkCronResult.get("cronDesc"));
		String cronJson = getCronJson(req.getCronJson());
		String cronExpression = checkCronResult.get("cronExpression");
		String cronDesc = nvl(checkCronResult.get("cronDesc"));
		
		rjob = ServiceUtil.deepCopy(rjob, TsmpDpApptRjob.class);
		rjob.setRjobName(req.getRjobName());
		rjob.setCronExpression(cronExpression);
		rjob.setCronJson(cronJson);
		rjob.setCronDesc(cronDesc);
		rjob.setNextDateTime(DateTimeUtil.now().getTime());
		rjob.setEffDateTime(effDateTime);
		rjob.setInvDateTime(invDateTime);
		rjob.setRemark(req.getRemark());
		rjob.setUpdateDateTime(DateTimeUtil.now());
		rjob.setUpdateUser(updateUser);
		try {
			rjob = getApptRjobDispatcher().update(rjob, updateUser, locale);
			return rjob;
		} catch (ObjectOptimisticLockingFailureException e) {
			this.logger.debug("週期排程版本不同: " + rjob.getVersion());
			throw TsmpDpAaRtnCode.ERROR_DATA_EDITED.throwing();
		}
	}

	private Long getDateInMillis(String dtStr) {
		if (StringUtils.isEmpty(dtStr)) {
			return null;
		}
		Optional<Date> opt = DateTimeUtil.stringToDateTime(dtStr, DateTimeFormatEnum.西元年月日時分秒_2);
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}
		return opt.get().getTime();
	}

	private String getCronJson(DPB0101Cron cron) {
		try {
			return new ObjectMapper().writeValueAsString(cron);
		} catch (JsonProcessingException e) {
			this.logger.error("無法轉換表單為JSON字串");
			throw TsmpDpAaRtnCode._1242.throwing();
		}
	}

	// 暫停
	private DPB0105Resp doPause(DPB0105Req req, String updateUser, String locale) {
		TsmpDpApptRjob rjob = getApptRjobDispatcher().pause(req.getApptRjobId(), updateUser);
		return createResp(rjob, locale);
	}

	// 啟動
	private DPB0105Resp doActivate(DPB0105Req req, String updateUser, String locale) {
		TsmpDpApptRjob rjob = getApptRjobDispatcher().activate(req.getApptRjobId(), updateUser);
		return createResp(rjob, locale);
	}

	// 略過
	private DPB0105Resp doIgnore(DPB0105Req req, String updateUser, String locale) {
		TsmpDpApptRjob rjob = getApptRjobDispatcher().ignore(req.getApptRjobId(), updateUser);
		return createResp(rjob, locale);
	}

	// 作廢
	private DPB0105Resp doSuspend(DPB0105Req req, String updateUser, String locale) {
		TsmpDpApptRjob rjob = getApptRjobDispatcher().suspend(req.getApptRjobId(), updateUser);
		return createResp(rjob, locale);
	}

	private DPB0105Resp createResp(TsmpDpApptRjob rjob, String locale) {
		DPB0105Resp resp = new DPB0105Resp();
		resp.setApptRjobId(rjob.getApptRjobId());
		resp.setLv(rjob.getVersion());
		resp.setCronExpression(rjob.getCronExpression());
		String nextDateTime = getNextDateTime(rjob.getStatus(), rjob.getNextDateTime());
		resp.setNextDateTime(nextDateTime);
		resp.setStatus(rjob.getStatus());
		String statusName = getStatusName(rjob.getStatus(), locale);
		resp.setStatusName(statusName);
		List<Map<Long, Long>> apptRjobDIds = getApptRjobDIds(rjob.getApptRjobId());
		resp.setApptRjobDIds(apptRjobDIds);
		return resp;
	}

	private String getNextDateTime(String status, Long nextDt) {
		if (nextDt == null ||
			StringUtils.isEmpty(status) ||
			TsmpDpRjobStatus.PAUSE.value().equals(status) ||
			TsmpDpRjobStatus.DISABLED.value().equals(status)
		) {
			return "-";
		}
		return DateTimeUtil.dateTimeToString(new Date(nextDt), DateTimeFormatEnum.西元年月日時分秒_2).orElse(new String());
	}

	private String getStatusName(String status, String locale) {
		if (!StringUtils.isEmpty(status)) {
			TsmpDpItems rjobStatus = getItemsById("RJOB_STATUS", status, false, locale);
			if (rjobStatus != null) {
				return rjobStatus.getSubitemName();
			}
		}
		return new String();
	}

	private TsmpDpItems getItemsById(String itemNo, String subitemNo, boolean errorWhenNotExists, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems i = getTsmpDpItemsCacheProxy().findById(id);
		if (errorWhenNotExists && i == null) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return i;
	}

	private List<Map<Long, Long>> getApptRjobDIds(String apptRjobId) {
		if (StringUtils.isEmpty(apptRjobId)) {
			return Collections.emptyList();
		}
		List<TsmpDpApptRjobD> rjobDList = getTsmpDpApptRjobDDao() //
			.findByApptRjobIdOrderBySortByAscApptRjobDIdAsc(apptRjobId);
		if (rjobDList == null || rjobDList.isEmpty()) {
			return Collections.emptyList();
		}
		List<Map<Long, Long>> apptRjobDIds = new ArrayList<>();
		for (TsmpDpApptRjobD rjobD : rjobDList) {
			Map<Long, Long> apptRjobDId = new HashMap<>();
			apptRjobDId.put(rjobD.getApptRjobDId(), rjobD.getVersion());
			apptRjobDIds.add(apptRjobDId);
		}
		return apptRjobDIds;
	}

	protected DPB0101Service getDPB0101Service() {
		return this.dpb0101Service;
	}

	protected TsmpDpApptRjobDao getTsmpDpApptRjobDao() {
		return this.tsmpDpApptRjobDao;
	}

	protected TsmpDpApptRjobDDao getTsmpDpApptRjobDDao() {
		return this.tsmpDpApptRjobDDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected ApptRjobDispatcher getApptRjobDispatcher() {
		return this.apptRjobDispatcher;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}
	
	

}
