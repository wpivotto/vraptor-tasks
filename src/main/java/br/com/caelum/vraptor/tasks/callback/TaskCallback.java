package br.com.caelum.vraptor.tasks.callback;

import org.quartz.Trigger;

import br.com.caelum.vraptor.tasks.TaskStatistics;

public interface TaskCallback {
	
	void scheduled(String taskKey, Trigger trigger);
	
	void unscheduled(String taskKey);
	
	void beforeExecute(String taskKey);
	
	void executionVetoed(String taskKey);
	
	void executed(String taskKey, TaskStatistics stats);
	
	void failed(String taskKey, TaskStatistics stats, Exception error);

	void paused(String taskKey);

	void resumed(String taskKey);

}
