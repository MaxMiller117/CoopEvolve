import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.atomic.AtomicBoolean;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;

public class Thrust extends SimulationFrame {
	// Time scale to fast forward the simulation
	private final static double timeScale = 1.0;
	
	/** The controlled robot */
	private Robot robot1;
	private Robot robot2;
	private Robot robot3;
	private short ticksTouching1;
	private short ticksTouching2;
	private short ticksTouching3;
	final short ticksTouchingMaximum = 100;
	final int tickCountMaximum = 5000;
	private Box box;
	private double goalDist;
	private long tickCount = 0;
	private double bestFitness = 0;
	
	public final boolean headless;
	
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
	
	private class CustomKeyListener extends KeyAdapter {
		@Override
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
		
		@Override
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
	
	public Thrust(boolean headless) {
		super("Thrust", 64.0, timeScale);
		
		this.headless = headless;
		
		KeyListener listener = new CustomKeyListener();
		this.addKeyListener(listener);
		this.canvas.addKeyListener(listener);
	}
	
	// Start world, create objects, and add objects to world
	protected void initializeWorld() {
		this.world.setGravity(new Vector2(0, 0));
		
		// Walls of the simulation
		SimulationBody l = new SimulationBody();
		l.addFixture(Geometry.createRectangle(1, 15));
		l.translate(-5, 0);
		l.setMass(MassType.INFINITE);
		this.world.addBody(l);
		
		SimulationBody r = new SimulationBody();
		r.addFixture(Geometry.createRectangle(1, 15));
		r.translate(5, 0);
		r.setMass(MassType.INFINITE);
		this.world.addBody(r);
		
		SimulationBody t = new SimulationBody();
		t.addFixture(Geometry.createRectangle(15, 1));
		t.translate(0, 5);
		t.setMass(MassType.INFINITE);
		this.world.addBody(t);
		
		SimulationBody b = new SimulationBody();
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
	
	// Things to do every simulation tick
	@Override
	protected void update(Graphics2D g, double elapsedTime) {
		super.update(g, elapsedTime);
		
		tickCount += timeScale;
		System.out.println("tickCount: "+tickCount);
		
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
        	System.out.println("In goal!!!");
        	goalDist = 0.0;
        }
        else
            System.out.println("Goal Dist: "+goalDist+"      \trobotboxDist: "+distTobox);
        
        // Spacebar emergency stop key
        if (this.stop.get()) {
        	robot1.stop();
        	robot2.stop();
        }
        
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
        
        robot2.linearStopMoving();
        robot2.angularStopMoving();
        robot2.limitSpeed(true);
        
        robot3.linearStopMoving();
        robot3.angularStopMoving();
        robot3.limitSpeed(true);
        
        box.linearStopMoving();
        box.angularStopMoving();
        box.limitSpeed(true);
        
        robot1.updateEncoders();
        robot2.updateEncoders();
        robot3.updateEncoders();
        
        System.out.println("Robot 1 Encoders: "+robot1.getLeftEncoder()+"\t"+robot1.getRightEncoder());
        
        // Check whether robots are touching the box
        // Number of ticks touching the box, maximum of ticksTouchingMaximum per robot
        if(robot1.isInContact(box) && ticksTouching1 < ticksTouchingMaximum)
        	ticksTouching1++;
        if(robot2.isInContact(box) && ticksTouching2 < ticksTouchingMaximum)
        	ticksTouching2++;
        if(robot3.isInContact(box) && ticksTouching3 < ticksTouchingMaximum)
        	ticksTouching3++;
        System.out.println("ticksTouching: "+ticksTouching1+"\t"+ticksTouching2+"\t"+ticksTouching3);
        
        double fitness = calculateFitness();
        if(fitness > bestFitness)
        	bestFitness = fitness;
        System.out.println("fitness: "+fitness+"\tBest: "+bestFitness);
	}
	
	public double calculateFitness() {
		if(goalDist > 3.5)
			return (ticksTouching1+ticksTouching2+ticksTouching3)/300.0/3.0;
		else if(goalDist > 0.0)
			return Math.min(1,1-goalDist/3.5)/3.0 + 1.0/3.0;
		else
			return Math.max(0,1-tickCount*1.0/tickCountMaximum)/3.0 + 2.0/3.0;
	}
	
	public static void main(String[] args) {
		Thrust simulation = new Thrust(false);
		simulation.run();
	}
}
