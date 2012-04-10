package br.com.caelum.vraptor.tasks;

import java.util.Collection;
import java.util.Map;

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

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.tasks.callback.TaskEventNotifier;

import com.google.common.collect.Maps;

@Component
@ApplicationScoped
@SuppressWarnings("unchecked")
public class TasksMonitor implements JobListener, SchedulerListener {
	
	private final TaskEventNotifier notifier;
	private Scheduler scheduler;
	private Map<String, TaskStatistics> statistics = Maps.newHashMap();

	public TasksMonitor(TaskEventNotifier notifier){
		this.notifier = notifier;
	}
	
	public void setScheduler(Scheduler scheduler){
		this.scheduler = scheduler;
	}
	
	public String getName() {
		return getClass().getName();
	}

	public void jobExecutionVetoed(JobExecutionContext context) {
		notifier.notifyExecutionVetoedEvent(taskFrom(context));
	}

	public void jobToBeExecuted(JobExecutionContext context) {
		notifier.notifyBeforeExecuteEvent(taskFrom(context));
	}

	public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
		TaskStatistics stats = updateStats(context, exception);
		if(exception != null)
			notifier.notifyFailedEvent(taskFrom(context), stats, exception);
		else
			notifier.notifyExecutedEvent(taskFrom(context), stats);
	}
	
	private TaskStatistics updateStats(JobExecutionContext context, JobExecutionException exception){
		TaskStatistics stats = getStatisticsFor(taskFrom(context));
		stats.update(context, exception);
		return stats;
	}
	
	public TaskStatistics getStatisticsFor(String taskKey){
		TaskStatistics stats = statistics.get(taskKey);
		if(stats != null)
			stats.update(scheduler);
		return stats;
	}

	public Collection<TaskStatistics> getStatistics(){
		for(TaskStatistics stats : statistics.values()){
			stats.update(scheduler);
		}
		return statistics.values();
	}
	
	private String taskFrom(JobExecutionContext context){
		return context.getJobDetail().getJobDataMap().getString("task-key");
	}
	
	private String taskFrom(JobKey key){
		try {
			return scheduler.getJobDetail(key).getJobDataMap().getString("task-key");
		} catch (SchedulerException e) {
			throw new IllegalStateException(e);
		}
	}

	public void jobScheduled(Trigger trigger) {
		String key = taskFrom(trigger.getJobKey());
		if(!statistics.containsKey(key)) {
			statistics.put(key, new TaskStatistics(key, trigger));
			notifier.notifyScheduledEvent(key, trigger);
		}
	}
	
	public void jobDeleted(JobKey jobKey) {
		notifier.notifyUnscheduledEvent(taskFrom(jobKey));
	}

	public void jobPaused(JobKey jobKey) {
		notifier.notifyPausedEvent(taskFrom(jobKey));
	}

	public void jobResumed(JobKey jobKey) {
		notifier.notifyResumedEvent(taskFrom(jobKey));
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
