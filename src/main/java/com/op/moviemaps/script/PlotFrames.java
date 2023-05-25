package com.op.moviemaps.script;

import java.awt.Color;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import javax.imageio.ImageIO;

import com.op.moviemaps.services.MotionBlurFilter;
import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;

public class PlotFrames {
	protected BufferedImage bgImage;
	private String scName = "ANH";
	private String dir = "scripts/" + scName + "/";
	boolean bySrc = true;
	int w = -1;
	private static PlotFrames tester;
	private BufferedImage opImage;
	private Graphics2D opG;
	int h = (int) ((double) w / 2.3);
	boolean blur = false;
	boolean outline = false;
	double ar = 2.35;
	boolean clip = true;

	public static void main(String[] args) throws IOException,
			FontFormatException {
		tester = new PlotFrames();
		tester.plotBackground();
		// tester.testBlur();
		tester.saveImage();
	}

	protected void testBlur() throws IOException {
		System.out.println("Creating...");
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir
				+ "src/scene01969.jpg";
		File f = new File(src);
		bgImage = ImageIO.read(f);

		opImage = RendererUtils.createAlphaBufferedImage(1000, 1000);
		opG = (Graphics2D) opImage.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		MotionBlurFilter op = new MotionBlurFilter();
		float deg = (float) Math.toRadians(90);
		op.setAngle(deg);
		op.setDistance(100f);

		BufferedImage dst = new BufferedImage(bgImage.getWidth(),
				bgImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		dst = op.filter(bgImage, null);

		opG.drawImage(dst, 0, 0, bgImage.getWidth(), bgImage.getHeight(), 0, 0,
				bgImage.getWidth(), bgImage.getHeight(), null);
	}

	protected void plotBackground() throws IOException {
		System.out.println("Creating...");
		if (bySrc) {
			String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
					+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir
					+ "src/";
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

				opImage = RendererUtils.createAlphaBufferedImage(w, h);
				opG = (Graphics2D) opImage.getGraphics();
				opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				opG.setColor(Color.BLACK);
				opG.fillRect(0, 0, w, h);

				MotionBlurFilter op = new MotionBlurFilter();
				if (blur) {
					float deg = (float) Math.toRadians(90);
					float d = (float) h / 10f;
					op.setAngle(deg);
					op.setDistance(d);
				}

				for (int i = 0; i < files.length; i++) {
					// for (int i = 1000; i < 1500; i++) {
					System.out.println("draw " + files[i].toString() + "   "
							+ i + "/" + files.length);
					bgImage = ImageIO.read(files[i]);

					if (outline) {
						// plotOutline(sc, sx1, sy1, sx2, sy2, op, i);
					} else {
						plotLines(sc, sx1, sy1, sx2, sy2, op, i);
					}
				}
			}
		} else {
			opImage = RendererUtils.createAlphaBufferedImage(w, h);
			opG = (Graphics2D) opImage.getGraphics();
			opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
					+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
			File inputFile = new File(src + scName + "Frames.jpg");
			bgImage = ImageIO.read(inputFile);
			for (int i = 0; i < w; i++) {
				int st = (int) (i);
				int sx1 = (st * 64) % 3840;
				int sy1 = (st / (3840 / 64)) * 36;
				int sx2 = 64 + sx1;
				int sy2 = 34 + sy1;
				int dx1 = i;
				int dx2 = i + 1;
				int dy1 = 0;
				int dy2 = h;
				opG.drawImage(bgImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
						null);
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

	protected void saveImage() throws IOException {
		System.out.println("Saving...");
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src + scName + "Paint.png");
		RendererUtils.savePNGFile(opImage, fFile1);
		opG.dispose();
		System.out.println("Saved " + fFile1.getPath());
	}
}
