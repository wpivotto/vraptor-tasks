package br.com.caelum.vraptor.tasks.jobs;


import java.util.List;

import org.quartz.Job;

import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.PrototypeScoped;

@Component
@PrototypeScoped
public class JobProviders {
	
	private final List<JobProvider> providers;
	
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
