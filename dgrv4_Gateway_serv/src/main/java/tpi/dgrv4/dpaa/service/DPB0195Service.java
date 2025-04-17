package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0195Req;
import tpi.dgrv4.dpaa.vo.DPB0195Resp;
import tpi.dgrv4.dpaa.vo.DPB0195RespItem;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoApi;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoApiDao;
import tpi.dgrv4.gateway.component.IdPApiHelper;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0195Service {
	private TPILogger logger = TPILogger.tl;

	private DgrAcIdpInfoApiDao dgrAcIdpInfoApiDao;

	@Autowired
	public DPB0195Service(DgrAcIdpInfoApiDao dgrAcIdpInfoApiDao) {
		super();
		this.dgrAcIdpInfoApiDao = dgrAcIdpInfoApiDao;
	}

	public DPB0195Resp queryIdPInfoList_api(TsmpAuthorization authorization, DPB0195Req req) {
		DPB0195Resp resp = new DPB0195Resp();
		try {
			List<DPB0195RespItem> items = new ArrayList<>();

			List<DgrAcIdpInfoApi> infoList = getDgrAcIdpInfoApiDao()
					.findAllByOrderByCreateDateTimeDescAcIdpInfoApiIdDesc();
				if(infoList.size() == 0)
					throw TsmpDpAaRtnCode._1298.throwing();

			infoList.forEach(a -> {
				DPB0195RespItem item = new DPB0195RespItem();
				item.setApiMethod(a.getApiMethod());
				item.setApiUrl(a.getApiUrl());
				item.setCreateDateTime(a.getCreateDateTime());
				item.setCreateUser(a.getCreateUser());
				Long longId = a.getAcIdpInfoApiId();
				item.setLongId(String.valueOf(longId));
				String stringId = RandomSeqLongUtil.toHexString(longId, RandomLongTypeEnum.YYYYMMDD);
				item.setId(stringId);
				item.setApprovalResultMail(a.getApprovalResultMail());
				item.setStatus(a.getStatus());
				item.setUpdateDateTime(a.getUpdateDateTime());
				item.setUpdateUser(a.getUpdateUser());
				String icon = StringUtils.hasLength(a.getIconFile()) ? a.getIconFile() : IdPHelper.DEFULT_ICON_FILE;
				item.setIconFile(icon);
				item.setPageTitle(a.getPageTitle());
				items.add(item);
			});

			resp.setDataList(items);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected DgrAcIdpInfoApiDao getDgrAcIdpInfoApiDao() {
		return dgrAcIdpInfoApiDao;
	}
}
