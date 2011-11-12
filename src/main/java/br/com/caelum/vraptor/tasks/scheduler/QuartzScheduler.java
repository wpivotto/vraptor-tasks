package br.com.caelum.vraptor.tasks.scheduler;

import static org.quartz.JobBuilder.newJob;

import java.util.List;

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
import br.com.caelum.vraptor.tasks.callback.TaskEventNotifier;
import br.com.caelum.vraptor.tasks.jobs.DefaultJob;
import br.com.caelum.vraptor.tasks.jobs.JobProvider;


@Component
@ApplicationScoped
public class QuartzScheduler implements TaskScheduler {

	protected Logger logger = LoggerFactory.getLogger(QuartzScheduler.class);
	protected final Scheduler quartz;
	private final List<JobProvider> providers;
	private final TaskEventNotifier notifier;

	public QuartzScheduler(Scheduler quartz, List<JobProvider> providers, TaskEventNotifier notifier) {
		this.quartz = quartz;
		this.providers = providers;
		this.notifier = notifier;
	}

	public void schedule(Task task, Trigger trigger) {

		JobDetail detail = newJob(getJobClass(task)).withIdentity(task.getClass().getName()).build();

		try {

			quartz.scheduleJob(detail, trigger);
			notifier.notifyScheduledEvent(task);

		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}

	}

	private Class<? extends Job> getJobClass(Task task) {
		for(JobProvider provider : providers){
			if(provider.canDecorate(task.getClass()))
				return provider.getJobWrapper();
		}
		
		return DefaultJob.class;
		
	}

	public void unschedule(Task task) throws SchedulerException {
		JobKey key = new JobKey(task.getClass().getName());
		if (quartz.checkExists(key))
			quartz.deleteJob(key);
		notifier.notifyUnscheduledEvent(task);
	}

}
