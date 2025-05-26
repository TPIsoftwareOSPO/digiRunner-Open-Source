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
import tpi.dgrv4.dpaa.vo.DPB0164Req;
import tpi.dgrv4.dpaa.vo.DPB0164Resp;
import tpi.dgrv4.dpaa.vo.DPB0164RespItem;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoL;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoLDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0164Service {
	private TsmpClientDao tsmpClientDao;
	private DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0164Service(TsmpClientDao tsmpClientDao, DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.dgrGtwIdpInfoLDao = dgrGtwIdpInfoLDao;
	}

	public DPB0164Resp queryGtwIdPInfoByClientId_ldap(TsmpAuthorization authorization, DPB0164Req req) {
		DPB0164Resp resp = new DPB0164Resp();
		try {
			chekParm(req);
			String clientId = req.getClientId();
			TsmpClient client = getTsmpClientDao().findById(clientId).orElse(null);
			if (client == null) {
				throw TsmpDpAaRtnCode._1344.throwing();
			}
			List<DgrGtwIdpInfoL> dgrGtwIdpInfoL = getDgrGtwIdpInfoLDao()
					.findByClientIdOrderByCreateDateTimeDescGtwIdpInfoLIdDesc(clientId);
			if (dgrGtwIdpInfoL.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			List<DPB0164RespItem> respItems = new ArrayList<>();

			for (DgrGtwIdpInfoL infoL : dgrGtwIdpInfoL) {
				DPB0164RespItem item = new DPB0164RespItem();
				String id = RandomSeqLongUtil.toHexString(infoL.getGtwIdpInfoLId(),
						RandomLongTypeEnum.YYYYMMDD);
				item.setId(id);
				item.setLongId(String.valueOf(infoL.getGtwIdpInfoLId()));
				item.setStatus(infoL.getStatus());
				item.setRemark(infoL.getRemark());
				item.setLdapUrl(infoL.getLdapUrl());
				item.setLdapDn(infoL.getLdapDn());
				item.setLdapTimeout(infoL.getLdapTimeout());
				item.setIconFile(infoL.getIconFile());
				item.setPageTitle(infoL.getPageTitle());
				item.setCreateDateTime(infoL.getCreateDateTime());
				item.setCreateUser(infoL.getCreateUser());
				item.setUpdateDateTime(infoL.getUpdateDateTime());
				item.setUpdateUser(infoL.getUpdateUser());
				item.setLdapBaseDn(infoL.getLdapBaseDn());
				respItems.add(item);
			}
			resp.setDataList(respItems);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	private void chekParm(DPB0164Req req) {
		if (!StringUtils.hasLength(req.getClientId())) {
			throw TsmpDpAaRtnCode._2025.throwing("clientId");
		}

	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected DgrGtwIdpInfoLDao getDgrGtwIdpInfoLDao() {
		return dgrGtwIdpInfoLDao;
	}
}
