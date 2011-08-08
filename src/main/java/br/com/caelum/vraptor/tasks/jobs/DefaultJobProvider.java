package br.com.caelum.vraptor.tasks.jobs;

import org.quartz.Job;

import br.com.caelum.vraptor.tasks.Task;

public class DefaultJobProvider implements JobProvider {

	public Job newJob(Task task) {
		return new DefaultJob(task);
	}

	public boolean canProvide(Class<? extends Job> job) {
		return DefaultJob.class.equals(job);
	}

	public boolean canDecorate(Class<? extends Task> task) {
		return false;
	}

	public Class<? extends Job> getJobWrapper() {
		return DefaultJob.class;
	}
	
}
