package tpi.dgrv4.dpaa.service;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0209Req;
import tpi.dgrv4.dpaa.vo.DPB0209Resp;
import tpi.dgrv4.entity.entity.DgrXApiKey;
import tpi.dgrv4.entity.repository.DgrXApiKeyDao;
import tpi.dgrv4.entity.repository.DgrXApiKeyMapDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * @author Mini <br>
 * 刪除 X-Api-Key 的資料
 */
@Service
public class DPB0209Service {
	private TPILogger logger = TPILogger.tl;

	private DgrXApiKeyDao dgrXApiKeyDao;
	private DgrXApiKeyMapDao dgrXApiKeyMapDao;

	@Autowired
	public DPB0209Service(DgrXApiKeyDao dgrXApiKeyDao, DgrXApiKeyMapDao dgrXApiKeyMapDao) {
		super();
		this.dgrXApiKeyDao = dgrXApiKeyDao;
		this.dgrXApiKeyMapDao = dgrXApiKeyMapDao;
	}

	@Transactional
	public DPB0209Resp deleteXApiKey(TsmpAuthorization authorization, DPB0209Req req) {
		DPB0209Resp resp = new DPB0209Resp();
		try {
			String id = req.getId();
			if (!StringUtils.hasLength(id)) {
				throw TsmpDpAaRtnCode._2025.throwing("id");
			}

			long longId = RandomSeqLongUtil.toLongValue(id);
			DgrXApiKey dgrXApiKey = getDgrXApiKeyDao().findById(longId).orElse(null);
			if (dgrXApiKey == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			} else {
				getDgrXApiKeyDao().delete(dgrXApiKey);
				getDgrXApiKeyMapDao().deleteByRefApiKeyId(longId);
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

	protected DgrXApiKeyDao getDgrXApiKeyDao() {
		return dgrXApiKeyDao;
	}

	protected DgrXApiKeyMapDao getDgrXApiKeyMapDao() {
		return dgrXApiKeyMapDao;
	}
}
