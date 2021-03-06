package nachos.threads;

import java.util.PriorityQueue;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 *
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		boolean intStatus = Machine.interrupt().disable();
		while (!waiterQueue.isEmpty()) {
			WaitingThread w = waiterQueue.peek();
			if (w.wakeTime > Machine.timer().getTime())
				break;
			w.thread.ready();
			waiterQueue.poll();
		}
		Machine.interrupt().restore(intStatus);
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 *
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 *
	 * @param x
	 *            the minimum number of clock ticks to wait.
	 *
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
		boolean intStatus = Machine.interrupt().disable();
		long wakeTime = Machine.timer().getTime() + x;
		waiterQueue.add(new WaitingThread(wakeTime, KThread.currentThread()));
		KThread.sleep();
		Machine.interrupt().restore(intStatus);
	}

	private class WaitingThread implements Comparable<WaitingThread> {
		private long wakeTime;
		private KThread thread;

		public WaitingThread(long wakeTime, KThread thread) {
			super();
			this.wakeTime = wakeTime;
			this.thread = thread;
		}

		@Override
		public int compareTo(WaitingThread o) {
			// TODO Auto-generated method stub
			int cmp = Long.compare(wakeTime, o.wakeTime);
			if (cmp != 0)
				return cmp;
			return thread.compareTo(o.thread);
		}
	}
	
	//run many threads, see what happened;
	public static void selfTest(){
		KThread t1=new KThread(new Runnable(){
			@Override
			public void run(){
				int t=4000;
				ThreadedKernel.alarm.waitUntil(t);
				Lib.debug('m',"wake "+t);
			}
		});
		KThread t2=new KThread(new Runnable(){
			@Override
			public void run(){
				int t=40;
				ThreadedKernel.alarm.waitUntil(t);
				Lib.debug('m',"wake "+t);
			}
		});
		KThread t3=new KThread(new Runnable(){
			@Override
			public void run(){
				int t=10000;
				ThreadedKernel.alarm.waitUntil(t);
				Lib.debug('m',"wake "+t);
			}
		});
		KThread t4=new KThread(new Runnable(){
			@Override
			public void run(){
				int t=2000;
				ThreadedKernel.alarm.waitUntil(t);
				Lib.debug('m',"wake "+t);
			}
		});
		KThread t5=new KThread(new Runnable(){
			@Override
			public void run(){
				int t=400;
				ThreadedKernel.alarm.waitUntil(t);
				Lib.debug('m',"wake "+t);
			}
		});
		KThread t6=new KThread(new Runnable(){
			@Override
			public void run(){
				int t=200;
				ThreadedKernel.alarm.waitUntil(t);
				Lib.debug('m',"wake "+t);
			}
		});
		KThread t7=new KThread(new Runnable(){
			@Override
			public void run(){
				int t=20;
				ThreadedKernel.alarm.waitUntil(t);
				Lib.debug('m',"wake "+t);
			}
		});
		t1.fork();
		t2.fork();
		t3.fork();
		t4.fork();
		t5.fork();
		t6.fork();
		t7.fork();
		t1.join();
		t2.join();
		t3.join();
		t4.join();
		t5.join();
		t6.join();
		t7.join();
	}

	private PriorityQueue<WaitingThread> waiterQueue = new PriorityQueue<WaitingThread>();
}
