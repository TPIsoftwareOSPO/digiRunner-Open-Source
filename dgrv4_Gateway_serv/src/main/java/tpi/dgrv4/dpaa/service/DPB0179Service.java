package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0179IdPInfoItem;
import tpi.dgrv4.dpaa.vo.DPB0179LdapDataItem;
import tpi.dgrv4.dpaa.vo.DPB0179Req;
import tpi.dgrv4.dpaa.vo.DPB0179Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapD;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapM;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoMLdapDDao;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoMLdapMDao;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0179Service {
	private TPILogger logger = TPILogger.tl;
	
	private DgrAcIdpInfoMLdapDDao dgrAcIdpInfoMLdapDDao;
	private DgrAcIdpInfoMLdapMDao dgrAcIdpInfoMLdapMDao;

	@Autowired
	public DPB0179Service(DgrAcIdpInfoMLdapDDao dgrAcIdpInfoMLdapDDao, DgrAcIdpInfoMLdapMDao dgrAcIdpInfoMLdapMDao) {
		super();
		this.dgrAcIdpInfoMLdapDDao = dgrAcIdpInfoMLdapDDao;
		this.dgrAcIdpInfoMLdapMDao = dgrAcIdpInfoMLdapMDao;
	}

	public DPB0179Resp queryIdPInfoList_mldap(TsmpAuthorization authorization, DPB0179Req req) {
		DPB0179Resp resp = new DPB0179Resp();
		try {

			List<DPB0179IdPInfoItem> idpInfoList = new ArrayList<>();
			List<DgrAcIdpInfoMLdapM> list = getDgrAcIdpInfoMLdapMDao()
					.findAllByOrderByCreateDateTimeDescAcIdpInfoMLdapMIdDesc();
			if (CollectionUtils.isEmpty(list)) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			for (DgrAcIdpInfoMLdapM infoMItem : list) {

				DPB0179IdPInfoItem item = new DPB0179IdPInfoItem();

				String StringId = RandomSeqLongUtil.toHexString(infoMItem.getAcIdpInfoMLdapMId(),
						RandomLongTypeEnum.YYYYMMDD);
				item.setId(StringId);
				item.setLdapTimeout(infoMItem.getLdapTimeout());
				String pageTitle = "";
				if (!StringUtils.hasLength(infoMItem.getPageTitle())) {
					pageTitle = IdPHelper.DEFULT_PAGE_TITLE;
				} else {
					pageTitle = infoMItem.getPageTitle();
				}

				item.setPageTitle(pageTitle);
				String iconfile = "";
				if (!StringUtils.hasLength(infoMItem.getIconFile())) {
					iconfile = IdPHelper.DEFULT_ICON_FILE;
				} else {
					iconfile = infoMItem.getIconFile();
				}

				item.setIconFile(iconfile);
				item.setStatus(infoMItem.getStatus());
				item.setLongId(String.valueOf(infoMItem.getAcIdpInfoMLdapMId()));
				List<DgrAcIdpInfoMLdapD> infoDList = getDgrAcIdpInfoMLdapDDao()
						.findAllByRefAcIdpInfoMLdapMIdOrderByOrderNoAscAcIdpInfoMLdapDIdAsc(
								infoMItem.getAcIdpInfoMLdapMId());
				List<DPB0179LdapDataItem> dataItemList = new ArrayList<>();
				for (DgrAcIdpInfoMLdapD infoD : infoDList) {
					DPB0179LdapDataItem dataItem = new DPB0179LdapDataItem();

					dataItem.setLdapBaseDn(infoD.getLdapBaseDn());
					dataItem.setLdapDn(infoD.getLdapDn());
					dataItem.setLdapUrl(infoD.getLdapUrl());
					dataItem.setOrderNo(infoD.getOrderNo());

					dataItem.setLongId(String.valueOf(infoD.getAcIdpInfoMLdapDId()));
					dataItem.setCreateDateTime(infoD.getCreateDateTime());
					dataItem.setCreateUser(infoD.getCreateUser());
					dataItem.setUpdateDateTime(infoD.getUpdateDateTime());
					dataItem.setUpdateUser(infoD.getUpdateUser());
					dataItemList.add(dataItem);
				}
				idpInfoList.add(item);
				item.setLdapDataList(dataItemList);
				
			}

			resp.setIdPInfoList(idpInfoList);
			
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

