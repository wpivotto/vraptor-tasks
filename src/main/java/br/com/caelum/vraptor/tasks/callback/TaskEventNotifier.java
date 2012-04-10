package br.com.caelum.vraptor.tasks.callback;

import java.util.List;

import org.quartz.Trigger;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.TaskStatistics;

@Component
@ApplicationScoped
public class TaskEventNotifier {
	
	private final List<TaskCallback> listeners;

	public TaskEventNotifier(List<TaskCallback> listeners) {
		this.listeners = listeners;
	}
	
	public void notifyScheduledEvent(String taskKey, Trigger trigger){
		for(TaskCallback listener : listeners){
			listener.scheduled(taskKey, trigger);
		}
	}

	public void notifyUnscheduledEvent(String taskKey){
		for(TaskCallback listener : listeners){
			listener.unscheduled(taskKey);
		}
	}
	
	public void notifyBeforeExecuteEvent(String taskKey){
		for(TaskCallback listener : listeners){
			listener.beforeExecute(taskKey);
		}
	}
	
	public void notifyExecutionVetoedEvent(String taskKey){
		for(TaskCallback listener : listeners){
			listener.executionVetoed(taskKey);
		}
	}
	
	public void notifyExecutedEvent(String taskKey, TaskStatistics stats){
		for(TaskCallback listener : listeners){
			listener.executed(taskKey, stats);
		}
	}
	
	public void notifyPausedEvent(String taskKey){
		for(TaskCallback listener : listeners){
			listener.paused(taskKey);
		}
	}
	
	public void notifyResumedEvent(String taskKey){
		for(TaskCallback listener : listeners){
			listener.resumed(taskKey);
		}
	}

	public void notifyFailedEvent(String taskKey, TaskStatistics stats, Exception exception) {
		for(TaskCallback listener : listeners){
			listener.failed(taskKey, stats, exception);
		}
	}
}

