package br.com.caelum.vraptor.tasks;

import java.util.Date;
import java.util.List;

import org.quartz.Calendar;
import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.spi.OperableTrigger;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;


public class TaskStatistics {
	
	private final Class<? extends Task> task;
	private final Trigger trigger;
	private TriggerState triggerState;
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
		this.scheduledFireTime = trigger.getStartTime();
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
		if(lastException != null)
			return Throwables.getStackTraceAsString(lastException);
		return "";
	}
	
	public Throwable getRootCause(){
		if(lastException != null)
			return Throwables.getRootCause(lastException);
		return null;
	}
	
	public List<Date> getNextEvents() {
		return getNextFireTimes(10);
	}

	public List<Date> getNextFireTimes(int maxCount) {
		List<Date> result = Lists.newArrayList();
		OperableTrigger baseTrigger = (OperableTrigger)((OperableTrigger)trigger).clone();
		Calendar baseCalendar = null;

		if (baseTrigger.getNextFireTime() == null) {
			baseTrigger.computeFirstFireTime(baseCalendar);
		}

		Date nextExecution = new Date();
		int count = 0;
		
		while(count < maxCount) {
			nextExecution = baseTrigger.getFireTimeAfter(nextExecution);
			if (nextExecution == null) break;
			result.add(nextExecution);
			baseTrigger.triggered(baseCalendar);
			count++;
		}
		return result;
	}
	
	public String getTriggerExpression() {
		if(SimpleTrigger.class.isAssignableFrom(trigger.getClass()))
			return "Fixed Rate: " + ((SimpleTrigger) trigger).getRepeatInterval();
		else
			return "Cron: " + ((CronTrigger) trigger).getCronExpression();
	}
	
	public String getTriggerExpressionSummary() {
		if(SimpleTrigger.class.isAssignableFrom(trigger.getClass()))
			return "Fixed Rate: " + ((SimpleTrigger) trigger).getRepeatInterval();
		else
			return ((CronTrigger) trigger).getExpressionSummary().replaceAll(System.getProperty("line.separator"), "<br />");
	}
	
	public Trigger getTrigger() {
		return trigger;
	}

	public TriggerState getTriggerState() {
		return triggerState;
	}
	
	public void update(JobExecutionContext context, Throwable exception) {
		
		this.fireTime = context.getFireTime();
		this.executionTime = context.getJobRunTime();
		this.nextFireTime = context.getNextFireTime();
		this.previousFireTime = context.getPreviousFireTime();
		this.refireCount = context.getRefireCount();
		this.executionCount++;
		
		if(executionTime > maxExecutionTime)
			maxExecutionTime = executionTime;
		
		if(executionTime < minExecutionTime)
			minExecutionTime = executionTime;
		
		if(exception != null){
			failCount++;
			lastException = exception;
		}
		
	}

	public void update(Scheduler scheduler) {
		try {
			this.triggerState = scheduler.getTriggerState(trigger.getKey());
		} catch (SchedulerException e) {
			new RuntimeException("Error retrieving trigger state");
		}
	}
	


}
