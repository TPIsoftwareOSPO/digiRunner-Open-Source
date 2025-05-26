package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9914Req;
import tpi.dgrv4.dpaa.vo.DPB9914Resp;
import tpi.dgrv4.entity.entity.jpql.CusSetting;
import tpi.dgrv4.entity.entity.jpql.CusSettingId;
import tpi.dgrv4.entity.repository.CusSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9914Service {

	private TPILogger logger = TPILogger.tl;

	private CusSettingDao cusSettingDao;

	@Autowired
	public DPB9914Service(CusSettingDao cusSettingDao) {
		super();
		this.cusSettingDao = cusSettingDao;
	}

	public DPB9914Resp deleteCusSetting(TsmpAuthorization auth, DPB9914Req req) {
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
			
			getCusSettingDao().deleteById(id);
			
			DPB9914Resp resp = new DPB9914Resp();
			return resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//刪除失敗
			throw TsmpDpAaRtnCode._1287.throwing();
		}
	}
	
	private void checkParam(DPB9914Req req) {
		if(!StringUtils.hasLength(req.getSettingNo()) || !StringUtils.hasLength(req.getSubsettingNo())) {
			//缺少必填參數
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	protected CusSettingDao getCusSettingDao() {
		return cusSettingDao;
	}

}
