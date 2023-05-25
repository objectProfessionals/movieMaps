package com.op.moviemaps.script.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;

public class AnimatedLenticularTest {
	private int frSc = 1;
	private int numSecsPerMin = 60;
	private int numPicsPerBlock = 15 * frSc;
	private int numLinesPerImage = -1;
	private int numMinsPerRow = -1;
	private int numRows = -1;
	private int numFrames = -1;
	private int totNumMins = -1;
	private int totMinsToPrint = -1;
	private int stripThickIn = -1;
	private int stripThick = 1;
	String mov = "ANH";
	private String dir = "scripts/" + mov + "/";
	private String fileName = "scene";
	private String srcDir = "scripts/" + mov + "/src/";
	private double wmm = 297;
	private double dpi = -1;
	private int w = 0;
	private int h = 0;
	private int ww = 0;
	private int hh = 0;
	private int wb = 0;
	private int hb = 0;
	private int hInn = 0;
	private int hOff = 0;
	private double ar = -1;
	private double sar = 2.35;
	private String inputType = ".jpg";
	private String outputType = ".jpg";
	private String finishedFileSuffix = "FIN" + outputType;
	private String src = "";
	private BufferedImage opImage;
	private BufferedImage ipImage;
	private double i2mm = 25.4;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		createLenticular();
	}

	public static void createLenticular() {
		try {
			createImageFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void createImageFile() throws IOException, Exception {
		AnimatedLenticularTest mr = new AnimatedLenticularTest();
		mr.createImageFiles();
	}

	public void createImageFiles() throws Exception {
		initData();
		createOutputImage();
	}

	private void initData() throws IOException {
		src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + srcDir;
		File sourceDir = new File(src);
		if (sourceDir.isDirectory()) {
			if (totMinsToPrint > -1) {
				totNumMins = totMinsToPrint;
				numFrames = totNumMins * 60;
			} else {
				numFrames = sourceDir.list().length;
				totNumMins = numFrames / numSecsPerMin;
			}
			int side = (int) Math.sqrt(totNumMins);
			numRows = side;
			numMinsPerRow = side + 1;

			String flName = src + fileName + "00001" + inputType;
			File inputFile = new File(flName);
			ipImage = ImageIO.read(inputFile);
			w = ipImage.getWidth();
			h = ipImage.getHeight();

			hInn = (int) (((double) w) / sar);
			hInn = h;
			hOff = (h - hInn) / 2;

			dpi = 600 * frSc;
			double in = wmm / i2mm;
			ww = (int) (in * dpi);
			ar = (double) w / (double) hInn;
			hh = (int) ((double) ww / ar);
			wb = ww / numMinsPerRow;
			hb = (int) ((double) wb / ar);
			hb = hb - (hb % numPicsPerBlock);

			numLinesPerImage = hb / numPicsPerBlock;
			stripThickIn = hInn / numLinesPerImage;
		}
	}

	private void createOutputImage() throws Exception {
		src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + srcDir;

		opImage = RendererUtils.createBufferedImage(ww, hh);
		Graphics2D opg = (Graphics2D) opImage.getGraphics();
		opg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		opg.setColor(Color.WHITE);
		opg.fillRect(0, 0, ww, hh);

		createStripsH(opg);
		// addAlignmentH(opg);

		src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src + fileName + finishedFileSuffix);
		RendererUtils.saveJPGFile(opImage, src + fileName + finishedFileSuffix, dpi, 1);
		opg.dispose();
		System.out.println("JPG image created : " + fFile1);
	}

	private void createStripsH(Graphics2D opg) throws IOException {
		for (int y = 0; y < numRows; y++) {
			for (int x = 0; x < numMinsPerRow - 1; x++) {
				createMinute(opg, x, y);
			}
		}
	}

	private void createMinute(Graphics2D opg, int col, int row)
			throws IOException {
		int nstep = numSecsPerMin / numPicsPerBlock;
		int minSt = row * numSecsPerMin * numMinsPerRow + col * numSecsPerMin;
		int xOff = (wb * col);
		int rowOff = row * hb;
		for (int n = minSt; n < numFrames && n < minSt + numSecsPerMin; n = n
				+ nstep) {
			String nStr = getFileName((n * 24) + 1); // (n < 10 ? "0" + n : "" +
														// n);
			String flName = src + fileName + nStr + inputType;
			File inputFile = new File(flName);
			ipImage = ImageIO.read(inputFile);

			int secInd = (n - minSt) / nstep;
			int yOff = rowOff + secInd;
			for (int y = 0; y < hInn - stripThickIn; y = y + stripThickIn) {
				BufferedImage clip = ipImage.getSubimage(0, y + hOff, w,
						stripThickIn);

				opg.drawImage(clip, xOff, yOff, wb, stripThick, null);
				if (xOff + wb > ww) {
					System.out.println("ERROR");
				}
				yOff = yOff + (stripThick * numPicsPerBlock);
			}
			xOff = xOff + (wb / numPicsPerBlock);
			System.out.println("Completed image: " + inputFile.getPath());
		}
	}

	private String getFileName(int n) {
		String nm = "";
		if (n < 10) {
			nm = "0000" + n;
		} else if (n < 100) {
			nm = "000" + n;
		} else if (n < 1000) {
			nm = "00" + n;
		} else if (n < 10000) {
			nm = "0" + n;
		} else {
			nm = "" + n;
		}
		return nm;
	}

	private void addAlignmentH(Graphics2D opG) {
		double mm = 2.0;
		int edge = (int) (dpi * (mm / i2mm));
		int d = (stripThick * numPicsPerBlock);
		opG.setColor(Color.BLACK);
		for (int y = 0; y < h; y = y + d) {
			int www = 0;
			if (y == 0 || y + d > h) {
				www = w;
			} else {
				www = edge;
			}
			opG.fillRect(0, y, www, stripThick);
			opG.fillRect(w - www, y, www, stripThick);
		}
	}
}
