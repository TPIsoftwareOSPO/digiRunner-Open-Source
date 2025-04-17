package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpSeqStoreKey;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9912Req;
import tpi.dgrv4.dpaa.vo.DPB9912Resp;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.jpql.CusSetting;
import tpi.dgrv4.entity.entity.jpql.CusSettingId;
import tpi.dgrv4.entity.repository.CusSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9912Service {

	private TPILogger logger = TPILogger.tl;

	private CusSettingDao cusSettingDao;
	private SeqStoreService seqStoreService;

	@Autowired
	public DPB9912Service(CusSettingDao cusSettingDao, SeqStoreService seqStoreService) {
		super();
		this.cusSettingDao = cusSettingDao;
		this.seqStoreService = seqStoreService;
	}

	public DPB9912Resp addCusSetting(TsmpAuthorization auth, DPB9912Req req) {
		try {
			
			if (!StringUtils.hasLength(auth.getUserName())) {
				//使用者名稱:必填參數
				throw TsmpDpAaRtnCode._1258.throwing();
			}
			
			CusSetting vo = getCusSettingDao().findFirstBySortBy(req.getSortBy());
			if(vo != null) {
				//[{{0}}] 已存在: {{1}}
				throw TsmpDpAaRtnCode._1353.throwing("{{sortBy}}", String.valueOf(req.getSortBy()));
			}
			
			CusSettingId id = new CusSettingId();
			id.setSettingNo(req.getSettingNo());
			id.setSubsettingNo(req.getSubsettingNo());
			vo = getCusSettingDao().findById(id).orElse(null);
			if(vo != null) {
				//[{{0}}] 已存在: {{1}}
				throw TsmpDpAaRtnCode._1353.throwing("{{subsettingNo}}", req.getSubsettingNo());
			}
			
			Long cusSettingId = getSeqStoreService().nextSequence(TsmpDpSeqStoreKey.CUS_SETTING);
			vo = new CusSetting();
			vo.setCusSettingId(cusSettingId);
			vo.setSettingNo(req.getSettingNo());
			vo.setSettingName(req.getSettingName());
			vo.setSubsettingNo(req.getSubsettingNo());
			vo.setSubsettingName(req.getSubsettingName());
			vo.setSortBy(req.getSortBy());
			vo.setIsDefault("V".equals(req.getIsDefault()) ? req.getIsDefault() : null);
			vo.setParam1(StringUtils.hasLength(req.getParam1()) ? req.getParam1() : null);
			vo.setParam2(StringUtils.hasLength(req.getParam2()) ? req.getParam2() : null);
			vo.setParam3(StringUtils.hasLength(req.getParam3()) ? req.getParam3() : null);
			vo.setParam4(StringUtils.hasLength(req.getParam4()) ? req.getParam4() : null);
			vo.setParam5(StringUtils.hasLength(req.getParam5()) ? req.getParam5() : null);
			vo.setCreateUser(auth.getUserName());
			
			getCusSettingDao().save(vo);
			
			DPB9912Resp resp = new DPB9912Resp();
			return resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//新增失敗
			throw TsmpDpAaRtnCode._1288.throwing();
		}
	}

	protected CusSettingDao getCusSettingDao() {
		return cusSettingDao;
	}

	protected SeqStoreService getSeqStoreService() {
		return seqStoreService;
	}

	
}
