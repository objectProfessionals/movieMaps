package com.op.moviemaps.script;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Vector;

/**
 * PathLength is a utility class for calculating the length of a path, the
 * location of a point at a particular length along the path, and the angle of
 * the tangent to the path at a given length.
 * <p>
 * It uses a FlatteningPathIterator to create a flattened version of the Path.
 * This means the values returned are not always exact (in fact, they rarely
 * are), but in most cases they are reasonably accurate.
 * 
 * @author <a href="mailto:dean.jackson@cmis.csiro.au">Dean Jackson</a>
 * @version $Id: PathLength.java,v 1.5 2003/08/08 11:39:05 vhardy Exp $
 */

public class PathLength {

	/**
	 * Construct a PathLength utility class to operate on the particular Shape.
	 * 
	 * @param path
	 *            The Path (or Shape) to use.
	 */

	public PathLength(Shape path) {
		setPath(path);
	}

	private Shape path = null;

	/**
	 * Get the path to use in calculations.
	 * 
	 * @return Path used in calculations.
	 */

	public Shape getPath() {
		return path;
	}

	/**
	 * Set the path to use in calculations.
	 * 
	 * @param v
	 *            Path to be used in calculations.
	 */

	public void setPath(Shape v) {
		this.path = v;
		initialised = false;
	}

	/**
	 * The list of flattened path segments.
	 */

	private Vector segments = null;

	/**
	 * Cached copy of the path length.
	 */
	private float pathLength = 0f;

	/**
	 * Has this path been flattened?
	 */
	private boolean initialised = false;

	/**
	 * Returns the length of the path used by this PathLength object.
	 * 
	 * @return The length of the path.
	 */

	public float lengthOfPath() {

		if (!initialised) {
			initialise();
		}

		return pathLength;
	}

	protected void initialise() {

		pathLength = 0f;

		FlatteningPathIterator fpi = new FlatteningPathIterator(
				path.getPathIterator(new AffineTransform()), 0.01f);
		segments = new Vector(20);
		float lastMoveX = 0f;
		float lastMoveY = 0f;
		float currentX = 0f;
		float currentY = 0f;
		float seg[] = new float[6];
		int segType;

		segments.add(new PathSegment(PathIterator.SEG_MOVETO, 0f, 0f, 0f));

		while (!fpi.isDone()) {

			segType = fpi.currentSegment(seg);

			switch (segType) {

			case PathIterator.SEG_MOVETO:

				// System.out.println("== MOVE TO " + seg[0] + " " + seg[1]);

				segments.add(new PathSegment(segType, seg[0], seg[1],
						pathLength));
				currentX = seg[0];
				currentY = seg[1];
				lastMoveX = currentX;
				lastMoveY = currentY;

				break;

			case PathIterator.SEG_LINETO:

				// System.out.println("== LINE TO " + seg[0] + " " + seg[1]);

				pathLength += Point2D.distance(currentX, currentY, seg[0],
						seg[1]);
				segments.add(new PathSegment(segType, seg[0], seg[1],
						pathLength));

				currentX = seg[0];
				currentY = seg[1];

				break;

			case PathIterator.SEG_CLOSE:

				// System.out.println("== CLOSE TO " + lastMoveX + " " +
				// lastMoveY);

				pathLength += Point2D.distance(currentX, currentY, lastMoveX,
						lastMoveY);
				segments.add(new PathSegment(PathIterator.SEG_LINETO,
						lastMoveX, lastMoveY, pathLength));

				currentX = lastMoveX;
				currentY = lastMoveY;

				break;

			default:

				// ouch, where have these come from
				System.out.println("Bad path segment types");

			}

			fpi.next();

		}

		initialised = true;

	}

	/**
	 * Return the point that is at the given length along the path.
	 * 
	 * @param length
	 *            The length along the path
	 * @return The point at the given length
	 */

	public Point2D pointAtLength(float length) {

		int upperIndex = findUpperIndex(length);

		if (upperIndex == -1) {
			// length is off the end of the path
			return null;
		}

		PathSegment upper = (PathSegment) segments.elementAt(upperIndex);

		if (upperIndex == 0) {
			// length was probably zero
			// return the upper point
			return new Point2D.Float(upper.getX(), upper.getY());
		}

		PathSegment lower = (PathSegment) segments.elementAt(upperIndex - 1);

		// now work out where along the line would be the length

		float offset = length - lower.getLength();

		// slope
		double theta = Math.atan2(upper.getY() - lower.getY(), upper.getX()
				- lower.getX());

		float xPoint = (float) (lower.getX() + offset * Math.cos(theta));
		float yPoint = (float) (lower.getY() + offset * Math.sin(theta));

		return new Point2D.Float(xPoint, yPoint);

	}

	public float angleAtLength(float length) {

		int upperIndex = findUpperIndex(length);

		if (upperIndex == -1) {
			// length is off the end of the path
			// return 0f
			return 0f;
		}

		PathSegment upper = (PathSegment) segments.elementAt(upperIndex);

		if (upperIndex == 0) {
			// length was probably zero
			// return the angle between the first and second segments
			// return new Point2D.Float(upper.getX(), upper.getY());
			upperIndex = 1;
		}

		PathSegment lower = (PathSegment) segments.elementAt(upperIndex - 1);

		// slope
		float theta = (float) Math.atan2(upper.getY() - lower.getY(),
				upper.getX() - lower.getX());

		return theta;

	}

	private int findUpperIndex(float length) {
		if (!initialised) {
			initialise();
		}
		if (length < 0) {
			// length is before the start of the path
			return -1;
		}

		// find the two segments that are each side of the length

		int upperIndex = -1;

		int numSegments = segments.size();
		int currentIndex = 0;

		while (upperIndex <= 0 && currentIndex < numSegments) {

			PathSegment ps = (PathSegment) segments.elementAt(currentIndex);

			if (ps.getLength() >= length
					&& ps.getSegType() != PathIterator.SEG_MOVETO) {
				upperIndex = currentIndex;
			}
			currentIndex++;
		}

		return upperIndex;
	}

	public static void main(String args[]) {

		GeneralPath path = new GeneralPath();
		path.moveTo(100f, 100f);
		path.lineTo(200f, 150f);
		path.lineTo(300f, 200f);
		path.quadTo(450f, 525f, 400f, 250f);
		path.closePath();

		PathLength pl = new PathLength(path);

		System.out.println("New Path Length created");
		System.out.println("Path Length = " + pl.lengthOfPath());
		System.out.println("Path Length = " + pl.lengthOfPath());
		System.out.println("Point at 0 = " + pl.pointAtLength(0f));
		System.out.println("Point at 10 = " + pl.pointAtLength(10f));
		System.out.println("Point at 20 = " + pl.pointAtLength(20f));
		System.out.println("Point at 300 = " + pl.pointAtLength(300f));
		System.out.println("Point at 3000 = " + pl.pointAtLength(3000f));
		System.out.println("Point at 0 = " + pl.pointAtLength(0f));
		System.out.println("Point at 10 = " + pl.pointAtLength(10f));
		System.out.println("Point at 20 = " + pl.pointAtLength(20f));
		System.out.println("Point at 300 = " + pl.pointAtLength(300f));
		System.out.println("Path Length = " + pl.lengthOfPath());
		System.out.println("Point at 0 = " + pl.pointAtLength(0f));
		System.out.println("Point at 10 = " + pl.pointAtLength(10f));

	}

	protected class PathSegment {

		public PathSegment(int a, float b, float c, float d) {
			setSegType(a);
			setX(b);
			setY(c);
			setLength(d);
		}

		int segType;

		/**
		 * Get the value of segType.
		 * 
		 * @return Value of segType.
		 */

		public int getSegType() {
			return segType;
		}

		/**
		 * Set the value of segType.
		 * 
		 * @param v
		 *            Value to assign to segType.
		 */

		public void setSegType(int v) {
			this.segType = v;
		}

		float X;

		/**
		 * Get the value of X.
		 * 
		 * @return Value of X.
		 */

		public float getX() {
			return X;
		}

		/**
		 * Set the value of X.
		 * 
		 * @param v
		 *            Value to assign to X.
		 */

		public void setX(float v) {
			this.X = v;
		}

		float Y;

		/**
		 * Get the value of Y.
		 * 
		 * @return Value of Y.
		 */

		public float getY() {
			return Y;
		}

		/**
		 * Set the value of Y.
		 * 
		 * @param v
		 *            Value to assign to Y.
		 */

		public void setY(float v) {
			this.Y = v;
		}

		float length;

		/**
		 * Get the value of Length.
		 * 
		 * @return Value of Length.
		 */

		public float getLength() {
			return length;
		}

		/**
		 * Set the value of Length.
		 * 
		 * @param v
		 *            Value to assign to Length.
		 */

		public void setLength(float v) {
			this.length = v;
		}
	}
}
