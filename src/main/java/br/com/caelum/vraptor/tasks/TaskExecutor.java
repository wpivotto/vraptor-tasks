package br.com.caelum.vraptor.tasks;

import org.quartz.SchedulerException;

public interface TaskExecutor {
	
	void execute(Task task) throws SchedulerException;
	void execute(Class<? extends Task> task) throws SchedulerException;
	
	void pause(Task task) throws SchedulerException;
	void pause(Class<? extends Task> task) throws SchedulerException;
	
	void resume(Task task) throws SchedulerException;
	void resume(Class<? extends Task> task) throws SchedulerException;
	
	void pauseAll() throws SchedulerException;
	void resumeAll() throws SchedulerException;

}
