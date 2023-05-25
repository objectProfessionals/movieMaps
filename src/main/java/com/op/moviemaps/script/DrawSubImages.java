package com.op.moviemaps.script;

import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import javax.imageio.ImageIO;

import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;

public class DrawSubImages {
	protected BufferedImage bgImage;
	private String scName = "GAN";
	private String dir = "scripts/" + scName + "/";
	boolean bySrc = true;
	private static DrawSubImages tester;
	private BufferedImage opImage;
	private Graphics2D opG;
	boolean blur = false;
	boolean outline = false;
	boolean clip = true;

	public static void main(String[] args) throws Exception,
			FontFormatException {
		tester = new DrawSubImages();
		tester.draw();
	}

	protected void draw() throws Exception {
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

				int end = files.length;
				// int end = 10;
				for (int i = 0; i < end; i++) {
					// for (int i = 1000; i < 1500; i++) {
					System.out.println("draw " + files[i].toString() + "   "
							+ i + "/" + files.length);
					bgImage = ImageIO.read(files[i]);

					int w = bgImage.getWidth();
					int h = bgImage.getHeight();
					int ww = w / 8;
					int hh = h / 8;

					opImage = RendererUtils.createBufferedImage(ww, hh);
					opG = (Graphics2D) opImage.getGraphics();
					opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);

					opG.drawImage(bgImage, 0, 0, ww, hh, 0, 0, w, h, null);
					saveImage(opImage, files[i].getName());
				}
			}
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

	protected void saveImage(BufferedImage im, String name) throws Exception {
		System.out.println("Saving - " + name);
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir + "srcA/";
		File fFile1 = new File(src + name);
		RendererUtils.saveJPGFile(opImage, src + name, 72, 1);
		opG.dispose();
		System.out.println("Saved " + fFile1.getPath());
	}
}
