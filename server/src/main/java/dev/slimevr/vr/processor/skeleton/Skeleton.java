package dev.slimevr.vr.processor.skeleton;

import dev.slimevr.util.ann.VRServerThread;
import dev.slimevr.vr.processor.TransformNode;
import io.eiren.util.ann.ThreadSafe;

import java.util.ArrayList;
import java.util.List;


public abstract class Skeleton {

	public final List<BoneInfo> allBoneInfo = new ArrayList<>();
	public final List<BoneInfo> shareableBoneInfo = new ArrayList<>();

	@VRServerThread
	public abstract void updatePose();

	@ThreadSafe
	public abstract TransformNode getRootNode();

	@ThreadSafe
	public abstract TransformNode[] getAllNodes();

	@ThreadSafe
	public abstract SkeletonConfig getSkeletonConfig();

	@ThreadSafe
	public abstract void resetSkeletonConfig(SkeletonConfigOffsets config);

	@ThreadSafe
	public void resetAllSkeletonConfigs() {
		for (SkeletonConfigOffsets config : SkeletonConfigOffsets.values) {
			resetSkeletonConfig(config);
		}
	}

	public abstract BoneInfo getBoneInfoForBodyPart(int bodyPart);

	public abstract TransformNode getTailNodeOfBone(BoneType bone);

	@VRServerThread
	public abstract void resetTrackersFull();

	@VRServerThread
	public abstract void resetTrackersMounting();

	@VRServerThread
	public abstract void resetTrackersYaw();

	@VRServerThread
	public abstract void updateLegTweaksConfig();

	@VRServerThread
	public abstract void updateTapDetectionConfig();

	@VRServerThread
	public abstract boolean[] getLegTweaksState();

	@VRServerThread
	public abstract void setLegTweaksEnabled(boolean value);

	@VRServerThread
	public abstract void setFloorclipEnabled(boolean value);

	@VRServerThread
	public abstract void setSkatingCorrectionEnabled(boolean value);
}
