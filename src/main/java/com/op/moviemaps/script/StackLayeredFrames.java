package com.op.moviemaps.script;

import java.awt.AlphaComposite;
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

import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;

public class StackLayeredFrames {
	protected BufferedImage bgImage;
	private String scName = "ANH";
	private String dir = "scripts/" + scName + "/";
	boolean bySrc = true;
	boolean byPhase = false;
	int w = 64;
	private static StackLayeredFrames tester;
	private BufferedImage opImage;
	private Graphics2D opG;
	int h = 36;

	public static void main(String[] args) throws IOException,
			FontFormatException {
		tester = new StackLayeredFrames();
		tester.plotStack();
		tester.saveImage();
	}

	protected void plotStack() throws IOException {
		if (byPhase) {
			String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
					+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir
					+ "src/";
			File dirF = new File(src);
			if (dirF.isDirectory()) {
				File[] files = dirF.listFiles();
				sort(files);
				double end = files.length;
				end = 100;
				bgImage = ImageIO.read(files[0]);
				w = (bgImage.getWidth());
				h = (bgImage.getHeight());
				w = w + (int) end;

				opImage = RendererUtils.createAlphaBufferedImage(w, h);
				opG = (Graphics2D) opImage.getGraphics();
				opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				for (double i = 0; i < end; i++) {
					bgImage = ImageIO.read(files[(int) i]);
					BufferedImage bgImage2 = ImageIO.read(files[(int) i + 1]);

					// plotStackSingle(end, i);
					plotStackPhased(bgImage2, end, i);
					System.out.println(((int) i) + "/" + ((int) end));
				}
			}
		} else if (bySrc) {
			String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
					+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir
					+ "src/";
			File dirF = new File(src);
			if (dirF.isDirectory()) {
				File[] files = dirF.listFiles();
				sort(files);
				double end = files.length;
				end = 1000;
				bgImage = ImageIO.read(files[0]);
				w = (bgImage.getWidth());
				h = (bgImage.getHeight());

				opImage = RendererUtils.createAlphaBufferedImage(w, h);
				opG = (Graphics2D) opImage.getGraphics();
				opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				for (double i = 0; i < end; i++) {
					bgImage = ImageIO.read(files[(int) i]);
					plotStackSingle(end, i);
					System.out.println(((int) i) + "/" + ((int) end));
				}
			}
		} else {
			int sc = 4;
			opImage = RendererUtils.createAlphaBufferedImage(w * sc, h * sc);
			opG = (Graphics2D) opImage.getGraphics();
			opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			// opG.setColor(Color.WHITE);
			// opG.fillRect(0, 0, w, h);
			String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
					+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
			File inputFile = new File(src + scName + "Frames.jpg");
			bgImage = ImageIO.read(inputFile);
			double sta = 0;
			double end = 7000;
			for (int i = (int) sta; i < end; i++) {
				float a = (float) (0.5f - (0.5 * (double) i / end));
				opG.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, a));
				int st = (int) (i);
				int sx1 = (st * w) % 3840;
				int sy1 = (st / (3840 / w)) * h;
				int sx2 = w + sx1;
				int sy2 = h + sy1;
				int dx1 = 0;
				int dx2 = w * sc;
				int dy1 = 0;
				int dy2 = h * sc;
				opG.drawImage(bgImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
						null);
			}
		}
	}

	private void plotStackSingle(double end, double i) {
		float a = (float) (0.5f - (0.5 * i / end));
		// float a = (float) (1 / end);
		opG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
		opG.drawImage(bgImage, 0, 0, w, h, 0, 0, w, h, null);
	}

	private void plotStackPhased(BufferedImage bgImage2, double end, double i) {
		for (int hh = 0; hh < bgImage.getHeight() - 2; hh++) {
			for (int ww = 0; ww < bgImage.getWidth() - 2; ww++) {
				// System.out.println(ww + ":" + hh);
				opG.setColor(getRGB(bgImage, bgImage2, ww, hh));
				opG.fillRect(ww + (int) i, hh, 1, 1);
			}
		}
	}

	protected void saveImage() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src + scName + "Stack.png");
		RendererUtils.savePNGFile(opImage, fFile1);
		opG.dispose();
		System.out.println("created " + fFile1.getName());
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

	private Color getRGB(BufferedImage image1, BufferedImage image2, int x1,
			int y1) {
		int rgb1 = image1.getRGB(x1, y1);
		int r1 = (rgb1 >>> 16) & 0x000000FF;
		int g1 = (rgb1 >>> 8) & 0x000000FF;
		int b1 = (rgb1 >>> 0) & 0x000000FF;

		int rgb2 = image2.getRGB(x1, y1);
		int r2 = (rgb2 >>> 16) & 0x000000FF;
		int g2 = (rgb2 >>> 8) & 0x000000FF;
		int b2 = (rgb2 >>> 0) & 0x000000FF;

		float delta = 25;
		float r = Math.abs(r2 - r1);
		float g = Math.abs(g2 - g1);
		float b = Math.abs(b2 - b1);
		if (r < delta && g < delta && b < delta) {
			return new Color(0, 0, 0, 0);
		} else {
			return new Color(r / 255f, g / 255f, b / 255f, 0.1f);
		}
	}

}
