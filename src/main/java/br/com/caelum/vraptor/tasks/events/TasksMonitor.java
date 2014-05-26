package br.com.caelum.vraptor.tasks.events;

import java.util.Collection;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;

import br.com.caelum.vraptor.tasks.TaskStatistics;

import com.google.common.collect.Maps;

@ApplicationScoped
public class TasksMonitor implements JobListener {
	
	private Scheduler scheduler;
	private Map<String, TaskStatistics> statistics = Maps.newHashMap();
	@Inject @Any Event<TaskExecution> executionEvent;
	
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	public String getName() {
		return getClass().getSimpleName();
	}

	public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
		String taskId = getId(context);
		TaskStatistics stats = findStats(context);
		stats.update(context, exception, scheduler);
		executionEvent.fire(new TaskExecution(taskId, stats, exception));
	}
	
	private TaskStatistics findStats(JobExecutionContext context) {
		String taskId = getId(context);
		if(!statistics.containsKey(taskId))
			statistics.put(taskId, new TaskStatistics(taskId, context));
		return statistics.get(taskId);
	}
	
	public TaskStatistics getStatisticsFor(String taskId) {
		return statistics.get(taskId);
	}

	public Collection<TaskStatistics> getStatistics() {
		return statistics.values();
	}
	
	private String getId(JobExecutionContext context) {
		return context.getJobDetail().getJobDataMap().getString("task-id");
	}

	public void jobToBeExecuted(JobExecutionContext context) {}

	public void jobExecutionVetoed(JobExecutionContext context) {}

}
