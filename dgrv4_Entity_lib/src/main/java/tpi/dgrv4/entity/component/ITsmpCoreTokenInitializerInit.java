package tpi.dgrv4.entity.component;

import org.springframework.context.event.ContextRefreshedEvent;

public interface ITsmpCoreTokenInitializerInit {

	StringBuffer init(ContextRefreshedEvent e, StringBuffer info);

}
