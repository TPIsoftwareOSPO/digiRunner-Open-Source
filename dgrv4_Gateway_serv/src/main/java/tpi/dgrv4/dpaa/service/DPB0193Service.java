package tpi.dgrv4.dpaa.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.ifs.TsmpCoreTokenBase;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0193Req;
import tpi.dgrv4.dpaa.vo.DPB0193Resp;
import tpi.dgrv4.entity.component.cipher.TsmpCoreTokenEntityHelper;
import tpi.dgrv4.entity.entity.DgrRdbConnection;
import tpi.dgrv4.entity.repository.DgrRdbConnectionDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0193Service {
	private TPILogger logger = TPILogger.tl;
	
//	@Autowired(required = false)
	private TsmpCoreTokenBase tsmpCoreTokenBase;
	
	private DgrRdbConnectionDao dgrRdbConnectionDao;

	@Autowired
	public DPB0193Service(@Nullable TsmpCoreTokenBase tsmpCoreTokenBase, DgrRdbConnectionDao dgrRdbConnectionDao) {
		super();
		this.tsmpCoreTokenBase = tsmpCoreTokenBase;
		this.dgrRdbConnectionDao = dgrRdbConnectionDao;
	}

	@Transactional
	public DPB0193Resp updateRdbConnectionInfo(TsmpAuthorization authorization, DPB0193Req req) {
		DPB0193Resp resp = new DPB0193Resp();
		try {
			DgrRdbConnection drc = getDgrRdbConnectionDao().findById(req.getConnectionName()).orElse(null);
			if (drc == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			//預設資料不可刪除
			if("APIM-default-DB".equalsIgnoreCase(req.getConnectionName())) {
				throw TsmpDpAaRtnCode._1286.throwing();
			}

			drc.setConnectionTimeout(req.getConnectionTimeout());
			drc.setUpdateUser(authorization.getUserName());
			drc.setUpdateDateTime(DateTimeUtil.now());
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
			getDgrRdbConnectionDao().saveAndFlush(drc);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();
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
