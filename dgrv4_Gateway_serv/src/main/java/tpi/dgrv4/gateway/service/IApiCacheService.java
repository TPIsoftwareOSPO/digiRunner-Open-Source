package tpi.dgrv4.gateway.service;

import tpi.dgrv4.gateway.vo.AutoCacheParamVo;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

public interface IApiCacheService {

	public HttpRespData callback(AutoCacheParamVo vo);
}
