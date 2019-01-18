package sim;

import java.rmi.RemoteException;

import jneat.Network;

public class SimHandler implements Simulation {
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
}
