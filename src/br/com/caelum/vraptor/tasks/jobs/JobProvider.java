package br.com.caelum.vraptor.tasks.jobs;

import org.quartz.Job;
import org.quartz.JobDetail;

public interface JobProvider {

	Job newJob(JobDetail jobDetail);

}
