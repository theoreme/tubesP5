package ptf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import processing.core.PApplet;
import toroide.LineStrip3D;
import toxi.geom.Vec3D;


public class ParallelTransportFrame extends LineStrip3D implements IFrameCurve{

	protected List<Vec3D> tangents = new ArrayList<Vec3D>();
	protected List<Vec3D> binormals = new ArrayList<Vec3D>();
	protected List<Vec3D> normals = new ArrayList<Vec3D>();

	private int curve_length;
	
	//-------------------------------------------------------- ctor

	public ParallelTransportFrame(Collection<? extends Vec3D> vertices) {
		super(vertices);

		this.curve_length = vertices.size();

		for(int i=0; i<=curve_length; i++)  {
			tangents.add(new Vec3D());
			binormals.add(new Vec3D());
			normals.add(new Vec3D());
		}

		getFirstFrame();
		getTangents();
		parallelTransportFrameApproach();
	}

	//-------------------------------------------------------- algorithm
	
	void getFirstFrame() {
		// first frame, needed by parallel transport frame approach
		// frenet method is used. 
		// more specific method (in case of complex target-oriented base animation) could be used
		
		Vec3D p0, p1, p2, b;
		
		// 1° derivate in p0-p1
		p0 = vertices.get(0);
		p1 = vertices.get(1);
		tangents.set(0, getTangentBetweenTwoPoint(p0, p1));
		
		// 1° derivate in p1-p2
		p1 = vertices.get(1);
		p2 = vertices.get(2);
		tangents.set(1, getTangentBetweenTwoPoint(p1, p2));
		
		// 2° derivate in t0 and t1
		b = tangents.get(0).cross(tangents.get(1));
		b.normalize();
		binormals.set(0, b);
		
		normals.set(0,b.cross(tangents.get(0)));
	}

	void getTangents() {
		Vec3D p0, p1;
		for(int i=1; i<curve_length-1; i++) {
			p0 = vertices.get(i);
			p1 = vertices.get(i+1);
			tangents.set(i, getTangentBetweenTwoPoint(p0, p1));
		}
	}

	void parallelTransportFrameApproach() {
		// p.t.f approach from article: Hanson and Ma, 1995
		Vec3D old_normal, p0, p1, b;
		float theta;
		for(int i=1; i<curve_length+1; i++) {
			p0 = tangents.get(i-1);
			p1 = tangents.get(i);

			// this is what is called A in game programming gems
			// and B in Hanson and Ma article
			b = p0.cross(p1);
			b.normalize();
		
			// normals
			theta = PApplet.acos(p0.dot(p1));
			old_normal = normals.get(i-1).copy();
			old_normal.normalize();
			old_normal.rotateAroundAxis(b,theta);
			old_normal.scale(normals.get(i-1));
			
			normals.set(i, old_normal);
			binormals.set(i, tangents.get(i).cross(old_normal));

			// ASSERT
			//    println("dovrebbe essere 90°, cioè PI/2: "+PI/2);
			//    println(Vec3D.angleBetween(binormals[i], tangents[i]));
			//    println(Vec3D.angleBetween(normals[i], tangents[i]));
			//    println(Vec3D.angleBetween(normals[i], binormals[i]));
			
		}
	}

	//-------------------------------------------------------- utils

	static Vec3D getTangentBetweenTwoPoint(Vec3D p1, Vec3D p2) {
		Vec3D r = p1.sub(p2);
		r.normalize();
		return r;
	}

	//-------------------------------------------------------- getter && setter
	
	@Override
	public Vec3D getBinormal(int i) {
		return binormals.get(i);
	}

	@Override
	public Vec3D getNormal(int i) {
		return normals.get(i);
	}

	@Override
	public Vec3D getTangent(int i) {
		return tangents.get(i);
	}
}
