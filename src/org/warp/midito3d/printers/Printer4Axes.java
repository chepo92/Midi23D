package org.warp.midito3d.printers;

import java.io.IOException;
import java.util.Locale;

import org.warp.midito3d.PrinterArea;

public class Printer4Axes implements Printer {
	public static final int modelID = 0x02;
	
	private Motor[] motors;
	private final PrinterArea printerArea;
	double[] motorsPosition = new double[4];
	double[] motorsDirection = new double[] {1d, 1d, 1d, 1d};
	
	public Printer4Axes(Motor x, Motor y, Motor z, Motor e, PrinterArea printerArea) {
		motors = new Motor[]{x,y,z,e};
		this.printerArea = printerArea;
	}

	@Override
	public final int getMotorsCount() {
		return 4;
	}

	@Override
	public void initialize(GCodeOutput po) throws IOException {
		po.writeLine("G21");
		po.writeLine("M302");
		goTo(po, 8000, printerArea.minX, printerArea.minY, printerArea.minZ, 0);
	}

	@Override
	public void wait(GCodeOutput po, double time) throws IOException {
		po.writeLine(String.format(Locale.US, "G04 P%.4f", time));
	}

	@Override
	public void move(GCodeOutput po, double time, double... speed) throws IOException {
		double totalSpeed = Math.sqrt(Math.pow(speed[0], 2d)+Math.pow(speed[1], 2d)+Math.pow(speed[2], 2d)+Math.pow(speed[3], 2d));
		double speedPart = Math.sqrt(Math.pow(speed[0], 2d)+Math.pow(speed[1], 2d)+Math.pow(speed[2], 2d));
		
		for (int i = 0; i < 4; i++) {
			double motorDelta = ((speed[i] * time) *motorsDirection[i]);
			if (i == 3 && motorDelta != 0) {
				motorDelta = (motorDelta) / totalSpeed * speedPart;
			}
			motorsPosition[i] += motorDelta;
			if (isBiggerThanMax(i, motorsPosition[i])) {
				motorsDirection[i] = -1d;
			}
			if (isSmallerThanMin(i, motorsPosition[i])) {
				motorsDirection[i] = 1d;
			}
		}
		
		po.writeLine(String.format(Locale.US, "G01 X%.10f Y%.10f Z%.10f E%.10f F%.10f", motorsPosition[0], motorsPosition[1], motorsPosition[2], motorsPosition[3], speedPart));
	}

	@Override
	public void goTo(GCodeOutput po, double speed, double... position) throws IOException {
		double speedPart = Math.sqrt(Math.pow(speed*motors[0].getStepsPerMillimeter(), 2d)+Math.pow(speed*motors[1].getStepsPerMillimeter(), 2d)+Math.pow(speed*motors[2].getStepsPerMillimeter(), 2d));
		motorsPosition = position;
		po.writeLine(String.format(Locale.US, "G00 X%.10f Y%.10f Z%.10f E%.10f F%.10f", position[0], position[1], position[2], position[3], speedPart));
	}

	@Override
	public void stop(GCodeOutput po) throws IOException {
	}

	@Override
	public Motor getMotor(int number) {
		return motors[number];
	}

	@Override
	public boolean isBiggerThanMax(int motor, double val) {
		if (motor == 0) {
			return val > printerArea.maxX;
		} else if (motor == 1) {
			return val > printerArea.maxY;
		} else if (motor == 2) {
			return val > printerArea.maxZ;
		} else {
			return false;
		}
	}

	@Override
	public boolean isSmallerThanMin(int motor, double val) {
		if (motor == 0) {
			return val < printerArea.minX;
		} else if (motor == 1) {
			return val < printerArea.minY;
		} else if (motor == 2) {
			return val < printerArea.minZ;
		} else {
			return false;
		}
	}
}
