package br.com.caelum.vraptor.tasks.jobs.jpa;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobExecutionContext;

import br.com.caelum.vraptor.tasks.validator.TaskValidator;

public class JPAJobTest {
	
	@Mock private EntityManager manager;
    @Mock private EntityTransaction transaction;
    @Mock private TransactionalTask task;
    @Mock private TaskValidator validator;
    @Mock private JobExecutionContext context;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void shouldStartAndCommitTransaction() throws Exception {

    	JPAJob job = new JPAJob(task, validator, manager);
       
    	when(manager.getTransaction()).thenReturn(transaction);
        when(transaction.isActive()).thenReturn(false);
        
        job.execute(context);

        InOrder callOrder = inOrder(manager, task, transaction);
        callOrder.verify(transaction).begin();
        callOrder.verify(task).setup(manager, validator);
        callOrder.verify(task).execute();
        callOrder.verify(transaction).commit();
    }
    
    @Test
    public void shouldRollbackTransactionIfStillActiveWhenExecutionFinishes() throws Exception {
    	
    	JPAJob job = new JPAJob(task, validator, manager);
        when(manager.getTransaction()).thenReturn(transaction);
        when(transaction.isActive()).thenReturn(true);
        job.execute(context);
        verify(transaction).rollback();
    }
    
}
