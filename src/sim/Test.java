package sim;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;

public class Test {
	public static void main(String[] args) {
		 final String[] serverIPs = {
			 "192.168.101.21"
		 };
		 final int port = 1099;
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
			
			Scanner sc = new Scanner(System.in);
			int N = sc.nextInt();
			int input;
			for(int i=0;i<N;i++) {
				input = sc.nextInt();
				while(!submitToServer(input,stubList))
					System.out.println("Failing to process input: "+input);
			}
			System.out.println("Input over; waiting for results...");
			for(Simulation stub:stubList)
				while(!stub.isIdle()) {
					System.out.println(stub.status());
					while(stub.hasResults())
						System.out.println(stub.getResult());
					Thread.sleep(200);
				}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static boolean submitToServer(int x,ArrayList<Simulation> stubList) throws RemoteException {
		for(Simulation stub:stubList)
			if(!stub.isBacklogged()) {
				stub.addToQueue(x);
				return true;
			}
		return false;
	}
}
