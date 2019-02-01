package sim;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;

import jNeatCommon.EnvConstant;
import jNeatCommon.EnvRoutine;
import jNeatCommon.IOseq;
import jneat.Genome;
import jneat.NNode;
import jneat.Network;

public class Thrust extends SimulationFrame {
	// Time scale to fast forward the simulation
	private final static double timeScale = 1.0;
	
	// The walls
	private SimulationBody l,r,t,b;
	
	/** The controlled robot */
	private Robot robot1;
	private Robot robot2;
	private Robot robot3;
	private short ticksTouching1;
	private short ticksTouching2;
	private short ticksTouching3;
	final short ticksTouchingMaximum = 100;
	final int tickCountMaximum = 1000;
	private Box box;
	private double goalDist;
	private long tickCount = 0;
	private double bestFitness = 0;
	
	private Network net;
	private Network net1,net2,net3;
	private double comm1,comm2,comm3;
	private boolean communicate;
	
	// Some booleans to indicate that a key is pressed
	
	private AtomicBoolean forwardThrustOn1 = new AtomicBoolean(false);
	private AtomicBoolean reverseThrustOn1 = new AtomicBoolean(false);
	private AtomicBoolean leftThrustOn1 = new AtomicBoolean(false);
	private AtomicBoolean rightThrustOn1 = new AtomicBoolean(false);
	private AtomicBoolean forwardThrustOn2 = new AtomicBoolean(false);
	private AtomicBoolean reverseThrustOn2 = new AtomicBoolean(false);
	private AtomicBoolean leftThrustOn2 = new AtomicBoolean(false);
	private AtomicBoolean rightThrustOn2 = new AtomicBoolean(false);
	private AtomicBoolean stop = new AtomicBoolean(false);
	private AtomicBoolean noKey1 = new AtomicBoolean(false);
	private AtomicBoolean noKey2 = new AtomicBoolean(false);
	
	private boolean disqualified = false;
	
	private class CustomKeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_W:
					forwardThrustOn1.set(true);
					break;
				case KeyEvent.VK_S:
					reverseThrustOn1.set(true);
					break;
				case KeyEvent.VK_A:
					leftThrustOn1.set(true);
					break;
				case KeyEvent.VK_D:
					rightThrustOn1.set(true);
					break;
				case KeyEvent.VK_UP:
					forwardThrustOn2.set(true);
					break;
				case KeyEvent.VK_DOWN:
					reverseThrustOn2.set(true);
					break;
				case KeyEvent.VK_LEFT:
					leftThrustOn2.set(true);
					break;
				case KeyEvent.VK_RIGHT:
					rightThrustOn2.set(true);
					break;
				case KeyEvent.VK_SPACE:
					forwardThrustOn1.set(false);
					reverseThrustOn1.set(false);
					leftThrustOn1.set(false);
					rightThrustOn1.set(false);
					forwardThrustOn2.set(false);
					reverseThrustOn2.set(false);
					leftThrustOn2.set(false);
					rightThrustOn2.set(false);
					stop.set(true);
			}
			noKey1.set(!(forwardThrustOn1.get() || reverseThrustOn1.get() || leftThrustOn1.get() || rightThrustOn1.get()));
			noKey2.set(!(forwardThrustOn2.get() || reverseThrustOn2.get() || leftThrustOn2.get() || rightThrustOn2.get()));
		}
		
		public void keyReleased(KeyEvent e) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_W:
					forwardThrustOn1.set(false);
					break;
				case KeyEvent.VK_S:
					reverseThrustOn1.set(false);
					break;
				case KeyEvent.VK_A:
					leftThrustOn1.set(false);
					break;
				case KeyEvent.VK_D:
					rightThrustOn1.set(false);
					break;
				case KeyEvent.VK_UP:
					forwardThrustOn2.set(false);
					break;
				case KeyEvent.VK_DOWN:
					reverseThrustOn2.set(false);
					break;
				case KeyEvent.VK_LEFT:
					leftThrustOn2.set(false);
					break;
				case KeyEvent.VK_RIGHT:
					rightThrustOn2.set(false);
					break;
				case KeyEvent.VK_SPACE:
					stop.set(false);
			}
			noKey1.set(!(forwardThrustOn1.get() || reverseThrustOn1.get() || leftThrustOn1.get() || rightThrustOn1.get()));
			noKey2.set(!(forwardThrustOn2.get() || reverseThrustOn2.get() || leftThrustOn2.get() || rightThrustOn2.get()));
		}
	}
	
	public Thrust(boolean headless,Network net) {
		super("Thrust",64.0,timeScale,headless);
		
		KeyListener listener = new CustomKeyListener();
		this.addKeyListener(listener);
		this.canvas.addKeyListener(listener);
		
		this.net = net;
		
		//System.out.println("Starting sim... NetID="+net.getNet_id());
	}
	public Thrust(boolean headless,Network net1,Network net2,Network net3,boolean communicate) {
		super("Thrust",64.0,timeScale,headless);
		
		this.net1 = net1;
		this.net2 = net2;
		this.net3 = net3;
		
		this.communicate = communicate;
		
		if(communicate) {
			comm1=0.0;
			comm2=0.0;
			comm3=0.0;
		}
	}
	
	// Start world, create objects, and add objects to world
	protected void initializeWorld() {
		this.world.setGravity(new Vector2(0, 0));
		
		// Walls of the simulation
		l = new SimulationBody();
		l.addFixture(Geometry.createRectangle(1, 15));
		l.translate(-5, 0);
		l.setMass(MassType.INFINITE);
		this.world.addBody(l);
		
		r = new SimulationBody();
		r.addFixture(Geometry.createRectangle(1, 15));
		r.translate(5, 0);
		r.setMass(MassType.INFINITE);
		this.world.addBody(r);
		
		t = new SimulationBody();
		t.addFixture(Geometry.createRectangle(15, 1));
		t.translate(0, 5);
		t.setMass(MassType.INFINITE);
		this.world.addBody(t);
		
		b = new SimulationBody();
		b.addFixture(Geometry.createRectangle(15, 1));
		b.translate(0, -5);
		b.setMass(MassType.INFINITE);
		this.world.addBody(b);
		
		// First robot
		robot1 = new Robot();
		robot1.translate(-2.0, 2.0);
		this.world.addBody(robot1);
		
		// Second robot
		robot2 = new Robot();
		robot2.translate(0.0, 2.0);
		this.world.addBody(robot2);
		
		// Third robot
		robot3 = new Robot();
		robot3.translate(1.0,-1.0);
		this.world.addBody(robot3);
		
		// The box
		box = new Box();
		box.translate(-1.0, 0.0);
		this.world.addBody(box);
	}
	
	// Gets the most confident output from a vector of neural network outputs
	public int getMostConfident(List<Double> outputs) {
		Double highest = outputs.get(0);
		int highestIndex = 0;
		for(int i=1;i<outputs.size();i++)
			if(outputs.get(i) > highest) {
				highest = outputs.get(i);
				highestIndex = i;
			}
		return highestIndex;
	}
	
	// Things to do every simulation tick
	@Override
	protected void update(Graphics2D g, double elapsedTime) {
		super.update(g, elapsedTime);
		
		tickCount += timeScale;
		//System.out.println("tickCount: "+tickCount);
		
		final double scale = this.scale;
		final double force = 2.0;
		
        final Vector2 r = new Vector2(robot1.getTransform().getRotation() + Math.PI * 0.5);
        final Vector2 c = robot1.getWorldCenter();
        
        // Rough goal calculations
        final Vector2 boxCenter = box.getWorldCenter();
        double distTobox = boxCenter.distance(c) - 1.82;
        Rectangle2D goal = new Rectangle2D.Double(-5.0, -220.0, 10.0, 10.0);
        g.draw(goal);
        
        goalDist = box.getWorldCenter().distance(0.0, -3.5);
        if(box.contains(new Vector2(0.0,-3.5))) {
        	//System.out.println("In goal!!!");
        	goalDist = 0.0;
        }
        else
            ;//System.out.println("Goal Dist: "+goalDist+"      \trobotboxDist: "+distTobox);
        
        // Spacebar emergency stop key
        if (this.stop.get()) {
        	robot1.stop();
        	robot2.stop();
        }
        if(net1!= null && net2!=null && net3!=null && communicate) {
        	double[] inputs1 = {
        			robot1.getLeftEncoder(),
        			robot1.getRightEncoder(),
        			comm2,
        			comm3};
        	double[] inputs2 = {
        			robot1.getLeftEncoder(),
        			robot1.getRightEncoder(),
        			comm1,
        			comm3};
        	double[] inputs3 = {
        			robot1.getLeftEncoder(),
        			robot1.getRightEncoder(),
        			comm1,
        			comm2};
        	
        	net1.load_sensors(inputs1);
        	net2.load_sensors(inputs2);
        	net3.load_sensors(inputs3);
        	
        	net1.activate();
        	for(int relax=0;relax<=net1.max_depth();relax++)
				net1.activate();
        	net2.activate();
        	for(int relax=0;relax<=net2.max_depth();relax++)
				net2.activate();
        	net3.activate();
        	for(int relax=0;relax<=net3.max_depth();relax++)
				net3.activate();
        	
        	Vector<NNode> outputNNodes1 = net1.getOutputs();
        	Vector<NNode> outputNNodes2 = net2.getOutputs();
        	Vector<NNode> outputNNodes3 = net3.getOutputs();
        	
			Vector<Double> outputs1 = new Vector<Double>();
			Vector<Double> outputs2 = new Vector<Double>();
			Vector<Double> outputs3 = new Vector<Double>();
			
			for(NNode node : outputNNodes1)
				outputs1.add(node.getActivation());
			for(NNode node : outputNNodes2)
				outputs2.add(node.getActivation());
			for(NNode node : outputNNodes3)
				outputs3.add(node.getActivation());
			
			net1.flush();
			net2.flush();
			net3.flush();
			
			robot1.doActionByIndex(getMostConfident(outputs1.subList(0,5)));
			comm1 = outputs1.get(5);
			robot2.doActionByIndex(getMostConfident(outputs2.subList(0,5)));
			comm2 = outputs2.get(5);
			robot3.doActionByIndex(getMostConfident(outputs3.subList(0,5)));
			comm3 = outputs3.get(5);
        }
        else if(net1!= null && net2!=null && net3!=null) {
        	double[] inputs1 = {
        			robot1.getLeftEncoder(),
        			robot1.getRightEncoder()};
        	double[] inputs2 = {
        			robot1.getLeftEncoder(),
        			robot1.getRightEncoder()};
        	double[] inputs3 = {
        			robot1.getLeftEncoder(),
        			robot1.getRightEncoder()};
        	
        	net1.load_sensors(inputs1);
        	net2.load_sensors(inputs2);
        	net3.load_sensors(inputs3);
        	
        	net1.activate();
        	for(int relax=0;relax<=net1.max_depth();relax++)
				net1.activate();
        	net2.activate();
        	for(int relax=0;relax<=net2.max_depth();relax++)
				net2.activate();
        	net3.activate();
        	for(int relax=0;relax<=net3.max_depth();relax++)
				net3.activate();
        	
        	Vector<NNode> outputNNodes1 = net1.getOutputs();
        	Vector<NNode> outputNNodes2 = net2.getOutputs();
        	Vector<NNode> outputNNodes3 = net3.getOutputs();
        	
			Vector<Double> outputs1 = new Vector<Double>();
			Vector<Double> outputs2 = new Vector<Double>();
			Vector<Double> outputs3 = new Vector<Double>();
			
			for(NNode node : outputNNodes1)
				outputs1.add(node.getActivation());
			for(NNode node : outputNNodes2)
				outputs2.add(node.getActivation());
			for(NNode node : outputNNodes3)
				outputs3.add(node.getActivation());
			
			net1.flush();
			net2.flush();
			net3.flush();
			
			robot1.doActionByIndex(getMostConfident(outputs1));
			robot2.doActionByIndex(getMostConfident(outputs2));
			robot3.doActionByIndex(getMostConfident(outputs3));
        }
        else if(net != null) {
			double[] inputs = {
			robot1.getLeftEncoder(),
			robot1.getRightEncoder(),
			robot2.getLeftEncoder(),
			robot2.getRightEncoder(),
			robot3.getLeftEncoder(),
			robot3.getRightEncoder()};
			net.load_sensors(inputs);
			
			net.activate();
			for(int relax=0;relax<=net.max_depth();relax++)
				net.activate();
			
			Vector<NNode> outputNNodes = net.getOutputs();
			Vector<Double> outputs = new Vector<Double>();
			for(NNode node : outputNNodes)
				outputs.add(node.getActivation());
			
			net.flush();
			
			//System.out.println(inputs);
			//System.out.println(outputs);
			// For now I'm choosing the highest confidence option for each of the 3 robots
			robot1.doActionByIndex(getMostConfident(outputs.subList(0,5)));
			robot2.doActionByIndex(getMostConfident(outputs.subList(5,10)));
			robot3.doActionByIndex(getMostConfident(outputs.subList(10,15)));
		}
        else {
        	// Apply linear thrust
            // Drive forward
            if (this.forwardThrustOn1.get())
            	robot1.driveForward();
            // Drive backward
            else if (this.reverseThrustOn1.get())
            	robot1.driveBackward();
            // Slow down by default
            else {
            	robot1.linearStopMoving();
            }
            
            // Apply angular thrust
            // Turn left
            if (this.leftThrustOn1.get()) {
            	robot1.driveLeft();
            }
            // Turn right
            else if (this.rightThrustOn1.get()) {
            	robot1.driveRight();
            }
            // Slow down by default
            else {
            	robot1.angularStopMoving();
            }
            // Maximum speed limiting and Minimum speed limiting
            robot1.limitSpeed(noKey1.get());
            
            // Apply linear thrust
            // Drive forward
            if (this.forwardThrustOn1.get())
            	robot1.driveForward();
            // Drive backward
            else if (this.reverseThrustOn1.get())
            	robot1.driveBackward();
            // Slow down by default
            else {
            	robot1.linearStopMoving();
            }
            
            // Apply angular thrust
            // Turn left
            if (this.leftThrustOn1.get()) {
            	robot1.driveLeft();
            }
            // Turn right
            else if (this.rightThrustOn1.get())
            	robot1.driveRight();
            else
            	robot1.angularStopMoving();
            robot1.limitSpeed(noKey1.get());
            
            robot2.driveBackward(); //***Hotwired into reverse for testing.
            
            //robot2.linearStopMoving();
            //robot2.angularStopMoving();
            //robot2.limitSpeed(true);
            
            robot3.linearStopMoving();
            robot3.angularStopMoving();
            robot3.limitSpeed(true);
        }
        box.linearStopMoving();
        box.angularStopMoving();
        box.limitSpeed(true);
        
        // Update robot encoders
        robot1.updateEncoders();
        robot2.updateEncoders();
        robot3.updateEncoders();
        
        //System.out.println("Robot 1 Encoders: "+robot1.getLeftEncoder()+"\t"+robot1.getRightEncoder());
        
        // Check whether robots are touching the box
        // Number of ticks touching the box, maximum of ticksTouchingMaximum per robot
        if(robot1.isInContact(box) && ticksTouching1 < ticksTouchingMaximum)
        	ticksTouching1++;
        if(robot2.isInContact(box) && ticksTouching2 < ticksTouchingMaximum)
        	ticksTouching2++;
        if(robot3.isInContact(box) && ticksTouching3 < ticksTouchingMaximum)
        	ticksTouching3++;
        //System.out.println("ticksTouching: "+ticksTouching1+"\t"+ticksTouching2+"\t"+ticksTouching3);
        
        if(anyWallContact())
        	disqualified = true;
        
        double fitness = calculateFitness();
        if(fitness > bestFitness)
        	bestFitness = fitness;
        //System.out.println("fitness: "+fitness+"\tBest: "+bestFitness);
        
        if(tickCount >= tickCountMaximum)
        	stop();

	}
	
	public boolean anyWallContact() {
		if(wallContact(robot1))
			return true;
		if(wallContact(robot2))
			return true;
		if(wallContact(robot3))
			return true;
		return false;
	}
	public boolean wallContact(Robot robotN) {
		if(robotN.isInContact(l))
			return true;
		if(robotN.isInContact(r))
			return true;
		if(robotN.isInContact(t))
			return true;
		if(robotN.isInContact(b))
			return true;
		return false;
	}
	
	public double calculateFitness() {
		if(disqualified)
			return calculateFitnessNoPenalties()-0.1;
		return calculateFitnessNoPenalties();
	}
	
	public double calculateFitnessNoPenalties() {
		if(goalDist > 3.5)
			return (ticksTouching1+ticksTouching2+ticksTouching3)/300.0/3.0;
		else if(goalDist > 0.0)
			return Math.min(1,1-goalDist/3.5)/3.0 + 1.0/3.0;
		else
			return Math.max(0,1-tickCount*1.0/tickCountMaximum)/3.0 + 2.0/3.0;
	}
	
	public double getBestFitness() {
		return bestFitness;
	}
	
	public static void main(String[] args) {
		Network net = readGenomeFromFile();
		//Thrust simulation = new Thrust(false,net);
		Thrust simulation = new Thrust(false,null);
		simulation.run();
		while(!simulation.isStopped()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Final best fitness: "+simulation.getBestFitness());
		simulation.dispose();
	}
	
	public static double getBestFitnessHeadlessSim(Network net) {
		return getBestFitnessSim(net,true);
	}
	public static double getBestFitnessSim(Network net,boolean head) {
		Thrust simulation = new Thrust(head,net);
		simulation.run();
		while(!simulation.isStopped()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		double best = simulation.getBestFitness();
		simulation.dispose();
		return best;
	}
	
	//
	public static double getBestFitnessSim(Network net1,Network net2,Network net3,boolean head,boolean communicate) {
		Thrust simulation = new Thrust(head,net1,net2,net3,communicate);
		simulation.run();
		while(!simulation.isStopped()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		double best = simulation.getBestFitness();
		simulation.dispose();
		return best;
	}
	
	// Adapted from code in Generation.java
	public static Network readGenomeFromFile() {
		System.out.println(EnvRoutine.getJneatFileData(EnvConstant.NAME_GENOMEA));
		//String path = "C:\\Users\\MaxMi\\Documents\\GitHub\\CoopEvolve\\src\\genomeNew";
		String path = "C:\\Users\\MaxMi\\Documents\\GitHub\\CoopEvolve\\data\\xwinner52_111";
		IOseq xFile = new IOseq(path);
		boolean rc = xFile.IOseqOpenR();
		
		String xline = xFile.IOseqRead();
		StringTokenizer st = new StringTokenizer(xline);
		//skip 
		String curword = st.nextToken();
		//id of genome can be readed (sic)
		curword = st.nextToken();
		int id = Integer.parseInt(curword);
		Genome u_genome = new Genome(id, xFile);
		
		return u_genome.genesis(0);
	}
}
