package br.com.caelum.vraptor.tasks;

import java.util.Collection;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.tasks.callback.TaskEventNotifier;

import com.google.common.collect.Maps;

@Component
@ApplicationScoped
public class TasksMonitor implements JobListener {
	
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

	private String taskName(JobExecutionContext context){
		return context.getJobDetail().getKey().getName();
	}
	
	@SuppressWarnings("unchecked")
	private Class<? extends Task> taskClass(JobExecutionContext context){
		try {
			return (Class<? extends Task>) Class.forName(taskName(context));
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	private TaskStatistics getStatistics(Class<? extends Task> task){
		if(!statistics.containsKey(task))
			statistics.put(task, new TaskStatistics(task));
		return statistics.get(task);
	}

	public TaskStatistics getStatisticsFor(Task task){
		return getStatistics(task.getClass());
	}
	
	public TaskStatistics getStatisticsFor(Class<? extends Task> task){
		return getStatistics(task);
	}

	public Collection<TaskStatistics> getStatistics(){
		return statistics.values();
	}


}
