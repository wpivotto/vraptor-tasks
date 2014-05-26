package br.com.caelum.vraptor.tasks.callback;

import javax.enterprise.context.ApplicationScoped;

import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.tasks.TaskStatistics;

@ApplicationScoped
public class TaskLogger implements TaskCallback {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void scheduled(String taskId, Trigger trigger) {
		logger.debug("Task {} was successfully scheduled. Trigger Expression {}", taskId, triggerInfo(trigger));
	}

	@Override
	public void unscheduled(String taskId) {
		logger.debug("Task {} was successfully unscheduled", taskId);
	}

	@Override
	public void beforeExecute(String taskId) {
		logger.debug("Executing task {}", taskId);
	}

	@Override
	public void executionVetoed(String taskId) {
		logger.debug("Task {} execution was vetoed", taskId);
	}

	@Override
	public void executed(String taskId, TaskStatistics stats) {
		logger.debug("Task {} was successfully executed", taskId);
	}
	
	@Override
	public void failed(String taskId, TaskStatistics stats, Exception error) {
		logger.error("Task" + taskId + " was failed", error.getCause());
	}

	@Override
	public void paused(String taskId) {
		logger.debug("Task {} was paused", taskId);
	}

	@Override
	public void resumed(String taskId) {
		logger.debug("Task {} was resumed", taskId);
	}
	
	private String triggerInfo(Trigger trigger){
		if(SimpleTrigger.class.isAssignableFrom(trigger.getClass()))
			return "Fixed Rate: " + ((SimpleTrigger) trigger).getRepeatInterval();
		else
			return "Cron: " + ((CronTrigger) trigger).getCronExpression();
	}
	
	

}
