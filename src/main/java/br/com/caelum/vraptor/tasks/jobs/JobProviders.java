package br.com.caelum.vraptor.tasks.jobs;


import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.quartz.Job;

import br.com.caelum.vraptor.tasks.jobs.simple.DefaultJobProvider;

@Dependent
public class JobProviders {
	
	private final List<JobProvider> providers;
	
	@Inject
	public JobProviders(List<JobProvider> providers) {
		this.providers = providers;
	}
	
	public JobProvider getProvider(Class<? extends Job> job){
		
		for(JobProvider provider : providers){
			if(provider.canProvide(job))
				return provider;
		}
		
		return new DefaultJobProvider();
		
	}	

}
