package tpi.dgrv4.dpaa.component.nodeTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import tpi.dgrv4.dpaa.constant.TsmpNodeTaskNames;
import tpi.dgrv4.dpaa.service.TsmpEventsService;
import tpi.dgrv4.entity.repository.TsmpNodeTaskDao;

@Configuration
//@EnableNodeTaskClient
public class RefreshDCNotifier extends TsmpNodeTaskNotifier<String> {
	
	@Autowired
	public RefreshDCNotifier(TsmpEventsService tsmpEventsService, TsmpNodeTaskDao tsmpNodeTaskDao) {
		super(tsmpEventsService, tsmpNodeTaskDao);
	}

	@Override
	protected String taskName() {
		return TsmpNodeTaskNames.DEPLOY_CONTAINER_REFRESH;
	}

	@Override
	public Class<String> eventType() {
		return String.class;
	}

}
