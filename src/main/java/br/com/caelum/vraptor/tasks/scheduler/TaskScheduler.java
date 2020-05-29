package br.com.caelum.vraptor.tasks.scheduler;

import org.quartz.JobDataMap;
import org.quartz.Trigger;

import br.com.caelum.vraptor.tasks.Task;

public interface TaskScheduler {

	void schedule(Class<? extends Task> task, Trigger trigger, String taskId);
	void schedule(Class<? extends Task> task, Trigger trigger, String taskId, JobDataMap dataMap);
	void unschedule(String taskId);
	void unscheduleAll();

}
