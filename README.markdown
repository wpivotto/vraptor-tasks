Tasks
======

Plug-in da biblioteca de agendamento de tarefas Quartz, para Vraptor


Tarefa Simples
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


Tarefa com controle transacional
--------

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
		public void setup(Session session) {
			database = new Database(session);
		}
	}
	
Agendamento manual
--------
	
	Remova a anotação @Scheduled das Tasks
	Crie o seguinte componente:
	
	@Component
	@ApplicationScoped
	public class CustomScheduler {

		public CustomScheduler(TaskScheduler scheduler, List<Task> tasks){
			for(Task task : tasks){
				scheduler.schedule(task, custom trigger);
			}
		}
	}
	
Monitorando Tasks
--------
	
	@Resource
	public class TasksController {

		public TasksController(TasksMonitor monitor){
			TaskStatistics stats = monitor.getStatisticsFor(MyTask.class);
			log.info("Next Fire Time " + stats.getNextFireTime());
			...
		}
	}
