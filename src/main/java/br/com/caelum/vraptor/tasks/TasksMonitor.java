package br.com.caelum.vraptor.tasks;

import java.util.Collection;
import java.util.Map;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
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
	
	private Map<Class<? extends Task>, TaskStatistics> statistics = Maps.newHashMap();

	public TasksMonitor(TaskEventNotifier notifier){
		this.notifier = notifier;
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
		
		TaskStatistics stats = getStatisticsFor(taskClass(context));
		stats.update(context);

		if(exception != null){
			stats.increaseFailCount(exception);
			notifier.notifyFailedEvent(taskClass(context), stats, exception);
		}
		
		else
			notifier.notifyExecutedEvent(taskClass(context), stats);

	}
	
	private Class<? extends Task> taskClass(JobExecutionContext context){
		return taskClass(context.getJobDetail().getKey().getName());
	}
	
	@SuppressWarnings("unchecked")
	private Class<? extends Task> taskClass(String className){
		try {
			return (Class<? extends Task>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	public TaskStatistics getStatisticsFor(Task task){
		return statistics.get(task.getClass());
	}
	
	public TaskStatistics getStatisticsFor(Class<? extends Task> task){
		return statistics.get(task);
	}

	public Collection<TaskStatistics> getStatistics(){
		return statistics.values();
	}

	public void jobScheduled(Trigger trigger) {
		Class<? extends Task> task = taskClass(trigger.getJobKey().getName());
		statistics.put(task, new TaskStatistics(task, trigger));
		notifier.notifyScheduledEvent(task, trigger);
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
