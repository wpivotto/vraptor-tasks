package br.com.caelum.vraptor.tasks.jobs.request;

import javax.enterprise.context.ApplicationScoped;

import org.quartz.Job;

import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.jobs.JobProvider;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;

@ApplicationScoped
public class RequestScopedJobProvider implements JobProvider {

	public Job newJob(Task task, Scheduled options) {
		return new RequestScopedJob((RequestScopedTask) task);
	}

	public boolean canProvide(Class<? extends Job> job) {
		return RequestScopedJob.class.equals(job);
	}

	public boolean canDecorate(Class<? extends Task> task) {
		return RequestScopedTask.class.isAssignableFrom(task);
	}

	public Class<? extends Job> getJobWrapper(Scheduled options) {
		return RequestScopedJob.class;
	}

}
