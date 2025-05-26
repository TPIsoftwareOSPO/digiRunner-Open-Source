package tpi.dgrv4.dpaa.component.alert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;

@Component
public class DpaaAlertNotifierLine extends DpaaAlertNotifierAbstract {

	@Autowired
	public DpaaAlertNotifierLine(ApptJobDispatcher apptJobDispatcher, ObjectMapper objectMapper) {
		super(apptJobDispatcher, objectMapper);
	}

	@Override
	protected String getRefItemNo() {
		return "DPAA_ALERT";
	}

	@Override
	protected String getRefSubitemNo() {
		return "LINE";
	}

}