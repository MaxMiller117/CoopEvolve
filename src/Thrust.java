/*
 * Copyright (c) 2010-2016 William Bittle  http://www.dyn4j.org/
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions 
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 *     and the following disclaimer in the documentation and/or other materials provided with the 
 *     distribution.
 *   * Neither the name of dyn4j nor the names of its contributors may be used to endorse or 
 *     promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;
import java.util.concurrent.atomic.AtomicBoolean;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;

/**
 * Moderately complex scene of a rocket that has propulsion at various points
 * to allow control.  Control is given by the left, right, up, and down keys
 * and applies forces when pressed.
 * @author William Bittle
 * @version 3.2.1
 * @since 3.2.0
 */
public class Thrust extends SimulationFrame {
	/** The serial version id */
	private static final long serialVersionUID = 3770932661470247325L;

	/** The controlled ship */
	private SimulationBody ship;
	
	private SimulationBody ball;
	
	// Some booleans to indicate that a key is pressed
	
	private AtomicBoolean forwardThrustOn = new AtomicBoolean(false);
	private AtomicBoolean reverseThrustOn = new AtomicBoolean(false);
	private AtomicBoolean leftThrustOn = new AtomicBoolean(false);
	private AtomicBoolean rightThrustOn = new AtomicBoolean(false);
	private AtomicBoolean stop = new AtomicBoolean(false);
	private AtomicBoolean noKey = new AtomicBoolean(false);
	
	/**
	 * Custom key adapter to listen for key events.
	 * @author William Bittle
	 * @version 3.2.1
	 * @since 3.2.0
	 */
	private class CustomKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_W:
					forwardThrustOn.set(true);
					break;
				case KeyEvent.VK_S:
					reverseThrustOn.set(true);
					break;
				case KeyEvent.VK_A:
					leftThrustOn.set(true);
					break;
				case KeyEvent.VK_D:
					rightThrustOn.set(true);
					break;
				case KeyEvent.VK_SPACE:
					forwardThrustOn.set(false);
					reverseThrustOn.set(false);
					leftThrustOn.set(false);
					rightThrustOn.set(false);
					stop.set(true);
			}
			noKey.set(!(forwardThrustOn.get() || reverseThrustOn.get() || leftThrustOn.get() || rightThrustOn.get()));
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_W:
					forwardThrustOn.set(false);
					break;
				case KeyEvent.VK_S:
					reverseThrustOn.set(false);
					break;
				case KeyEvent.VK_A:
					leftThrustOn.set(false);
					break;
				case KeyEvent.VK_D:
					rightThrustOn.set(false);
					break;
				case KeyEvent.VK_SPACE:
					stop.set(false);
			}
			noKey.set(!(forwardThrustOn.get() || reverseThrustOn.get() || leftThrustOn.get() || rightThrustOn.get()));
		}
	}
	
	/**
	 * Default constructor.
	 */
	public Thrust() {
		super("Thrust", 64.0);
		
		KeyListener listener = new CustomKeyListener();
		this.addKeyListener(listener);
		this.canvas.addKeyListener(listener);
	}
	
	/**
	 * Creates game objects and adds them to the world.
	 */
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
		ship.translate(0.0, 2.0);
		ship.setMass(MassType.NORMAL);
		this.world.addBody(ship);
		
		// the ball
		ball = new SimulationBody();
		ball.addFixture(Geometry.createCircle(0.928575), //  2.25 in diameter = 0.028575 m radius
				17.97925, 								  //  0.126 oz/in^3 = 217.97925 kg/m^3
				0.08,
				0.9);
		ball.translate(-1.0, 0.0);
		//ball1.setLinearVelocity(5.36448, 0.0); 		  // 12 mph = 5.36448 m/s
		ball.setMass(MassType.NORMAL);
		this.world.addBody(ball);
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.samples.SimulationFrame#update(java.awt.Graphics2D, double)
	 */
	@Override
	protected void update(Graphics2D g, double elapsedTime) {
		super.update(g, elapsedTime);
		
		final double scale = this.scale;
		final double force = 2.0; //* elapsedTime;
		
        final Vector2 r = new Vector2(ship.getTransform().getRotation() + Math.PI * 0.5);
        final Vector2 c = ship.getWorldCenter();
		
        // Spacebar emergency stop key
        if (this.stop.get()) {
        	ship.setLinearVelocity(0.0,0.0);
        	ship.setAngularVelocity(0.0);
        }
        
		// Apply linear thrust
        // Drive forward
        if (this.forwardThrustOn.get()) {
        	Vector2 f = r.product(force);
        	Vector2 p = c.sum(r.product(-0.9));
        	
        	ship.applyForce(f);
        	
        	g.setColor(Color.ORANGE);
        	g.draw(new Line2D.Double(p.x * scale, p.y * scale, (p.x - f.x) * scale, (p.y - f.y) * scale));
        } 
        // Drive backward
        else if (this.reverseThrustOn.get()) {
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
        if (this.leftThrustOn.get()) {
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
        else if (this.rightThrustOn.get()) {
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
        if(noKey.get()) {
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
	
	/**
	 * Entry point for the example application.
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		Thrust simulation = new Thrust();
		simulation.run();
	}
}
