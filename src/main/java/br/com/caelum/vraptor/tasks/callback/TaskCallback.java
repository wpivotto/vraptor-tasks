package br.com.caelum.vraptor.tasks.callback;

import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.TaskStatistics;


public interface TaskCallback {
	
	void scheduled(Task task);
	
	void unscheduled(Task task);
	
	void beforeExecute(Class<? extends Task> task);
	
	void executionVetoed(Class<? extends Task> task);
	
	void executed(Class<? extends Task> task, TaskStatistics stats);
	
	void failed(Class<? extends Task> task, TaskStatistics stats, Exception error);

	void paused(Class<? extends Task> task);

	void resumed(Class<? extends Task> task);
	
	void pausedAll();
	
	void resumedAll();

}
