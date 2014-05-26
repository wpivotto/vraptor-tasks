package br.com.caelum.vraptor.tasks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import br.com.caelum.vraptor.tasks.helpers.TriggerBuilder;
import br.com.caelum.vraptor.tasks.scheduler.TaskScheduler;

@ApplicationScoped
public class DefaultExecutor implements TaskExecutor {

	private @Inject Scheduler quartz;
	private @Inject TaskScheduler scheduler;
	private @Inject TriggerBuilder builder;
	
	private JobKey getKey(String taskId){
		return new JobKey(taskId);
	}
	
	public void execute(String taskId) throws SchedulerException {
		quartz.triggerJob(getKey(taskId));
	}
	
	public void pause(String taskId) throws SchedulerException {
		quartz.pauseJob(getKey(taskId));
	}

	public void resume(String taskId) throws SchedulerException {
		quartz.resumeJob(getKey(taskId));
	}

	public void pauseAll() throws SchedulerException {
		quartz.pauseAll();
	}
	
	public void resumeAll() throws SchedulerException {
		quartz.resumeAll();
	}

	public void runOnce(Class<? extends Task> task) {
		Trigger trigger = builder.runOnce(task);
		scheduler.schedule(task, trigger, task.getSimpleName());
	}

}
