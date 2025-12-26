package tpi.dgrv4.dpaa.component.alert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.CollectionUtils;

public class DpaaAlertNotifierFactory {

	public static List<DpaaAlertNotifier> findNotifiers(DpaaAlertEvent dpaaAlertEvent, List<DpaaAlertNotifier> notifiers) {
		if (CollectionUtils.isEmpty(notifiers)) {
			return Collections.emptyList();
		}

		List<DpaaAlertNotifier> matchedNotifiers = new ArrayList<>();
		
		notifiers.forEach((n) -> {
			// 目前 digiLogs 規劃一定會依據相關 Role 發送 Email
			if (n instanceof DpaaAlertNotifierRoleEmail) {
				matchedNotifiers.add(n);
			}
			
			// 是否要發送"通訊群組"
			Boolean imFlag = dpaaAlertEvent.getEntity().getImFlag();
			if (imFlag && n instanceof DpaaAlertNotifierLine) {
				matchedNotifiers.add(n);
			}
			
			// 是否處理客製告警通知
			Boolean cFlag = dpaaAlertEvent.getEntity().getcFlag();
			if (cFlag && n instanceof DpaaAlertNotifierCustom) {
				matchedNotifiers.add(n);
			}
		});

		return matchedNotifiers;
	}

}