package com.example.sensortest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class C45Tree {
	private List<C45Node> mNodes;
	private C45Node mRoot;

	public C45Tree(String data) {
		mNodes = new ArrayList<C45Node>();
		String[] lines = data.split("\n");
		for (String line : lines) {
			C45Node node = new C45Node(line);
			if (mRoot == null)
				mRoot = node;
			mNodes.add(node);
		}
	}

	public int classify(Map<String, Double> params) {
		C45Node node = mRoot;
		while (!node.isLeaf()) {
			C45Node left = leftOf(node);
			C45Node right = rightOf(node);

			String key = left.getFeature();
			double val = params.get(key);
			if (val < left.getPartitionVal()) {
				node = left;
			} else {
				node = right;
			}
		}
		return node.getMode();
	}

	private C45Node leftOf(C45Node node) {
		if (node.getLeftIndex() >= 0)
			return mNodes.get(node.getLeftIndex());
		else
			return null;
	}

	private C45Node rightOf(C45Node node) {
		if (node.getRightIndex() >= 0)
			return mNodes.get(node.getRightIndex());
		else
			return null;
	}
}
