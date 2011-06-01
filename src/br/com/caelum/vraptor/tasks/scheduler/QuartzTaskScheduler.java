package br.com.caelum.vraptor.tasks.scheduler;

import static org.quartz.JobBuilder.newJob;

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
import br.com.caelum.vraptor.tasks.TransactionalTask;
import br.com.caelum.vraptor.tasks.jobs.DefaultJob;
import br.com.caelum.vraptor.tasks.jobs.TransactionalJob;

@Component
@ApplicationScoped
public class QuartzTaskScheduler implements TaskScheduler {

	protected static Logger logger = LoggerFactory.getLogger(QuartzTaskScheduler.class);
	protected Scheduler quartz;

	public QuartzTaskScheduler(Scheduler quartz) {
		this.quartz = quartz;
	}

	public void schedule(Task task, Trigger trigger) {

		JobDetail detail = newJob(getJobClass(task)).withIdentity(task.getClass().getName()).build();

		try {

			quartz.scheduleJob(detail, trigger);
			logger.debug("Task " + task.getClass().getName() + " scheduled");

		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}

	}

	private Class<? extends Job> getJobClass(Task task) {
		return TransactionalTask.class.isAssignableFrom(task.getClass()) ? TransactionalJob.class : DefaultJob.class;
	}

	@Override
	public void unschedule(Task task) throws SchedulerException {
		JobKey key = new JobKey(task.getClass().getName());
		if (quartz.checkExists(key))
			quartz.deleteJob(key);

	}

}
