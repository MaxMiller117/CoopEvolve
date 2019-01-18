package sim;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Test {
	public static void main(String[] args) {
		 
		 try {
			 Registry registry = LocateRegistry.getRegistry(1111);
			 
			Simulation stub = (Simulation)registry.lookup("Simulation");
			
			System.out.println(stub.printMsg());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
