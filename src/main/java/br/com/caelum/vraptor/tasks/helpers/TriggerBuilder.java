package br.com.caelum.vraptor.tasks.helpers;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.quartz.JobDataMap;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import br.com.caelum.vraptor.http.Parameter;
import br.com.caelum.vraptor.http.ParameterNameProvider;
import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.TaskLinker;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;

@RequestScoped
public class TriggerBuilder {
	
	@Inject private TaskLinker linker;
	@Inject private ParameterNameProvider provider;
	
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
	
	public SimpleTrigger runOnce() {
		return (SimpleTrigger) newTrigger() 
	            .withIdentity(randomKey())
	            .startAt(new Date())
	            .build();
	}
	
	public Trigger triggerFor(Method method) {
		return triggerFor(method, new Object[method.getParameterTypes().length]);
	}
	
	public Trigger triggerFor(Method method, Object[] args) {
		Class<?> controller = method.getDeclaringClass();
		Trigger trigger = getTrigger(method);
		String uri = linker.linkTo(controller, method, args);
		Parameter[] parameters = provider.parametersFor(method);
		JobDataMap dataMap = trigger.getJobDataMap();
		dataMap.put("task-controller", controller.getName());
		dataMap.put("task-method", method.getName());
		dataMap.put("task-uri", uri);
		for (int i = 0; i < parameters.length; i++) {
			if (args[i] != null)
				dataMap.put(parameters[i].getName(), args[i]);
		}
		return trigger;
	}
	
	private Trigger getTrigger(Method method) {
		if (method.isAnnotationPresent(Scheduled.class))
			return build(method.getAnnotation(Scheduled.class));
		return runOnce();
	}
	
	public Trigger cron(String expression) {
		return newTrigger().withIdentity(randomKey()).withSchedule(cronSchedule(expression)).build();
	}
	
	public String randomKey() {
		return UUID.randomUUID().toString();
	}

}
