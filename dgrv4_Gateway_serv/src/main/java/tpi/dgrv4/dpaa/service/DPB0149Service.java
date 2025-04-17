package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0149Req;
import tpi.dgrv4.dpaa.vo.DPB0149Resp;
import tpi.dgrv4.dpaa.vo.DPB0149RespItem;
import tpi.dgrv4.entity.entity.DgrAcIdpInfo;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
@Service
public class DPB0149Service {
	private DgrAcIdpInfoDao dgrAcIdpInfoDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0149Service(DgrAcIdpInfoDao dgrAcIdpInfoDao) {
		super();
		this.dgrAcIdpInfoDao = dgrAcIdpInfoDao;
	}

	public DPB0149Resp queryIdPInfoList(TsmpAuthorization authorization, DPB0149Req body) {
		DPB0149Resp resp = new DPB0149Resp();
		try {
			List<DgrAcIdpInfo> dgrAcIdpInfoList = getDgrAcIdpInfoDao()
					.findAllByOrderByCreateDateTimeDescAcIdpInfoIdDesc();
			
			if (CollectionUtils.isEmpty(dgrAcIdpInfoList)) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			List<DPB0149RespItem> items = new ArrayList<>();
			for (DgrAcIdpInfo dgrAcIdpInfo : dgrAcIdpInfoList) {
				DPB0149RespItem item = new DPB0149RespItem();
				String StringId = RandomSeqLongUtil.toHexString(dgrAcIdpInfo.getAcIdpInfoId(),
						RandomLongTypeEnum.YYYYMMDD);
				item.setAuthUrl(dgrAcIdpInfo.getAuthUrl());
				item.setCallbackUrl(dgrAcIdpInfo.getCallbackUrl());
				item.setClientId(dgrAcIdpInfo.getClientId());
				item.setClientMima(dgrAcIdpInfo.getClientMima());
				item.setClientName(dgrAcIdpInfo.getClientName());
				item.setClientStatus(dgrAcIdpInfo.getClientStatus());
				item.setCreateDateTime(dgrAcIdpInfo.getCreateDateTime());
				item.setCreateUser(dgrAcIdpInfo.getCreateUser());
				item.setId(StringId);
				item.setIdpType(dgrAcIdpInfo.getIdpType());
				item.setIdpWellKnownUrl(dgrAcIdpInfo.getWellKnownUrl());
				item.setScope(dgrAcIdpInfo.getScope());
				item.setAccessTokenUrl(dgrAcIdpInfo.getAccessTokenUrl());
				item.setUpdateDateTime(dgrAcIdpInfo.getUpdateDateTime());
				item.setUpdateUser(dgrAcIdpInfo.getUpdateUser());
				items.add(item);
			}
		
			resp.setDgrAcIdpInfo(items);
		
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected DgrAcIdpInfoDao getDgrAcIdpInfoDao() {
		return dgrAcIdpInfoDao;
	}
}
