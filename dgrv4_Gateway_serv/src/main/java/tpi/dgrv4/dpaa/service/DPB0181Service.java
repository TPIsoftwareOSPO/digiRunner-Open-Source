package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0181LdapDataItem;
import tpi.dgrv4.dpaa.vo.DPB0181Req;
import tpi.dgrv4.dpaa.vo.DPB0181Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapD;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapM;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoMLdapDDao;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoMLdapMDao;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0181Service {
	private TPILogger logger = TPILogger.tl;
	
	private DgrAcIdpInfoMLdapDDao dgrAcIdpInfoMLdapDDao;
	private DgrAcIdpInfoMLdapMDao dgrAcIdpInfoMLdapMDao;

	@Autowired
	public DPB0181Service(DgrAcIdpInfoMLdapDDao dgrAcIdpInfoMLdapDDao, DgrAcIdpInfoMLdapMDao dgrAcIdpInfoMLdapMDao) {
		super();
		this.dgrAcIdpInfoMLdapDDao = dgrAcIdpInfoMLdapDDao;
		this.dgrAcIdpInfoMLdapMDao = dgrAcIdpInfoMLdapMDao;
	}

	public DPB0181Resp createIdPInfo_mldap(TsmpAuthorization authorization, DPB0181Req req) {
		DPB0181Resp resp = new DPB0181Resp();
		try {
			checkParm(req);
			DgrAcIdpInfoMLdapM m = new DgrAcIdpInfoMLdapM();
			m.setApprovalResultMail(req.getApprovalResultMail());
			String icon = StringUtils.hasLength(req.getIconFile()) ? req.getIconFile() : IdPHelper.DEFULT_ICON_FILE;

			m.setIconFile(icon);
			m.setLdapTimeout(req.getLdapTimeout());
			String pageTitle = StringUtils.hasLength(req.getPageTitle()) ? req.getPageTitle()
					: IdPHelper.DEFULT_PAGE_TITLE;
			m.setPageTitle(pageTitle);
			m.setPolicy(req.getPolicy());
			m.setStatus(req.getStatus());
			m.setCreateUser(authorization.getUserName());
			m = getDgrAcIdpInfoMLdapMDao().save(m);
			List<DgrAcIdpInfoMLdapD> dList = new ArrayList<>();
			List<DPB0181LdapDataItem> ldapDataList = req.getLdapDataList();
			for (DPB0181LdapDataItem dpb0181LdapDataItem : ldapDataList) {
				DgrAcIdpInfoMLdapD d = new DgrAcIdpInfoMLdapD();
				d.setRefAcIdpInfoMLdapMId(m.getAcIdpInfoMLdapMId());
				d.setCreateUser(authorization.getUserName());
				d.setLdapBaseDn(dpb0181LdapDataItem.getLdapBaseDn());
				d.setLdapDn(dpb0181LdapDataItem.getLdapDn());
				d.setLdapUrl(dpb0181LdapDataItem.getLdapUrl());
				d.setOrderNo(dpb0181LdapDataItem.getOrderNo());
				getDgrAcIdpInfoMLdapDDao().save(d);
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return resp;
	}

	private void checkParm(DPB0181Req req) {
		String URIRegex = "^(ldaps?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		String orderRegex = "^[0-9]*$";
		List<DPB0181LdapDataItem> ldapDataList = req.getLdapDataList();

		if (CollectionUtils.isEmpty(ldapDataList)) {
			throw TsmpDpAaRtnCode._2025.throwing("Detail data");
		}

		Set<Integer> order = new HashSet<>();
		for (DPB0181LdapDataItem item : ldapDataList) {
			Integer orderNo = item.getOrderNo();
			if (orderNo == null) {
				throw TsmpDpAaRtnCode._2025.throwing("order");
			} else {
				Pattern pattern = Pattern.compile(orderRegex);
				Matcher matcher = pattern.matcher(String.valueOf(orderNo));
				if (matcher.matches()) {
					order.add(item.getOrderNo());
				} else {
					throw TsmpDpAaRtnCode._1352.throwing("{{Order}}");
				}

			}

			if (!StringUtils.hasLength(item.getLdapBaseDn())) {
				throw TsmpDpAaRtnCode._2025.throwing("ldapBaseDn");
			}
			if (!StringUtils.hasLength(item.getLdapDn())) {
				throw TsmpDpAaRtnCode._2025.throwing("ldapDn");
			}
			if (!StringUtils.hasLength(item.getLdapUrl())) {
				throw TsmpDpAaRtnCode._2025.throwing("ldapUrl");
			} else {
				Pattern pattern = Pattern.compile(URIRegex);
				Matcher matcher = pattern.matcher(item.getLdapUrl());
				if (!matcher.matches()) {
					throw TsmpDpAaRtnCode._1352.throwing("{{ldapUrl}}");
				}
			}

		}
		if (order.size() != ldapDataList.size()) {
			throw TsmpDpAaRtnCode._1284.throwing("Order");
		}

	}

	protected DgrAcIdpInfoMLdapDDao getDgrAcIdpInfoMLdapDDao() {
		return dgrAcIdpInfoMLdapDDao;
	}

	protected DgrAcIdpInfoMLdapMDao getDgrAcIdpInfoMLdapMDao() {
		return dgrAcIdpInfoMLdapMDao;
	}
}