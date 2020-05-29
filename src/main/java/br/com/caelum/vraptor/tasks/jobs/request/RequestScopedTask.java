package br.com.caelum.vraptor.tasks.jobs.request;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.TaskContext;

public class RequestScopedTask implements Task {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public void execute(TaskContext context) {
	
		try {
			
			URL endpoint = new URL(context.getString("task-uri"));
			String uri = endpoint.toExternalForm();
			logger.info("Executing task in URL " + uri);
			HttpClient client = new HttpClient();
			PostMethod post = new PostMethod(uri);
			
			int statusCode = client.executeMethod(post);
			logger.info("Task returned status code " + statusCode);
			
			if (!isOk(statusCode))
				throw new RuntimeException("Task failed with status code " + statusCode);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	private boolean isOk(int statusCode) {
		return statusCode >= 200 && statusCode <= 299;
	}


}