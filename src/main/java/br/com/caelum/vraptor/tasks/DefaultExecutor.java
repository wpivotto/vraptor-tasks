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
	
	private JobKey getKey(String taskKey){
		return new JobKey(taskKey);
	}
	
	public void execute(String taskKey) throws SchedulerException {
		quartz.triggerJob(getKey(taskKey));
	}
	
	public void pause(String taskKey) throws SchedulerException {
		quartz.pauseJob(getKey(taskKey));
	}

	public void resume(String taskKey) throws SchedulerException {
		quartz.resumeJob(getKey(taskKey));
	}

	public void pauseAll() throws SchedulerException {
		quartz.pauseAll();
	}
	
	public void resumeAll() throws SchedulerException {
		quartz.resumeAll();
	}

}
