package br.com.caelum.vraptor.tasks.scheduler;

import static org.quartz.JobBuilder.newJob;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.quartz.Calendar;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.Trigger.TriggerState;
import org.quartz.spi.OperableTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.jobs.JobProvider;
import br.com.caelum.vraptor.tasks.jobs.simple.DefaultJobProvider;

import com.google.common.collect.Lists;


@ApplicationScoped
public class QuartzScheduler implements TaskScheduler {

	protected Scheduler quartz;
	private List<JobProvider> providers;
	private Logger log = LoggerFactory.getLogger(getClass());

	@Deprecated // CDI eyes only
	public QuartzScheduler() {}
	
	@Inject
	public QuartzScheduler(Scheduler quartz, @Any Instance<JobProvider> providers) {
		this.quartz = quartz;
		this.providers = Lists.newArrayList(providers);
	}

	public void schedule(Class<? extends Task> task, Trigger trigger, String id) {
		schedule(task, trigger, id, new JobDataMap());
	}
	
	public void schedule(Class<? extends Task> task, Trigger trigger, String id, JobDataMap dataMap) {

		JobDetail detail = newJob(jobFor(task)).withIdentity(id).usingJobData(dataMap).build();

		try {
			if(!alreadyExists(id)) {
				detail.getJobDataMap().put("task-class", task);
				detail.getJobDataMap().put("task-id", id);
				quartz.scheduleJob(detail, trigger);
			}
			else
				log.warn("Unable to schedule task {} because one already exists with this identification", task);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}

	}

	private Class<? extends Job> jobFor(Class<? extends Task> task) {

		Scheduled options = task.getAnnotation(Scheduled.class);

		for(JobProvider provider : providers){
			if(provider.canDecorate(task))
				return provider.getJobWrapper(options);
		}

		return new DefaultJobProvider().getJobWrapper(options);

	}

	public void unschedule(String id) {
		JobKey jobKey = new JobKey(id);
		try {
			if (quartz.checkExists(jobKey)) {
				quartz.deleteJob(jobKey);
			}
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean alreadyExists(String id) {
		try {
			return quartz.checkExists(new JobKey(id));
		} catch (SchedulerException e) {
			return false;
		}
	}

	@PostConstruct
	public void setup() {
		System.setProperty("org.terracotta.quartz.skipUpdateCheck", "true");
	}

	public void unscheduleAll() {
		try {
			quartz.clear();
		} catch (SchedulerException e) {}
	}
	
	public TriggerState getTriggerState(String id) {
		try {
			return quartz.getTriggerState(new TriggerKey(id));
		} catch (SchedulerException e) {
			return TriggerState.NONE;
		}
	}
	
	public boolean pauseTrigger(String id) {
		try {
			quartz.pauseTrigger(new TriggerKey(id));
			return true;
		} catch (SchedulerException e) {
			return false;
		}
	}
	
	public boolean rescheduleTrigger(String id, Trigger trigger) {
		try {
			quartz.rescheduleJob(new TriggerKey(id), trigger);
			return true;
		} catch (SchedulerException e) {
			return false;
		}
	}
	
	public boolean resumeTrigger(String id) {
		try {
			quartz.resumeTrigger(new TriggerKey(id));
			return true;
		} catch (SchedulerException e) {
			return false;
		}
	}
	
	public boolean resetTrigger(String id) {
		try {
			quartz.resetTriggerFromErrorState(new TriggerKey(id));
			return true;
		} catch (SchedulerException e) {
			return false;
		}
	}
	
	public Date getPreviousFireTime(String id) {
		try {
			Trigger trigger = quartz.getTrigger(new TriggerKey(id));
			return trigger.getPreviousFireTime();
		} catch (Exception e) {
			return null;
		}
	}
	
	public Date getNextFireTime(String id) {
		try {
			Trigger trigger = quartz.getTrigger(new TriggerKey(id));
			return trigger.getNextFireTime();
		} catch (Exception e) {
			return null;
		}
	}
	
	public List<Date> getNextFireTimes(String id, int maxCount) {
		
		List<Date> result = new ArrayList<>();
		
		try {
			
			Trigger trigger = quartz.getTrigger(new TriggerKey(id));
			
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
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
		
	}

}
