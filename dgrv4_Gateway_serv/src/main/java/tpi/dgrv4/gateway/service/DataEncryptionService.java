package tpi.dgrv4.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.DataEncryptionReq;
import tpi.dgrv4.gateway.vo.DataEncryptionResp;

@Service
public class DataEncryptionService {
	private TsmpTAEASKHelper tsmpTAEASKHelper;

	@Autowired
	public DataEncryptionService(TsmpTAEASKHelper tsmpTAEASKHelper) {
		super();
		this.tsmpTAEASKHelper = tsmpTAEASKHelper;
	}

	public DataEncryptionResp dataEncryption(DataEncryptionReq req) {
		DataEncryptionResp resp = new DataEncryptionResp();
		String data = req.getData();
		String ciphertext = "";
		if (!StringUtils.hasLength(data)) {
			resp.setText(ciphertext);
			return resp;
		}

		try {
			ciphertext = getTAEASKEncode(data);
		} catch (Exception e) {
			ciphertext = "";
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
		}

		resp.setText(ciphertext);
		return resp;
	}

	public String getTAEASKEncode(String data) throws Exception {
		String encoded = getTsmpTAEASKHelper().encrypt(data);
		return encoded;
	}

	protected TsmpTAEASKHelper getTsmpTAEASKHelper() {
		return this.tsmpTAEASKHelper;
	}
}
