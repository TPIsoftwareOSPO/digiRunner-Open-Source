package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0159Req;
import tpi.dgrv4.dpaa.vo.DPB0159Resp;
import tpi.dgrv4.dpaa.vo.DPB0159RespItem;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoLdap;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoLdapDao;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0159Service {
	private DgrAcIdpInfoLdapDao dgrAcIdpInfoLdapDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0159Service(DgrAcIdpInfoLdapDao dgrAcIdpInfoLdapDao) {
		super();
		this.dgrAcIdpInfoLdapDao = dgrAcIdpInfoLdapDao;
	}

	public DPB0159Resp queryIdPInfoList_ldap(TsmpAuthorization authorization, DPB0159Req req) {
		DPB0159Resp resp = new DPB0159Resp();
		try {
			List<DgrAcIdpInfoLdap> dgrAcIdpInfoLdapList = getDgrAcIdpInfoLdapDao()
					.findAllByOrderByCreateDateTimeDescAcIdpInfoLdapIdDesc();

			if (CollectionUtils.isEmpty(dgrAcIdpInfoLdapList)) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			List<DPB0159RespItem> items = new ArrayList<>();
			for (DgrAcIdpInfoLdap DgrAcIdpInfoLdap : dgrAcIdpInfoLdapList) {
				String StringId = RandomSeqLongUtil.toHexString(DgrAcIdpInfoLdap.getAcIdpInfoLdapId(),
						RandomLongTypeEnum.YYYYMMDD);
				DPB0159RespItem item = new DPB0159RespItem();
				item.setApprovalResultMail(DgrAcIdpInfoLdap.getApprovalResultMail());
				item.setCreateDateTime(DgrAcIdpInfoLdap.getCreateDateTime());
				item.setCreateUser(DgrAcIdpInfoLdap.getCreateUser());
				String iconFile = "";
				if (StringUtils.isBlank(DgrAcIdpInfoLdap.getIconFile())) {
					iconFile = IdPHelper.DEFULT_ICON_FILE;
				} else {
					iconFile = DgrAcIdpInfoLdap.getIconFile();
				}
				item.setIconFile(iconFile);
				item.setId(StringId);
				item.setLdapBaseDn(DgrAcIdpInfoLdap.getLdapBaseDn());
				item.setLdapDn(DgrAcIdpInfoLdap.getLdapDn());
				item.setLdapStatus(DgrAcIdpInfoLdap.getLdapStatus());
				item.setLdapTimeout(DgrAcIdpInfoLdap.getLdapTimeout());
				item.setLdapUrl(DgrAcIdpInfoLdap.getLdapUrl());
				item.setUpdateDateTime(DgrAcIdpInfoLdap.getUpdateDateTime());
				item.setUpdateUser(DgrAcIdpInfoLdap.getUpdateUser());
				item.setPageTitle(DgrAcIdpInfoLdap.getPageTitle());
				items.add(item);
			}
			resp.setLdapIdPInfoList(items);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected DgrAcIdpInfoLdapDao getDgrAcIdpInfoLdapDao() {
		return dgrAcIdpInfoLdapDao;
	}
}
