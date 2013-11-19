package com.example.sensortest;

public class C45Node {
	private String mFeature;
	private double mPartitionVal;
	private int mLeftIndex;
	private int mRightIndex;
	private int mMode;

	public C45Node(String str) {
		String[] items = str.split(" ");
		mFeature = items[0];
		mPartitionVal = Double.parseDouble(items[1]);
		mLeftIndex = Integer.valueOf(items[2]);
		mRightIndex = Integer.valueOf(items[3]);
		if (items.length > 4 && items[4].length() > 0) {
			mMode = Integer.parseInt(items[4]);
		}
	}

	public String getFeature() {
		return mFeature;
	}

	public double getPartitionVal() {
		return mPartitionVal;
	}

	public int getLeftIndex() {
		return mLeftIndex;
	}

	public int getRightIndex() {
		return mRightIndex;
	}

	public int getMode() {
		return mMode;
	}

	public boolean isLeaf() {
		return mLeftIndex < 0 && mRightIndex < 0;
	}

}
