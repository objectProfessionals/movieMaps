package com.op.moviemaps.bak;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
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

public class PlotVolumeFramesOverlay {
	protected BufferedImage bgImage;
	private static String scName = "ANH";
	private static String wav = scName + ".wav";
	private static String dir = "scripts/" + scName + "/";
	int w = -1;
	private static PlotVolumeFramesOverlay tester;
	private static WaveFileReader reader;
	private double max = -1;
	private BufferedImage opImage;
	private Graphics2D opG;
	int h = (int) ((double) w / 2.3);
	double ar = 2.35;
	int m = 3;
	double samples = 8000;
	private boolean linOrCurve = true;

	public static void main(String[] args) throws IOException,
			FontFormatException {
		tester = new PlotVolumeFramesOverlay();
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
		bgImage = ImageIO.read(file);
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
		opImage = RendererUtils.createAlphaBufferedImage(w, h);
		opG = (Graphics2D) opImage.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		opG.setColor(Color.WHITE);
		opG.fillRect(0, 0, w, h);

		Composite comp = opG.getComposite();
		opG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				0.5f));
		opG.drawImage(bgImage, 0, 0, w, h, 0, 0, bgImage.getWidth(),
				bgImage.getHeight(), null);

		ArrayList<Point2D> p = new ArrayList<Point2D>();
		for (int i = 0; i < end; i = i + 1) {
			calcLines(sx1, sy1, sx2, sy2, i, p);
		}

		opG.setComposite(AlphaComposite
				.getInstance(AlphaComposite.SRC_OVER, 1f));
		drawAsBars(p);
		opG.setComposite(comp);
		// drawAsLines(p);
	}

	private void drawAsBars(ArrayList<Point2D> ps) {
		int cy = h / 2;
		for (int i = 0; i < ps.size(); i = i + 1) {
			Point2D p1 = ps.get(i);
			int x = (int) p1.getX();
			int y = (int) p1.getY();

			float tot = 4;
			int r = 2;
			for (float f = 1; f > 0; f = f - 1f / tot) {
				int yy = (int) ((float) y * f);
				opG.drawImage(bgImage, x, cy - yy, x + 1, cy + yy, x, 0, x + 1,
						bgImage.getHeight(), null);
				// opG.setColor(Color.BLACK);
				// opG.fillRect(x - r, cy - yy - r, r, r);
				// opG.fillRect(x - r, cy + yy - r, r, r);
			}
		}
	}

	private void drawAsLines(ArrayList<Point2D> p) {
		opG.setColor(Color.WHITE);
		opG.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
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
					path.lineTo(p3.getX(), p3.getY());
				} else {
					path.curveTo(p1.getX(), p1.getY(), p2.getX(), p2.getY(),
							p3.getX(), p3.getY());
				}
			}
		}
		if (mirror) {
			AffineTransform tr = AffineTransform.getTranslateInstance(0, 3193);
			AffineTransform mi = AffineTransform.getScaleInstance(1, -1);
			AffineTransform at = new AffineTransform();
			at.concatenate(tr);
			at.concatenate(mi);
			Shape p2 = path.createTransformedShape(at);
			return p2;
		}
		return path;
	}

	private void calcLines(int sx1, int sy1, int sx2, int sy2, int i,
			ArrayList<Point2D> p) {
		int dx1 = (int) (i);
		double hh = (double) h;
		double e = getWavHeight(i);

		double v = Math.pow(e, 1.1) * hh * 0.75;

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
		File fFile1 = new File(src + scName + "PaintVolOv.png");
		RendererUtils.savePNGFile(opImage, fFile1);
		opG.dispose();
		System.out.println("Saved " + fFile1.getPath());
	}
}
