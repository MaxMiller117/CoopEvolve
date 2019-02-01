package sim;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import jneat.Network;

public class SimHandler implements Simulation {
	ArrayList<Integer> queue = new ArrayList<Integer>();
	ArrayList<Integer> running = new ArrayList<Integer>();
	ArrayList<Double> results = new ArrayList<Double>();
	
	final int maxBacklog = 10;
	final int numThreads = 8;
	Thread[] threads = new Thread[numThreads];
	
	public boolean hasRequest() {
		return queue.size() > 0;
	}
	public synchronized Integer getRequest() {
		if(queue.size() == 0)
			return null;
		Integer req = queue.get(0);
		queue.remove(req);
		running.add(req);
		return req;
	}
	public synchronized void addResult(Integer req,Double r) {
		results.add(r);
		running.remove(req);
	}
	
	class QueueProcessor implements Runnable{
		SimHandler server;
		int tID = -1;
		public QueueProcessor(SimHandler server) {
			this.server = server;
		}
		public QueueProcessor(SimHandler server,int tID) {
			this.server = server;
			this.tID = tID;
		}
		public void run() {
			while(true) {
				if(server.hasRequest()) {
					//System.out.println("Thread "+tID+": attempting to get request...");
					Integer request = server.getRequest();
					if(request != null) {
						addResult(request,processInput(request));
					}
				}
				else {
					try {
						//System.out.println("Thread "+tID+": idle!");
						Thread.sleep(1000);
						//TimeUnit.SECONDS.sleep(1);
					} catch (Exception e) {
						//e.printStackTrace();
					}
				}
			}
		}
		public Double processInput(Integer x) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return tID*100 + Math.sqrt(x);
		}
		
	}
	
	public SimHandler(){
		for(int i=0;i<numThreads;i++) {
			threads[i] = new Thread(new QueueProcessor(this,i));
			threads[i].start();
		}
	}
	
	public String printMsg() {
		System.out.println("Test message...");
		return "Printed message...";
	}
	public double getBestFitnessSim(Network net,boolean head) {
		return Thrust.getBestFitnessSim(net, head);
	}
	public double getBestFitnessHeadlessSim(Network net) {
		return Thrust.getBestFitnessHeadlessSim(net);
	}
	
	// Adds int to processing queue
	// Returns true if added
	// Returns false if queue is backlogged
	public boolean addToQueue(int x) {
		if(queue.size() < 10) {
			queue.add(x);
			return true;
		}
		return false;
	}
	public boolean hasResults() {
		return results.size() > 0;
	}
	public double getResult() {
		double r = results.get(0);
		results.remove(r);
		return r;
	}
	public ArrayList<Double> getAllResults(){
		ArrayList<Double> r = results;
		results = new ArrayList<Double>();
		return r;
	}
	public boolean isIdle() {
		return queue.size()==0 && running.size()==0 && results.size()==0;
	}
	public String status() {
		return "Queue: "+queue.size()+"     Status: "+running.size()+"     Results: "+results.size();
	}
}
