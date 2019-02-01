package sim;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;

import jneat.Network;

public class Test {
	public static void main(String[] args) {
		 final String[] serverIPs = {
			 "192.168.101.21"
		 };
		 final int port = 1099;
		 final int population = 250;
		 try {
			//System.setProperty("java.security.policy","file:./test.policy");
			System.setProperty("java.security.policy","file:/C:/Users/MaxMi/Documents/GitHub/CoopEvolve/src/test.policy");
	        if (System.getSecurityManager() == null) {
	            System.setSecurityManager(new SecurityManager());
	        }
			 
	        ArrayList<Registry> registryList = new ArrayList<Registry>();
	        for(String ip:serverIPs) {
	        	try {
	        		registryList.add(LocateRegistry.getRegistry(ip, port));
	        	}catch(Exception e) {
	        		System.out.println("Finding registry for server \""+ip+"\"failed!");
	        	}
	        }
			
	        ArrayList<Simulation> stubList = new ArrayList<Simulation>();
			//Simulation stub = (Simulation)registry.lookup("Simulation");
			for(Registry registry:registryList) {
				try {
					stubList.add((Simulation)registry.lookup("Simulation"));
				}catch(Exception e) {
					System.out.println("Getting stub from a registry failed!");
				}
			}
			
			//System.out.println(stub.printMsg());
			
			//Scanner sc = new Scanner(System.in);
			//int N = sc.nextInt();
			
			Network[] networkList = new Network[population];
			for(int i=0;i<networkList.length;i++) {
				Network input=Thrust.readGenomeFromFile();
				input.setNet_id(i);
				networkList[i]=input;
			}
			
			long startTime = System.currentTimeMillis();
			for(int i=0;i<networkList.length;i++) {
				boolean success = false;
				while(!success) {
					success = submitToServer(networkList[i],stubList);
					if(!success) {
						//System.out.println("Failing to add input to queue: "+input);
						Thread.sleep(50);
					}
				}
			}
			long stopTimeInput = System.currentTimeMillis();
			double elapsedTime = (stopTimeInput - startTime)/1000.0;
			System.out.println("Input time took "+elapsedTime+"s");
			System.out.println("Input over; waiting for results...");
			for(Simulation stub:stubList)
				while(!stub.isIdle()) {
					//System.out.println(stub.status());
					while(stub.hasResults())
						nicerPrint(stub.getResult());
					Thread.sleep(200);
				}
			long stopTime = System.currentTimeMillis();
			elapsedTime = (stopTime - stopTimeInput)/1000.0;
			System.out.println("Processing took "+elapsedTime+"s");
			elapsedTime = (stopTime - startTime)/1000.0;
			System.out.println("Total time "+elapsedTime+"s");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static boolean submitToServer(Network x,ArrayList<Simulation> stubList) throws RemoteException {
		for(Simulation stub:stubList)
			if(!stub.isBacklogged()) {
				stub.addToQueue(x);
				return true;
			}
		return false;
	}
	static void nicerPrint(Double[] x) {
		int netID = (int)x[0].doubleValue();
		System.out.println("NN "+netID+" returned fitness "+x[1]+"!");
	}
}
