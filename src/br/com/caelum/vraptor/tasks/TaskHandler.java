package br.com.caelum.vraptor.tasks;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.quartz.Trigger;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.tasks.scheduler.Scheduled;
import br.com.caelum.vraptor.tasks.scheduler.TaskScheduler;

@Component
@ApplicationScoped
public class TaskHandler {

	private final TaskScheduler scheduler;
	private final List<Task> tasks;
	private final AtomicInteger id = new AtomicInteger();

	public TaskHandler(TaskScheduler scheduler, List<Task> tasks) {
		this.scheduler = scheduler;
		this.tasks = tasks;
	}

	@PostConstruct
	public void setup() {
		for (Task task : tasks) {
			if(task.getClass().isAnnotationPresent(Scheduled.class))
				handle(task);
		}
	}

	public void handle(Task task) {

		try {

			Trigger trigger = getTriggerExpression(task.getClass());
			scheduler.schedule(task, trigger);

		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

	}

	private Trigger getTriggerExpression(Class<? extends Task> resource) throws ParseException {

		Scheduled params = resource.getAnnotation(Scheduled.class);
		
		String expression = params.value();

		if (!expression.isEmpty()) {
			return newTrigger().withIdentity("trigger" + id.incrementAndGet())
							   .withSchedule(cronSchedule(expression))
							   .build();
		}

		int interval = params.fixedRate();
		int delay = params.initialDelay();
		Date startTime = new Date(System.currentTimeMillis() + delay);
		
		return newTrigger()
				.withIdentity("trigger" + id.incrementAndGet())
				.startAt(startTime)
				.withSchedule(simpleSchedule().withIntervalInMilliseconds(interval)
				.repeatForever())
				.build();
	}

}
