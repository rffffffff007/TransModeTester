package com.example.sensortest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.location.Location;

public class PointRoute {

	private final long mWindowSize = Config.WINDOW_SIZE;
	private LinkedList<Location> mLocList;
	private LinkedList<GSensorPoint> mGSensorList;

	public PointRoute() {
		mLocList = new LinkedList<Location>();
		mGSensorList = new LinkedList<GSensorPoint>();
	}

	public synchronized void addLocation(Location loc) {
		mLocList.offer(loc);
		removeOldPoints();
	}

	public synchronized void addGSensorPoint(GSensorPoint gp) {
		mGSensorList.offer(gp);
		removeOldPoints();
	}

	private void removeOldPoints() {
		long validTime = System.currentTimeMillis() - mWindowSize;
		int removeCount = 0;
		for (Location loc : mLocList) {
			if (loc.getTime() < validTime)
				removeCount++;
			else
				break;
		}
		for (int i = 0; i < removeCount; i++) {
			mLocList.remove();
		}

		removeCount = 0;
		for (GSensorPoint p : mGSensorList) {
			if (p.getTime() < validTime)
				removeCount++;
			else
				break;
		}
		for (int i = 0; i < removeCount; i++)
			mGSensorList.remove();
	}

	public synchronized double getSpanVelocity() {
		Location last = null;
		double distanceSpan = 0;
		double timeSpan = 0;
		for (Location loc : mLocList) {
			if (last != null) {
				distanceSpan += last.distanceTo(loc);
				timeSpan = loc.getTime() - last.getTime();
			}
			last = loc;
		}
		if (timeSpan > 0)
			return distanceSpan / timeSpan;
		else
			return 0;
	}

	public synchronized double getMedianVelocity() {
		Location last = null;
		List<Double> velocities = new ArrayList<Double>();
		for (Location loc : mLocList) {
			if (last != null) {
				double velocity = last.distanceTo(loc)
						/ (loc.getTime() - last.getTime());
				velocities.add(velocity);
			}
			last = loc;
		}
		return getMedain(velocities);
	}

	public synchronized double getGAccelMean() {
		return getMean(getAccelList());
	}

	public synchronized double getGAbsMean() {
		List<Double> list = getAccelList();
		for (int i = 0; i < list.size(); i++)
			list.set(i, list.get(i) - Config.EARTH_GRAVITY);
		return getMean(list);
	}

	public synchronized double getGAccelEnergy() {
		return getEnergy(getAccelList(), Config.EARTH_GRAVITY);
	}

	public synchronized double getGAccelVariance() {
		return getVariance(getAccelList());
	}

	private List<Double> getAccelList() {
		List<Double> list = new ArrayList<Double>();
		for (GSensorPoint p : mGSensorList) {
			list.add(p.getMagnitude());
		}
		return list;
	}

	private double getMedain(List<Double> list) {
		Collections.sort(list);
		int size = list.size();
		if (size > 0)
			return list.get(size / 2);
		else
			return 0;
	}

	private double getMean(List<Double> list) {
		int size = list.size();
		if (size == 0)
			return 0;
		double sum = 0;
		for (double d : list)
			sum += d;
		return sum / size;
	}

	private double getEnergy(List<Double> list, double baseLine) {
		int size = list.size();
		if (size == 0)
			return 0;
		double sum = 0;
		for (double d : list)
			sum += (d - baseLine) * (d - baseLine);
		return sum / size;
	}

	private double getVariance(List<Double> list) {
		int size = list.size();
		if (size <= 1)
			return 0;
		double mean = getMean(list);
		double sum = 0;
		for (double d : list)
			sum += (d - mean) * (d - mean);
		return sum / (size - 1);
	}
}
