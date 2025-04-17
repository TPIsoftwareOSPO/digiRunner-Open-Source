package tpi.dgrv4.dpaa.component.nodeTask;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import tpi.dgrv4.dpaa.constant.TsmpNodeTaskNames;
import tpi.dgrv4.dpaa.service.TsmpEventsService;
import tpi.dgrv4.entity.repository.TsmpNodeTaskDao;

@Configuration
//@EnableNodeTaskClient
public class CleanCacheByTableNameNotifier extends TsmpNodeTaskNotifier<List> {
	@Autowired
	public CleanCacheByTableNameNotifier(TsmpEventsService tsmpEventsService, TsmpNodeTaskDao tsmpNodeTaskDao) {
		super(tsmpEventsService, tsmpNodeTaskDao);
	}

	@Override
	public Class<List> eventType() {
		return java.util.List.class;
	}

	@Override
	protected String taskName() {
		return TsmpNodeTaskNames.Clean_CacheByTableName;
	}

}
