import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;

public class Box extends SimulationBody {
	public Box() {
		super();
		this.addFixture(Geometry.createRectangle(1.6, 1.2), // size
				17.97925, 								  // density
				0.08,									// friction
				0.9);									// restitution (bounciness)
		this.setMass(MassType.NORMAL);
	}
}
