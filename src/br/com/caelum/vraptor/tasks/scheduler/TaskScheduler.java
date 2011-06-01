package br.com.caelum.vraptor.tasks.scheduler;

import org.quartz.SchedulerException;
import org.quartz.Trigger;

import br.com.caelum.vraptor.tasks.Task;

public interface TaskScheduler {

	void schedule(Task task, Trigger trigger);
	
	void unschedule(Task task) throws SchedulerException;

}
