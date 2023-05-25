package com.op.moviemaps.script;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.imageio.ImageIO;

import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;

public class PlotPixels {
	protected BufferedImage bgImage;
	private String scName = "ANH";
	boolean bySrc = true;
	private String dir = "scripts/" + scName + "/";
	int w = -1;
	int h = -1;
	private static PlotPixels tester;
	private BufferedImage opImage;
	private Graphics2D opG;
	protected ArrayList<BufferedImage> iImages = new ArrayList<BufferedImage>();

	public static void main(String[] args) throws Exception {
		tester = new PlotPixels();
		tester.plotBackground();
		tester.saveImage();
	}

	protected void plotBackground() throws Exception {
		System.out.println("starting...");
		if (bySrc) {
			String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
					+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir
					+ "src/";
			File dirF = new File(src);
			if (dirF.isDirectory()) {
				File[] files = dirF.listFiles();
				sort(files);
				bgImage = ImageIO.read(files[0]);

				w = bgImage.getWidth();
				h = bgImage.getHeight();

				opImage = RendererUtils.createAlphaBufferedImage(w, h);
				opG = (Graphics2D) opImage.getGraphics();
				opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				opG.setColor(Color.WHITE);
				opG.fillRect(0, 0, w, h);
				int end = files.length;
				for (int i = 0; i < end; i++) {
					// readFile(files, i);
				}
				System.out.println("read...");
				for (int i = 0; i < w * h; i++) {
					// drawPixel(i);
					drawPixel(files, i);
				}
			}
		} else {
			w = 64;
			h = 36;

			opImage = RendererUtils.createAlphaBufferedImage(w, h);
			opG = (Graphics2D) opImage.getGraphics();
			opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
					+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
			File inputFile = new File(src + scName + "Frames.jpg");
			bgImage = ImageIO.read(inputFile);

			int numFrames = (bgImage.getWidth() / w)
					* (bgImage.getHeight() / h);
			numFrames = w * h;
			for (int i = 0; i < numFrames; i++) {
				readFile(i);
			}
			for (int i = 0; i < numFrames; i++) {
				int st = (int) (i);
				int sx1 = (st * 64) % 3840;
				int sy1 = (st / (3840 / 64)) * 36;

				int x = (int) sx1 + (i % w);
				int y = (int) sy1 + (i / w);
				drawPixel(x, y, i);
			}
		}
		System.out.println("drawn...");
	}

	private void readFile(File[] files, int i) throws IOException {
		iImages.add(ImageIO.read(files[i]));
	}

	private void readFile(int i) throws IOException {
		int x = (i * 64) % 3840;
		int y = (i / (3840 / 64)) * 36;

		iImages.add(bgImage.getSubimage(x, y, w, h));
	}

	private void drawPixel(File[] files, int i) throws IOException {

	}

	private void drawPixel(int i) throws IOException {
		int x = (int) (i % w);
		int ww = 1;
		int y = (int) (i / w);
		int hh = 1;
		drawPixel(x, y, ww, hh);
	}

	private void drawPixel(int x, int y, int ww, int hh) throws IOException {
		// System.out.println("x,y=" + x + "," + y);
		Color col = getAverageRGB(x, y, iImages.size());
		opG.setColor(col);
		opG.fillRect(x, y, ww, hh);
	}

	private void drawPixel(int x, int y, int i) throws Exception {
		int xx = (int) (i % w);
		int yy = (int) (i / w);
		// Color col = getAverageRGB(xx, yy, w * h);
		Color col = getDominantRGB(xx, yy, w * h);
		opG.setColor(col);
		opG.fillRect(xx, yy, 1, 1);
	}

	private void sort(File[] files) {
		Arrays.sort(files, new Comparator<File>() {

			@Override
			public int compare(File f1, File f2) {

				return (new Long(f1.lastModified()).compareTo(new Long(f2
						.lastModified())));
			}
		});
	}

	protected void saveImage() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src + scName + "PIX.jpg");
		RendererUtils.savePNGFile(opImage, fFile1);
		opG.dispose();
		System.out.println("created " + fFile1);
	}

	private Color getAverageRGB(int x, int y, float len) {
		System.out.println("x,y=" + x + "," + y);
		float rf = 0;
		float gf = 0;
		float bf = 0;
		for (BufferedImage im : iImages) {
			int rgb = im.getRGB(x, y);
			int r = (rgb >>> 16) & 0x000000FF;
			int g = (rgb >>> 8) & 0x000000FF;
			int b = (rgb >>> 0) & 0x000000FF;
			rf = rf + ((float) r) / 255f;
			gf = gf + ((float) g) / 255f;
			bf = bf + ((float) b) / 255f;
		}
		float rr = rf / len;
		float gg = gf / len;
		float bb = bf / len;
		return new Color(rr, gg, bb);
	}

	private Color getDominantRGB(int x, int y, float len) throws Exception {
		System.out.println("x,y=" + x + "," + y);
		float rf = 0;
		float gf = 0;
		float bf = 0;
		return null; // ImageTester.getCommonColor(iImages, x, y);
	}
}
