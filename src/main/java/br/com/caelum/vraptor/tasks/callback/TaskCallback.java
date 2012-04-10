package br.com.caelum.vraptor.tasks.callback;

import org.quartz.Trigger;

import br.com.caelum.vraptor.tasks.TaskStatistics;

public interface TaskCallback {
	
	void scheduled(String taskId, Trigger trigger);
	
	void unscheduled(String taskId);
	
	void beforeExecute(String taskId);
	
	void executionVetoed(String taskId);
	
	void executed(String taskId, TaskStatistics stats);
	
	void failed(String taskId, TaskStatistics stats, Exception error);

	void paused(String taskId);

	void resumed(String taskId);

}
