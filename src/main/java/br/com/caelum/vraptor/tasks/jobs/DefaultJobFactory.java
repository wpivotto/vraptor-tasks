package br.com.caelum.vraptor.tasks.jobs;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.tasks.Task;

@Component
@ApplicationScoped
public class DefaultJobFactory implements JobFactory {

	private final JobProviders providers;
	private final TaskFactory factory;

	public DefaultJobFactory(JobProviders providers, TaskFactory factory) {
		this.providers = providers;
		this.factory = factory;
	}

	public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {

		JobDetail detail = bundle.getJobDetail();
		JobProvider provider = providers.getProvider(detail.getJobClass());
		Task task = factory.newTask(detail.getKey().getName());
		return provider.newJob(task);

	}

}
