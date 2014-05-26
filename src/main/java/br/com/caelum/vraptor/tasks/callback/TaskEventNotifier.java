package br.com.caelum.vraptor.tasks.callback;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.quartz.Trigger;

import br.com.caelum.vraptor.tasks.TaskStatistics;

import com.google.common.collect.Lists;

@ApplicationScoped
public class TaskEventNotifier {
	
	private List<TaskCallback> listeners;

	@Deprecated // CDI eyes only
	public TaskEventNotifier() {}
	
	@Inject
	public TaskEventNotifier(@Any Instance<TaskCallback> listeners) {
		this.listeners = Lists.newArrayList(listeners);
	}
	
	public void notifyScheduledEvent(String taskId, Trigger trigger){
		for(TaskCallback listener : listeners){
			listener.scheduled(taskId, trigger);
		}
	}

	public void notifyUnscheduledEvent(String taskId){
		for(TaskCallback listener : listeners){
			listener.unscheduled(taskId);
		}
	}
	
	public void notifyBeforeExecuteEvent(String taskId){
		for(TaskCallback listener : listeners){
			listener.beforeExecute(taskId);
		}
	}
	
	public void notifyExecutionVetoedEvent(String taskId){
		for(TaskCallback listener : listeners){
			listener.executionVetoed(taskId);
		}
	}
	
	public void notifyExecutedEvent(String taskId, TaskStatistics stats){
		for(TaskCallback listener : listeners){
			listener.executed(taskId, stats);
		}
	}
	
	public void notifyPausedEvent(String taskId){
		for(TaskCallback listener : listeners){
			listener.paused(taskId);
		}
	}
	
	public void notifyResumedEvent(String taskId){
		for(TaskCallback listener : listeners){
			listener.resumed(taskId);
		}
	}

	public void notifyFailedEvent(String taskId, TaskStatistics stats, Exception exception) {
		for(TaskCallback listener : listeners){
			listener.failed(taskId, stats, exception);
		}
	}
}

