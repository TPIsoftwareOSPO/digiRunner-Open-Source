package tpi.dgrv4.gateway.component.cache.proxy;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.ifs.TraceCodeUtilIfs;
import tpi.dgrv4.gateway.component.cache.core.AbstractCacheProxy;
import tpi.dgrv4.gateway.component.cache.core.GenericCache;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.service.IApiCacheService;
import tpi.dgrv4.gateway.service.ProxyMethodService;
import tpi.dgrv4.gateway.vo.AutoCacheParamVo;
import tpi.dgrv4.gateway.vo.AutoCacheRespVo;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Component
public class ProxyMethodServiceCacheProxy extends AbstractCacheProxy {
	
//	@Autowired(required = false)
	private TraceCodeUtilIfs traceCodeUtil ;

	@Autowired
	public ProxyMethodServiceCacheProxy(@Nullable TraceCodeUtilIfs traceCodeUtil, GenericCache genericCache,
			ObjectMapper objectMapper, ApplicationContext ctx, JobHelper jobHelper) {
		super(genericCache, objectMapper, ctx);
		this.traceCodeUtil = traceCodeUtil;
	}

	public AutoCacheRespVo queryByIdCallApi(String id, IApiCacheService service, AutoCacheParamVo vo) {
		Supplier<AutoCacheRespVo> supplier = () -> {
			
			// 說明 dgr-v4 流程使用
			if (traceCodeUtil != null) traceCodeUtil.logger(this);
			
			HttpRespData respObj = service.callback(vo);
			if(respObj != null) {
				AutoCacheRespVo rsVo = new AutoCacheRespVo();
				rsVo.setId(id);
				rsVo.setRespHeader(respObj.respHeader);
				rsVo.setRespStr(respObj.respStr);
				rsVo.setStatusCode(respObj.statusCode);
				rsVo.setHttpRespArray(respObj.httpRespArray);
				return rsVo;
			}else {
				return null;
			}
			
		};
		
		return getOne("queryByIdCallApi", supplier, AutoCacheRespVo.class, id).orElse(null);
	}
	
	@Override
	protected Class<?> getDaoClass() {
		return ProxyMethodService.class;
	}

	@Override
	protected void kryoRegistration(Kryo kryo) {
		kryo.register(AutoCacheRespVo.class);
	}

}