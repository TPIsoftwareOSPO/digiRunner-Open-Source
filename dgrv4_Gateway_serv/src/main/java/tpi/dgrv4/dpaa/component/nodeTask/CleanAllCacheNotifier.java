package tpi.dgrv4.dpaa.component.nodeTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import tpi.dgrv4.dpaa.constant.TsmpNodeTaskNames;
import tpi.dgrv4.dpaa.service.TsmpEventsService;
import tpi.dgrv4.entity.repository.TsmpNodeTaskDao;

@Configuration
//@EnableNodeTaskClient
public class CleanAllCacheNotifier extends TsmpNodeTaskNotifier<Void> {
	@Autowired
	public CleanAllCacheNotifier(TsmpEventsService tsmpEventsService, TsmpNodeTaskDao tsmpNodeTaskDao) {
		super(tsmpEventsService, tsmpNodeTaskDao);
	}

	@Override
	public Class<Void> eventType() {
		return Void.class;
	}

	@Override
	protected String taskName() {
		return TsmpNodeTaskNames.Clean_AllCache;
	}

}
