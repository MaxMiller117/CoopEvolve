package sim;

import java.rmi.Remote;
import java.rmi.RemoteException;

import jneat.Network;

public interface Simulation extends Remote {
	String printMsg() throws RemoteException;
	double getBestFitnessSim(Network net,boolean head) throws RemoteException;
	double getBestFitnessHeadlessSim(Network net) throws RemoteException;
}
