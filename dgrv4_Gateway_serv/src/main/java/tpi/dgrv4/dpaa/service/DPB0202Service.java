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
import tpi.dgrv4.dpaa.vo.DPB0202Req;
import tpi.dgrv4.dpaa.vo.DPB0202Resp;
import tpi.dgrv4.dpaa.vo.DPB0202RespItem;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoJdbc;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoJdbcDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.component.IdPApiHelper;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * @author Mini <br>
 * 查詢 Client ID 的 GTW IdP (JDBC) 資料
 */
@Service
public class DPB0202Service {
	private TPILogger logger = TPILogger.tl;

	private TsmpClientDao tsmpClientDao;
	private DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao;

	@Autowired
	public DPB0202Service(TsmpClientDao tsmpClientDao, DgrGtwIdpInfoJdbcDao dgrGtwIdpInfoJdbcDao) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.dgrGtwIdpInfoJdbcDao = dgrGtwIdpInfoJdbcDao;
	}

	public DPB0202Resp queryGtwIdPInfoByClientId_jdbc(TsmpAuthorization authorization, DPB0202Req req) {
		DPB0202Resp resp = new DPB0202Resp();
		
		try {
			String clientId = req.getClientId();
			if (!StringUtils.hasLength(clientId)) {
				throw TsmpDpAaRtnCode._2025.throwing("{{clientId}}");
			}

			TsmpClient client = getTsmpClientDao().findById(clientId).orElse(null);
			if (client == null) {
				throw TsmpDpAaRtnCode._1344.throwing();
			}

			List<DgrGtwIdpInfoJdbc> infoJdbcList = getDgrGtwIdpInfoJdbcDao()
					.findByClientIdOrderByCreateDateTimeDescGtwIdpInfoJdbcIdDesc(clientId);
			if (infoJdbcList.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			List<DPB0202RespItem> itemList = new ArrayList<>();
			infoJdbcList.forEach(infoJdbc -> {
				Long longId = infoJdbc.getGtwIdpInfoJdbcId();
				String stringId = RandomSeqLongUtil.toHexString(longId, RandomLongTypeEnum.YYYYMMDD);
				String icon = StringUtils.hasLength(infoJdbc.getIconFile()) ? infoJdbc.getIconFile()
						: IdPHelper.DEFULT_ICON_FILE;

				DPB0202RespItem item = new DPB0202RespItem();
				item.setId(stringId);
				item.setLongId(String.valueOf(longId));
				item.setClientId(infoJdbc.getClientId());
				item.setStatus(infoJdbc.getStatus());
				item.setRemark(infoJdbc.getRemark());
				item.setConnectionName(infoJdbc.getConnectionName());
				item.setSqlPtmt(infoJdbc.getSqlPtmt());
				item.setIconFile(icon);
				item.setPageTitle(infoJdbc.getPageTitle());
				item.setCreateDateTime(infoJdbc.getCreateDateTime());
				item.setCreateUser(infoJdbc.getCreateUser());
				item.setUpdateDateTime(infoJdbc.getUpdateDateTime());
				item.setUpdateUser(infoJdbc.getUpdateUser());
				itemList.add(item);
			});

			resp.setDataList(itemList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	protected DgrGtwIdpInfoJdbcDao getDgrGtwIdpInfoJdbcDao() {
		return dgrGtwIdpInfoJdbcDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;

	}
}
