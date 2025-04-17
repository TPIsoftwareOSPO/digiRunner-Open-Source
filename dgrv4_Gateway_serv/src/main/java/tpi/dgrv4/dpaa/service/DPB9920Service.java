package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9920Req;
import tpi.dgrv4.dpaa.vo.DPB9920Resp;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9920Service {

	private TsmpDpFileDao tsmpDpFileDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB9920Service(TsmpDpFileDao tsmpDpFileDao) {
		super();
		this.tsmpDpFileDao = tsmpDpFileDao;
	}

	@Transactional
	public DPB9920Resp eliminateTsmpDpFile(TsmpAuthorization tsmpAuthorization, DPB9920Req req) {

		try {

			List<Long> fileIdList = req.getFileIdList();

			// 若缺少必填參數則拋出 1296 錯誤。
			if (CollectionUtils.isEmpty(fileIdList)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			/*
			 * Long count = fileIdList.stream().filter(L -> L == null).count(); if (count >
			 * 0) {
			 */
			boolean hasInvalidId = fileIdList.stream().anyMatch(Objects::isNull);
			if (hasInvalidId) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}

			// 確定資料是否存在,並刪除
			checkInformationAndDelete(fileIdList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}

		DPB9920Resp resp = new DPB9920Resp();
		return resp;
	}

	private void checkInformationAndDelete(List<Long> fileIdList) {
		for (Long fileId : fileIdList) {
			boolean state = getTsmpDpFileDao().existsById(fileId);
			if (!state) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			getTsmpDpFileDao().deleteById(fileId);
		}
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

}
