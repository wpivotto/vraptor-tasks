package br.com.caelum.vraptor.tasks.jobs;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;

@ApplicationScoped
public class DefaultJobFactory implements JobFactory {

	private final JobProviders providers;
	private final TaskFactory factory;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	public DefaultJobFactory(JobProviders providers, TaskFactory factory) {
		this.providers = providers;
		this.factory = factory;
	}

	public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {

		JobDetail detail = bundle.getJobDetail();
		JobProvider provider = providers.getProvider(detail.getJobClass());
		Task task = factory.newTask(taskClass(detail), detail);
		logger.debug("Using {} to provide {}", provider.getClass().getName(), task.getClass().getName());
		return provider.newJob(task, task.getClass().getAnnotation(Scheduled.class));

	}

	@SuppressWarnings("unchecked")
	private Class<? extends Task> taskClass(JobDetail detail) {
		return (Class<? extends Task>) detail.getJobDataMap().get("task-class");
	}

}
