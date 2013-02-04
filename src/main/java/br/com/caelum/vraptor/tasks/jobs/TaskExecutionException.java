package br.com.caelum.vraptor.tasks.jobs;

import org.quartz.JobExecutionException;

public class TaskExecutionException extends JobExecutionException {

	private static final long serialVersionUID = -6343004913127584888L;

	public TaskExecutionException(String string) {
		super(string);
	}
	
	public TaskExecutionException(String msg, Throwable t) {
		super(msg, t);
	}
	
	public TaskExecutionException(Throwable t) {
		super(t);
	}
	
}
