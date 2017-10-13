package com.op.moviemaps.script;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;

public class PlotLogo {
	private static final int CAP = BasicStroke.CAP_ROUND;
	protected BufferedImage bgImage;
	private String scName = "ANH";
	private String dir = "scripts/" + scName + "/";
	int w = 1500;
	int h = 1000;
	private static PlotLogo tester;
	private BufferedImage opImage;
	private Graphics2D opG;
	private String fontName = "ROUNDED.TTF";

	public static void main(String[] args) throws IOException,
			FontFormatException {
		tester = new PlotLogo();
		tester.plot();
		tester.saveImage();
	}

	protected void plot() throws IOException, FontFormatException {
		opImage = RendererUtils.createAlphaBufferedImage(w, h);
		opG = (Graphics2D) opImage.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		opG.setColor(Color.WHITE);
		opG.fillRect(0, 0, w, h);
		plotLogo();
	}

	private void plotLogo() throws FontFormatException, IOException {
		float a = 1f;
		int cx = w / 2;
		int cy = 150 + h / 2;
		float str = 100;

		// plotAsLines(a, cx, cy, str);
		plotAsCurves(a, cx - 200, cy, str);

		// plotAsSine(a, cx, cy, str);
	}

	private void plotAsLines(float a, int cx, int cy, float str)
			throws FileNotFoundException, FontFormatException, IOException {
		plotBG(a, cx);
		int xOff = 200;
		plotTop(a, cx - xOff, cy, str);
		plotBot(a, cx - xOff, cy, str);
		plotText((int) str * 2, cy, str, 7);
	}

	private void plotAsCurves(float a, int cx, int cy, float str)
			throws FileNotFoundException, FontFormatException, IOException {
		plotBGC(a, cx);
		// plotStrip();
		// str = 50;
		int xOff = 150;
		plotCTop(a, cx - xOff, cy, str, 150);
		plotCBot(a, cx - xOff, cy, str, 150 - (int) str);
		plotTextC((int) str, cy - 345, str, 16);
	}

	private void plotStrip() {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src + "/strip.jpg");
		try {
			BufferedImage bImage = ImageIO.read(fFile1);
			opG.drawImage(bImage, null, 0, 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void plotCTop(float a, int cx, int cy, float str, int rad) {
		int s2 = (int) (str * 0.5);
		float[] dashes2 = { 1500, str, s2, str, s2, str, s2, str, s2, str, s2,
				str, s2, str, s2, str, s2 };
		Color t = Color.decode("#274CA0");
		opG.setStroke(new BasicStroke(str, CAP, BasicStroke.JOIN_ROUND, 1f,
				dashes2, 0f));
		opG.setColor(getColorWithAlpha(t, a));

		Path2D.Double path2 = new Path2D.Double();
		path2.moveTo(str, h - (cy - 200));
		path2.lineTo(cx, h - (cy - 200));
		path2.lineTo(cx, h - (cy));
		Arc2D.Double arc1 = new Arc2D.Double(new Rectangle(cx, h - (cy + rad),
				rad * 2, rad * 2), 180, -180, Arc2D.OPEN);
		path2.append(arc1, true);
		Arc2D.Double arc2 = new Arc2D.Double(new Rectangle(cx + 2 * rad, h
				- (cy + rad), rad * 2, rad * 2), 180, -180, Arc2D.OPEN);
		path2.append(arc2, true);
		path2.lineTo(cx + 4 * rad, h - (cy - 550));

		opG.draw(path2);

	}

	private void plotCBot(float a, int cx, int cy, float str, int rad) {
		int s2 = (int) (str * 0.5);
		float[] dashes2 = { 1677, str, s2, str, s2, str, s2, str, s2, str, s2,
				str, s2, str, s2, str, s2, str, s2 };
		Color t = Color.decode("#EFAC26");
		opG.setStroke(new BasicStroke(str, CAP, BasicStroke.JOIN_ROUND, 1f,
				dashes2, 0f));
		opG.setColor(getColorWithAlpha(t, a));

		Path2D.Double path2 = new Path2D.Double();
		path2.moveTo(str, h - (cy - 200 - str));
		path2.lineTo(cx + str, h - (cy - 200 - str));
		path2.lineTo(cx + str, h - (cy));
		Arc2D.Double arc1 = new Arc2D.Double(new Rectangle(cx + (int) str, h
				- (cy + rad), rad * 2, rad * 2), 180, -180, Arc2D.OPEN);
		path2.append(arc1, true);

		Arc2D.Double arc2 = new Arc2D.Double(new Rectangle(cx + (int) str + 2
				* rad, h - (cy + (int) str), (int) str * 2, (int) str * 2),
				180, 180, Arc2D.OPEN);
		path2.append(arc2, true);

		Arc2D.Double arc3 = new Arc2D.Double(new Rectangle(cx + (int) str + 2
				* rad + 2 * (int) str, h - (cy + rad), rad * 2, rad * 2), 180,
				-180, Arc2D.OPEN);
		path2.append(arc3, true);

		path2.lineTo(cx + (int) str + 4 * rad + 2 * (int) str, h
				- (cy - 200 - str));
		path2.lineTo(w - rad, h - (cy - 200 - str));

		opG.draw(path2);
	}

	private void plotBGC(float a, int cx) {
		double bo = 0.1;
		double bw = w * bo;
		double bh = h * bo;
		double arc = w * 0.25;
		Color l = Color.decode("#574949");
		Color r = Color.decode("#847474");
		Rectangle2D.Double whole = new Rectangle2D.Double(bw, bh, w - 2 * bw, h
				- 2 * bh);
		opG.setColor(Color.BLACK);
		opG.fill(whole);

		opG.setColor(Color.WHITE);
		opG.fillArc((int) (-w * 1), (int) (-h * 3.0), (int) (w * 3.0),
				(int) (h * 3.2), 180, 180);
		opG.fillArc((int) (-w * 1), (int) (h * 0.8), (int) (w * 3.0),
				(int) (h * 3.2), 0, 180);

		opG.setClip(null);
	}

	private void plotBGC1(float a, int cx) {
		double bo = 0.1;
		double bw = w * bo;
		double bh = h * bo;
		double arc = w * 0.25;
		Rectangle2D.Double whole = new Rectangle2D.Double(0, 0, w, h);
		RoundRectangle2D.Double round = new RoundRectangle2D.Double(bw, bh, w
				- 2 * bw, h - 2 * bh, arc, arc);

		Color l = Color.decode("#574949");
		Color r = Color.decode("#847474");
		int xs = (int) (((double) w) * 0.47);
		opG.setClip(round);
		opG.setColor(getColorWithAlpha(l, a));
		opG.fillRect(0, 0, xs, h);
		opG.setColor(getColorWithAlpha(r, a));
		opG.fillRect(xs, 0, w - xs, h);

		opG.setClip(null);
	}

	private void plotBG(float a, int cx) {
		double bo = 0.1;
		double bw = w * bo;
		double bh = h * bo;
		double arc = w * 0.25;
		Rectangle2D.Double whole = new Rectangle2D.Double(0, 0, w, h);
		RoundRectangle2D.Double round = new RoundRectangle2D.Double(bw, bh, w
				- 2 * bw, h - 2 * bh, arc, arc);
		// Area aWhole = new Area(whole);
		// Area aRound = new Area(round);
		// aWhole.subtract(aRound);
		// opG.setClip(aWhole);
		// opG.setColor(Color.DARK_GRAY);
		// opG.fillRect(0, 0, w, h);

		Color l = Color.decode("#9e2680");
		Color r = Color.decode("#D68734");
		int xs = (int) (((double) w) * 0.33);
		opG.setClip(round);
		opG.setColor(getColorWithAlpha(l, a));
		opG.fillRect(0, 0, xs, h);
		opG.setColor(getColorWithAlpha(r, a));
		opG.fillRect(xs, 0, w - xs, h);

		opG.setClip(null);
	}

	private void plotText(int xs, int cy, float str, float f)
			throws FileNotFoundException, FontFormatException, IOException {
		String fName = SharedUtils.HOST_MODULE + SharedUtils.DIR_FONTS
				+ fontName;
		InputStream is = new BufferedInputStream(new FileInputStream(fName));
		Font font = Font.createFont(Font.TRUETYPE_FONT, is);
		font = font.deriveFont((float) 10);
		opG.setFont(font);
		plot(xs, h - (cy - 200), f, "movie");
		plot(xs, h - (cy - 200 - str), f, "maps");
	}

	private void plotTextC(int xs, int cy, float str, float f)
			throws FileNotFoundException, FontFormatException, IOException {
		String fName = SharedUtils.HOST_MODULE + SharedUtils.DIR_FONTS
				+ fontName;
		InputStream is = new BufferedInputStream(new FileInputStream(fName));
		Font font = Font.createFont(Font.TRUETYPE_FONT, is);
		font = font.deriveFont((float) 10);
		opG.setFont(font);
		plot(xs, h - (cy + 3 * f), f, "movie          maps");
	}

	private void plotTop(float a, int cx, int cy, float str) {
		float[] dashes2 = { 1500, str, str, str, str, str, str, str, str };
		Color t = Color.decode("#314492");
		opG.setStroke(new BasicStroke(str, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND, 1f, dashes2, 0f));
		opG.setColor(getColorWithAlpha(t, a));

		Path2D.Double path2 = new Path2D.Double();
		path2.moveTo(str, h - (cy - 200));
		path2.lineTo(cx, h - (cy - 200));
		path2.lineTo(cx, h - (cy + 200));
		path2.lineTo(cx + 200, h - (cy));
		path2.lineTo(cx + 400, h - (cy + 200));
		path2.lineTo(cx + 400, h);
		opG.draw(path2);
	}

	private void plotBot(float a, int cx, int cy, float str) {
		Color b = Color.decode("#020100");
		float[] dashes = { 1700, str, str, str, str, str, str };
		opG.setStroke(new BasicStroke(str, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND, 1f, dashes, 0f));
		opG.setColor(getColorWithAlpha(b, a));
		double yd = str * 2.35;
		double yd2 = str * 1.25;
		Path2D.Double path = new Path2D.Double();
		path.moveTo(str, h - (cy - 200 - str));
		path.lineTo(cx + str, h - (cy - 200 - str));
		path.lineTo(cx + str, h - (cy + 200 - yd));
		path.lineTo(cx + 200, h - (cy - yd2));
		path.lineTo(cx + 400 - str, h - (cy + 200 - yd));
		path.lineTo(cx + 400 - str, h - (cy - 200 - str));
		path.lineTo(cx + 800, h - (cy - 200 - str));
		opG.draw(path);
	}

	protected void saveImage() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src + scName + "Logo.png");
		RendererUtils.savePNGFile(opImage, fFile1);
		opG.dispose();
		System.out.println("created " + fFile1);
	}

	protected Color getColorWithAlpha(Color col2, float a) {
		Color col = new Color(((float) col2.getRed()) / 255f,
				((float) col2.getGreen()) / 255f,
				((float) col2.getBlue()) / 255f, a);
		return col;
	}

	private void plot(double x, double y, double scale, String name) {
		opG.setColor(Color.WHITE);
		GlyphVector gv = opG.getFont().createGlyphVector(
				opG.getFontRenderContext(), name);
		Shape glyph = gv.getOutline();
		AffineTransform at = new AffineTransform();
		AffineTransform sc = AffineTransform.getScaleInstance(scale, scale);
		AffineTransform tr = AffineTransform.getTranslateInstance(x, y);
		AffineTransform tr1 = AffineTransform
				.getTranslateInstance(0, 3 * scale);

		at.concatenate(tr1);
		at.concatenate(tr);
		at.concatenate(sc);
		Shape transformedGlyph = at.createTransformedShape(glyph);
		opG.fill(transformedGlyph);
	}

	private void plotAsSine(float a, int cx, int cy, float str) {
		double rad = str * 3;
		double xs = str * 3;
		double ys = cy - rad * 0.5;
		double wf = ((double) w * 0.5);
		plotSine(rad, wf, xs, ys, str, cx, cy, a, "#314492");
		plotSine(rad * 0.5, wf, xs, ys - str, str, cx, cy, a, "#020100");
	}

	private void plotSine(double rad, double wf, double xs, double ys,
			float str, int cx, int cy, float a, String col) {
		float[] dashes2 = { 1000, str, str, str, str, str, str, str, str };
		Color t = Color.decode(col);
		opG.setStroke(new BasicStroke(str, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND, 1f, dashes2, 0f));
		opG.setColor(getColorWithAlpha(t, a));

		Path2D.Double path2 = new Path2D.Double();
		path2.moveTo(str, h - ys);
		path2.lineTo(xs, h - ys);
		for (double b = 0; b <= 2 * Math.PI; b = b + Math.PI * 0.1) {
			double x = b * wf / (2 * Math.PI);
			double y = Math.abs(Math.sin(b) * rad);
			path2.lineTo(xs + x, h - (ys + y));
		}
		path2.lineTo(w, h - ys);

		opG.draw(path2);
	}

}
