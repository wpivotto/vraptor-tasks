package br.com.caelum.vraptor.tasks.events;

import br.com.caelum.vraptor.tasks.TaskStatistics;

public class TaskExecution {

	private final String taskId;
	private final TaskStatistics stats;
	private final Exception exception;
	
	public TaskExecution(String taskId, TaskStatistics stats, Exception exception) {
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

	public Exception getException() {
		return exception;
	}
	
	public boolean hasFailed() {
		return exception != null;
	}
	
}
