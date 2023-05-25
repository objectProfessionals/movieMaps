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

public class ScriptCombinePC {
	private String scName1 = "FCL";
	private String scName2 = "ROTJ";
	private String scNameTOT = scName1 + "_" + scName2;
	private String dir1 = "scripts/" + scName1 + "/";
	private String dir2 = "scripts/" + scName2 + "/";
	private static ScriptCombinePC tester;
	private BufferedImage opImage;
	private Graphics2D opG;
	double ww = 0;
	double wmm = 210;
	double hmm = 297;
	double mm2in = 25.4;

	public static void main(String[] args) throws Exception {
		tester = new ScriptCombinePC();
		tester.drawImages();
		tester.saveImage();
	}

	protected void drawImages() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT;
		File file = new File(src + dir1 + scName1 + "VolumePC.jpg");
		BufferedImage image1 = ImageIO.read(file);
		File file2 = new File(src + dir2 + scName2 + "VolumePC.jpg");
		BufferedImage image2 = ImageIO.read(file2);
		int w = image1.getWidth();
		ww = w;
		int h = image1.getHeight() * 2;
		int y1 = image1.getHeight();
		opImage = RendererUtils.createBufferedImage(w, h);
		opG = (Graphics2D) opImage.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		opG.setColor(Color.WHITE);
		opG.fillRect(0, 0, w, h);
		opG.drawImage(image1, 0, 0, null);

		int w2 = image2.getWidth();
		int h2 = image2.getHeight();
		double sc = ww / w2;

		opG.drawImage(image2, 0, y1, (int) (w2 * sc), (int) (h2 * sc), null);
	}

	protected void saveImage() throws Exception {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + "scripts/";
		double dpi = ww / (wmm / mm2in);
		RendererUtils.saveJPGFile(opImage, src + scNameTOT + ".jpg", dpi, 1);
		opG.dispose();
		System.out.println("created " + src + scNameTOT + ".jpg");
	}
}
