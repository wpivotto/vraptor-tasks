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

}
