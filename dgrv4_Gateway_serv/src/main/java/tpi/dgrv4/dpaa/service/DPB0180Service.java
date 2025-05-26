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
import tpi.dgrv4.dpaa.vo.DPB0180LdapDataItem;
import tpi.dgrv4.dpaa.vo.DPB0180Req;
import tpi.dgrv4.dpaa.vo.DPB0180Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapD;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapM;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoMLdapDDao;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoMLdapMDao;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0180Service {
	private TPILogger logger = TPILogger.tl;
	
	private DgrAcIdpInfoMLdapDDao dgrAcIdpInfoMLdapDDao;
	private DgrAcIdpInfoMLdapMDao dgrAcIdpInfoMLdapMDao;

	@Autowired
	public DPB0180Service(DgrAcIdpInfoMLdapDDao dgrAcIdpInfoMLdapDDao, DgrAcIdpInfoMLdapMDao dgrAcIdpInfoMLdapMDao) {
		super();
		this.dgrAcIdpInfoMLdapDDao = dgrAcIdpInfoMLdapDDao;
		this.dgrAcIdpInfoMLdapMDao = dgrAcIdpInfoMLdapMDao;
	}

	public DPB0180Resp queryIdPInfoDetailByPk_mldap(TsmpAuthorization authorization, DPB0180Req req) {
		DPB0180Resp resp = new DPB0180Resp();
		try {
			if (!StringUtils.hasLength(req.getMasterId())) {
				throw TsmpDpAaRtnCode._2025.throwing("masterId");
			}
			String masterId = req.getMasterId();
			long masterLongId = RandomSeqLongUtil.toLongValue(masterId);

			DgrAcIdpInfoMLdapM mdata = getDgrAcIdpInfoMLdapMDao().findById(masterLongId).orElse(null);
			if (mdata == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			String pageTitle = "";
			if (!StringUtils.hasLength(mdata.getPageTitle())) {
				pageTitle = IdPHelper.DEFULT_PAGE_TITLE;
			} else {
				pageTitle = mdata.getPageTitle();
			}

			resp.setPageTitle(pageTitle);
			String iconfile = "";
			if (!StringUtils.hasLength(mdata.getIconFile())) {
				iconfile = IdPHelper.DEFULT_ICON_FILE;
			} else {
				iconfile = mdata.getIconFile();
			}
			resp.setIconFile(iconfile);
			resp.setMasterId(masterId);
			resp.setMasterLongId(String.valueOf(masterLongId));
			resp.setApprovalResultMail(mdata.getApprovalResultMail());
			resp.setLdapTimeout(mdata.getLdapTimeout());
			resp.setPolicy(mdata.getPolicy());
			resp.setStatus(mdata.getStatus());
			List<DPB0180LdapDataItem> dataList = new ArrayList<>();

			List<DgrAcIdpInfoMLdapD> dList = getDgrAcIdpInfoMLdapDDao()
					.findAllByRefAcIdpInfoMLdapMIdOrderByOrderNoAscAcIdpInfoMLdapDIdAsc(masterLongId);

			for (DgrAcIdpInfoMLdapD dgrAcIdpInfoMLdapD : dList) {
				DPB0180LdapDataItem item = new DPB0180LdapDataItem();
				item.setCreateDateTime(dgrAcIdpInfoMLdapD.getCreateDateTime());
				item.setCreateUser(dgrAcIdpInfoMLdapD.getCreateUser());
				Long dId = dgrAcIdpInfoMLdapD.getAcIdpInfoMLdapDId();

				item.setDetailId(RandomSeqLongUtil.toHexString(dId, RandomLongTypeEnum.YYYYMMDD));
				item.setDetailLongId(String.valueOf(dId));
				item.setLdapBaseDn(dgrAcIdpInfoMLdapD.getLdapBaseDn());
				item.setLdapDn(dgrAcIdpInfoMLdapD.getLdapDn());
				item.setLdapUrl(dgrAcIdpInfoMLdapD.getLdapUrl());
				item.setOrderNo(dgrAcIdpInfoMLdapD.getOrderNo());
				item.setUpdateDateTime(dgrAcIdpInfoMLdapD.getUpdateDateTime());
				item.setUpdateUser(dgrAcIdpInfoMLdapD.getUpdateUser());

				dataList.add(item);
			}

			resp.setLdapDataList(dataList);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;

	}

	protected DgrAcIdpInfoMLdapDDao getDgrAcIdpInfoMLdapDDao() {
		return dgrAcIdpInfoMLdapDDao;
	}

	protected DgrAcIdpInfoMLdapMDao getDgrAcIdpInfoMLdapMDao() {
		return dgrAcIdpInfoMLdapMDao;
	}
}