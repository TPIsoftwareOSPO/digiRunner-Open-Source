package tpi.dgrv4.dpaa.service;

import java.util.Map;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0194Req;
import tpi.dgrv4.dpaa.vo.DPB0194Resp;
import tpi.dgrv4.dpaa.vo.DataSourceInfoVo;
import tpi.dgrv4.entity.entity.DgrRdbConnection;
import tpi.dgrv4.entity.repository.DgrRdbConnectionDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0194Service {
	private TPILogger logger = TPILogger.tl;
	
	private DgrRdbConnectionDao dgrRdbConnectionDao;
	private DPB0189Service DPB0189Service;

	@Autowired
	public DPB0194Service(DgrRdbConnectionDao dgrRdbConnectionDao,
			tpi.dgrv4.dpaa.service.DPB0189Service dPB0189Service) {
		super();
		this.dgrRdbConnectionDao = dgrRdbConnectionDao;
		DPB0189Service = dPB0189Service;
	}

	@Transactional
	public DPB0194Resp deleteRdbConnectionInfo(TsmpAuthorization authorization, DPB0194Req req) {
		DPB0194Resp resp = new DPB0194Resp();

		try {
			String cN = req.getConnectionName();

			if (!StringUtils.hasLength(req.getConnectionName())) {
				throw TsmpDpAaRtnCode._2025.throwing("{{ConnectionName}}");
			}
			
			//預設資料不可刪除
			if("APIM-default-DB".equalsIgnoreCase(cN)) {
				throw TsmpDpAaRtnCode._1287.throwing();
			}

			DgrRdbConnection info = getDgrRdbConnectionDao().findById(cN).orElse(null);
			if (info == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			getDgrRdbConnectionDao().delete(info);
			
			
			//釋放connection pool
			Map<String, DataSourceInfoVo> dataSourceMap = getDPB0189Service().getDataSourceMap();
			DataSourceInfoVo vo = dataSourceMap.get(info.getConnectionName());
			if(vo != null) {
				vo.getHikariDataSource().close();
				dataSourceMap.remove(info.getConnectionName());
			}

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}

		return resp;
	}

	protected DgrRdbConnectionDao getDgrRdbConnectionDao() {
		return dgrRdbConnectionDao;
	}

	protected DPB0189Service getDPB0189Service() {
		return DPB0189Service;
	}
	
}
