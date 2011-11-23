package br.com.caelum.vraptor.tasks.jobs.simple;

import org.quartz.Job;

import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.jobs.JobProvider;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;

public class DefaultJobProvider implements JobProvider {
	
	public Job newJob(Task task, Scheduled options) {
		if(options == null)
			return new ConcurrentJobWrapper(task);
		return options.concurrent() ? new ConcurrentJobWrapper(task) : new StatefulJobWrapper(task);
	}

	public boolean canProvide(Class<? extends Job> job) {
		return ConcurrentJobWrapper.class.equals(job) || StatefulJobWrapper.class.equals(job);
	}

	public boolean canDecorate(Class<? extends Task> task) {
		return false;
	}

	public Class<? extends Job> getJobWrapper(Scheduled options) {
		if(options == null)
			return ConcurrentJobWrapper.class;
		return options.concurrent() ? ConcurrentJobWrapper.class : StatefulJobWrapper.class;
	}
	
}
