package sim;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;

// Simulates the Dexter GoPiGo3 robots
// These robots continue to run the last command given to them
public class Robot extends SimulationBody {
	final double force = 0.6;
	private double motorEncoderLeft;
	private double motorEncoderRight;
	private Vector2 leftWheelLastPosition;
	private Vector2 rightWheelLastPosition;
	final Vector2 leftWheelLocalPoint = new Vector2(-0.23125,0.021875); 
	final Vector2 rightWheelLocalPoint = new Vector2(0.23125,0.021875);
	final double wheelRadius = 0.13125;
	final double wheelCircumference = 2*Math.PI*wheelRadius;
	final double wheelConversionRate = 360.0/wheelCircumference;
	
	// Command options
	public Robot() {
		super();
		this.addFixture(Geometry.createRectangle(0.775, 0.96875), 0.6, 0.2, 0.2);
		//BodyFixture bf2 = this.addFixture(Geometry.createEquilateralTriangle(0.5), 1, 0.2, 0.2);
		//bf2.getShape().translate(0, 0.9);
		this.setMass(MassType.NORMAL);
		motorEncoderLeft = 0.0;
		motorEncoderRight = 0.0;
	}
	public void stop() {
		this.setLinearVelocity(0.0,0.0);
    	this.setAngularVelocity(0.0);
	}
	
	// Converts integers from NNs into commands
	public void doActionByIndex(int command) {
		switch(command) {
		case 0: driveForward(); angularStopMoving(); limitSpeed(false); break;
		case 1: driveBackward(); angularStopMoving(); limitSpeed(false); break;
		case 2: driveLeft(); linearStopMoving(); limitSpeed(false); break;
		case 3: driveRight(); linearStopMoving(); limitSpeed(false); break;
		case 4: doNothing(); linearStopMoving(); angularStopMoving(); limitSpeed(true); break;
		default: System.out.println("Unrecognized command: "+command); break;
		}
	}
	
	public void driveForward() {
		final Vector2 r = new Vector2(this.getTransform().getRotation() + Math.PI * 0.5);
        final Vector2 c = this.getWorldCenter();
        
        Vector2 f = r.product(force);
    	Vector2 p = c.sum(r.product(-0.9));
    	
    	this.applyForce(f);
	}
	public void driveBackward() {
		final Vector2 r = new Vector2(this.getTransform().getRotation() + Math.PI * 0.5);
        final Vector2 c = this.getWorldCenter();
        
        Vector2 f = r.product(-force);
    	Vector2 p = c.sum(r.product(0.9));
    	
    	this.applyForce(f);
	}
	public void driveLeft() {
		final Vector2 r = new Vector2(this.getTransform().getRotation() + Math.PI * 0.5);
        final Vector2 c = this.getWorldCenter();
        
        Vector2 f1 = r.product(force * 0.2).right();
    	Vector2 f2 = r.product(force * 0.2).left();
    	Vector2 p1 = c.sum(r.product(0.9));
    	Vector2 p2 = c.sum(r.product(-0.9));
    	
    	// apply a force to the top going left
    	this.applyForce(f1, p1);
    	// apply a force to the bottom going right
    	this.applyForce(f2, p2);
	}
	public void driveRight() {
		final Vector2 r = new Vector2(this.getTransform().getRotation() + Math.PI * 0.5);
        final Vector2 c = this.getWorldCenter();
        
        Vector2 f1 = r.product(force * 0.2).left();
    	Vector2 f2 = r.product(force * 0.2).right();
    	Vector2 p1 = c.sum(r.product(0.9));
    	Vector2 p2 = c.sum(r.product(-0.9));
    	
    	// apply a force to the top going left
    	this.applyForce(f1, p1);
    	// apply a force to the bottom going right
    	this.applyForce(f2, p2);
	}
	public void doNothing() {
		
	}
	public void linearStopMoving() {
		final Vector2 r = new Vector2(this.getTransform().getRotation() + Math.PI * 0.5);
        final Vector2 c = this.getWorldCenter();
		
		if (Math.abs(this.getLinearVelocity().getMagnitude()) > 0) {
        	Vector2 vel = this.getLinearVelocity();
        	double speed = vel.getMagnitudeSquared();
        	double slowForce = Math.min(speed/1.0, force);
        	Vector2 f = vel.getNormalized().product(slowForce*-2.0);
        	Vector2 p = c.sum(r.product(-0.9));
    		this.applyForce(f);
		}
		
	}
	public void angularStopMoving() {
		final Vector2 r = new Vector2(this.getTransform().getRotation() + Math.PI * 0.5);
        final Vector2 c = this.getWorldCenter();
		
		if (Math.abs(this.getAngularVelocity()) > 0.0) {
        	short positive = -1;
        	if(this.getAngularVelocity() > 0.0)
        		positive = 1;
        	
        	Vector2 f1 = r.product(force * 0.1 * positive).left();
        	Vector2 f2 = r.product(force * 0.1 * positive).right();
        	Vector2 p1 = c.sum(r.product(0.9));
        	Vector2 p2 = c.sum(r.product(-0.9));
        	
        	// apply a force to the top going left
        	this.applyForce(f1, p1);
        	// apply a force to the bottom going right
        	this.applyForce(f2, p2);
		}
	}
	public void limitSpeed(boolean noKey) {
		Vector2 vel = this.getLinearVelocity();
        double maxLinV = 1.5;
        if(vel.getMagnitude() > maxLinV) {
        	vel.setMagnitude(maxLinV);
        	this.setLinearVelocity(vel);
        }
        double maxAngV = 1.0;
        double avel = this.getAngularVelocity();
        if(Math.abs(avel) > maxAngV) {
        	if(avel > maxAngV)
        		avel = maxAngV;
        	else
        		avel = -1.0 * maxAngV;
        	this.setAngularVelocity(avel);
        }
        
        // If the speed gets too low, stop it to prevent the simulated friction from thrashing
        if(noKey) {
	        double minLinV = 0.5;
	        double minAngV = 0.2;
	        if(vel.getMagnitude() < minLinV) {
	        	vel.setMagnitude(0.0);
	        	this.setLinearVelocity(vel);
	        }
	        if(Math.abs(avel) < minAngV) {
	        	avel = 0;
	        	this.setAngularVelocity(avel);
	        }
        }
	}
	
	public void updateEncoders() {
		Vector2 leftWheelNewPosition = this.getWorldPoint(leftWheelLocalPoint);
		Vector2 rightWheelNewPosition = this.getWorldPoint(rightWheelLocalPoint);
		
		if(leftWheelLastPosition != null) {
			double distance = leftWheelNewPosition.distance(leftWheelLastPosition);
			motorEncoderLeft += distance * wheelConversionRate;
		}
		if(rightWheelLastPosition != null) {
			double distance = rightWheelNewPosition.distance(rightWheelLastPosition);
			motorEncoderRight += distance * wheelConversionRate;
		}
		leftWheelLastPosition = leftWheelNewPosition;
		rightWheelLastPosition = rightWheelNewPosition;
	}
	public double getLeftEncoder() {
		return motorEncoderLeft;
	}
	public double getRightEncoder() {
		return motorEncoderRight;
	}
}
