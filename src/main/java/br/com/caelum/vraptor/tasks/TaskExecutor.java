package br.com.caelum.vraptor.tasks;

import org.quartz.SchedulerException;

public interface TaskExecutor {
	
	void execute(String taskKey) throws SchedulerException;
	void pause(String taskKey) throws SchedulerException;
	void resume(String taskKey) throws SchedulerException;
	void pauseAll() throws SchedulerException;
	void resumeAll() throws SchedulerException;

}
