package br.com.caelum.vraptor.tasks.callback;

import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.TaskStatistics;

@Component
@ApplicationScoped
public class TaskLogger implements TaskCallback {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void scheduled(String taskKey, Trigger trigger) {
		logger.debug("Task {} was successfully scheduled. Trigger Expression {}", taskKey, triggerInfo(trigger));
	}

	@Override
	public void unscheduled(String taskKey) {
		logger.debug("Task {} was successfully unscheduled", taskKey);
	}

	@Override
	public void beforeExecute(String taskKey) {
		logger.debug("Executing task {}", taskKey);
	}

	@Override
	public void executionVetoed(String taskKey) {
		logger.debug("Task {} execution was vetoed", taskKey);
	}

	@Override
	public void executed(String taskKey, TaskStatistics stats) {
		logger.debug("Task {} was successfully executed", taskKey);
	}
	
	@Override
	public void failed(String taskKey, TaskStatistics stats, Exception error) {
		logger.error("Task" + taskKey + " was failed", error.getCause());
	}

	@Override
	public void paused(String taskKey) {
		logger.debug("Task {} was paused", taskKey);
	}

	@Override
	public void resumed(String taskKey) {
		logger.debug("Task {} was resumed", taskKey);
	}
	
	private String triggerInfo(Trigger trigger){
		if(SimpleTrigger.class.isAssignableFrom(trigger.getClass()))
			return "Fixed Rate: " + ((SimpleTrigger) trigger).getRepeatInterval();
		else
			return "Cron: " + ((CronTrigger) trigger).getCronExpression();
	}
	
	

}
