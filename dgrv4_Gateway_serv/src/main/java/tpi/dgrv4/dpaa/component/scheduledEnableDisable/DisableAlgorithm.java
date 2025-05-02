package tpi.dgrv4.dpaa.component.scheduledEnableDisable;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.TsmpApi;

@Component
public class DisableAlgorithm {

	public void getImmediatelyDisableAlgorithm(TsmpApi tsmpApi, String status) {

		if (StringUtils.hasText(status)) {
			tsmpApi.setApiStatus(status);
			getScheduledDisableAlgorithm(tsmpApi, 0L);
		}
	}

	public void getScheduledDisableAlgorithm(TsmpApi tsmpApi, long l) {

		tsmpApi.setDisableScheduledDate(l);
	}
}
