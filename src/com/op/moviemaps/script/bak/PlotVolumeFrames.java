package com.op.moviemaps.script.bak;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
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

public class PlotVolumeFrames {
	protected BufferedImage bgImage;
	private static String scName = "ANH";
	private static String wav = scName + ".wav";
	private static String dir = "scripts/" + scName + "/";
	int w = -1;
	private static PlotVolumeFrames tester;
	private static WaveFileReader reader;
	private double max = -1;
	private BufferedImage opImage;
	private Graphics2D opG;
	int h = (int) ((double) w / 2.3);
	double ar = 2.35;
	int m = 10;
	boolean linesOrBars = false;
	boolean linOrCurve = true;
	boolean fillBGimage = true;
	double samples = 8000;
	private double hF = 0.25;
	private int hM = -1;

	public static void main(String[] args) throws Exception,
			FontFormatException {
		tester = new PlotVolumeFrames();
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
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File file = new File(src + scName + "Paint.jpg");
		BufferedImage b2 = ImageIO.read(file);
		bgImage = new BufferedImage(1024, 576, BufferedImage.TYPE_INT_RGB);
		double wSrc = bgImage.getWidth();
		double hSrc = bgImage.getHeight();

		int sx1 = 0;
		int sy1 = 0;
		int sx2 = (int) wSrc;
		int sy2 = (int) hSrc;
		// wSrc / hSrc; // 2.35;
		int end = (int) (reader.getDataLen() / samples); // files.length;
		w = (int) (end);
		h = (int) (((double) w) / ar);
		int hM = 0;
		opImage = RendererUtils.createAlphaBufferedImage(w, h + hM);
		opG = (Graphics2D) opImage.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (fillBGimage) {
			opG.drawImage(b2, 0, 0, w, h, 0, 0, w, b2.getHeight(), null);
		} else {
			opG.setColor(Color.BLACK);
			opG.fillRect(0, 0, w, h + hM);
		}
		opG.setColor(Color.WHITE);
		ArrayList<Point2D> p = new ArrayList<Point2D>();
		for (int i = 0; i < end; i = i + 1) {
			plotLines(sx1, sy1, sx2, sy2, i, p);
		}

		if (linesOrBars) {
			drawAsLines(p);
		} else {
			double wF = ((double) w) / ((double) end);
			drawAsBars(p, wF);
		}

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

	private void drawAsLines(ArrayList<Point2D> p) {
		opG.setStroke(new BasicStroke(50f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND, 1f, null, 0f));
		opG.draw(convertToCurve(p, false));
		opG.draw(convertToCurve(p, true));
	}

	private Shape convertToCurve(ArrayList<Point2D> ps, boolean mirror) {
		Path2D.Double path = new Path2D.Double();
		for (int i = 0; i < ps.size() - 3; i = i + 3 * m) {
			Point2D p1 = ps.get(i);
			Point2D p2 = ps.get(i + 1);
			Point2D p3 = ps.get(i + 2);
			if (path.getCurrentPoint() == null) {
				path.moveTo(p1.getX(), p1.getY());
				i = i - 2;
			} else {
				if (linOrCurve) {
					double y = p1.getY();
					path.lineTo(p3.getX(), y);
				} else {
					path.curveTo(p1.getX(), p1.getY(), p2.getX(), p2.getY(),
							p3.getX(), p3.getY());
				}
			}
		}
		if (mirror) {
			AffineTransform tr = AffineTransform.getTranslateInstance(0, h);
			AffineTransform mi = AffineTransform.getScaleInstance(1, -1);
			AffineTransform at = new AffineTransform();
			at.concatenate(tr);
			at.concatenate(mi);
			Shape p2 = path.createTransformedShape(at);
			return p2;
		}
		return path;
	}

	private void plotLines(int sx1, int sy1, int sx2, int sy2, int i,
			ArrayList<Point2D> p) {
		int dx1 = (int) (i);
		int dx2 = dx1 + 1;
		int cy = h / 2;
		int dy1 = 0;
		int dy2 = h;
		double hh = (double) h;
		double base = (hh * 0.0);
		double e = getWavHeight(i);

		// double v = (1 - Math.pow(e, 0.1)) * hh * 0.5;
		// double he = Math.log10(v) * 20 * 40; // 20 *

		double v = Math.pow(e, 1.1) * hh * 0.5;
		// double he = v; // 20 *
		// 60;
		// int y1 = (int) (cy - base - he);
		// int y2 = (int) (cy + base + he);
		// BufferedImage sub = bgImage;
		// int hhe = bgImage.getHeight();
		// int hhh = (int) (((double) bgImage.getWidth()) / ar);
		// int hhd = (hhe - hhh) / 2;
		// sub = bgImage.getSubimage(0, hhd, bgImage.getWidth(), hhh);
		// sy2 = sub.getHeight();
		// opG.drawImage(sub, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
		// opG.drawLine(dx1, y1, dx1, y2);

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
		// double ret = tot / (samples);
		// return ret / (max);
		return mx / (max);
	}

	protected void saveImage() throws IOException {
		System.out.println("Saving...");
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src + scName + "PaintVol1.png");
		RendererUtils.savePNGFile(opImage, fFile1);
		opG.dispose();
		System.out.println("Saved " + fFile1.getPath());
	}
}
