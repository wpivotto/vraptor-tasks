package br.com.caelum.vraptor.tasks;

import org.quartz.SchedulerException;

public interface TaskExecutor {
	
	void execute(String taskId) throws SchedulerException;
	void pause(String taskId) throws SchedulerException;
	void resume(String taskId) throws SchedulerException;
	void runOnce(Class<? extends Task> task);
	void pauseAll() throws SchedulerException;
	void resumeAll() throws SchedulerException;

}
