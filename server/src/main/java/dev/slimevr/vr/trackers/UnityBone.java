package dev.slimevr.vr.trackers;

import dev.slimevr.vr.processor.skeleton.BoneType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Unity HumanBodyBones from:
 * https://docs.unity3d.com/ScriptReference/HumanBodyBones.html
 */
public enum UnityBone {
	HIPS("Hips", BoneType.HIP),
	LEFT_UPPER_LEG("LeftUpperLeg", BoneType.LEFT_UPPER_LEG),
	RIGHT_UPPER_LEG("RightUpperLeg", BoneType.RIGHT_UPPER_LEG),
	LEFT_LOWER_LEG("LeftLowerLeg", BoneType.LEFT_LOWER_LEG),
	RIGHT_LOWER_LEG("RightLowerLeg", BoneType.RIGHT_LOWER_LEG),
	LEFT_FOOT("LeftFoot", BoneType.LEFT_FOOT),
	RIGHT_FOOT("RightFoot", BoneType.RIGHT_FOOT),
	SPINE("Spine", BoneType.WAIST),
	CHEST("Chest", BoneType.CHEST),
	UPPER_CHEST("UpperChest", Optional.empty()),
	NECK("Neck", BoneType.NECK),
	HEAD("Head", BoneType.HEAD),
	LEFT_SHOULDER("LeftShoulder", BoneType.LEFT_SHOULDER_TAIL),
	RIGHT_SHOULDER("RightShoulder", BoneType.RIGHT_SHOULDER_TAIL),
	LEFT_UPPER_ARM("LeftUpperArm", BoneType.LEFT_UPPER_ARM),
	RIGHT_UPPER_ARM("RightUpperArm", BoneType.RIGHT_UPPER_ARM),
	LEFT_LOWER_ARM("LeftLowerArm", BoneType.LEFT_LOWER_ARM),
	RIGHT_LOWER_ARM("RightLowerArm", BoneType.RIGHT_LOWER_ARM),
	LEFT_HAND("LeftHand", BoneType.LEFT_HAND),
	RIGHT_HAND("RightHand", BoneType.RIGHT_HAND),
	LEFT_TOES("LeftToes", Optional.empty()),
	RIGHT_TOES("RightToes", Optional.empty()),
	LEFT_EYE("LeftEye", Optional.empty()),
	RIGHT_EYE("RightEye", Optional.empty()),
	JAW("Jaw", Optional.empty()),
	LEFT_THUMB_PROXIMAL("LeftThumbProximal", Optional.empty()),
	LEFT_THUMB_INTERMEDIATE("LeftThumbIntermediate", Optional.empty()),
	LEFT_THUMB_DISTAL("LeftThumbDistal", Optional.empty()),
	LEFT_INDEX_PROXIMAL("LeftIndexProximal", Optional.empty()),
	LEFT_INDEX_INTERMEDIATE("LeftIndexIntermediate", Optional.empty()),
	LEFT_INDEX_DISTAL("LeftIndexDistal", Optional.empty()),
	LEFT_MIDDLE_PROXIMAL("LeftMiddleProximal", Optional.empty()),
	LEFT_MIDDLE_INTERMEDIATE("LeftMiddleIntermediate", Optional.empty()),
	LEFT_MIDDLE_DISTAL("LeftMiddleDistal", Optional.empty()),
	LEFT_RING_PROXIMAL("LeftRingProximal", Optional.empty()),
	LEFT_RING_INTERMEDIATE("LeftRingIntermediate", Optional.empty()),
	LEFT_RING_DISTAL("LeftRingDistal", Optional.empty()),
	LEFT_LITTLE_PROXIMAL("LeftLittleProximal", Optional.empty()),
	LEFT_LITTLE_INTERMEDIATE("LeftLittleIntermediate", Optional.empty()),
	LEFT_LITTLE_DISTAL("LeftLittleDistal", Optional.empty()),
	RIGHT_THUMB_PROXIMAL("RightThumbProximal", Optional.empty()),
	RIGHT_THUMB_INTERMEDIATE("RightThumbIntermediate", Optional.empty()),
	RIGHT_THUMB_DISTAL("RightThumbDistal", Optional.empty()),
	RIGHT_INDEX_PROXIMAL("RightIndexProximal", Optional.empty()),
	RIGHT_INDEX_INTERMEDIATE("RightIndexIntermediate", Optional.empty()),
	RIGHT_INDEX_DISTAL("RightIndexDistal", Optional.empty()),
	RIGHT_MIDDLE_PROXIMAL("RightMiddleProximal", Optional.empty()),
	RIGHT_MIDDLE_INTERMEDIATE("RightMiddleIntermediate", Optional.empty()),
	RIGHT_MIDDLE_DISTAL("RightMiddleDistal", Optional.empty()),
	RIGHT_RING_PROXIMAL("RightRingProximal", Optional.empty()),
	RIGHT_RING_INTERMEDIATE("RightRingIntermediate", Optional.empty()),
	RIGHT_RING_DISTAL("RightRingDistal", Optional.empty()),
	RIGHT_LITTLE_PROXIMAL("RightLittleProximal", Optional.empty()),
	RIGHT_LITTLE_INTERMEDIATE("RightLittleIntermediate", Optional.empty()),
	RIGHT_LITTLE_DISTAL("RightLittleDistal", Optional.empty()),
	LAST_BONE("LastBone", Optional.empty());


	private static final Map<String, UnityBone> byStringVal = new HashMap<>();
	private static final Map<Optional<BoneType>, UnityBone> byBoneType = new HashMap<>();

	static {
		for (UnityBone configVal : values()) {
			byStringVal.put(configVal.stringVal.toLowerCase(), configVal);
		}
	}
	static {
		for (UnityBone configVal : values()) {
			byBoneType.put(configVal.boneType, configVal);
		}
	}

	public static final UnityBone[] values = values();
	public final String stringVal;
	public final Optional<BoneType> boneType;

	UnityBone(String stringVal, BoneType boneType) {
		this.stringVal = stringVal;
		this.boneType = Optional.ofNullable(boneType);
	}

	UnityBone(String stringVal, Optional<BoneType> boneType) {
		this.stringVal = stringVal;
		this.boneType = boneType;
	}

	public static UnityBone getByStringVal(String stringVal) {
		return stringVal == null ? null : byStringVal.get(stringVal.toLowerCase());
	}

	public static UnityBone getByBoneType(BoneType bone) {
		return bone == null ? null : byBoneType.get(bone);
	}
}
