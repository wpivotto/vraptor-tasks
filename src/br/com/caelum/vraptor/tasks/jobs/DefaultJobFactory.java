package br.com.caelum.vraptor.tasks.jobs;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;

@Component
@ApplicationScoped
public class DefaultJobFactory implements JobFactory {

	private final JobProviders providers;

	public DefaultJobFactory(JobProviders providers) {
		this.providers = providers;
	}

	public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {

		JobDetail detail = bundle.getJobDetail();
		JobProvider provider = providers.getProvider(detail.getJobClass());
		return provider.newJob(detail);

	}

}
