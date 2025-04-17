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
import tpi.dgrv4.dpaa.vo.DPB0184Req;
import tpi.dgrv4.dpaa.vo.DPB0184Resp;
import tpi.dgrv4.dpaa.vo.DPB0184RespItem;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoA;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoADao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0184Service {
	private TPILogger logger = TPILogger.tl;
	
	private TsmpClientDao tsmpClientDao;
	private DgrGtwIdpInfoADao dgrGtwIdpInfoADao;

	@Autowired
	public DPB0184Service(TsmpClientDao tsmpClientDao, DgrGtwIdpInfoADao dgrGtwIdpInfoADao) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.dgrGtwIdpInfoADao = dgrGtwIdpInfoADao;
	}

	public DPB0184Resp queryGtwIdPInfoByClientId_api(TsmpAuthorization authorization, DPB0184Req req) {
		DPB0184Resp resp = new DPB0184Resp();
		try {
			String clientId = req.getClientId();
			if (!StringUtils.hasLength(clientId)) {
				throw TsmpDpAaRtnCode._2025.throwing("{{clientId}}");
			}
			TsmpClient client = getTsmpClientDao().findById(clientId).orElse(null);
			if (client == null) {
				throw TsmpDpAaRtnCode._1344.throwing();
			}

			List<DgrGtwIdpInfoA> aList = getDgrGtwIdpInfoADao()
					.findByClientIdOrderByCreateDateTimeDescGtwIdpInfoAIdDesc(clientId);
			if (aList.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			List<DPB0184RespItem> dataList = new ArrayList<>();
			aList.forEach(a -> {
				DPB0184RespItem item = new DPB0184RespItem();
				item.setApiMethod(a.getApiMethod());
				item.setApiUrl(a.getApiUrl());
				item.setClientId(a.getClientId());
				item.setCreateDateTime(a.getCreateDateTime());
				item.setCreateUser(a.getCreateUser());
				Long longId = a.getGtwIdpInfoAId();
				item.setLongId(String.valueOf(longId));
				String stringId = RandomSeqLongUtil.toHexString(longId, RandomLongTypeEnum.YYYYMMDD);
				item.setId(stringId);
				item.setRemark(a.getRemark());
				item.setStatus(a.getStatus());
				item.setUpdateDateTime(a.getUpdateDateTime());
				item.setUpdateUser(a.getUpdateUser());
				String icon = StringUtils.hasLength(a.getIconFile()) ? a.getIconFile() : IdPHelper.DEFULT_ICON_FILE;
				item.setIconFile(icon);
				item.setPageTitle(a.getPageTitle());
				dataList.add(item);
			});

			resp.setDataList(dataList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	protected DgrGtwIdpInfoADao getDgrGtwIdpInfoADao() {

		return dgrGtwIdpInfoADao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;

	}
}
