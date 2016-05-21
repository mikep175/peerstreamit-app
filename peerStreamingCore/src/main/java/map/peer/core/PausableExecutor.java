package map.peer.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class PausableExecutor extends ScheduledThreadPoolExecutor {
    public Continue ExecutorContinue;

    public PausableExecutor(int poolSize)
    {
    	super(poolSize, Executors.defaultThreadFactory());
    	
    	ExecutorContinue = new Continue();
    	
    }
    
    public PausableExecutor(int poolSize, Continue c)
    {
    	super(poolSize, Executors.defaultThreadFactory());
    	
    	ExecutorContinue = c;
    	
    }
    
    public PausableExecutor(int corePoolSize, ThreadFactory threadFactory, Continue c) {
        super(corePoolSize, threadFactory);
        ExecutorContinue = c;
    }

    protected void beforeExecute(Thread t, Runnable r) {
        try { 
        	ExecutorContinue.checkIn();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        super.beforeExecute(t, r);
    }
}
