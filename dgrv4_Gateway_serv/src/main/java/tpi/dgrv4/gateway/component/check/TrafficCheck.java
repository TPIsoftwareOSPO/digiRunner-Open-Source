package tpi.dgrv4.gateway.component.check;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.exceptions.ICheck;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpClientCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpRtnCodeService;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.vo.TpsVo;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Component
public class TrafficCheck implements ICheck{
	public Map<String, TpsVo> map = new HashMap<>();

	private final TsmpSettingService tsmpSettingService;
	private final TsmpClientCacheProxy tsmpClientCacheProxy;
	private final TsmpRtnCodeService tsmpRtnCodeService;

	public boolean check(String clientId) {
		boolean isEnabled = getTsmpSettingService().getVal_CHECK_TRAFFIC_ENABLE();
		if(isEnabled) {
			TpsVo tpsVo = map.get(clientId);
			if(tpsVo == null) {
				tpsVo = generatorTpsVo(clientId);
				map.put(clientId, tpsVo);
			}else {
				long nowTimestampSec = getNowTimestampSec();
				if(tpsVo.getTimestampSec() != nowTimestampSec) {
					tpsVo = generatorTpsVo(clientId);
					map.put(clientId, tpsVo);
				}else {
					tpsVo.setNumber(tpsVo.getNumber() + 1);
				}
			}
			
			if (tpsVo.getLimit() == 0) {
				// [ZH] 0 為不限制次數,故不用檢查
				// [EN] 0 means unlimited times, so no need to check
				return false;
			}
			
			if(tpsVo.getNumber() > tpsVo.getLimit()) {
				return true;
			}
		}
		return false;
	}
	
	private TpsVo generatorTpsVo(String clientId) {
		TpsVo tpsVo = new TpsVo();
		tpsVo.setTimestampSec(getTspTimestampSec());
		tpsVo.setNumber(1);
		TsmpClient tsmpClient = getTsmpClientCacheProxy().findById(clientId).orElse(null);
		if(tsmpClient == null) {
			TPILogger.tl.debug("not found clientId is " + clientId);
			throw new DgrException(HttpStatus.UNAUTHORIZED.value(), new ICheck() {
				@Override
				public String getMessage(String locale) {
					TsmpRtnCode tsmpRtnCode = getTsmpRtnCodeService().findById(getRtnCode().getCode(), locale);
					if (tsmpRtnCode!=null) {
						return tsmpRtnCode.getTsmpRtnMsg();	
					}else {
						return getRtnCode().getDefaultMessage();
					}
				}
				
				@Override
				public DgrRtnCode getRtnCode() {
					return DgrRtnCode._1344;
				}
			});
			
		}else {
			tpsVo.setLimit(tsmpClient.getTps());
		}
		
		return tpsVo;
	}
	
	protected long getTspTimestampSec() {
		return System.currentTimeMillis() / 1000;
	}
	
	protected long getNowTimestampSec() {
		return System.currentTimeMillis() / 1000;
	}

	protected void setMap(Map<String, TpsVo> map) {//主要是testcase要用的
		this.map = map;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected TsmpClientCacheProxy getTsmpClientCacheProxy() {
		return tsmpClientCacheProxy;
	}

	
	protected TsmpRtnCodeService getTsmpRtnCodeService() {
		return this.tsmpRtnCodeService;
	}
	
	
	@Override
	public DgrRtnCode getRtnCode() {
		return DgrRtnCode._9906;
	}

	@Override
	public String getMessage(String locale) {
		TsmpRtnCode traffic_tsmpRtnCode = getTsmpRtnCodeService().findById(getRtnCode().getCode(), locale);
		if (traffic_tsmpRtnCode!=null) {
			return traffic_tsmpRtnCode.getTsmpRtnMsg();	
		}else {
			return getRtnCode().getDefaultMessage();
		}
	}

	
	
	
}
