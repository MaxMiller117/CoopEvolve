package sim;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.security.auth.Policy;

public class SimServer extends SimHandler{
	public SimServer() {
		super();
	}
	public static void main(String args[]) {
		final int port = 1099;
		try {
			System.setProperty("java.security.policy","file:./test.policy");
			//System.setProperty("java.security.policy","file:/C:/Users/Max/Documents/GitHub/CoopEvolve/test.policy");
			
	        if (System.getSecurityManager() == null) {
	            System.setSecurityManager(new SecurityManager());
	        }
			
			SimHandler simObject = new SimHandler();
			
			Simulation stub = (Simulation)UnicastRemoteObject.exportObject(simObject,0);
			
			//Registry registry = LocateRegistry.getRegistry("172.16.10.237",port);
			Registry registry = LocateRegistry.createRegistry(port);
			
			registry.bind("Simulation", stub);
			System.err.println("Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: "+e.toString());
			e.printStackTrace();
		}
	}
}
