package tpi.dgrv4.aws.ifs;

import tpi.dgrv4.common.keeper.ITPILogger;

public interface IdgRaws {
	public Boolean awsApi(String productCode, Integer publicKeyVersion, String nonce, String keyStr, ITPILogger tl)
			throws Exception ;

	public String getRegisterUsageResult() ;
}
