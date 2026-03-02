package tpi.dgrv4.gateway.service;

import java.util.Map;

import tpi.dgrv4.gateway.vo.AutoCacheParamVo;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

public interface IApiCacheService {

	public HttpRespData callback(AutoCacheParamVo vo, Map<String, String> maskInfo);
}
