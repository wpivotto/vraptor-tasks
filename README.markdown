Tasks
======

Plug-in library of Quartz job scheduling for vraptor 

Installation 
--------

1. 	In a Maven project's pom.xml file:
	 
 		<repositories>
    		<repository>
        		<id>sonatype-oss-public</id>
        		<url>https://oss.sonatype.org/content/groups/public/</url>
        		<releases>
            			<enabled>true</enabled>
          		</releases>
         		<snapshots>
            			<enabled>true</enabled>
	       		</snapshots>
       	 	</repository>
		</repositories>
      
		<dependency>
  			<groupId>br.com.prixma</groupId>
  			<artifactId>vraptor-tasks</artifactId>
  			<version>1.0.0</version>
		</dependency>
		
		<dependency>
  			<groupId>org.quartz-scheduler</groupId>
  			<artifactId>quartz</artifactId>
  			<version>2.0.1</version>
  		</dependency>

    
Simple Task 
--------   

	@PrototypeScoped
	@Scheduled(cron = "* * 0/12 * * ?")
	public class Backup implements Task {

		private Database database = new Database();

		public void execute() {
			database.backup();
		}

	}


Transactional Task (Hibernate)
--------

	import br.com.caelum.vraptor.tasks.jobs.hibernate.TransactionalTask;
	
	@PrototypeScoped
	@Scheduled(fixedRate = 5000, initialDelay = 3000)
	public class DatabaseFiller implements TransactionalTask {

		private Database database;

		public void execute() {
			database.add(new RandomRecord());
		}

		//setup DAOs, Repositories...
		public void setup(Session session, Validator validator) {
			database = new HibernateDatabase(session);
		}
	}
	
Transactional Task (JPA)
--------

	import br.com.caelum.vraptor.tasks.jobs.jpa.TransactionalTask;
	
	@PrototypeScoped
	@Scheduled(fixedRate = 5000, initialDelay = 3000)
	public class DatabaseFiller implements TransactionalTask {

		private Database database;

		public void execute() {
			database.add(new RandomRecord());
		}

		public void setup(EntityManager manager, Validator validator) {
			database = new JPADatabase(manager);
		}
	}

Bean Validation (JSR303)	
--------

To use these features you only need to put any implementation of Bean Validation jars in your classpath.
If validation fails the transaction will not be effective. 

	import br.com.caelum.vraptor.tasks.jobs.jpa.TransactionalTask;
	import br.com.caelum.vraptor.tasks.validator.Validator;
	
	@PrototypeScoped
	@Scheduled(fixedRate = 60000)
	public class CsvImporter implements TransactionalTask {

		private Database database;
		private Validator validator;
		private CsvFile file = ...

		public void execute() {
			if(file.exists()){
				while(file.hasNext()){
					Client client = (Client) file.next();
					validator.validate(client);
					database.add(client);
				}
			}
		}

		public void setup(EntityManager manager, Validator validator) {
			this.database = new JPADatabase(manager);
			this.validator = validator;
		}
	}

--------

	@Entity
	public class client {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@CreditCardNumber
	private String creditCard;
	
	...


Manual Scheduling
--------
	
	Remove @Scheduled annotations
	Create the following component: 
	
	@Component
	@ApplicationScoped
	public class CustomScheduler {

		public CustomScheduler(TaskScheduler scheduler, List<Task> tasks){
			for(Task task : tasks){
				scheduler.schedule(task, custom trigger);
			}
		}
	}
	
Monitoring Tasks 
--------
	
	@Resource
	public class TasksController {

		public TasksController(TasksMonitor monitor) {
			
			TaskStatistics stats = monitor.getStatisticsFor(MyTask.class);
			
			log.debug("Fire Time: {}", stats.getFireTime());
			log.debug("Scheduled Fire Time: {}", stats.getScheduledFireTime());
			log.debug("Next Fire Time: {}", stats.getNextFireTime());
			log.debug("Previous Fire Time: {}", stats.getPreviousFireTime());
			log.debug("Execution Time: {}", stats.getExecutionTime());
			log.debug("Max Execution Time: {}", stats.getMaxExecutionTime());
			log.debug("Min Execution Time: {}", stats.getMinExecutionTime());
			log.debug("Execution Count: {}", stats.getExecutionCount());
			log.debug("Refire Count: {}", stats.getRefireCount());
			log.debug("Fail Count: {}", stats.getFailCount());
			log.debug("Last Fault: {}", stats.getLastException());
			
		}
	}
	
	
Tasks and Request Scope
--------

If you need to access components with request scope, the easiest way is to build your logic into a method of a controller and call it from a task.
There´s a helper class (TaskRequest) that does it for you. This class finds the path for your method dynamically and performs a get request.

	@ApplicationScoped
	@Scheduled(fixedRate = 5000)
	public class RequestScopeTask implements Task {

		private final TaskRequest request;
	
		public RequestScopeTask(TaskRequest request) {
			this.request = request;
		}

		public void execute() {
			request.access(Controller.class).method();
		}

	}
	
	@Resource
	public class Controller {
	
		private RequestComponent component;
		
		Controller(RequestComponent component){
			this.component = component;
		}
		
		@Get("task/execute")
		public void method(){
			//put task logic here
		}
	}
	
If you want to block requests from outside the server, there´s a solution here: <https://gist.github.com/1312993>

Creating Custom Tasks 
--------

To create custom tasks: 

1.	Create an interface that extends `br.com.caelum.vraptor.tasks.Task`

		public interface CustomTask extends Task {
		
			void myCustomBehaviour(MyCustomDependency dep); 
			
		}
	
2.	Create a class that decorate the execution of its task (must implement `org.quartz.Job`)

		public class CustomJobWrapper implements Job {
	
			private final CustomTask task;
			private final MyCustomDependency dep;
		
			public CustomJobWrapper(CustomTask task, MyCustomDependency dep) {
				this.task = task;
				this.dep = dep;
			}
		
			public void execute(JobExecutionContext context) throws JobExecutionException {
				task.myCustomBehaviour(dep);
				task.execute();
			}
	
		}

3.	Create a class that implements `br.com.caelum.vraptor.tasks.jobs.JobProvider`

		@Component
		@ApplicationScoped
		public class CustomTaskProvider implements JobProvider {
	
			private MyCustomDependency dep;
			
			//Receive your task dependencies via constructor
			public CustomTaskProvider(MyCustomDependency dep){
				this.dep = dep;
			}
			
			//Should only decorate custom Tasks 
			public boolean canDecorate(Class<? extends Task> task) {
				return CustomTask.class.isAssignableFrom(task);
			}
			
			//Register your decorator 
			public Class<? extends Job> getJobWrapper() {
				return CustomJobWrapper.class;
			}
			
			//Should only instantiate custom Tasks 
			public boolean canProvide(Class<? extends Job> job) {
				return CustomJobWrapper.class.equals(job);
			}
	
			//Delegates the execution
			public Job newJob(Task task) {
				return new CustomJobWrapper((CustomTask) task, dep);
			}
	
		}

License
--------
Copyright (c) 2011 William Pivotto
All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
