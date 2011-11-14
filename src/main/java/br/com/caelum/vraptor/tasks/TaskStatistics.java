package br.com.caelum.vraptor.tasks;

import java.util.Date;

import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import com.google.common.base.Throwables;


public class TaskStatistics {
	
	private final Class<? extends Task> task;
	private final Trigger trigger;
	private Date fireTime;
	private Date scheduledFireTime;
	private Date nextFireTime;
	private Date previousFireTime;
	private long executionTime;
	private long maxExecutionTime = Long.MIN_VALUE;
	private long minExecutionTime = Long.MAX_VALUE;
	private long executionCount;
	private int refireCount;
	private int failCount;
	private Throwable lastException;
	
	public TaskStatistics(Class<? extends Task> task, Trigger trigger) {
		this.task = task;
		this.trigger = trigger;
	}
	
	public Class<? extends Task> getTask() {
		return task;
	}

	public String getTaskName() {
		return task.getName();
	}

	public Date getFireTime() {
		return fireTime;
	}

	public Date getScheduledFireTime() {
		return scheduledFireTime;
	}

	public Date getNextFireTime() {
		return nextFireTime;
	}

	public Date getPreviousFireTime() {
		return previousFireTime;
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public long getMaxExecutionTime() {
		return maxExecutionTime;
	}

	public long getMinExecutionTime() {
		return minExecutionTime;
	}

	public int getRefireCount() {
		return refireCount;
	}
	
	public long getExecutionCount() {
		return executionCount;
	}

	public long getFailCount() {
		return failCount;
	}
	
	public Throwable getLastException() {
		return lastException;
	}
	
	public String getStackTraceAsString() {
		return Throwables.getStackTraceAsString(lastException);
	}
	
	public String getTriggerExpression(){
		if(SimpleTrigger.class.isAssignableFrom(trigger.getClass()))
			return "Fixed Rate: " + ((SimpleTrigger) trigger).getRepeatInterval();
		else
			return "Cron: " + ((CronTrigger) trigger).getCronExpression();
	}
	
	public Trigger getTrigger(){
		return trigger;
	}

	public void update(JobExecutionContext context) {
		
		this.fireTime = context.getFireTime();
		this.scheduledFireTime = context.getScheduledFireTime();
		this.executionTime = context.getJobRunTime();
		this.nextFireTime = context.getNextFireTime();
		this.previousFireTime = context.getPreviousFireTime();
		this.refireCount = context.getRefireCount();
		this.executionCount++;
		
		if(executionTime > maxExecutionTime)
			maxExecutionTime = executionTime;
		
		if(executionTime < minExecutionTime)
			minExecutionTime = executionTime;
		
	}

	public void increaseFailCount(Throwable exception) {
		this.failCount++;
		this.lastException = exception;
	}

}
