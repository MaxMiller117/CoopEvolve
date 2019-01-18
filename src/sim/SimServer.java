package sim;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class SimServer extends SimHandler{
	public SimServer() {}
	public static void main(String args[]) {
		try {
			SimHandler simObject = new SimHandler();
			
			Simulation stub = (Simulation)UnicastRemoteObject.exportObject(simObject,0);
			
			//Registry registry = LocateRegistry.getRegistry();
			Registry registry = LocateRegistry.createRegistry(1111);
			
			registry.bind("Simulation", stub);
			System.err.println("Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: "+e.toString());
			e.printStackTrace();
		}
	}
}
