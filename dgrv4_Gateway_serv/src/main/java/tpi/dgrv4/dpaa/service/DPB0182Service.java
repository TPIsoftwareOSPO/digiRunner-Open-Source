package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0182LdapDataItem;
import tpi.dgrv4.dpaa.vo.DPB0182Req;
import tpi.dgrv4.dpaa.vo.DPB0182Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapD;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapM;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoMLdapDDao;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoMLdapMDao;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0182Service {
	private TPILogger logger = TPILogger.tl;
	
	private DgrAcIdpInfoMLdapDDao dgrAcIdpInfoMLdapDDao;
	private DgrAcIdpInfoMLdapMDao dgrAcIdpInfoMLdapMDao;

	@Autowired
	public DPB0182Service(DgrAcIdpInfoMLdapDDao dgrAcIdpInfoMLdapDDao, DgrAcIdpInfoMLdapMDao dgrAcIdpInfoMLdapMDao) {
		super();
		this.dgrAcIdpInfoMLdapDDao = dgrAcIdpInfoMLdapDDao;
		this.dgrAcIdpInfoMLdapMDao = dgrAcIdpInfoMLdapMDao;
	}

	@Transactional
	public DPB0182Resp updateIdPInfo_mldap(TsmpAuthorization authorization, DPB0182Req req) {
		DPB0182Resp resp = new DPB0182Resp();

		try {
			checkParm(req);
			String masterId = req.getMasterId();
			long masterLongId = RandomSeqLongUtil.toLongValue(masterId);
			DgrAcIdpInfoMLdapM ldapM = getDgrAcIdpInfoMLdapMDao().findById(masterLongId).orElse(null);
			if (ldapM == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			String icon = StringUtils.hasLength(req.getIconFile()) ? req.getIconFile() : IdPHelper.DEFULT_ICON_FILE;

			String pageTitle = StringUtils.hasLength(req.getPageTitle()) ? req.getPageTitle()
					: IdPHelper.DEFULT_PAGE_TITLE;
			ldapM.setApprovalResultMail(req.getApprovalResultMail());
			ldapM.setIconFile(icon);
			ldapM.setLdapTimeout(req.getLdapTimeout());
			ldapM.setPageTitle(pageTitle);
			ldapM.setPolicy(req.getPolicy());
			ldapM.setStatus(req.getStatus());
			ldapM.setUpdateUser(authorization.getUserName());

			List<DPB0182LdapDataItem> reqDataList = req.getLdapDataList();
			Map<Long, DPB0182LdapDataItem> reqDetailMap = new HashMap<>();
			for (DPB0182LdapDataItem dpb0182LdapDataItem : reqDataList) {
				Long detailLongId = RandomSeqLongUtil.toLongValue(dpb0182LdapDataItem.getDetailId());

				if (detailLongId != 0 || detailLongId != null) {
					reqDetailMap.put(detailLongId, dpb0182LdapDataItem);
				}

			}
			List<DgrAcIdpInfoMLdapD> dList = getDgrAcIdpInfoMLdapDDao().findAllByRefAcIdpInfoMLdapMId(masterLongId);

			List<DgrAcIdpInfoMLdapD> deletList = new ArrayList<>();
			// 如果不存在req的map 則代表要刪除 ，如果存在 視為 更新
			dList.forEach(d -> {
				if (!reqDetailMap.containsKey(d.getAcIdpInfoMLdapDId())) {
					deletList.add(d);
				} else {
					DPB0182LdapDataItem updateData = reqDetailMap.get(d.getAcIdpInfoMLdapDId());
					d.setLdapBaseDn(updateData.getLdapBaseDn());
					d.setLdapDn(updateData.getLdapDn());
					d.setLdapUrl(updateData.getLdapUrl());
					d.setOrderNo(updateData.getOrderNo());
					d.setUpdateUser(authorization.getUserName());
					d.setUpdateDateTime(DateTimeUtil.now());
					d = getDgrAcIdpInfoMLdapDDao().save(d);
				}
			});
			getDgrAcIdpInfoMLdapDDao().deleteAll(deletList);
			// 如果req 的 id 是空 則視為 新增

			reqDataList.forEach(v -> {
				String id = v.getDetailId();

				if (!StringUtils.hasLength(id)) {

					DgrAcIdpInfoMLdapD d = new DgrAcIdpInfoMLdapD();
					d.setCreateUser(authorization.getUserName());
					d.setCreateDateTime(DateTimeUtil.now());
					d.setLdapBaseDn(v.getLdapBaseDn());
					d.setLdapDn(v.getLdapDn());
					d.setLdapUrl(v.getLdapUrl());
					d.setOrderNo(v.getOrderNo());
					d.setRefAcIdpInfoMLdapMId(masterLongId);
					d = getDgrAcIdpInfoMLdapDDao().save(d);

				}
			});

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();
		}

		return resp;
	}

	private void checkParm(DPB0182Req req) {
		String URIRegex = "^(ldaps?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		String orderRegex = "^[0-9]*$";

		if (!StringUtils.hasLength(req.getMasterId())) {
			throw TsmpDpAaRtnCode._2025.throwing("masterId");
		}
		List<DPB0182LdapDataItem> ldapDataList = req.getLdapDataList();

		if (CollectionUtils.isEmpty(ldapDataList)) {
			throw TsmpDpAaRtnCode._2025.throwing("Detail data");
		}

		Set<Integer> order = new HashSet<>();
		for (DPB0182LdapDataItem item : ldapDataList) {
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
