package com.op.moviemaps.script;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;

public class ScriptContactSheet {
	private String[] scNames = { "ANH", "ESB", "ROTJ", "PUL", "BR", "DKR",
			"FCL", "FOTR", "GDF", "INC", "INCP", "JAW", "MAT", "POA", "RLA",
			"T2" };
	private String[] scDescs = { "Star Wars IV - A New Hope",
			"Star Wars IV - The Empire Stries Back",
			"Star Wars IV - Return of the Jedi", "Pulp Fiction",
			"Blade Runner", "The Dark Knight Returns", "Fight Club",
			"Lord of the Rings - Fellowship of the Ring", "The Godfather",
			"The Incredibles", "Inception", "Jaws", "The Matrix",
			"Harry Potter and the Prisoner of Azkaban",
			"Raiders of the Lost Ark", "Terminator 2" };
	private String scNameTOT = "ThankYou";
	private String dir = "scripts/kickstarter/";
	private String fontName = "SLIMBOLD.TTF";
	private static ScriptContactSheet tester;
	private BufferedImage opImage;
	private Graphics2D opG;
	double ww = 0;
	double wmm = 841;
	double hmm = 594;
	double mm2in = 25.4;
	Color fCol = Color.decode("#050c3f");

	public static void main(String[] args) throws Exception {
		tester = new ScriptContactSheet();
		tester.drawImages();
		tester.saveImage();
	}

	protected void drawImages() throws IOException, FontFormatException {
		int w = (int) (300 * 210 / 25.4);
		int h = (int) (300 * 297 / 25.4);
		opImage = RendererUtils.createBufferedImage(w, h);
		opG = (Graphics2D) opImage.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		opG.setColor(Color.WHITE);
		opG.fillRect(0, 0, w, h);

		String fName = SharedUtils.HOST_MODULE + SharedUtils.DIR_FONTS
				+ fontName;
		InputStream is = new BufferedInputStream(new FileInputStream(fName));
		Font font = Font.createFont(Font.TRUETYPE_FONT, is);

		drawText(font);
		drawScripts(font);
		drawPCs(font);

	}

	private void drawText(Font font) {
		int ySt = 175;
		float f1 = 83;
		float f2 = f1 * 0.66f;
		font = font.deriveFont(f1);
		opG.setFont(font);
		opG.setColor(fCol);
		String text = "Thank you for supporting our MovieMaps Kickstarter project";
		opG.drawString(text, 140, ySt);

		float ff = 1.5f;
		font = font.deriveFont(f2);
		opG.setFont(font);
		text = "We'd love to hear from you again";
		opG.drawString(text, 140, ySt + (int) (f1));

		text = "You can reach us at contact@readMyArt.com";
		opG.drawString(text, 140, ySt + (int) (f1 + f2));

		text = "Here's a reminder of some of the prints currently in our library";
		opG.drawString(text, 140, ySt + (int) (f1 + f2 * 2));

		text = "And keep sending us more of your great suggestions!";
		opG.drawString(text, 140, ySt + (int) (f1 + f2 * 3));
	}

	private void drawScripts(Font font) throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT;

		int hOff = 400;
		int cols = 4;
		int mar = 100;
		int wi = 1777;
		int hi = 1000;
		double f = 0.32;
		double fs = 75;
		int xOff = (int) (120 * f);
		int fOff = (int) (20 * f);

		font = font.deriveFont((float) (fs * f));
		opG.setFont(font);

		opG.setColor(fCol);
		for (int i = 0; i < scNames.length; i++) {
			String scName = scNames[i];
			String scDesc = scDescs[i];

			File file = new File(src + dir + "Frame2" + scName + ".jpg");
			BufferedImage image2 = ImageIO.read(file);

			int ww = (int) (wi * f);
			int hh = (int) (hi * f);
			int x = mar + (i % cols) * ww;
			int y = mar + (i / cols) * hh;

			opG.drawImage(image2, x, y + hOff, ww, hh, null);

			opG.drawString(scDesc, x + xOff, y + hh + hOff - fOff);

			System.out.println("Script drawn=" + scDesc);
		}
	}

	private void drawPCs(Font font) throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT;

		int hOff = 1775;
		int cols = 4;
		int mar = 32;
		int wi = 3008;
		int hi = 2000;
		double f = 0.197;
		double fs = 100;
		int xOff = (int) (666 * f);
		int fOff = (int) (30 * f);
		font = font.deriveFont((float) (fs * f));
		opG.setFont(font);

		opG.setColor(fCol);
		for (int i = 0; i < scNames.length; i++) {
			String scName = scNames[i];
			String scDesc = scDescs[i];

			File file = new File(src + dir + "Frame-PC-" + scName + ".jpg");
			BufferedImage image2 = ImageIO.read(file);

			int ww = (int) (wi * f);
			int hh = (int) (hi * f);
			int x = mar + (i % cols) * ww;
			int y = mar + (i / cols) * hh;

			opG.drawImage(image2, x, y + hOff, ww, hh, null);

			opG.drawString(scDesc, x + xOff, y + hh + hOff - fOff);
			System.out.println("PC drawn=" + scDesc);
		}
	}

	protected void saveImage() throws Exception {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src + scNameTOT + ".jpg");
		RendererUtils.saveJPGFile(opImage, src + scNameTOT + ".jpg", 300, 1);
		opG.dispose();
		System.out.println("created " + fFile1.getName());
	}
}
