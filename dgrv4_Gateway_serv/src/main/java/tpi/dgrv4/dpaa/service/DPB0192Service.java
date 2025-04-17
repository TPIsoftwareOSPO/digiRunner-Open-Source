package tpi.dgrv4.dpaa.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.ifs.TsmpCoreTokenBase;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0192Req;
import tpi.dgrv4.dpaa.vo.DPB0192Resp;
import tpi.dgrv4.entity.entity.DgrRdbConnection;
import tpi.dgrv4.entity.repository.DgrRdbConnectionDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0192Service {
	private TPILogger logger = TPILogger.tl;
	
//	@Autowired(required = false)
	private TsmpCoreTokenBase tsmpCoreTokenBase;
	
	private DgrRdbConnectionDao dgrRdbConnectionDao;

	@Autowired
	public DPB0192Service(@Nullable TsmpCoreTokenBase tsmpCoreTokenBase, DgrRdbConnectionDao dgrRdbConnectionDao) {
		super();
		this.tsmpCoreTokenBase = tsmpCoreTokenBase;
		this.dgrRdbConnectionDao = dgrRdbConnectionDao;
	}

	public DPB0192Resp createRdbConnectionInfo(TsmpAuthorization authorization, DPB0192Req req) {
		DPB0192Resp resp = new DPB0192Resp();
		try {
			DgrRdbConnection info = getDgrRdbConnectionDao().findById(req.getConnectionName()).orElse(null);
			if (info != null) {
				throw TsmpDpAaRtnCode._1284.throwing("{{connectionName}}");

			}

			DgrRdbConnection drc = new DgrRdbConnection();
			drc.setConnectionName(req.getConnectionName());
			drc.setConnectionTimeout(req.getConnectionTimeout());
			drc.setCreateUser(authorization.getUserName());
			drc.setDataSourceProperty(req.getDataSourceProperty());
			drc.setIdleTimeout(req.getIdleTimeout());
			drc.setJdbcUrl(req.getJdbcUrl());
			drc.setMaxLifetime(req.getMaxLifetime());
			drc.setMaxPoolSize(req.getMaxPoolSize());
			String mima = req.getMima();
			String encMima = "";

			Pattern pattern = Pattern.compile("^ENC\\((\\S*)\\)$");
			if (StringUtils.hasLength(mima)) {
				Matcher matcher = pattern.matcher(mima);
				if (matcher.matches()) {
					encMima = mima;

				} else {
					encMima = getENCEncode(mima);
				}
			} else {
				encMima = getENCEncode("");
			}
			drc.setMima(encMima);
			drc.setUserName(req.getUserName());
			drc = getDgrRdbConnectionDao().save(drc);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}

		return resp;
	}

	public String getENCEncode(String value) throws Exception {
		String encoded = getTsmpCoreTokenBase().encrypt(value);
		encoded = String.format("ENC(%s)", encoded);
		logger.debug("ENC encode  " + encoded);
		return encoded;
	}

	protected TsmpCoreTokenBase getTsmpCoreTokenBase() {
		return this.tsmpCoreTokenBase;
	}
	
	protected DgrRdbConnectionDao getDgrRdbConnectionDao() {
		return dgrRdbConnectionDao;
	}
}
