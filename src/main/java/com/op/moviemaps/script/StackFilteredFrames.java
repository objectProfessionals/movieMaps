package com.op.moviemaps.script;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.ChromeFilter;
import com.jhlabs.image.DoGFilter;
import com.jhlabs.image.EdgeFilter;
import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.LaplaceFilter;
import com.jhlabs.image.PerspectiveFilter;
import com.jhlabs.image.ShearFilter;
import com.jhlabs.image.StampFilter;
import com.jhlabs.image.ThresholdFilter;
import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;

public class StackFilteredFrames {
	protected BufferedImage bgImage;
	private String scName = "ANH";
	private String dir = "scripts/" + scName + "/";
	int w = 1024;
	int h = 576;
	private static StackFilteredFrames tester;
	private BufferedImage opImage;
	private Graphics2D opG;
	ArrayList<AbstractBufferedImageOp> filters = new ArrayList<AbstractBufferedImageOp>();
	ArrayList<AbstractBufferedImageOp> transforms = new ArrayList<AbstractBufferedImageOp>();

	public static void main(String[] args) throws IOException,
			FontFormatException {
		tester = new StackFilteredFrames();
		tester.plotBackground();
		tester.saveImage();
	}

	protected void plotBackground() throws IOException {
		int sta = 1000;
		int end = 1001;
		float a = 0.5f;
		int num = end - sta;
		opImage = RendererUtils.createAlphaBufferedImage(w + num, h);
		opG = (Graphics2D) opImage.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		opG.setColor(Color.WHITE);
		opG.fillRect(0, 0, w + num, h + num);
		opG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));

		// setupDOGFilters();
		// setupEdgeFilters();
		// setupEdgeThresholdFilters(50, 150);// 10
		// setupStampFilters(0.25f);// 10
		// setupBWFilters(10, 100);// 10
		// setupLaplaceFilters();// 10
		// setupChromeFilters();

		setupPerspTransforms();
		// setupShearTransforms();
		stackFromSrc(sta, end);
		// stackFromFrames(sta, end);
	}

	private void setupPerspTransforms() {
		float d = 200f;
		float e = 500f;
		PerspectiveFilter op = new PerspectiveFilter();
		op.setCorners(0, 0, w - d, d, w - d, h - d, 0, h);
		op.setClip(true);
		System.out.println(op.getOriginX() + "," + op.getOriginY());
		transforms.add(op);
	}

	private void setupShearTransforms() {
		float d = 0.5f;
		float e = 0.95f;
		ShearFilter op = new ShearFilter();
		float x = (float) Math.PI * 0.1f;
		float y = (float) Math.PI * 0.1f;
		op.setXAngle(x);
		op.setYAngle(0);
		transforms.add(op);
	}

	private void stackFromFrames(int sta, int end) throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File inputFile = new File(src + scName + "Frames.jpg");
		bgImage = ImageIO.read(inputFile);

		for (int i = (int) sta; i < end; i++) {
			stackFromFrame(sta, i);
		}
	}

	private void stackFromSrc(int sta, int end) throws IOException {
		opG.setColor(Color.WHITE);
		opG.fillRect(0, 0, w, h);
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir + "src/";
		File dirF = new File(src);
		if (dirF.isDirectory()) {
			File[] files = dirF.listFiles();
			PlotFrames.sort(files, false);
			bgImage = ImageIO.read(files[0]);

			if (end == -1) {
				end = files.length;
			}
			for (int i = sta; i < end; i++) {
				bgImage = ImageIO.read(files[i]);
				if (bgImage == null
						|| (bgImage.getWidth() == 0 && bgImage.getHeight() == 0)) {
					continue;
				}
				BufferedImage retImage = applyFilters(bgImage);
				retImage = applyTransforms(retImage);

				int dx1 = (int) (i - sta);
				int dx2 = w;
				int dy1 = 0;
				int dy2 = h;
				opG.drawImage(retImage, dx1, dy1, dx2, dy2, null);
				System.out.println("drawn " + files[i]);
			}
		}
	}

	private BufferedImage applyTransforms(BufferedImage subimage) {
		BufferedImage retimage = null;
		for (AbstractBufferedImageOp op : transforms) {
			retimage = op.filter(subimage, null);
			subimage = retimage;
		}
		return retimage;
	}

	private void stackFromFrame(int sta, int i) {
		int[] arr = getDimsFromFrames(i, sta);
		int dx1 = arr[0];
		int dy1 = arr[1];
		int sx1 = arr[2];
		int sy1 = arr[3];
		BufferedImage fImage = filterFromFrames(i, sx1, sy1, w, h);
		opG.drawImage(fImage, dx1, dy1, w, h, null);
	}

	private int[] getDimsFromFrames(int i, int sta) {
		int st = (int) (i);
		int sx1 = (st * w) % 3840;
		int sy1 = (st / (3840 / w)) * h;
		int dx1 = i - sta;
		int dy1 = 0;

		int[] arr = { dx1, dy1, sx1, sy1 };
		return arr;
	}

	private void setupDOGFilters() {
		DoGFilter op = new DoGFilter();
		op.setRadius1(1f);
		op.setRadius2(2f);
		filters.add(op);
	}

	private void setupEdgeFilters() {
		EdgeFilter op = new EdgeFilter();
		InvertFilter op2 = new InvertFilter();
		filters.add(op);
		filters.add(op2);
	}

	private void setupEdgeThresholdFilters(int th, int th2) {
		EdgeFilter op = new EdgeFilter();
		InvertFilter op2 = new InvertFilter();
		ThresholdFilter op3 = new ThresholdFilter();
		StampFilter op4 = new StampFilter();
		op3.setLowerThreshold(th);
		op3.setUpperThreshold(th2);
		filters.add(op);
		filters.add(op2);
		filters.add(op4);
	}

	private void setupStampFilters(float th1) {
		StampFilter op = new StampFilter(th1);
		filters.add(op);
	}

	private void setupChromeFilters() {
		ChromeFilter op = new ChromeFilter();
		InvertFilter op2 = new InvertFilter();
		filters.add(op);
		filters.add(op2);
	}

	private void setupBWFilters(int th, int th2) {
		ThresholdFilter op = new ThresholdFilter();
		op.setLowerThreshold(th);
		op.setUpperThreshold(th2);
		InvertFilter op2 = new InvertFilter();
		filters.add(op);
		// filters.add(op2);
	}

	private void setupLaplaceFilters() {
		LaplaceFilter op = new LaplaceFilter();
		InvertFilter op2 = new InvertFilter();
		filters.add(op);
		// filters.add(op2);
	}

	private BufferedImage filterFromFrames(int i, int sx1, int sy1, int sx2,
			int sy2) {
		System.out.println("filtered " + i);
		BufferedImage subimage = bgImage.getSubimage(sx1, sy1, w, h);
		BufferedImage retimage = applyFilters(subimage);
		return retimage;
	}

	private BufferedImage applyFilters(BufferedImage subimage) {
		BufferedImage retimage = filters.isEmpty() ? subimage : null;
		for (AbstractBufferedImageOp op : filters) {
			retimage = op.filter(subimage, subimage);
			subimage = retimage;
		}
		return retimage;
	}

	protected void saveImage() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src + scName + "FStack.png");
		RendererUtils.savePNGFile(opImage, fFile1);
		opG.dispose();
		System.out.println("created " + fFile1.getName());
	}
}
