package br.com.caelum.vraptor.tasks.jobs;

import java.lang.reflect.Field;
import java.util.List;

import net.vidageek.mirror.dsl.Matcher;
import net.vidageek.mirror.dsl.Mirror;

import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.tasks.Param;
import br.com.caelum.vraptor.tasks.Task;

@Component
@ApplicationScoped
public class TaskFactory {
	
	private final Container container;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public TaskFactory(Container container) {
		this.container = container;
	}
	
	public Task newTask(Class<? extends Task> taskClass, JobDetail detail) {
		Task task = newInstance(taskClass);
		setParameters(task, detail);
		return task;
	}
	
	private Task newInstance(Class<? extends Task> taskClass) {
		try {
			if(container.canProvide(taskClass))
				return (Task) container.instanceFor(taskClass);
			else
				return (Task) taskClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void setParameters(Task task, JobDetail detail) {
		List<Field> params = new Mirror().on(task.getClass()).reflectAll().fieldsMatching(new ParamsFilter(detail));
		for (Field field : params) {
			set(task, field, paramValue(field, detail));
		}
	}
	
	private Object paramValue(Field field, JobDetail detail) {
		return detail.getJobDataMap().get(paramName(field));
	}
	
	private String paramName(Field field) {
		String name = new Mirror().on(field).reflect().annotation(Param.class).value();
		return !name.isEmpty() ? name : field.getName();
	}
	
	private void set(Task task, Field field, Object value) {
		try {
			logger.debug("Applying {} with {}", field.getType(), value);
			new Mirror().on(task).set().field(field.getName()).withValue(value);
		} catch (Exception ex) {
			logger.error("Unable to apply " + field.getType() + " with " + value, ex);
		}
	}
	
	class ParamsFilter implements Matcher<Field> {

		private final JobDetail detail;
	
		public ParamsFilter(JobDetail detail) {
			this.detail = detail;
		}
		
		private boolean contains(Field field) {
			return detail.getJobDataMap().containsKey(paramName(field));
		}
		
		public boolean accepts(Field element) {
			return element.isAnnotationPresent(Param.class) && contains(element);
		}
	}

}
