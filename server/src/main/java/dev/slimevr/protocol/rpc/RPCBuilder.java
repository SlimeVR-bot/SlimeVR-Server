package dev.slimevr.protocol.rpc;

import com.google.flatbuffers.FlatBufferBuilder;
import dev.slimevr.vr.processor.HumanPoseProcessor;
import dev.slimevr.vr.processor.skeleton.SkeletonConfigOffsets;
import solarxr_protocol.rpc.SkeletonConfigResponse;
import solarxr_protocol.rpc.SkeletonPart;


public class RPCBuilder {

	public static int createSkeletonConfig(
		FlatBufferBuilder fbb,
		HumanPoseProcessor humanPoseProcessor
	) {
		int[] partsOffsets = new int[SkeletonConfigOffsets.values().length];

		for (int index = 0; index < SkeletonConfigOffsets.values().length; index++) {
			SkeletonConfigOffsets val = SkeletonConfigOffsets.values[index];
			int part = SkeletonPart
				.createSkeletonPart(fbb, val.id, humanPoseProcessor.getSkeletonConfig(val));
			partsOffsets[index] = part;
		}

		int parts = SkeletonConfigResponse.createSkeletonPartsVector(fbb, partsOffsets);
		return SkeletonConfigResponse.createSkeletonConfigResponse(fbb, parts);
	}
}
