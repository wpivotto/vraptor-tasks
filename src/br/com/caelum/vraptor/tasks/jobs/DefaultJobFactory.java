package br.com.caelum.vraptor.tasks.jobs;

import java.util.List;

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

	private final List<JobProvider> providers;

	public DefaultJobFactory(List<JobProvider> providers) {
		this.providers = providers;
	}

	@Override
	public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
		
		JobDetail detail = bundle.getJobDetail();
		
		for(JobProvider provider : providers){
			if(provider.canProvide(detail.getJobClass()))
				return provider.newJob(detail);
		}
		
		throw new IllegalArgumentException("Can´t provide job " + detail.getKey());
		
	}

}
