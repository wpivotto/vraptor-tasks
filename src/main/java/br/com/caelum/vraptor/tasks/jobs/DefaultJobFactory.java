package br.com.caelum.vraptor.tasks.jobs;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;

@Component
@ApplicationScoped
public class DefaultJobFactory implements JobFactory {

	private final JobProviders providers;
	private final TaskFactory factory;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public DefaultJobFactory(JobProviders providers, TaskFactory factory) {
		this.providers = providers;
		this.factory = factory;
	}

	public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {

		JobDetail detail = bundle.getJobDetail();
		JobProvider provider = providers.getProvider(detail.getJobClass());
		Task task = factory.newTask(detail.getKey().getName());
		logger.debug("Using {} to provide {}", provider.getClass().getName(), task.getClass().getName());
		return provider.newJob(task, task.getClass().getAnnotation(Scheduled.class));

	}

}
