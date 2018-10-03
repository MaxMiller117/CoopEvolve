import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;

public class Robot extends SimulationBody {
	public Robot() {
		super();
		this.addFixture(Geometry.createRectangle(0.5, 1.5), 0.6, 0.2, 0.2);
		BodyFixture bf2 = this.addFixture(Geometry.createEquilateralTriangle(0.5), 1, 0.2, 0.2);
		bf2.getShape().translate(0, 0.9);
		this.setMass(MassType.NORMAL);
	}
	public void stop() {
		this.setLinearVelocity(0.0,0.0);
    	this.setAngularVelocity(0.0);
	}
	public void driveForward() {
		
	}
}
