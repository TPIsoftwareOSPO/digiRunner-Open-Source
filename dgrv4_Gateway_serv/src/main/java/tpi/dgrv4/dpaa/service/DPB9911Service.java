package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB9911Req;
import tpi.dgrv4.dpaa.vo.DPB9911Resp;
import tpi.dgrv4.entity.entity.jpql.CusSetting;
import tpi.dgrv4.entity.entity.jpql.CusSettingId;
import tpi.dgrv4.entity.repository.CusSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9911Service {

	private TPILogger logger = TPILogger.tl;

	private CusSettingDao cusSettingDao;

	@Autowired
	public DPB9911Service(CusSettingDao cusSettingDao) {
		super();
		this.cusSettingDao = cusSettingDao;
	}

	public DPB9911Resp queryCusSettingDetail(TsmpAuthorization auth, DPB9911Req req) {
		try {
			checkParam(req);
			
			CusSettingId id = new CusSettingId();
			id.setSettingNo(req.getSettingNo());
			id.setSubsettingNo(req.getSubsettingNo());
			CusSetting vo = getCusSettingDao().findById(id).orElse(null);
			if(vo == null) {
				//查無資料
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			DPB9911Resp resp = new DPB9911Resp();
			resp.setCusSettingId(vo.getCusSettingId());
			resp.setSettingNo(vo.getSettingNo());
			resp.setSettingName(vo.getSettingName());
			resp.setSubsettingNo(vo.getSubsettingNo());
			resp.setSubsettingName(vo.getSubsettingName());
			resp.setSortBy(vo.getSortBy());
			resp.setIsDefault(vo.getIsDefault());
			resp.setParam1(ServiceUtil.nvl(vo.getParam1()));
			resp.setParam2(ServiceUtil.nvl(vo.getParam2()));
			resp.setParam3(ServiceUtil.nvl(vo.getParam3()));
			resp.setParam4(ServiceUtil.nvl(vo.getParam4()));
			resp.setParam5(ServiceUtil.nvl(vo.getParam5()));
			resp.setVersion(vo.getVersion());
			
			return resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}
	
	private void checkParam(DPB9911Req req) {
		if(!StringUtils.hasLength(req.getSettingNo()) || !StringUtils.hasLength(req.getSubsettingNo())) {
			//缺少必填參數
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	protected CusSettingDao getCusSettingDao() {
		return cusSettingDao;
	}

}
