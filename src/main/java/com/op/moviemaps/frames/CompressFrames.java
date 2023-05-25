package com.op.moviemaps.frames;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import javax.imageio.ImageIO;

import com.op.moviemaps.services.MotionBlurFilter;
import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;

public class CompressFrames {
	protected BufferedImage bgImage;
	private String scName = "LST";
	private String dir = "scripts/" + scName + "/";
	private String opFile = "Paint.jpg";
	double ar = 1.85; // 2.4;
	int w = -1;
	int h = -1;
	private static CompressFrames tester;
	private BufferedImage opImage;
	private Graphics2D opG;
	boolean blur = false;
	boolean outline = false;
	boolean clip = true;

	public static void main(String[] args) throws Exception {
		tester = new CompressFrames();
		tester.drawCompressed();
		tester.saveImage();
	}

	protected void drawCompressed() throws IOException {
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
			double sc = 1;
			int sx1 = 0;
			int sy1 = 0;
			int sx2 = (int) wSrc;
			int sy2 = (int) hSrc;
			// wSrc / hSrc; // 2.35;
			w = (int) (files.length * sc);
			h = (int) (((double) w) / ar);
			opImage = RendererUtils.createBufferedImage(w, h);
			opG = (Graphics2D) opImage.getGraphics();
			opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			opG.setColor(Color.BLACK);
			opG.fillRect(0, 0, w, h);
			MotionBlurFilter op = null;
			if (blur) {
				op = new MotionBlurFilter();
				float deg = (float) Math.toRadians(90);
				float d = (float) h / 10f;
				op.setAngle(deg);
				op.setDistance(d);
			}
			int end = files.length;
			for (int i = 0; i < end; i++) {
				System.out.println("draw " + files[i].toString() + "   " + i
						+ "/" + files.length);
				bgImage = ImageIO.read(files[i]);
				plotLines(sc, sx1, sy1, sx2, sy2, op, i);
			}
		}
	}

	private void plotLines(double sc, int sx1, int sy1, int sx2, int sy2,
			MotionBlurFilter op, int i) {
		int dx1 = (int) (i * sc);
		int dx2 = dx1 + ((int) sc);
		int dy1 = 0;
		int dy2 = h;
		if (blur) {
			opG.drawImage(op.filter(bgImage, null), dx1, dy1, dx2, dy2, sx1,
					sy1, sx2, sy2, null);
		} else {
			BufferedImage sub = bgImage;
			if (clip) {
				int he = bgImage.getHeight();
				int hh = (int) (((double) bgImage.getWidth()) / ar);
				int hd = (he - hh) / 2;
				sub = bgImage.getSubimage(0, hd, bgImage.getWidth(), hh);
				sy2 = sub.getHeight();
			}
			opG.drawImage(sub, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
		}
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

	protected void saveImage() throws Exception {
		System.out.println("Saving...");
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src + scName + opFile);
		// RendererUtils.savePNGFile(opImage, fFile1);
		RendererUtils.saveJPGFile(opImage, src + scName + opFile, 72.0, 1);
		opG.dispose();
		Date now = new Date();
		System.out.println("Saved " + fFile1.getPath() + " @" + now);
	}
}
