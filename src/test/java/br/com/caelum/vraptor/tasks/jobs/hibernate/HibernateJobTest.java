package br.com.caelum.vraptor.tasks.jobs.hibernate;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobExecutionContext;

import br.com.caelum.vraptor.tasks.validator.TaskValidator;

public class HibernateJobTest {
	
	@Mock private Session session;
    @Mock private Transaction transaction;
    @Mock private TransactionalTask task;
    @Mock private TaskValidator validator;
    @Mock private JobExecutionContext context;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void shouldStartAndCommitTransaction() throws Exception {

    	HibernateJob job = new HibernateJob(task, validator, session);

        when(session.beginTransaction()).thenReturn(transaction);
        when(transaction.isActive()).thenReturn(false);

        job.execute(context);

        InOrder callOrder = inOrder(session, task, transaction);
        callOrder.verify(session).beginTransaction();
        callOrder.verify(task).setup(session, validator);
        callOrder.verify(task).execute();
        callOrder.verify(transaction).commit();
    }
    
    @Test
    public void shouldRollbackTransactionIfStillActiveWhenExecutionFinishes() throws Exception {
    	
    	HibernateJob job = new HibernateJob(task, validator, session);
        when(session.beginTransaction()).thenReturn(transaction);
        when(transaction.isActive()).thenReturn(true);
        job.execute(context);
        verify(transaction).rollback();
        
    }
    
}
