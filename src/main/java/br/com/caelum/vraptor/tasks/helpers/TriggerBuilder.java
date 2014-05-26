package br.com.caelum.vraptor.tasks.helpers;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;

@ApplicationScoped
public class TriggerBuilder {
	
	private Trigger build(Scheduled params) {
		String expression = params.cron();
		int delay = params.initialDelay();
		Date startTime = new Date(System.currentTimeMillis() + delay);

		if (!expression.isEmpty()) {
			return newTrigger().withIdentity(randomKey())
							   .withSchedule(cronSchedule(expression))
							   .startAt(startTime)
							   .build();
		}

		int interval = params.fixedRate();
		
		return newTrigger()
				.withIdentity(randomKey())
				.startAt(startTime)
				.withSchedule(simpleSchedule().withIntervalInMilliseconds(interval)
				.repeatForever())
				.build();
	}
	
	public Trigger triggerFor(Class<? extends Task> task) throws ParseException {
		return build(task.getAnnotation(Scheduled.class));
	}
	
	public SimpleTrigger runOnce(Class<? extends Task> task) {
		return (SimpleTrigger) newTrigger() 
	            .withIdentity(randomKey())
	            .startAt(new Date())
	            .build();
	}
	
	public Trigger triggerFor(Class<?> controller, Method method) throws ParseException {
		Trigger trigger = build(method.getAnnotation(Scheduled.class));
		trigger.getJobDataMap().put("task-controller", controller.getName());
		trigger.getJobDataMap().put("task-method", method.getName());
		return trigger;
	}
	
	public Trigger cron(String expression) {
		return newTrigger().withIdentity(randomKey()).withSchedule(cronSchedule(expression)).build();
	}
	
	public String randomKey() {
		return UUID.randomUUID().toString();
	}

}
