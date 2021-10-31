package dev.slimevr.posestreamer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import org.apache.commons.lang3.StringUtils;

import io.eiren.vr.processor.HumanSkeleton;
import io.eiren.vr.processor.TransformNode;

public class BVHFileStream extends PoseDataStream {

	private static final int LONG_MAX_VALUE_DIGITS = Long.toString(Long.MAX_VALUE).length();
	private static final float POS_SCALE = 100f;

	private long frameCount = 0;
	private final BufferedWriter writer;

	private long frameCountOffset;

	private float[] angleBuf = new float[3];
	private Quaternion rotBuf = new Quaternion();

	private HumanSkeleton wrappedSkeleton;
	private TransformNodeWrapper rootNode;

	public BVHFileStream(OutputStream outputStream) {
		super(outputStream);
		writer = new BufferedWriter(new OutputStreamWriter(outputStream), 4096);
	}

	public BVHFileStream(File file) throws FileNotFoundException {
		super(file);
		writer = new BufferedWriter(new OutputStreamWriter(outputStream), 4096);
	}

	public BVHFileStream(String file) throws FileNotFoundException {
		super(file);
		writer = new BufferedWriter(new OutputStreamWriter(outputStream), 4096);
	}

	private String getBufferedFrameCount(long frameCount) {
		String frameString = Long.toString(frameCount);
		int bufferCount = LONG_MAX_VALUE_DIGITS - frameString.length();

		return bufferCount > 0 ? frameString + StringUtils.repeat(' ', bufferCount) : frameString;
	}

	private TransformNodeWrapper wrapSkeletonIfNew(HumanSkeleton skeleton) {
		TransformNodeWrapper wrapper = rootNode;

		// If the wrapped skeleton is missing or the skeleton is updated
		if (wrapper == null || skeleton != wrappedSkeleton) {
			wrapper = wrapSkeleton(skeleton);
		}

		return wrapper;
	}

	private TransformNodeWrapper wrapSkeleton(HumanSkeleton skeleton) {
		TransformNodeWrapper wrapper = wrapSkeletonNodes(skeleton.getRootNode());

		wrappedSkeleton = skeleton;
		rootNode = wrapper;

		return wrapper;
	}

	protected TransformNodeWrapper wrapSkeletonNodes(TransformNode rootNode) {
		return TransformNodeWrapper.wrapFullHierarchy(rootNode);
	}

	private void writeNodeHierarchy(TransformNodeWrapper node) throws IOException {
		writeNodeHierarchy(node, 0);
	}

	private void writeNodeHierarchy(TransformNodeWrapper node, int level) throws IOException {
		// Don't write end sites at populated nodes
		if (node.children.isEmpty() && node.getParent().children.size() > 1) {
			return;
		}

		String indentLevel = StringUtils.repeat("\t", level);
		String nextIndentLevel = indentLevel + "\t";

		// Handle ends
		if (node.children.isEmpty()) {
			writer.write(indentLevel + "End Site\n");
		} else {
			writer.write((level > 0 ? indentLevel + "JOINT " : "ROOT ") + node.getName() + "\n");
		}
		writer.write(indentLevel + "{\n");

		// Ignore the root offset and original root offset
		if (level > 0 && node.wrappedNode.getParent() != null) {
			Vector3f offset = node.localTransform.getTranslation();
			float reverseMultiplier = node.reversedHierarchy ? -1 : 1;
			writer.write(nextIndentLevel + "OFFSET " + Float.toString(offset.getX() * POS_SCALE * reverseMultiplier) + " " + Float.toString(offset.getY() * POS_SCALE * reverseMultiplier) + " " + Float.toString(offset.getZ() * POS_SCALE * reverseMultiplier) + "\n");
		} else {
			writer.write(nextIndentLevel + "OFFSET 0.0 0.0 0.0\n");
		}

		// Handle ends
		if (!node.children.isEmpty()) {
			// Only give position for root
			if (level > 0) {
				writer.write(nextIndentLevel + "CHANNELS 3 Zrotation Xrotation Yrotation\n");
			} else {
				writer.write(nextIndentLevel + "CHANNELS 6 Xposition Yposition Zposition Zrotation Xrotation Yrotation\n");
			}

			for (TransformNodeWrapper childNode : node.children) {
				writeNodeHierarchy(childNode, level + 1);
			}
		}

		writer.write(indentLevel + "}\n");
	}

	@Override
	public void writeHeader(HumanSkeleton skeleton, PoseStreamer streamer) throws IOException {
		if (skeleton == null) {
			throw new NullPointerException("skeleton must not be null");
		}
		if (streamer == null) {
			throw new NullPointerException("streamer must not be null");
		}

		writer.write("HIERARCHY\n");
		writeNodeHierarchy(wrapSkeletonIfNew(skeleton));

		writer.write("MOTION\n");
		writer.write("Frames: ");

		// Get frame offset for finishing writing the file
		if (outputStream instanceof FileOutputStream) {
			FileOutputStream fileOutputStream = (FileOutputStream)outputStream;
			// Flush buffer to get proper offset
			writer.flush();
			frameCountOffset = fileOutputStream.getChannel().position();
		}

		writer.write(getBufferedFrameCount(frameCount) + "\n");

		// Frame time in seconds
		writer.write("Frame Time: " + (streamer.frameRecordingInterval / 1000d) + "\n");
	}

	private void writeNodeHierarchyRotation(TransformNodeWrapper node, Quaternion inverseRootRot) throws IOException {
		rotBuf = node.localTransform.getRotation(rotBuf);

		// Adjust to local rotation
		if (inverseRootRot != null) {
			rotBuf = inverseRootRot.mult(rotBuf, rotBuf);
		}

		angleBuf = rotBuf.toAngles(angleBuf);
		writer.write(Float.toString((float)Math.toDegrees(angleBuf[2])) + " " + Float.toString((float)Math.toDegrees(angleBuf[0])) + " " + Float.toString((float)Math.toDegrees(angleBuf[1])));

		// Get inverse rotation for child local rotations
		Quaternion inverseRot = node.localTransform.getRotation().inverse();
		for (TransformNodeWrapper childNode : node.children) {
			if (childNode.children.isEmpty()) {
				// If it's an end node, skip
				continue;
			}

			// Add spacing
			writer.write(" ");
			writeNodeHierarchyRotation(childNode, inverseRot);
		}
	}

	@Override
	public void writeFrame(HumanSkeleton skeleton) throws IOException {
		if (skeleton == null) {
			throw new NullPointerException("skeleton must not be null");
		}

		TransformNodeWrapper rootNode = wrapSkeletonIfNew(skeleton);

		Vector3f rootPos = rootNode.worldTransform.getTranslation();

		// Write root position
		writer.write(Float.toString(rootPos.getX() * POS_SCALE) + " " + Float.toString(rootPos.getY() * POS_SCALE) + " " + Float.toString(rootPos.getZ() * POS_SCALE) + " ");
		writeNodeHierarchyRotation(rootNode, null);

		writer.newLine();

		frameCount++;
	}

	@Override
	public void writeFooter(HumanSkeleton skeleton) throws IOException {
		// Write the final frame count for files
		if (outputStream instanceof FileOutputStream) {
			FileOutputStream fileOutputStream = (FileOutputStream)outputStream;
			// Flush before anything else
			writer.flush();
			// Seek to the count offset
			fileOutputStream.getChannel().position(frameCountOffset);
			// Overwrite the count with a new value
			writer.write(Long.toString(frameCount));
		}
	}

	@Override
	public void close() throws IOException {
		writer.close();
		super.close();
	}
}
