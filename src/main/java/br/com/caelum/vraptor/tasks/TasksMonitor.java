package br.com.caelum.vraptor.tasks;

import java.util.Collection;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import br.com.caelum.vraptor.tasks.callback.TaskEventNotifier;

import com.google.common.collect.Maps;

@ApplicationScoped
public class TasksMonitor implements JobListener, SchedulerListener {
	
	private final TaskEventNotifier notifier;
	private Scheduler scheduler;
	private Map<String, TaskStatistics> statistics = Maps.newHashMap();

	@Inject
	public TasksMonitor(TaskEventNotifier notifier) {
		this.notifier = notifier;
	}
	
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	public String getName() {
		return getClass().getSimpleName();
	}

	public void jobExecutionVetoed(JobExecutionContext context) {
		notifier.notifyExecutionVetoedEvent(getId(context));
	}

	public void jobToBeExecuted(JobExecutionContext context) {
		findStats(context);
		notifier.notifyBeforeExecuteEvent(getId(context));
	}

	public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
		String taskId = getId(context);
		TaskStatistics stats = findStats(context);
		stats.update(context, exception);
		if(exception != null)
			notifier.notifyFailedEvent(taskId, stats, exception);
		else
			notifier.notifyExecutedEvent(taskId, stats);
	}
	
	private TaskStatistics findStats(JobExecutionContext context) {
		String taskId = getId(context);
		if(!statistics.containsKey(taskId))
			statistics.put(taskId, new TaskStatistics(taskId, context));
		return statistics.get(taskId);
	}
	
	public TaskStatistics getStatisticsFor(String taskId) {
		TaskStatistics stats = statistics.get(taskId);
		if(stats != null)
			stats.updateTriggerState(scheduler);
		return stats;
	}

	public Collection<TaskStatistics> getStatistics() {
		for(TaskStatistics stats : statistics.values()) {
			stats.updateTriggerState(scheduler);
		}
		return statistics.values();
	}
	
	private String getId(JobExecutionContext context) {
		return context.getJobDetail().getJobDataMap().getString("task-id");
	}
	
	private String getId(JobKey key) {
		try {
			return scheduler.getJobDetail(key).getJobDataMap().getString("task-id");
		} catch (SchedulerException e) {
			throw new IllegalStateException(e);
		}
	}

	public void jobScheduled(Trigger trigger) {
		notifier.notifyScheduledEvent(getId(trigger.getJobKey()), trigger);
	}
	
	public void jobDeleted(JobKey jobKey) {
		notifier.notifyUnscheduledEvent(getId(jobKey));
	}

	public void jobPaused(JobKey jobKey) {
		notifier.notifyPausedEvent(getId(jobKey));
	}

	public void jobResumed(JobKey jobKey) {
		notifier.notifyResumedEvent(getId(jobKey));
	}
	
	public void jobsPaused(String jobGroup) {}

	public void jobUnscheduled(TriggerKey triggerKey) {}

	public void triggerFinalized(Trigger trigger) {}

	public void triggerPaused(TriggerKey triggerKey) {}

	public void triggersPaused(String triggerGroup) {}

	public void triggerResumed(TriggerKey triggerKey) {}

	public void triggersResumed(String triggerGroup) {}

	public void jobAdded(JobDetail jobDetail) {}

	public void jobsResumed(String jobGroup) {}

	public void schedulerError(String msg, SchedulerException cause) {}

	public void schedulerInStandbyMode() {}

	public void schedulerStarted() {}

	public void schedulerShutdown() {}

	public void schedulerShuttingdown() {}

	public void schedulingDataCleared() {}

}
