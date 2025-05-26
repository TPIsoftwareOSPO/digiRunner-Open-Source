package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.ifs.TsmpCoreTokenBase;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9902Req;
import tpi.dgrv4.dpaa.vo.DPB9902Resp;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9902Service {
    
    public enum EncrptionType{
        ENC,TAEASK,NONE
    }
    
	private TPILogger logger = TPILogger.tl;

//	@Autowired(required = false)
	private TsmpCoreTokenBase tsmpCoreTokenBase;
	
	private TsmpSettingDao tsmpSettingDao;
	private DgrAuditLogService dgrAuditLogService;
    private TsmpTAEASKHelper tsmpTAEASKHelper;
	
    @Autowired
	public DPB9902Service(@Nullable TsmpCoreTokenBase tsmpCoreTokenBase, TsmpSettingDao tsmpSettingDao,
			DgrAuditLogService dgrAuditLogService, TsmpTAEASKHelper tsmpTAEASKHelper) {
		super();
		this.tsmpCoreTokenBase = tsmpCoreTokenBase;
		this.tsmpSettingDao = tsmpSettingDao;
		this.dgrAuditLogService = dgrAuditLogService;
		this.tsmpTAEASKHelper = tsmpTAEASKHelper;
	}

	public DPB9902Resp addTsmpSetting(TsmpAuthorization auth, DPB9902Req req, InnerInvokeParam iip) throws Exception {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_TSMP_SETTING.value());
		
		TsmpSetting setting = checkParams(req);
		try {
			setting = getTsmpSettingDao().saveAndFlush(setting);
			
			//寫入 Audit Log D
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber,
					TsmpSetting.class.getSimpleName(), TableAct.C.value(), null, setting);// C

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.SETTING.value());
			return new DPB9902Resp();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			}
			throw TsmpDpAaRtnCode._1288.throwing();
		}
	}

	protected TsmpSetting checkParams(DPB9902Req req) throws Exception {
		Optional<TsmpSetting> opt = getTsmpSettingDao().findById(req.getId());
		TsmpSetting setting = new TsmpSetting();
		if (opt.isPresent()) {
			throw TsmpDpAaRtnCode._1353.throwing("{{id}}", req.getId());
		}
		
		if (req.getEncrptionType().equals(EncrptionType.ENC.toString())) {
		    String encEncode=getENCEncode(req.getValue());
		    setting.setValue(encEncode);
        }
		else if (req.getEncrptionType().equals(EncrptionType.TAEASK.toString())) {
		   String TAEASKEncode=getTAEASKEncode(req.getValue());
		    setting.setValue(TAEASKEncode);
        }else {
            setting.setValue(req.getValue());
        }
		setting.setId(req.getId());
		 setting.setMemo(req.getMemo());
		return setting;
	}
	
    public String getENCEncode(String value) throws Exception {
        String encoded = getTsmpCoreTokenBase().encrypt(value);
        encoded = String.format("ENC(%s)", encoded);
        logger.debug("ENC encode  " + encoded);
        return encoded;
    }

    public String getTAEASKEncode(String value) {
        String encoded = null;
        encoded = getTsmpTAEASKHelper().encrypt(value);
        logger.debug("TAEASK encode   " + encoded);
        return encoded;
    }
	protected TsmpSettingDao getTsmpSettingDao() {
		return this.tsmpSettingDao;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected TsmpCoreTokenBase getTsmpCoreTokenBase() {
		return this.tsmpCoreTokenBase;
	}
	
    protected TsmpTAEASKHelper getTsmpTAEASKHelper() {
        return this.tsmpTAEASKHelper;
    }
}
