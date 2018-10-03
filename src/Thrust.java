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
	/** The controlled ship */
	private SimulationBody ship;
	private SimulationBody ship2;
	private SimulationBody ball;
	
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
	
	public Thrust() {
		super("Thrust", 64.0);
		
		KeyListener listener = new CustomKeyListener();
		this.addKeyListener(listener);
		this.canvas.addKeyListener(listener);
	}
	
	// Start world, create objects, add objects to world
	protected void initializeWorld() {
		this.world.setGravity(new Vector2(0, 0));
		
		// create all your bodies/joints
		
		// the bounds so we can keep playing
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
		
		// the ship
		ship = new SimulationBody();
		ship.addFixture(Geometry.createRectangle(0.5, 1.5), 1, 0.2, 0.2);
		BodyFixture bf2 = ship.addFixture(Geometry.createEquilateralTriangle(0.5), 1, 0.2, 0.2);
		bf2.getShape().translate(0, 0.9);
		ship.translate(-2.0, 2.0);
		ship.setMass(MassType.NORMAL);
		this.world.addBody(ship);
		
		// second ship
		ship2 = new SimulationBody();
		ship2.addFixture(Geometry.createRectangle(0.5, 1.5), 1, 0.2, 0.2);
		BodyFixture bf22 = ship2.addFixture(Geometry.createEquilateralTriangle(0.5), 1, 0.2, 0.2);
		bf22.getShape().translate(0, 0.9);
		ship2.translate(2.0, 2.0);
		ship2.setMass(MassType.NORMAL);
		this.world.addBody(ship2);
		
		// the ball
		ball = new SimulationBody();
		ball.addFixture(Geometry.createRectangle(1.6, 1.2), // radius
				17.97925, 								  // density
				0.08,									// friction
				0.9);									// restitution (bounciness)
		ball.translate(-1.0, 0.0);
		//ball1.setLinearVelocity(5.36448, 0.0); 		  // 12 mph = 5.36448 m/s
		ball.setMass(MassType.NORMAL);
		this.world.addBody(ball);
	}
	
	// Things to do every simulation tick
	@Override
	protected void update(Graphics2D g, double elapsedTime) {
		super.update(g, elapsedTime);
		
		final double scale = this.scale;
		final double force = 2.0; //* elapsedTime;
		
        final Vector2 r = new Vector2(ship.getTransform().getRotation() + Math.PI * 0.5);
        final Vector2 c = ship.getWorldCenter();
        
        // Rough goal calculations
        final Vector2 ballCenter = ball.getWorldCenter();
        double distToBall = ballCenter.distance(c) - 1.82;
        g.draw(new Rectangle2D.Double(-150.0, -220.0, 300.0, 100.0));
        if(ballCenter.x < 0.5 && ballCenter.x > -0.5 && ballCenter.y < -1.5 && ballCenter.y > -2.5)
        	System.out.println("In goal!!!");
        else
            System.out.println("Goal Dist: "+ball.getWorldCenter().distance(0.0, -2.0)+"      \tShipBallDist: "+distToBall);
        
        // Spacebar emergency stop key
        if (this.stop.get()) {
        	ship.setLinearVelocity(0.0,0.0);
        	ship.setAngularVelocity(0.0);
        	ship2.setLinearVelocity(0.0,0.0);
        	ship2.setAngularVelocity(0.0);
        }
        
		// Apply linear thrust
        // Drive forward
        if (this.forwardThrustOn1.get()) {
        	Vector2 f = r.product(force);
        	Vector2 p = c.sum(r.product(-0.9));
        	
        	ship.applyForce(f);
        	
        	g.setColor(Color.ORANGE);
        	g.draw(new Line2D.Double(p.x * scale, p.y * scale, (p.x - f.x) * scale, (p.y - f.y) * scale));
        } 
        // Drive backward
        else if (this.reverseThrustOn1.get()) {
        	Vector2 f = r.product(-force);
        	Vector2 p = c.sum(r.product(0.9));
        	
        	ship.applyForce(f);
        	
        	g.setColor(Color.ORANGE);
        	g.draw(new Line2D.Double(p.x * scale, p.y * scale, (p.x - f.x) * scale, (p.y - f.y) * scale));
        }
        // Slow down by default
        else if (Math.abs(ship.getLinearVelocity().getMagnitude()) > 0) {
        	Vector2 vel = ship.getLinearVelocity();
        	Vector2 f = vel.getNormalized().product(force*-1.0);
        	Vector2 p = c.sum(r.product(-0.9));
    		ship.applyForce(f);
    		
    		g.setColor(Color.ORANGE);
        	g.draw(new Line2D.Double(p.x * scale, p.y * scale, (p.x - f.x) * scale, (p.y - f.y) * scale));
        }
        
        // Apply angular thrust
        // Turn left
        if (this.leftThrustOn1.get()) {
        	Vector2 f1 = r.product(force * 0.1).right();
        	Vector2 f2 = r.product(force * 0.1).left();
        	Vector2 p1 = c.sum(r.product(0.9));
        	Vector2 p2 = c.sum(r.product(-0.9));
        	
        	// apply a force to the top going left
        	ship.applyForce(f1, p1);
        	// apply a force to the bottom going right
        	ship.applyForce(f2, p2);
        	
        	g.setColor(Color.RED);
        	g.draw(new Line2D.Double(p1.x * scale, p1.y * scale, (p1.x - f1.x) * scale, (p1.y - f1.y) * scale));
        	g.draw(new Line2D.Double(p2.x * scale, p2.y * scale, (p2.x - f2.x) * scale, (p2.y - f2.y) * scale));
        }
        // Turn right
        else if (this.rightThrustOn1.get()) {
        	Vector2 f1 = r.product(force * 0.1).left();
        	Vector2 f2 = r.product(force * 0.1).right();
        	Vector2 p1 = c.sum(r.product(0.9));
        	Vector2 p2 = c.sum(r.product(-0.9));
        	
        	// apply a force to the top going left
        	ship.applyForce(f1, p1);
        	// apply a force to the bottom going right
        	ship.applyForce(f2, p2);
        	
        	g.setColor(Color.RED);
        	g.draw(new Line2D.Double(p1.x * scale, p1.y * scale, (p1.x - f1.x) * scale, (p1.y - f1.y) * scale));
        	g.draw(new Line2D.Double(p2.x * scale, p2.y * scale, (p2.x - f2.x) * scale, (p2.y - f2.y) * scale));
        }
        // Slow down by default
        else if (Math.abs(ship.getAngularVelocity()) > 0.0) {
        	short positive = -1;
        	if(ship.getAngularVelocity() > 0.0)
        		positive = 1;
        	
        	Vector2 f1 = r.product(force * 0.1 * positive).left();
        	Vector2 f2 = r.product(force * 0.1 * positive).right();
        	Vector2 p1 = c.sum(r.product(0.9));
        	Vector2 p2 = c.sum(r.product(-0.9));
        	
        	// apply a force to the top going left
        	ship.applyForce(f1, p1);
        	// apply a force to the bottom going right
        	ship.applyForce(f2, p2);
        	
        	g.setColor(Color.RED);
        	g.draw(new Line2D.Double(p1.x * scale, p1.y * scale, (p1.x - f1.x) * scale, (p1.y - f1.y) * scale));
        	g.draw(new Line2D.Double(p2.x * scale, p2.y * scale, (p2.x - f2.x) * scale, (p2.y - f2.y) * scale));
        }
        
        // Maximum speed limiting
        Vector2 vel = ship.getLinearVelocity();
        double maxLinV = 1.5;
        if(vel.getMagnitude() > maxLinV) {
        	vel.setMagnitude(maxLinV);
        	ship.setLinearVelocity(vel);
        }
        double maxAngV = 1.0;
        double avel = ship.getAngularVelocity();
        if(Math.abs(avel) > maxAngV) {
        	if(avel > maxAngV)
        		avel = maxAngV;
        	else
        		avel = -1.0 * maxAngV;
        	ship.setAngularVelocity(avel);
        }
        
        // Minimum speed limiting
        // If the speed gets too low, stop it to prevent the simulated friction from thrashing
        if(noKey1.get()) {
	        double minLinV = 0.5;
	        double minAngV = 0.2;
	        if(vel.getMagnitude() < minLinV) {
	        	vel.setMagnitude(0.0);
	        	ship.setLinearVelocity(vel);
	        }
	        if(Math.abs(avel) < minAngV) {
	        	avel = 0;
	        	ship.setAngularVelocity(avel);
	        }
        }
	}
	
	public static void main(String[] args) {
		Thrust simulation = new Thrust();
		simulation.run();
	}
}
