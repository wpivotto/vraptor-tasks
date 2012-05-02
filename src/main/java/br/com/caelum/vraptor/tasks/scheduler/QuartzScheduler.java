package br.com.caelum.vraptor.tasks.scheduler;

import static org.quartz.JobBuilder.newJob;

import java.util.List;

import javax.annotation.PostConstruct;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private final Logger log = LoggerFactory.getLogger(getClass());

	public QuartzScheduler(Scheduler quartz, List<JobProvider> providers) {
		this.quartz = quartz;
		this.providers = providers;
	}

	public void schedule(Class<? extends Task> task, Trigger trigger, String id) {

		JobDetail detail = newJob(jobFor(task)).withIdentity(id).build();
		
		try {
			if(!alreadyExists(id)) {
				detail.getJobDataMap().put("task-class", task);
				detail.getJobDataMap().put("task-id", id);
				quartz.scheduleJob(detail, trigger);
			}
			else
				log.warn("Unable to schedule task {} because one already exists with this identification", task);
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

	public void unschedule(String id) {
		JobKey jobKey = new JobKey(id);
		try {
			if (quartz.checkExists(jobKey))
				quartz.deleteJob(jobKey);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean alreadyExists(String id) throws SchedulerException {
		return quartz.checkExists(new JobKey(id));
	}
	
	@PostConstruct
	public void setup() {
		System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");
	}

}
