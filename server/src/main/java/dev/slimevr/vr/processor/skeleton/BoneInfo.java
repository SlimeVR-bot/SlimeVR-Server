package dev.slimevr.vr.processor.skeleton;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.slimevr.vr.processor.TransformNode;
import dev.slimevr.vr.trackers.UnityBone;
import io.eiren.util.collections.FastList;
import solarxr_protocol.datatypes.BodyPart;


/**
 * Provides an easy way to access pose information of a particular skeletal bone
 * (as opposed to trackers).
 */
public class BoneInfo {

	// TODO(thebutlah): I don't think `BoneType` should include trackers, so
	// this might make more sense to be `BodyPart` or something.
	public final int bodyPart;
	public final TransformNode tailNode;
	public float length;

	/**
	 * Creates a `BoneInfo`.
	 *
	 * We use `tailNode` because the length of the bone comes from the tail
	 * node's local transform (offset from head), but the rotation of a bone
	 * comes from the head node's rotation.
	 */
	public BoneInfo(int bodyPart, TransformNode tailNode) {
		this.bodyPart = bodyPart;
		this.tailNode = tailNode;
		updateLength();
	}

	/**
	 * Recomputes `BoneInfo.length`
	 */
	public void updateLength() {
		this.length = this.tailNode.localTransform.getTranslation().length();
	}

	// TODO : There shouldn't be edge cases like multiplying
	// feet by rotation. This is the best solution right now,
	// or we'd need to store this info on the client, which is
	// worse. Need to rework the skeleton using new @SkeletonData
	// system
	public Quaternion getLocalRotation() {
		var rot = this.tailNode.getParent().localTransform.getRotation();
		if (this.bodyPart == BodyPart.LEFT_FOOT || this.bodyPart == BodyPart.RIGHT_FOOT) {
			rot = rot.mult(Quaternion.X_90_DEG);
		}
		return rot;
	}

	public Quaternion getGlobalRotation() {
		var rot = this.tailNode.getParent().worldTransform.getRotation();
		if (this.bodyPart == BodyPart.LEFT_FOOT || this.bodyPart == BodyPart.RIGHT_FOOT) {
			rot = rot.mult(Quaternion.X_90_DEG);
		}
		if (this.bodyPart == BodyPart.LEFT_LOWER_ARM || this.bodyPart == BodyPart.RIGHT_LOWER_ARM) {
			rot = rot.mult(Quaternion.X_180_DEG);
		}
		return rot;
	}

	/**
	 * @param root The new root for offsetting the translation
	 * @param unity Only use bones from Unity's HumanBodyBones
	 * @return The bone's local translation relative to a new root
	 */
	public Vector3f getLocalBoneTranslationFromRoot(BoneInfo root, boolean unity) {
		// FIXME
		TransformNode towardsNode = getNodeTowards(tailNode, root.tailNode, unity);

		if (this == root) {
			return new Vector3f().zero();
		} else if (towardsNode != null) {
			return tailNode.worldTransform
				.getTranslation()
				.subtract(towardsNode.worldTransform.getTranslation());
		} else {
			return tailNode.worldTransform
				.getTranslation()
				.subtract(
					tailNode.getParent().getParent().worldTransform.getTranslation()
				);
		}
	}

	/**
	 * @param root The new root for offsetting the rotation
	 * @param unity Only use bones from Unity's HumanBodyBones
	 * @return The bone's local rotation relative to a new root
	 */
	public Quaternion getLocalBoneRotationFromRoot(BoneInfo root, boolean unity) {
		// FIXME
		TransformNode towardsNode = getNodeTowards(tailNode, root.tailNode, unity);
		if (this == root) {
			return tailNode.worldTransform.getRotation();
		} else if (towardsNode != null) {
			return tailNode.worldTransform
				.getRotation()
				.mult(towardsNode.worldTransform.getRotation().inverse());
		} else if (hasInParents(tailNode, root.tailNode)) {
			return tailNode.worldTransform
				.getRotation()
				.mult(
					tailNode.getParent().getParent().worldTransform.getRotation().inverse()
				);
		} else {
			return tailNode.localTransform
				.getRotation();
		}
	}

	/**
	 * @param from The root of the search
	 * @param towards The goal of the search
	 * @param unity Only use bones from Unity's HumanBodyBones
	 * @return the first child node towards "towards", or null if "towards" is
	 * not present anywhere in the children of "from".
	 */
	private TransformNode getNodeTowards(
		TransformNode from,
		TransformNode towards,
		boolean unity
	) {
		FastList<TransformNode> searching = new FastList<>(from.children);
		int i = 0;
		while (i < searching.size()) {
			if (searching.get(i).getName().equalsIgnoreCase(towards.getName())) {
				if (unity) {
					if (
						UnityBone
							.getByBodyPart(
								BoneType.valueOf(searching.get(i).getParent().getName()).bodyPart
							)
							!= null
					) {
						return searching.get(i);
					} else {
						return searching.get(i).getParent();
					}
				} else {
					return searching.get(i);
				}
			}
			searching.addAll(searching.get(i).children);
			i++;
		}
		return null;
	}

	private boolean hasInParents(TransformNode from, TransformNode towards) {
		TransformNode searchingNode;
		searchingNode = from;
		while (searchingNode.getParent() != null && searchingNode.getParent() != towards) {
			searchingNode = searchingNode.getParent();
			if (searchingNode.getParent() == towards)
				return true;
		}
		return false;
	}
}
