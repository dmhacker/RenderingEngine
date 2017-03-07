package io.github.dmhacker.rendering.kdtrees;

import java.util.ArrayList;
import java.util.List;

import io.github.dmhacker.rendering.objects.Object3d;
import io.github.dmhacker.rendering.vectors.Ray;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class KDNode {
	private static final int MAX_BUCKET_CAPACITY = 10;
	private static final int MAX_BUCKET_VARIANCE = 3;
	
	private KDNode left;
	private KDNode right;
	private BoundingBox boundingBox;
	private List<Object3d> contained;
	
	public KDNode(KDNode left, KDNode right, List<Object3d> objects) {
		this.left = left;
		this.right = right;
		this.contained = objects;
		
		if (!objects.isEmpty()) {
			this.boundingBox = BoundingBox.fromObjects(objects);
		}
	}
	
	public KDNode getLeft() {
		return left;
	}
	
	public KDNode getRight() {
		return right;
	}
	
	public void setLeft(KDNode node) {
		this.left = node;
	}
	
	public void setRight(KDNode node) {
		this.right = node;
	}
	
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}
	
	public List<Object3d> getObjects() {
		return contained;
	}
	
	public boolean isLeaf() {
		return left == null && right == null;
	}
	
	public String toString() {
		if (isLeaf()) {
			return contained.size()+"";
		}
		return "["+left+"|"+right+"]";
	}
	
	public static KDNode build(KDNode root, List<Object3d> objects, int depth) {
		KDNode node = new KDNode(null, null, objects);
		
		// Save memory by clearing objects from branching nodes
		// Since these nodes will never be tested for intersections
		if (root != null && root.getLeft() != null && root.getRight() != null) {
			root.getObjects().clear();
		}
		
		if (objects.size() < 2) {
			return node;
		}
		
		Vec3d midpoint = new Vec3d(0, 0, 0);
		for (Object3d obj : objects) {
			midpoint = midpoint.add(obj.getCenter());
		}
		midpoint = midpoint.divide(objects.size());
		
		int axis = depth % 3;
		List<Object3d> leftmost = new ArrayList<Object3d>();
		List<Object3d> rightmost = new ArrayList<Object3d>();
		for (Object3d obj : objects) {
			Vec3d objMidpoint = obj.getCenter();
			if (axis == 0) {
				if (objMidpoint.getX() < midpoint.getX()) {
					leftmost.add(obj);
				}
				else {
					rightmost.add(obj);
				}
			}
			if (axis == 1) {
				if (objMidpoint.getY() < midpoint.getY()) {
					leftmost.add(obj);
				}
				else {
					rightmost.add(obj);
				}
			}
			if (axis == 2) {
				if (objMidpoint.getZ() < midpoint.getZ()) {
					leftmost.add(obj);
				}
				else {
					rightmost.add(obj);
				}
			}
		}
		
		if ((int) Math.abs(leftmost.size() - rightmost.size()) < MAX_BUCKET_VARIANCE && leftmost.size() <= MAX_BUCKET_CAPACITY && rightmost.size() <= MAX_BUCKET_CAPACITY) {
			node.setLeft(new KDNode(null, null, leftmost));
			node.setRight(new KDNode(null, null, rightmost));
		}
		else {
			node.setLeft(build(node, leftmost, depth + 1));
			node.setRight(build(node, rightmost, depth + 1));
		}
		
		return node;
	}
	
	public static Object[] parseTree(KDNode node, Ray ray, boolean shadow) {
		if (node.getBoundingBox().isIntersecting(ray)) {
			
			if (node.isLeaf()) {
				double tMin = Double.MAX_VALUE;
				Object3d closest = null;
				for (Object3d obj : node.getObjects()) {
					if (shadow && obj.isTransparent()) {
						continue;
					}
					double t = obj.getIntersection(ray);
					if (t > 0 && t < tMin) {
						tMin = t;
						closest = obj;
					}
				}
				
				return new Object[] {closest, tMin};
			}
			
			boolean leftExists = node.getLeft() != null && !node.getLeft().getObjects().isEmpty();
			boolean rightExists = node.getRight() != null && !node.getRight().getObjects().isEmpty();
			
			Object[] leftNode = new Object[] {null, Double.MAX_VALUE};
			Object[] rightNode =  new Object[] {null, Double.MAX_VALUE};
			
			if (leftExists) {
				leftNode = parseTree(node.getLeft(), ray, shadow);
			}
			
			if (rightExists) {
				rightNode = parseTree(node.getRight(), ray, shadow);
			}
			
			if (leftNode[0] == null && rightNode[0] == null) {
				return leftNode; // Could be either
			}
			if (leftNode[0] == null && rightNode[0] != null) {
				return rightNode;
			}
			if (leftNode[0] != null && rightNode[0] == null) {
				return leftNode;
			}
			if (leftNode[0] != null && rightNode[0] != null) {
				if ((double) leftNode[1] < (double) rightNode[1]) {
					return leftNode;
				}
				else {
					return rightNode;
				}
			}
		}
		return new Object[] {null, Double.MAX_VALUE};
	}
}
