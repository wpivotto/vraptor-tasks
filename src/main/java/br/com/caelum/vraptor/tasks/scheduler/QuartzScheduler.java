package br.com.caelum.vraptor.tasks.scheduler;

import static org.quartz.JobBuilder.newJob;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.jobs.JobProvider;
import br.com.caelum.vraptor.tasks.jobs.simple.DefaultJobProvider;


@Component
@ApplicationScoped
public class QuartzScheduler implements TaskScheduler {

	protected final Scheduler quartz;
	private final List<JobProvider> providers;

	public QuartzScheduler(Scheduler quartz, List<JobProvider> providers) {
		this.quartz = quartz;
		this.providers = providers;
	}

	public void schedule(Class<? extends Task> task, Trigger trigger, String key) {

		JobDetail detail = newJob(jobFor(task)).withIdentity(key).build();
		detail.getJobDataMap().put("task", task);
		detail.getJobDataMap().put("task-key", key);
		
		try {
			quartz.scheduleJob(detail, trigger);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}

	}

	private Class<? extends Job> jobFor(Class<? extends Task> task) {
	
		Scheduled options = task.getAnnotation(Scheduled.class);
		
		for(JobProvider provider : providers){
			if(provider.canDecorate(task))
				return provider.getJobWrapper(options);
		}
		
		return new DefaultJobProvider().getJobWrapper(options);
		
	}

	public void unschedule(String key) {
		JobKey jobKey = new JobKey(key);
		try {
			if (quartz.checkExists(jobKey))
				quartz.deleteJob(jobKey);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}

}
