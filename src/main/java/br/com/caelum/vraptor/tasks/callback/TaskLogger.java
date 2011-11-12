package br.com.caelum.vraptor.tasks.callback;

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
	public void scheduled(Task task) {
		logger.debug("Task {} was sucessfully scheduled", task.getClass().getName());
	}

	@Override
	public void unscheduled(Task task) {
		logger.debug("Task {} was sucessfully unscheduled", task.getClass().getName());
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
		logger.debug("Task {} was sucessfully executed", task.getName());
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
	
	

}
