package sim;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import jneat.Network;

public class SimHandler implements Simulation {
	ArrayList<Network> queue = new ArrayList<Network>();
	ArrayList<Network> running = new ArrayList<Network>();
	ArrayList<Double[]> results = new ArrayList<Double[]>();
	int numNets = 1;
	boolean comm = false;
	
	final int maxBacklog = 4;
	final int numThreads = 4;
	Thread[] threads = new Thread[numThreads];
	
	public boolean hasRequest() {
		return queue.size() > 0;
	}
	public synchronized Network getRequest() {
		if(queue.size() == 0)
			return null;
		Network req = queue.get(0);
		queue.remove(req);
		running.add(req);
		return req;
	}
	public synchronized Network[] getMultiRequest(int n) {
		if(queue.size() < n)
			return null;
		Network[] out = new Network[n];
		for(int i=0;i<n;i++) {
			out[i] = queue.get(0);
			queue.remove(out[i]);
			running.add(out[i]);
		}
		return out;
	}
	
	//Add result to results, remove it from running
	public synchronized void addResult(Network req,Double r) {
		results.add(new Double[]{(double)req.getNet_id(),r});
		running.remove(req);
	}
	public synchronized void addMultiResult(Network[] reqs,Double[] res) {
		for(int i=0;i<reqs.length;i++) {
			results.add(new Double[]{(double)reqs[i].getNet_id(),res[i]});
			running.remove(reqs[i]);
		}
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
					Network request = null;
					Network[] multiRequest = null;
					if(numNets == 1) {
						request = server.getRequest();
						if(request != null)
							addResult(request,processInput(request));
					}
					else {
						multiRequest = getMultiRequest(numNets);
						if(multiRequest != null)
							addMultiResult(multiRequest,processMultiInput(multiRequest));
					}
				}
				else {
					try {
						//System.out.println("Thread "+tID+": idle!");
						Thread.sleep(500);
						//TimeUnit.SECONDS.sleep(1);
					} catch (Exception e) {
						//e.printStackTrace();
					}
				}
			}
		}
		public Double processInput(Network x) {
			return Thrust.getBestFitnessHeadlessSim(x);
		}
		//CAUTION: assumes 3 neural networks, does not work for any N
		public Double[] processMultiInput(Network[] nets) {
			Double fit = Thrust.getBestFitnessSim(nets[0], nets[1], nets[2], false, comm);
			return new Double[]{fit,fit,fit};
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
	public double getBestFitnessHeadlessSim(Network net1,Network net2,Network net3) {
		return Thrust.getBestFitnessSim(net1, net2, net3, false, comm);
	}
	
	// Adds net to processing queue
	// Returns true if added
	// Returns false if queue is backlogged
	public boolean addToQueue(Network x) {
		if(queue.size() < maxBacklog) {
			numNets = 1;
			comm = false;
			queue.add(x);
			return true;
		}
		return false;
	}
	public boolean addToQueue(Network net1,Network net2,Network net3,boolean comm) {
		if(queue.size() < maxBacklog) {
			numNets = 3;
			this.comm = comm;
			queue.add(net1);
			queue.add(net2);
			queue.add(net3);
			return true;
		}
		return false;
	}
	public boolean hasResults() {
		return results.size() > 0;
	}
	public Double[] getResult() {
		Double[] r = results.get(0);
		results.remove(r);
		return r;
	}
	public ArrayList<Double[]> getAllResults(){
		ArrayList<Double[]> r = results;
		results = new ArrayList<Double[]>();
		return r;
	}
	public boolean isIdle() {
		return queue.size()==0 && running.size()==0 && results.size()==0;
	}
	public String status() {
		return "Queue: "+queue.size()+"     Status: "+running.size()+"     Results: "+results.size();
	}
	public boolean isBacklogged(){
		return queue.size() >= maxBacklog;
	}
}
