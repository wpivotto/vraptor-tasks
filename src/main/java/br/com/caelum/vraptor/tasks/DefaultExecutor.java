package br.com.caelum.vraptor.tasks;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;

@Component
@ApplicationScoped
public class DefaultExecutor implements TaskExecutor {

	private final Scheduler quartz;
	
	public DefaultExecutor(Scheduler quartz) {
		this.quartz = quartz;
	}
	
	private JobKey getKey(Class<? extends Task> task){
		return new JobKey(task.getName());
	}

	public void execute(Task task) throws SchedulerException {
		execute(task.getClass());
	}

	public void execute(Class<? extends Task> task) throws SchedulerException {
		quartz.triggerJob(getKey(task));
	}
	
	public void pause(Task task) throws SchedulerException {
		pause(task.getClass());
	}
	
	public void pause(Class<? extends Task> task) throws SchedulerException {
		quartz.pauseJob(getKey(task));
	}

	public void resume(Task task) throws SchedulerException {
		resume(task.getClass());
	}
	
	public void resume(Class<? extends Task> task) throws SchedulerException {
		quartz.resumeJob(getKey(task));
	}

	public void pauseAll() throws SchedulerException {
		quartz.pauseAll();
	}
	
	public void resumeAll() throws SchedulerException {
		quartz.resumeAll();
	}

}
