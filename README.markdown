Tasks
======

Plug-in library of Quartz job scheduling for vraptor 

Installation 
--------

Put `quartz.jar` and `vraptor-tasks.jar` in your `WEB-INF/lib` folder. You can get a copy here
Add packages on `web.xml`

	<context-param>
        	<param-name>br.com.caelum.vraptor.packages</param-name>
	        <param-value>br.com.caelum.vraptor.tasks</param-value>
    </context-param>
    
Simple Task 
--------   

	@PrototypeScoped
	@Scheduled(cron = "* * 0/12 * * ?")
	public class Backup implements Task {

		private Database database = new Database();

		@Override
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

		@Override
		public void execute() {
			database.add(new RandomRecord());
		}

		@Override
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

		@Override
		public void execute() {
			database.add(new RandomRecord());
		}

		@Override
		//setup DAOs, Repositories...
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

		public TasksController(TasksMonitor monitor){
			TaskStatistics stats = monitor.getStatisticsFor(MyTask.class);
			log.info("Next Fire Time " + stats.getNextFireTime());
			...
		}
	}
	
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
