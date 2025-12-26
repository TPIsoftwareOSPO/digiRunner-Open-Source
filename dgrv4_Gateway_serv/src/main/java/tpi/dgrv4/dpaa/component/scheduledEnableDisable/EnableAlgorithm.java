package tpi.dgrv4.dpaa.component.scheduledEnableDisable;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.TsmpApi;

@Component
public class EnableAlgorithm {

	public void getImmediatelyEnableAlgorithm(TsmpApi tsmpApi, String status) {

		if (StringUtils.hasText(status)) {
			tsmpApi.setApiStatus(status);
			getScheduledEnableAlgorithm(tsmpApi, 0L);
		}
	}

	public void getScheduledEnableAlgorithm(TsmpApi tsmpApi, long l) {

		tsmpApi.setEnableScheduledDate(l);
	}
}
