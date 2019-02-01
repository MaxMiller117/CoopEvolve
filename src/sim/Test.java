package sim;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Test {
	public static void main(String[] args) {
		 
		 try {
			Registry registry = LocateRegistry.getRegistry(1111);
			 
			Simulation stub = (Simulation)registry.lookup("Simulation");
			
			System.out.println(stub.printMsg());
			
			Scanner sc = new Scanner(System.in);
			int N = sc.nextInt();
			for(int i=0;i<N;i++)
				stub.addToQueue(sc.nextInt());
			System.out.println("Input over; waiting for results...");
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
}
