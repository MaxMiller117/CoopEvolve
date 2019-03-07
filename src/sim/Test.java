package sim;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import jneat.Network;
import jneat.Organism;

public class Test {
	static final String[] serverIPs = {
		"172.16.6.228"     //Server outside
		//"192.168.7.100"    // Server
		//"192.168.101.21" // Desktop
		//"127.0.0.1"      // Laptop (localhost)
		//"172.16.5.185"    // Old Desktop
		//"25.6.128.250"     // Old Desktop Hamachi
	};
	static final int port = 1099;
	public static void main(String[] args) {
		 final int numNets = 1;
		 final boolean comm = false;
		 final int population = 25*numNets;
		 try {
			setSecurityPolicy();
			 
	        ArrayList<Registry> registryList = getRegistryList();
			
	        ArrayList<Simulation> stubList = getStubList(registryList);
			
			//System.out.println(stub.printMsg());
			
			//Scanner sc = new Scanner(System.in);
			//int N = sc.nextInt();
			
	        //Generate list of neural networks for testing
			ArrayList<Network> networkList = new ArrayList<Network>();
			for(int i=0;i<population;i++) {
				String filename = "ex_genome_1";
				Network input=Thrust.readGenomeFromFile(filename);
				input.setNet_id(i);
				networkList.add(input);
			}
			
			processNNList(networkList,numNets,comm,stubList);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setSecurityPolicy() {
		//System.setProperty("java.security.policy","file:./test.policy");
		System.setProperty("java.security.policy","file:/C:/Users/MaxMi/Documents/GitHub/CoopEvolve/src/test.policy");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
	}
	
	public static ArrayList<Registry> getRegistryList(){
		ArrayList<Registry> registryList = new ArrayList<Registry>();
        for(String ip:serverIPs) {
        	try {
        		registryList.add(LocateRegistry.getRegistry(ip, port));
        	}catch(Exception e) {
        		System.out.println("Finding registry for server \""+ip+"\"failed!");
        	}
        }
        return registryList;
	}
	
	public static ArrayList<Simulation> getStubList(ArrayList<Registry> registryList){
		ArrayList<Simulation> stubList = new ArrayList<Simulation>();
		//Simulation stub = (Simulation)registry.lookup("Simulation");
		for(Registry registry:registryList) {
			try {
				stubList.add((Simulation)registry.lookup("Simulation"));
			}catch(Exception e) {
				System.out.println("Getting stub from a registry failed!");
				e.printStackTrace();
			}
		}
		return stubList;
	}
	
	public static ArrayList<Double[]> processOrganismList(List<Organism> organismList,int numNets,boolean comm,List<Simulation> stubList) throws RemoteException {
		ArrayList<Network> networkList = new ArrayList<Network>();
		for(int i=0;i<organismList.size();i++)
			networkList.add(organismList.get(i).net);
		return processNNList(networkList,numNets,comm,stubList);
	}
	public static ArrayList<Double[]> processNNList(List<Network> networkList,int numNets,boolean comm,List<Simulation> stubList) throws RemoteException {
		System.out.println("Starting to simulate "+networkList.size()+" neural networks.");
		long startTime = System.currentTimeMillis();
		for(int i=0;i<networkList.size();i+=numNets) {
			boolean success = false;
			while(!success) {
				//success = submitToServer(networkList[i],stubList);
				if(numNets == 1)
					success = submitToServer(networkList.get(i),stubList);
				else
					success = submitToServer(networkList.subList(i,i+numNets),comm,stubList);
				if(!success) {
					//System.out.println("Failing to add input to queue: "+input);
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		long stopTimeInput = System.currentTimeMillis();
		double elapsedTime = (stopTimeInput - startTime)/1000.0;
		System.out.println("Input time took "+elapsedTime+"s");
		System.out.println("Input over; waiting for results...");
		
		ArrayList<Double[]> resultList = new ArrayList<Double[]>();
		for(Simulation stub:stubList)
			while(!stub.isIdle()) {
				//System.out.println(stub.status());
				while(stub.hasResults()) {
					Double[] result = stub.getResult();
					nicerPrint(result);
					resultList.add(result);
				}
					
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		
		long stopTime = System.currentTimeMillis();
		elapsedTime = (stopTime - stopTimeInput)/1000.0;
		System.out.println("Processing took "+elapsedTime+"s");
		elapsedTime = (stopTime - startTime)/1000.0;
		System.out.println("Total time "+elapsedTime+"s");
		
		return resultList;
	}
	
	public static boolean submitToServer(Network x,List<Simulation> stubList) throws RemoteException {
		for(Simulation stub:stubList)
			if(!stub.isBacklogged())
				return stub.addToQueue(x);
		return false;
	}
	public static boolean submitToServer(Network net1,Network net2,Network net3,boolean comm,List<Simulation> stubList) throws RemoteException {
		for(Simulation stub:stubList)
			if(!stub.isBacklogged())
				return stub.addToQueue(net1,net2,net3,comm);
		return false;
	}
	//Only support for 1 or 3 neural networks
	public static boolean submitToServer(List<Network> netList,boolean comm,List<Simulation> stubList) throws RemoteException {
		if(netList.size()==3)
			return submitToServer(netList.get(0),netList.get(1),netList.get(2),comm,stubList);
		return submitToServer(netList.get(0),stubList);
	}
	
	public static void nicerPrint(Double[] x) {
		int netID = (int)x[0].doubleValue();
		System.out.println("NN "+netID+" returned fitness "+x[1]+"!");
	}
}
