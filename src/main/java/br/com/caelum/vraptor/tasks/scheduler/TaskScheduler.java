package br.com.caelum.vraptor.tasks.scheduler;

import org.quartz.Trigger;

import br.com.caelum.vraptor.tasks.Task;

public interface TaskScheduler {

	void schedule(Class<? extends Task> task, Trigger trigger, String key);
	void unschedule(String key);

}
