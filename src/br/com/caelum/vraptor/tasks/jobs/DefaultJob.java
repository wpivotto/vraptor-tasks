package br.com.caelum.vraptor.tasks.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.com.caelum.vraptor.tasks.Task;

public class DefaultJob implements Job {

	private final Task task;

	public DefaultJob(Task task) {
		this.task = task;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		task.execute();
	}

}
