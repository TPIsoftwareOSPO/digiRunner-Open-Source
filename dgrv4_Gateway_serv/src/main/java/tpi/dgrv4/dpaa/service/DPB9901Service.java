package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.vo.DPB9901Req;
import tpi.dgrv4.dpaa.vo.DPB9901Resp;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9901Service {

	private TsmpSettingDao tsmpSettingDao;

	@Autowired
	public DPB9901Service(TsmpSettingDao tsmpSettingDao) {
		super();
		this.tsmpSettingDao = tsmpSettingDao;
	}

	public DPB9901Resp queryTsmpSettingDetail(TsmpAuthorization auth, DPB9901Req req) {
		String id = Optional.ofNullable(req.getId()) //
			.map(sId -> StringUtils.hasLength(sId) ? sId : null) //
			.orElseThrow(TsmpDpAaRtnCode._1296::throwing);

		TsmpSetting setting = getTsmpSettingDao().findById(id).orElseThrow(TsmpDpAaRtnCode._1298::throwing);

		DPB9901Resp resp = new DPB9901Resp();
		resp.setId(setting.getId());
		resp.setValue(setting.getValue());
		resp.setMemo(setting.getMemo());
		return resp;
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return this.tsmpSettingDao;
	}

}