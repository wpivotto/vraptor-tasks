package br.com.caelum.vraptor.tasks.callback;

import java.util.List;

import org.quartz.Trigger;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.tasks.TaskStatistics;

@Component
@ApplicationScoped
public class TaskEventNotifier {
	
	private final List<TaskCallback> listeners;

	public TaskEventNotifier(List<TaskCallback> listeners) {
		this.listeners = listeners;
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

