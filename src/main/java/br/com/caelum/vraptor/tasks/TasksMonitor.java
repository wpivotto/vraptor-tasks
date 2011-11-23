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
		notifier.notifyExecutionVetoedEvent(taskClass(context));
	}

	public void jobToBeExecuted(JobExecutionContext context) {
		notifier.notifyBeforeExecuteEvent(taskClass(context));
	}

	public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
		TaskStatistics stats = updateStats(context, exception);
		if(exception != null)
			notifier.notifyFailedEvent(taskClass(context), stats, exception);
		else
			notifier.notifyExecutedEvent(taskClass(context), stats);
	}
	
	private TaskStatistics updateStats(JobExecutionContext context, JobExecutionException exception){
		TaskStatistics stats = getStatisticsFor(taskName(context));
		stats.update(context, exception);
		return stats;
	}
	
	public TaskStatistics getStatisticsFor(String taskName){
		TaskStatistics stats = statistics.get(taskName);
		if(stats != null)
			stats.update(scheduler);
		return stats;
	}

	public TaskStatistics getStatisticsFor(Task task){
		return getStatisticsFor(task.getClass().getName());
	}
	
	public TaskStatistics getStatisticsFor(Class<? extends Task> task){
		return getStatisticsFor(task.getName());
	}

	public Collection<TaskStatistics> getStatistics(){
		for(TaskStatistics stats : statistics.values()){
			stats.update(scheduler);
		}
		return statistics.values();
	}
	
	private Class<? extends Task> taskClass(JobExecutionContext context){
		String taskName = taskName(context);
		return taskClass(taskName);
	}
	
	private String taskName(JobExecutionContext context){
		return context.getJobDetail().getKey().getName();
	}
	
	@SuppressWarnings("unchecked")
	private Class<? extends Task> taskClass(String className){
		try {
			return (Class<? extends Task>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	public void jobScheduled(Trigger trigger) {
		String taskName = trigger.getJobKey().getName();
		Class<? extends Task> taskClass = taskClass(taskName);
		if(!statistics.containsKey(taskName)) {
			statistics.put(taskName, new TaskStatistics(taskClass, trigger));
			notifier.notifyScheduledEvent(taskClass, trigger);
		}
	}
	
	public void jobDeleted(JobKey jobKey) {
		notifier.notifyUnscheduledEvent(taskClass(jobKey.getName()));
	}

	public void jobPaused(JobKey jobKey) {
		notifier.notifyPausedEvent(taskClass(jobKey.getName()));
	}

	public void jobResumed(JobKey jobKey) {
		notifier.notifyResumedEvent(taskClass(jobKey.getName()));
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
