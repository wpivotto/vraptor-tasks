package br.com.caelum.vraptor.tasks.jobs.simple;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.com.caelum.vraptor.tasks.Task;

public class ConcurrentJobWrapper implements Job {

	private final Task task;

	public ConcurrentJobWrapper(Task task) {
		this.task = task;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		task.execute();
	}

}
