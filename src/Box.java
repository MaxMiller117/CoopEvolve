import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;

public class Box extends SimulationBody {
	final double force = 2.2;
	public Box() {
		super();
		this.addFixture(Geometry.createRectangle(1.6, 1.2), // size
				0.36925, 								  // density
				0.08,									// friction
				0.49);									// restitution (bounciness)
		this.setMass(MassType.NORMAL);
	}
	public void linearStopMoving() {
		final Vector2 r = new Vector2(this.getTransform().getRotation() + Math.PI * 0.5);
        final Vector2 c = this.getWorldCenter();
		
		if (Math.abs(this.getLinearVelocity().getMagnitude()) > 0) {
        	Vector2 vel = this.getLinearVelocity();
        	double speed = vel.getMagnitudeSquared();
        	double slowForce = Math.min(speed/1.0, force);
        	Vector2 f = vel.getNormalized().product(slowForce*-5.0);
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
        double maxLinV = 10.5;
        if(vel.getMagnitude() > maxLinV) {
        	vel.setMagnitude(maxLinV);
        	this.setLinearVelocity(vel);
        }
        double maxAngV = 10.0;
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
	        double minLinV = 0.1;
	        double minAngV = 0.1;
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
}
