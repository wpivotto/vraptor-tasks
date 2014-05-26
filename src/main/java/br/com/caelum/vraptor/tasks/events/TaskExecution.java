package br.com.caelum.vraptor.tasks.events;

import org.quartz.JobExecutionException;

import br.com.caelum.vraptor.tasks.TaskStatistics;

public class TaskExecution {

	private final String taskId;
	private final TaskStatistics stats;
	private final JobExecutionException exception;
	
	public TaskExecution(String taskId, TaskStatistics stats, JobExecutionException exception) {
		this.taskId = taskId;
		this.stats = stats;
		this.exception = exception;
	}

	public String getTaskId() {
		return taskId;
	}

	public TaskStatistics getStats() {
		return stats;
	}

	public JobExecutionException getException() {
		return exception;
	}
	
}
