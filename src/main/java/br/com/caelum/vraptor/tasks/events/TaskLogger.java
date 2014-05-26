package br.com.caelum.vraptor.tasks.events;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class TaskLogger {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public void executed(@Observes TaskExecution event) {
		if (event.getException() != null) 
			logger.error("Task" + event.getTaskId() + " was failed", event.getException().getMessage());
		else
			logger.debug("Task {} was successfully executed", event.getTaskId());
	}
	
}
