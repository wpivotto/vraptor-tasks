package br.com.caelum.vraptor.tasks;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.quartz.Calendar;
import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.spi.OperableTrigger;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public class TaskStatistics {
	
	private final String task;
	private final Trigger trigger;
	private TriggerState triggerState = TriggerState.NONE;
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
	private Map<String, Object> parameters = Maps.newHashMap();
	
	public TaskStatistics(String task, JobExecutionContext context) {
		this.task = task;
		this.trigger = context.getTrigger();
		this.scheduledFireTime = trigger.getStartTime();
		update(context, null);
	}
	
	public String getTask() {
		return task;
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
			return ((CronTrigger) trigger).getExpressionSummary().replaceAll(System.getProperty("line.separator"), "\n");
	}
	
	public Trigger getTrigger() {
		return trigger;
	}

	public TriggerState getTriggerState() {
		return triggerState;
	}
	
	public String printParameters() {
		return Joiner.on(", ").withKeyValueSeparator(" => ").join(parameters);
	}
	
	public String printNextEvents() {
		return Joiner.on(", ").join(getNextFireTimes(10));
	}
	
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	public void update(JobExecutionContext context, Throwable exception) {
		
		this.fireTime = context.getFireTime();
		this.executionTime = context.getJobRunTime();
		this.nextFireTime = context.getNextFireTime();
		this.previousFireTime = context.getPreviousFireTime();
		this.refireCount = context.getRefireCount();
		this.executionCount++;
		this.parameters = context.getMergedJobDataMap().getWrappedMap();
		
		if(executionTime > maxExecutionTime)
			maxExecutionTime = executionTime;
		
		if(executionTime < minExecutionTime)
			minExecutionTime = executionTime;
		
		if(exception != null){
			failCount++;
			lastException = exception;
		}
		
	}

	public void updateTriggerState(Scheduler scheduler) {
		try {
			this.triggerState = scheduler.getTriggerState(trigger.getKey());
		} catch (SchedulerException e) {
			new RuntimeException("Error retrieving trigger state");
		}
	}

	@Override
	public String toString() {
		return "[Task: " + getTask() + ",\n Trigger: " + getTriggerExpression()
				+ ",\n Trigger State: " + getTriggerState() + ",\n Fire Time: " + getFireTime()
				+ ",\n Scheduled Fire Time: " + getScheduledFireTime()
				+ ",\n Next Fire Times: " + printNextEvents() + ",\n Previous Fire Time: "
				+ getPreviousFireTime() + ",\n Execution Time: " + getExecutionTime()
				+ ",\n Max Execution Time: " + getMaxExecutionTime()
				+ ",\n Min Execution Time: " + getMinExecutionTime()
				+ ",\n Execution Count: " + getExecutionCount() + ",\n Refire Count: "
				+ getRefireCount() + ",\n Fail Count: " + getFailCount() + ",\n Last Exception: "
				+ getLastException() + ",\n Parameters: " + printParameters() + "]";
	}

}
