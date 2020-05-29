package br.com.caelum.vraptor.tasks;

import java.util.Iterator;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

public class TaskContext {
	
	private JobDataMap map;

	public TaskContext(JobDetail detail) {
		this.map = detail.getJobDataMap();
	}

	public TaskContext(JobExecutionContext context) {
		this.map = context.getMergedJobDataMap();
	}

	public Object get(String key) {
		return map.get(key);
	}
	
	public String getString(String key) {
		return map.getString(key);
	}
	
	public void put(String key, Object value) {
		map.put(key, value);
	}
	
	public String getId() {
		return map.getString("task-id");
	}
	
	public String dump() {

		StringBuilder dump = new StringBuilder();

		for (Iterator<String> keys = (Iterator<String>) map.keySet().iterator(); keys.hasNext();) {
			String key = keys.next();
			dump.append(key).append(" = ").append(map.get(key));
			if (keys.hasNext()) {
				dump.append(System.lineSeparator());
			}
		}

		return dump.toString();

	}
	
	

}
