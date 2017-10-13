package com.op.moviemaps.script.bak;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;
import com.op.moviemaps.sound.WaveFileReader;

public class PlotVolumeFramesPC {
	protected BufferedImage bgImage;
	private static String scName = "ROTJ";
	private static String wav = scName + ".wav";
	private static String dir = "scripts/" + scName + "/";
	private static String srcDir = dir + "src/";
	int w = -1;
	int h = -1;
	int wSrc = -1;
	int hSrc = -1;
	private static PlotVolumeFramesPC tester;
	private static WaveFileReader reader;
	private double max = -1;
	private BufferedImage opImage;
	private Graphics2D opG;
	double ar = 1.41; // 2.35;
	int m = 3;
	boolean fillBGimage = true;
	double samples = 8000;
	private double hF = 0.25;
	private int hM = -1;
	boolean linesOrBars = false;
	boolean linOrCurve = false;

	public static void main(String[] args) throws Exception,
			FontFormatException {
		tester = new PlotVolumeFramesPC();
		createWavData();
		tester.plotBackground();
		tester.saveImage();
	}

	private static void createWavData() {
		System.out.println("reading...");
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		reader = new WaveFileReader(src + wav);
		for (int i = 0; i < reader.getData()[0].length; i++) {
			int r = Math.abs(reader.getData()[0][i]);
			int l = Math.abs(reader.getData()[1][i]);
			int rl = (r + l) / 2;
			if (rl > tester.max) {
				tester.max = rl;
			}
		}
	}

	protected void plotBackground() throws IOException {
		System.out.println("Creating...");
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + srcDir;
		File file = new File(src + "scene00001.jpg");
		BufferedImage b2 = ImageIO.read(file);
		wSrc = b2.getWidth();
		hSrc = b2.getHeight();

		src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		file = new File(src + scName + "Paint.jpg");
		b2 = ImageIO.read(file);
		bgImage = new BufferedImage(wSrc, hSrc, BufferedImage.TYPE_INT_RGB);

		int end = (int) (reader.getDataLen() / samples); // files.length;
		w = (int) (b2.getWidth()); // (end);
		h = (int) (((double) w) / ar);
		hM = (int) (((double) h) * hF);
		opImage = RendererUtils.createAlphaBufferedImage(w, h);
		opG = (Graphics2D) opImage.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		opG.setColor(Color.BLACK);
		opG.fillRect(0, 0, w, h);
		opG.drawImage(b2, 0, 0, w, h, 0, 0, w, b2.getHeight(), null);

		opG.setColor(Color.WHITE);
		ArrayList<Point2D> p = new ArrayList<Point2D>();
		for (int i = 0; i < end; i = i + 1) {
			plotLines(i, p);
		}

		double wF = ((double) w) / ((double) end);
		if (linesOrBars) {
			drawAsLines(p);
		} else {
			drawAsBars(p, wF);
		}
	}

	private void drawAsLines(ArrayList<Point2D> p) {
		opG.setStroke(new BasicStroke((float) m, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND, 1f, null, 0f));
		opG.draw(convertToCurve(p));
	}

	private Shape convertToCurve(ArrayList<Point2D> ps) {
		Path2D.Double path = new Path2D.Double();
		for (int i = 0; i < ps.size() - 3 * m; i = i + 3 * m) {
			Point2D p1 = ps.get(i);
			Point2D p2 = ps.get(i + 1 * m);
			Point2D p3 = ps.get(i + 2 * m);
			int y1 = h - (int) ((float) p1.getY() * hF);
			int y2 = h - (int) ((float) p2.getY() * hF);
			int y3 = h - (int) ((float) p3.getY() * hF);
			if (path.getCurrentPoint() == null) {
				path.moveTo(p1.getX(), p1.getY());
				i = i - 2;
			} else {
				if (linOrCurve) {
					double y = p1.getY();
					path.lineTo(p3.getX(), y);
				} else {
					path.curveTo(p1.getX(), y1, p2.getX(), y2, p3.getX(), y3);
				}
			}
		}
		return path;
	}

	private void drawAsBars(ArrayList<Point2D> ps, double wF) {
		// int cy = h / 2;
		int cy = h - hM / 2;
		for (int i = 0; i < ps.size(); i = i + 1) {
			Point2D p1 = ps.get(i);
			int x = (int) (((double) p1.getX()) * wF);
			int y = (int) p1.getY();

			float tot = 10;
			for (float f = 1; f > 0; f = f - 1f / tot) {
				float pf = (float) Math.pow(f, 1.1);
				Color col = new Color(f, f, f);
				int yy = (int) ((float) y * pf * hF);
				opG.setColor(col);
				opG.drawLine(x, cy, x, cy + yy);
				opG.drawLine(x, cy, x, cy - yy);
			}
		}
	}

	private void plotLines(int i, ArrayList<Point2D> p) {
		int dx1 = (int) (i);
		double hh = (double) h;
		double e = getWavHeight(i);

		double v = Math.pow(e, 1.1) * hh * 0.5;

		p.add(new Point2D.Double(dx1, v));
	}

	private double getWavHeight(int i) {
		int ii = i * (int) samples;
		double n1 = getAverage(ii, 0); // reader.getData()[0][ii];
		double n2 = getAverage(ii, 1); // reader.getData()[0][ii];
		double hh = (n1 + n2) / 2.0;
		// return (hh);
		return hh;
	}

	private double getAverage(int i, int p) {
		double tot = 0;
		double mx = 0;
		for (int ii = i; ii < i + samples; ii++) {
			double val = Math.abs(reader.getData()[p][ii]);
			tot = tot + val;
			if (val > mx) {
				mx = val;
			}
		}
		return mx / (max);
	}

	protected void saveImage() throws Exception {
		System.out.println("Saving...");
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src + scName + "PaintVolPC.png");
		double w1 = opImage.getWidth();
		double wmm = 148;
		double mm2i = 25.4;
		double dpi = (w1 / wmm) * mm2i;
		RendererUtils.savePNGFile(opImage, fFile1, dpi);
		opG.dispose();
		System.out.println("Saved " + fFile1.getPath());
	}
}
