package br.com.caelum.vraptor.tasks.jobs;


import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.quartz.Job;

import br.com.caelum.vraptor.tasks.jobs.simple.DefaultJobProvider;

import com.google.common.collect.Lists;

@Dependent
public class JobProviders {
	
	private List<JobProvider> providers;
	
	@Deprecated // CDI eyes only
	public JobProviders() {}
	
	@Inject
	public JobProviders(@Any Instance<JobProvider> providers) {
		this.providers = Lists.newArrayList(providers);
	}
	
	public JobProvider getProvider(Class<? extends Job> job){
		
		for(JobProvider provider : providers){
			if(provider.canProvide(job))
				return provider;
		}
		
		return new DefaultJobProvider();
		
	}	

}
