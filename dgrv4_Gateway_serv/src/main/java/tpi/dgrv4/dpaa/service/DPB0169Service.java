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
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.DPB0169Req;
import tpi.dgrv4.dpaa.vo.DPB0169Resp;
import tpi.dgrv4.dpaa.vo.DPB0169RespItem;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoO;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoODao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0169Service {
	private TsmpClientDao tsmpClientDao;
	private DgrGtwIdpInfoODao dgrGtwIdpInfoODao;

	@Autowired
	public DPB0169Service(TsmpClientDao tsmpClientDao, DgrGtwIdpInfoODao dgrGtwIdpInfoODao) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.dgrGtwIdpInfoODao = dgrGtwIdpInfoODao;
	}

	public DPB0169Resp queryGtwIdPInfoByClientId_oauth2(TsmpAuthorization authorization, DPB0169Req req,
			ReqHeader reqHeader) {
		DPB0169Resp resp = new DPB0169Resp();

		try {
			String clientId = req.getClientId();

			checkParams(clientId);

			List<DgrGtwIdpInfoO> dgrGtwIdpInfoOList = getDgrGtwIdpInfoODao()
					.findByClientIdOrderByCreateDateTimeDescGtwIdpInfoOIdDesc(clientId);
			if (dgrGtwIdpInfoOList.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			List<DPB0169RespItem> respItemList = new ArrayList<>();
			for (DgrGtwIdpInfoO dgrGtwIdpInfoO : dgrGtwIdpInfoOList) {
				DPB0169RespItem respItem = new DPB0169RespItem();
				Long gtwIdpInfoOId = dgrGtwIdpInfoO.getGtwIdpInfoOId();
				String id = RandomSeqLongUtil.toHexString(gtwIdpInfoOId, RandomLongTypeEnum.YYYYMMDD);
				respItem.setId(id);
				respItem.setLongId(String.valueOf(gtwIdpInfoOId));
				respItem.setClientId(dgrGtwIdpInfoO.getClientId());
				respItem.setIdpType(dgrGtwIdpInfoO.getIdpType());
				respItem.setStatus(dgrGtwIdpInfoO.getStatus());
				respItem.setRemark(dgrGtwIdpInfoO.getRemark());
				respItem.setIdpClientId(dgrGtwIdpInfoO.getIdpClientId());
				respItem.setIdpClientName(dgrGtwIdpInfoO.getIdpClientName());
				respItem.setCreateDateTime(dgrGtwIdpInfoO.getCreateDateTime());
				respItem.setCreateUser(dgrGtwIdpInfoO.getCreateUser());
				respItem.setUpdateDateTime(dgrGtwIdpInfoO.getUpdateDateTime());
				respItem.setUpdateUser(dgrGtwIdpInfoO.getUpdateUser());

				respItemList.add(respItem);
			}

			resp.setDataList(respItemList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	private void checkParams(String clientId) {
		if (!StringUtils.hasLength(clientId)) {
			throw TsmpDpAaRtnCode._2025.throwing("clientId");
		}

		TsmpClient client = getTsmpClientDao().findById(clientId).orElse(null);
		if (client == null) {
			throw TsmpDpAaRtnCode._1344.throwing();
		}
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected DgrGtwIdpInfoODao getDgrGtwIdpInfoODao() {
		return dgrGtwIdpInfoODao;
	}
}
