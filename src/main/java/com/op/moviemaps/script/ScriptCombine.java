package com.op.moviemaps.script;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;

public class ScriptCombine {
	private String scName1 = "ANH";
	private String scName2 = "BR";
	private String scNameTOT = scName1 + "_" + scName2;
	private String dir1 = "scripts/" + scName1 + "/";
	private String dir2 = "scripts/" + scName2 + "/";
	private static ScriptCombine tester;
	private BufferedImage opImage;
	private Graphics2D opG;
	double ww = 0;
	double wmm = 841;
	double hmm = 594;
	double mm2in = 25.4;

	public static void main(String[] args) throws Exception {
		tester = new ScriptCombine();
		tester.drawImages();
		tester.saveImage();
	}

	protected void drawImages() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT;
		File file = new File(src + dir1 + scName1 + "Sc_OUT.jpg");
		BufferedImage image1 = ImageIO.read(file);
		File file2 = new File(src + dir2 + scName2 + "Sc_OUT.jpg");
		BufferedImage image2 = ImageIO.read(file2);
		int w = image1.getWidth();
		ww = (double) w;
		double dpi = ww / (wmm / mm2in);
		int h = (int) (dpi * (hmm / mm2in));
		double w2 = image2.getWidth();
		double h2 = image2.getHeight();
		double sc = ww / w2;
		int y2 = (int) (h - h2 * sc);
		opImage = RendererUtils.createBufferedImage(w, h);
		opG = (Graphics2D) opImage.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		opG.setColor(Color.WHITE);
		opG.fillRect(0, 0, w, h);
		opG.drawImage(image1, 0, 0, null);
		opG.drawImage(image2, 0, y2, (int) (w2 * sc), (int) (h2 * sc), null);
	}

	protected void saveImage() throws Exception {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + "scripts/";
		File fFile1 = new File(src + scNameTOT + ".jpg");
		double dpi = ww / (wmm / mm2in);
		RendererUtils.saveJPGFile(opImage, src + scNameTOT + ".jpg", dpi, 1);
		opG.dispose();
		System.out.println("created " + fFile1.getName());
	}
}
