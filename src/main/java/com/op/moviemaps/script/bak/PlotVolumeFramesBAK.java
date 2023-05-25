package com.op.moviemaps.script.bak;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import javax.imageio.ImageIO;

import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;
import com.op.moviemaps.sound.WaveFileReader;

public class PlotVolumeFramesBAK {
	protected BufferedImage bgImage;
	private static String scName = "ANH";
	private static String dir = "scripts/" + scName + "/";
	int w = -1;
	private static PlotVolumeFramesBAK tester;
	private static WaveFileReader reader;
	private BufferedImage opImage;
	private Graphics2D opG;
	int h = (int) ((double) w / 2.3);
	double ar = 2.35;

	public static void main(String[] args) throws IOException,
			FontFormatException {
		tester = new PlotVolumeFramesBAK();
		createWavData();
		tester.plotBackground();
		tester.saveImage();
	}

	private static void createWavData() {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		reader = new WaveFileReader(src + "ANH.wav");
	}

	protected void plotBackground() throws IOException {
		System.out.println("Creating...");
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir + "src/";
		File dirF = new File(src);
		if (dirF.isDirectory()) {
			File[] files = dirF.listFiles();
			sort(files, false);
			bgImage = ImageIO.read(files[0]);
			double wSrc = bgImage.getWidth();
			double hSrc = bgImage.getHeight();

			int sx1 = 0;
			int sy1 = 0;
			int sx2 = (int) wSrc;
			int sy2 = (int) hSrc;
			// wSrc / hSrc; // 2.35;

			w = (int) (files.length);
			h = (int) (((double) w) / ar);

			opImage = RendererUtils.createAlphaBufferedImage(w, h);
			opG = (Graphics2D) opImage.getGraphics();
			opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			opG.setColor(Color.BLACK);
			opG.fillRect(0, 0, w, h);

			Path2D.Double p = new Path2D.Double();
			int end = 1000; // files.length;
			for (int i = 0; i < end; i++) {
				// for (int i = 1000; i < 1500; i++) {
				System.out.println("draw " + files[i].toString() + "   " + i
						+ "/" + files.length);
				bgImage = ImageIO.read(files[i]);

				plotLines(sx1, sy1, sx2, sy2, i, p);
			}

			opG.setColor(Color.WHITE);
			opG.setStroke(new BasicStroke(((float) (2)), BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND, 1f, null, 0f));
			opG.draw(p);
		}
	}

	private void plotLines(int sx1, int sy1, int sx2, int sy2, int i,
			Path2D.Double p) {
		int dx1 = (int) (i);
		int dx2 = dx1 + 1;
		int cy = h / 2;
		int dy1 = 0;
		int dy2 = h;
		double hh = (double) h;

		double base = (hh * 0.0);
		double e = getWavHeight(i);
		double he = Math.log10(e) * 20 * 10;
		int y1 = (int) (cy - base - he);
		int y2 = (int) (cy + base + he);
		BufferedImage sub = bgImage;

		int hhe = bgImage.getHeight();
		int hhh = (int) (((double) bgImage.getWidth()) / ar);
		int hhd = (hhe - hhh) / 2;
		sub = bgImage.getSubimage(0, hhd, bgImage.getWidth(), hhh);
		sy2 = sub.getHeight();

		opG.drawImage(sub, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);

		if (p.getCurrentPoint() == null) {
			p.moveTo(dx1, y1);
		} else {
			p.lineTo(dx1, y1);
		}
	}

	private double getWavHeight(int i) {
		int ii = i * 8000;
		double r = 1;// Math.pow(2, 16) / 2.0;
		double n1 = getAverage(ii, 0); // reader.getData()[0][ii];
		double n2 = getAverage(ii, 1); // reader.getData()[0][ii];
		double hh = (n1 / r + n2 / r) / 2.0;

		return (hh);
	}

	private double getAverage(int i, int p) {
		double tot = 0;
		double max = -1;
		for (int ii = i; ii < i + 8000; ii++) {
			double val = Math.abs(reader.getData()[p][ii]);
			tot = tot + val;
			if (val > max) {
				max = val;
			}
		}
		// return Math.abs(tot / 8000);
		return max;
	}

	public static void sort(File[] files, boolean modOrName) {
		if (modOrName) {
			Arrays.sort(files, new Comparator<File>() {
				@Override
				public int compare(File f1, File f2) {
					return (new Long(f1.lastModified()).compareTo(new Long(f2
							.lastModified())));
				}
			});
		} else {
			Arrays.sort(files, new Comparator<File>() {
				@Override
				public int compare(File f1, File f2) {
					String ff1 = f1.getName().replaceAll(".jpg", "")
							.substring(5);
					String ff2 = f2.getName().replaceAll(".jpg", "")
							.substring(5);
					return new Integer(ff1).compareTo(new Integer(ff2));
				}
			});
		}
	}

	protected void saveImage() throws IOException {
		System.out.println("Saving...");
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src + scName + "PaintVol.png");
		RendererUtils.savePNGFile(opImage, fFile1);
		opG.dispose();
		System.out.println("Saved " + fFile1.getPath());
	}
}
