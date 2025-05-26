package tpi.dgrv4.dpaa.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB9913Req;
import tpi.dgrv4.dpaa.vo.DPB9913Resp;
import tpi.dgrv4.entity.entity.jpql.CusSetting;
import tpi.dgrv4.entity.entity.jpql.CusSettingId;
import tpi.dgrv4.entity.repository.CusSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9913Service {

	private TPILogger logger = TPILogger.tl;

	private CusSettingDao cusSettingDao;

	@Autowired
	public DPB9913Service(CusSettingDao cusSettingDao) {
		super();
		this.cusSettingDao = cusSettingDao;
	}

	public DPB9913Resp updateCusSetting(TsmpAuthorization auth, DPB9913Req req) {
		try {

			this.checkParam(auth, req);

			List<CusSetting> list = getCusSettingDao().findBySortBy(req.getSortBy());
			if (!CollectionUtils.isEmpty(list)) {
				if(list.size() > 1) {
					// [{{0}}] 已存在: {{1}}
					throw TsmpDpAaRtnCode._1353.throwing("{{sortBy}}", String.valueOf(req.getSortBy()));
				}else {//size=1
					if(!(req.getOriSettingNo().equals(list.get(0).getSettingNo()) 
							&& req.getOriSubsettingNo().equals(list.get(0).getSubsettingNo()))) {
						// [{{0}}] 已存在: {{1}}
						throw TsmpDpAaRtnCode._1353.throwing("{{sortBy}}", String.valueOf(req.getSortBy()));
					}
				}
				
			}
			
			CusSettingId id = new CusSettingId();
			id.setSettingNo(req.getOriSettingNo());
			id.setSubsettingNo(req.getOriSubsettingNo());
			CusSetting vo = getCusSettingDao().findById(id).orElse(null);
			if (vo == null) {
				// 查無資料
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			vo = ServiceUtil.deepCopy(vo, CusSetting.class);
			vo.setSettingName(req.getSettingName());
			vo.setSubsettingName(req.getSubsettingName());
			vo.setSortBy(req.getSortBy());
			vo.setIsDefault("V".equals(req.getIsDefault()) ? req.getIsDefault() : null);
			vo.setParam1(StringUtils.hasLength(req.getParam1()) ? req.getParam1() : null);
			vo.setParam2(StringUtils.hasLength(req.getParam2()) ? req.getParam2() : null);
			vo.setParam3(StringUtils.hasLength(req.getParam3()) ? req.getParam3() : null);
			vo.setParam4(StringUtils.hasLength(req.getParam4()) ? req.getParam4() : null);
			vo.setParam5(StringUtils.hasLength(req.getParam5()) ? req.getParam5() : null);
			vo.setUpdateUser(auth.getUserName());
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setVersion(req.getVersion());
			
			// 樂觀鎖
			try {
				getCusSettingDao().saveAndFlush(vo);
			} catch (ObjectOptimisticLockingFailureException e) {
				throw TsmpDpAaRtnCode.ERROR_DATA_EDITED.throwing();
			}
			
			DPB9913Resp resp = new DPB9913Resp();
			return resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			// 更新失敗
			throw TsmpDpAaRtnCode._1286.throwing();
		}
	}

	private void checkParam(TsmpAuthorization auth, DPB9913Req req) {
		if (!StringUtils.hasLength(auth.getUserName())) {
			// 使用者名稱:必填參數
			throw TsmpDpAaRtnCode._1258.throwing();
		}

		if (!StringUtils.hasLength(req.getOriSettingNo()) || !StringUtils.hasLength(req.getOriSubsettingNo())
				|| req.getVersion() == null) {
			// 缺少必填參數
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	protected CusSettingDao getCusSettingDao() {
		return cusSettingDao;
	}

}
