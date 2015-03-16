package br.com.caelum.vraptor.tasks.jobs.request;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRequestScopedTask implements RequestScopedTask {

	private URL endpoint;
	private Map<String, Object> params;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public void execute() {
		String uri = endpoint.toExternalForm();
		logger.info("Executing task in URL " + uri);
		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod(uri);
		includeParams(post);

		int statusCode = 0;
		try {
			statusCode = client.executeMethod(post);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		logger.info("Task returned status code " + statusCode);
		if (!isOk(statusCode))
			throw new RuntimeException("Task failed with status code " + statusCode);
	}
	
	private boolean isOk(int statusCode) {
		return statusCode >= 200 && statusCode <= 299;
	}
	
	private PostMethod includeParams(PostMethod post) {
		Iterator<Entry<String, Object>> i = params.entrySet().iterator();  
		while(i.hasNext()) {
			Entry<String, Object> param = i.next();
		    if (!param.getKey().startsWith("task-")) { //skip plugin params
			    post.setParameter(param.getKey(), param.getValue().toString());
		    }
		}
		return post;
	}

	public void setup(URL endpoint, Map<String, Object> params) {
		this.endpoint = endpoint;
		this.params = params;
	}

}