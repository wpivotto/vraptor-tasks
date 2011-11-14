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
	public void scheduled(Class<? extends Task> task, Trigger trigger) {
		logger.debug("Task {} was successfully scheduled. Trigger Expression {}", task.getName(), triggerInfo(trigger));
	}

	@Override
	public void unscheduled(Class<? extends Task> task) {
		logger.debug("Task {} was successfully unscheduled", task.getName());
	}

	@Override
	public void beforeExecute(Class<? extends Task> task) {
		logger.debug("Executing task {}", task.getName());
	}

	@Override
	public void executionVetoed(Class<? extends Task> task) {
		logger.debug("Task {} execution was vetoed", task.getName());
	}

	@Override
	public void executed(Class<? extends Task> task, TaskStatistics stats) {
		logger.debug("Task {} was successfully executed", task.getName());
	}
	
	@Override
	public void failed(Class<? extends Task> task, TaskStatistics stats, Exception error) {
		logger.error("Task" + task.getName() + " was failed", error);
	}

	@Override
	public void paused(Class<? extends Task> task) {
		logger.debug("Task {} was paused", task.getName());
	}

	@Override
	public void resumed(Class<? extends Task> task) {
		logger.debug("Task {} was resumed", task.getName());
	}

	@Override
	public void pausedAll() {
		logger.debug("All tasks have been paused");
	}

	@Override
	public void resumedAll() {
		logger.debug("All tasks have been resumed");
	}
	
	private String triggerInfo(Trigger trigger){
		if(SimpleTrigger.class.isAssignableFrom(trigger.getClass()))
			return "Fixed Rate: " + ((SimpleTrigger) trigger).getRepeatInterval();
		else
			return "Cron: " + ((CronTrigger) trigger).getCronExpression();
	}
	
	

}
