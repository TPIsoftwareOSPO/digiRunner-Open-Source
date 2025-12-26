package tpi.dgrv4.dpaa.component.alert;

import org.springframework.stereotype.Component;

import tpi.dgrv4.entity.entity.jpql.TsmpAlert;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class DpaaAlertNotifierCustom implements DpaaAlertNotifier {

	private TPILogger logger = TPILogger.tl;

	@Override
	public void notice(DpaaAlertEvent dpaaAlertEvent) {
		TsmpAlert entity = dpaaAlertEvent.getEntity();
		this.logger.info(String.format("尚未實作客製化告警通知: alertId=%d, alertName=%s", entity.getAlertId(), entity.getAlertName()));
	}

}