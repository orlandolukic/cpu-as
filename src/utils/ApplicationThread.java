package utils;

import java.util.Iterator;
import java.util.LinkedList;

public class ApplicationThread extends Thread {

    private static LinkedList<Thread> threads;
    static {
        threads = new LinkedList<>();
    }
    public static void shutdownAllThreads()
    {
        Iterator<Thread> it = threads.iterator();
        Thread t;
        while( it.hasNext() )
        {
            t = it.next();
            t.interrupt();
        };
    }

    public static void addApplicationThread( Thread t ) {
        if ( threads.contains(t) )
            return;
        threads.add(t);
    }

    protected Runnable runnable;

    public ApplicationThread(Runnable r) {
        runnable = r;
    }

    public ApplicationThread() {}

    @Override
    public synchronized void start() {
        threads.add(this);
        super.start();
    }

    @Override
    public void run() {
        runnable.run();
        threads.remove(this);
    }
}
