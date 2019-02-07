package sim;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import jneat.Network;

public interface Simulation extends Remote {
	String printMsg() throws RemoteException;
	double getBestFitnessSim(Network net,boolean head) throws RemoteException;
	double getBestFitnessHeadlessSim(Network net) throws RemoteException;
	
	boolean addToQueue(Network x) throws RemoteException;
	boolean addToQueue(Network net1,Network net2,Network net3,boolean comm) throws RemoteException;
	
	boolean hasResults() throws RemoteException;
	Double[] getResult() throws RemoteException;
	ArrayList<Double[]> getAllResults() throws RemoteException;
	boolean isIdle() throws RemoteException;
	String status() throws RemoteException;
	boolean isBacklogged() throws RemoteException;
}
