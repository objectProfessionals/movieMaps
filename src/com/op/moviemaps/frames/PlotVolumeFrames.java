package com.op.moviemaps.frames;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import com.op.moviemaps.script.ScriptPath;
import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;
import com.op.moviemaps.sound.WaveFileReader;

public class PlotVolumeFrames {
	protected BufferedImage bgImage;
	private static final String scName = "WALL";
	private static final String wav = scName + ".wav";
	private static final String dir = "scripts/" + scName + "/";
	private static final String bgFile = "Paint.jpg";
	private static final String refDir = "src/";
	private static final String outFile = "Volume";
	private static final String scaledPCFile = "Scaled-PC-";
	private static final String outFileExt = ".jpg";
	private static int TYPE_ORIG = 0;
	private static int TYPE_A4 = 1;
	private static int TYPE_PC = 2;
	private int type = TYPE_A4;
	private static final String[] TYPES = { "Orig", "A4", "PC" };

	boolean framePCorA4 = false;
	private static final String framePC = "Frame-PC";
	Rectangle2D frame2 = new Rectangle2D.Double(1066, 583, 1125, 890);
	private static final String frameA4 = "Frame3-A4";
	Rectangle2D frame3A4 = new Rectangle2D.Double(439, 362, 1124, 795);

	private static PlotVolumeFrames tester;
	private WaveFileReader reader;
	double ar = 2.4;
	private int w = -1;
	private double max = -1;
	int h = -1;
	private BufferedImage opImage;
	private Graphics2D opG;
	boolean linesOrBars = false;
	boolean linOrCurve = true;
	double samples = 8000;
	float barNums = 4;
	private double hF = 1; // for MID = 1, TOP = 0.75, BOT = 0.25
	private double dpi = -1;
	private boolean drawTitle = true;
	private double barF = 0.5; // 0.33
	private double fScale = -1;

	public static void main(String[] args) throws Exception,
			FontFormatException {
		tester = new PlotVolumeFrames();
		tester.createWavData();
		tester.initData();
		tester.drawBackground();
		tester.drawVolume();
		tester.saveImage();
	}

	private void initData() {
		if (type == TYPE_ORIG) {
			hF = 1;
		} else if (type == TYPE_A4) {
			double ww = 297.0;
			double hh = 210.0;
			double bb = 5.0;
			ar = (ww - 2 * bb) / (hh - 2 * bb);
			hF = 0.25;
			fScale = 5;
		} else if (type == TYPE_PC) {
			ar = 148.0 / 105.0;
			hF = 0.25;
			fScale = 10;
		}
	}

	private void createWavData() {
		System.out.println("reading...");
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		reader = new WaveFileReader(src + wav);
		for (int i = 0; i < reader.getData()[0].length; i++) {
			int r = Math.abs(reader.getData()[0][i]);
			int l = Math.abs(reader.getData()[1][i]);
			int rl = (r + l) / 2;
			if (rl > max) {
				max = rl;
			}
		}
	}

	protected void drawBackground() throws IOException {
		System.out.println("Creating...");
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File direc = new File(src + refDir);
		File file = direc.listFiles()[0];
		BufferedImage b2 = ImageIO.read(file);
		int wSrc = b2.getWidth();
		int hSrc = b2.getHeight();
		src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		file = new File(src + scName + bgFile);
		b2 = ImageIO.read(file);
		bgImage = new BufferedImage(wSrc, hSrc, BufferedImage.TYPE_INT_RGB);
		w = (int) (b2.getWidth());
		h = (int) (((double) w) / ar);
		opImage = RendererUtils.createBufferedImage(w, h);
		opG = (Graphics2D) opImage.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		opG.setColor(Color.BLACK);
		opG.fillRect(0, 0, w, h);
		opG.drawImage(b2, 0, 0, w, h, 0, 0, w, b2.getHeight(), null);
		opG.setColor(Color.WHITE);
	}

	private void drawVolume() {
		System.out.println("Drawing...");
		int end = (int) (reader.getDataLen() / samples);
		ArrayList<Point2D> p = new ArrayList<Point2D>();
		for (int i = 0; i < end; i = i + 1) {
			plotLines(i, p);
		}
		double wF = ((double) w) / ((double) end);
		if (linesOrBars) {
			drawAsLines(p);
		} else {
			drawAsBars(p, wF);
		}
	}

	private void drawAsBars(ArrayList<Point2D> ps, double wF) {
		opG.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND, 1f, null, 0f));
		int hM = (int) (((double) h) * hF);
		int cy = h - hM / 2;
		for (int i = 0; i < ps.size(); i = i + 1) {
			Point2D p1 = ps.get(i);
			int x = (int) (((double) p1.getX()) * wF);
			int y = (int) p1.getY();
			for (float f = 1; f > 0; f = f - 1f / barNums) {
				float pf = (float) Math.pow(f, 1.1);
				float cf = (float) Math.pow(f, 1.0);
				Color col = new Color(cf, cf, cf);
				int yy = (int) ((float) y * pf * hF);
				opG.setColor(col);
				opG.drawLine(x, cy, x, cy + yy);
				opG.drawLine(x, cy, x, cy - yy);
			}
		}
	}

	private void drawAsLines(ArrayList<Point2D> p) {
		opG.setStroke(new BasicStroke(50f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND, 1f, null, 0f));
		opG.draw(convertToCurve(p, false));
		opG.draw(convertToCurve(p, true));
	}

	private Shape convertToCurve(ArrayList<Point2D> ps, boolean mirror) {
		int m = 10;
		Path2D.Double path = new Path2D.Double();
		for (int i = 0; i < ps.size() - 3; i = i + 3 * m) {
			Point2D p1 = ps.get(i);
			Point2D p2 = ps.get(i + 1);
			Point2D p3 = ps.get(i + 2);
			if (path.getCurrentPoint() == null) {
				path.moveTo(p1.getX(), p1.getY());
				i = i - 2;
			} else {
				if (linOrCurve) {
					double y = p1.getY();
					path.lineTo(p3.getX(), y);
				} else {
					path.curveTo(p1.getX(), p1.getY(), p2.getX(), p2.getY(),
							p3.getX(), p3.getY());
				}
			}
		}
		if (mirror) {
			AffineTransform tr = AffineTransform.getTranslateInstance(0, h);
			AffineTransform mi = AffineTransform.getScaleInstance(1, -1);
			AffineTransform at = new AffineTransform();
			at.concatenate(tr);
			at.concatenate(mi);
			Shape p2 = path.createTransformedShape(at);
			return p2;
		}
		return path;
	}

	private void plotLines(int i, ArrayList<Point2D> p) {
		double pow = 1.1;
		// double pow = 1.5;
		int dx1 = (int) (i);
		double hh = (double) h;
		double e = getWavHeight(i);
		double v = Math.pow(e, pow) * hh * barF;
		p.add(new Point2D.Double(dx1, v));
	}

	private double getWavHeight(int i) {
		int ii = i * (int) samples;
		double n1 = getAverage(ii, 0); // reader.getData()[0][ii];
		double n2 = getAverage(ii, 1); // reader.getData()[0][ii];
		double hh = (n1 + n2) / 2.0;
		// return (hh);
		return hh;
	}

	private double getAverage(int i, int p) {
		double tot = 0;
		double mx = 0;
		for (int ii = i; ii < i + samples; ii++) {
			double val = Math.abs(reader.getData()[p][ii]);
			tot = tot + val;
			if (val > mx) {
				mx = val;
			}
		}
		return mx / (max);
	}

	protected void saveImage() throws Exception {
		System.out.println("Saving...");
		BufferedImage finalImage = null;
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		if (type == TYPE_ORIG) {
			finalImage = opImage;
			double ww = opImage.getWidth();
			double inches = 500.0 / 25.4;
			dpi = ((double) ww) / inches;
		} else if (type == TYPE_A4) {
			double wmm = 297.0;
			double hmm = 210.0;
			double bmm = 5.0;
			finalImage = addBorderToA4Image(bmm, finalImage, wmm, hmm);
		} else if (type == TYPE_PC) {
			double wmm = 148.0;
			double fac = 0.1;
			// BufferedImage scaled = addScaledPCImage(fac, finalImage, wmm);
			// finalImage = addBorderToPCImage(fac, finalImage, wmm);
			// File fFile1 = new File(src + scaledPCFile + scName + outFileExt);
			// RendererUtils.saveJPGFile(scaled, fFile1, 72);
		}
		File fFile1 = new File(src + scName + outFile + getTypeString()
				+ outFileExt);
		// RendererUtils.savePNGFile(finalImage, fFile1);
		RendererUtils.saveJPGFile(finalImage, src + scName + outFile + getTypeString()
				+ outFileExt, dpi, 1);
		if (type == TYPE_PC || type == TYPE_A4) {
			createWithFrame();
		}
		opG.dispose();
		printFileInfo(fFile1);
	}

	private void printFileInfo(File fFile1) {
		Date now = new Date();
		System.out.println("Saved " + fFile1.getPath() + " @" + now);
	}

	private void createWithFrame() throws Exception {
		String source = framePCorA4 ? framePC : frameA4;
		Rectangle2D rect = framePCorA4 ? frame2 : frame3A4;
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File inputFile = new File(src + "../" + source + ".jpg");
		BufferedImage fr2Image = ImageIO.read(inputFile);
		Graphics2D fr2G = (Graphics2D) fr2Image.getGraphics();
		int x1 = (int) rect.getX();
		int y1 = (int) rect.getY();
		int w1 = (int) rect.getWidth();
		int h1 = (int) rect.getHeight();
		fr2G.drawImage(opImage, x1, y1, w1, h1, null);
		File opFile = new File(src + source + "-" + scName + ".jpg");
		RendererUtils.saveJPGFile(fr2Image, src + source + "-" + scName + ".jpg", 72, 1);
		printFileInfo(opFile);
	}

	private BufferedImage addBorderToA4Image(double bmm,
			BufferedImage finalImage, double wmm, double hmm) throws Exception {
		double width = (double) opImage.getWidth();
		double height = (double) opImage.getHeight();

		double fac = 0.025;
		double bi = (((double) opImage.getWidth()) * fac);
		drawTitle(bi, opImage.getHeight() - bi, opG, fScale);

		double nwmm = wmm - 2 * bmm;
		double nhmm = hmm - 2 * bmm;
		double wF = wmm / nwmm;
		double hF = hmm / nhmm;
		int ww = (int) (width * wF);
		int hh = (int) (height * hF);
		int wB = (int) ((ww - width) / 2.0);
		int hB = (int) ((hh - height) / 2.0);
		finalImage = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_RGB);
		Graphics2D fg = (Graphics2D) finalImage.getGraphics();
		fg.setColor(Color.WHITE);
		fg.fillRect(0, 0, ww, hh);

		double inches = (wmm) / 25.4;
		fg.drawImage(opImage, wB, hB, wB + w, hB + h, 0, 0, w, h, null);
		dpi = (((double) ww)) / inches;

		return finalImage;
	}

	private BufferedImage addBorderToPCImage(double fac,
			BufferedImage finalImage, double wmm) throws Exception {
		double bdi = (((double) opImage.getWidth()) * fac * 0.25);
		drawTitle(bdi, opImage.getHeight() - bdi, opG, fScale);
		double wBd = (((double) opImage.getWidth()) * fac);
		double hBd = (((double) opImage.getHeight()) * fac);
		int wB = (int) wBd;
		int hB = (int) hBd;

		int ww = opImage.getWidth() + 2 * wB;
		int hh = opImage.getHeight() + 2 * hB;
		double wwd = (double) ww;
		double hhd = (double) hh;
		finalImage = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_RGB);
		Graphics2D fg = (Graphics2D) finalImage.getGraphics();
		fg.setColor(Color.WHITE);
		fg.fillRect(0, 0, ww, hh);

		double wbf = 0.02;
		int nwb = (int) (((double) w) * wbf);
		int nhb = nwb;

		fg.setStroke(new BasicStroke(2));
		fg.setColor(Color.LIGHT_GRAY);
		fg.drawRect(((int) wB), hB, w, h);

		fg.drawImage(opImage, wB + nwb, hB + nhb, wB + w - nwb, hB + h - nhb,
				0, 0, w, h, null);
		double inches = (wmm * (1 + 2 * fac)) / 25.4;
		dpi = ((double) ww) / inches;
		return finalImage;
	}

	private BufferedImage addScaledPCImage(double fac,
			BufferedImage finalImage, double wmm) throws Exception {
		double bdi = (((double) opImage.getWidth()) * fac * 0.05);
		int ww = 1000;
		int hh = 707;
		finalImage = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_RGB);
		Graphics2D fg = (Graphics2D) finalImage.getGraphics();
		fg.setColor(Color.WHITE);
		fg.fillRect(0, 0, ww, hh);

		fg.drawImage(opImage, 0, 0, ww, hh, null);
		drawTitle(bdi, hh - bdi, fg, fScale * 0.1);
		return finalImage;
	}

	private String getTypeString() {
		return TYPES[type];
	}

	protected Color getColorFromData(String el) {
		float a = 1.0f; // 0.75f;
		Color col2 = Color.decode("0x" + el);
		Color col = new Color(((float) col2.getRed()) / 255f,
				((float) col2.getGreen()) / 255f,
				((float) col2.getBlue()) / 255f, a);
		return col;
	}

	private void drawTitle(double x, double y, Graphics2D fg, double fontSc)
			throws Exception {
		if (!drawTitle) {
			return;
		}
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		BufferedReader br = new BufferedReader(new FileReader(src + scName
				+ "Sc.csv"));

		StringTokenizer st = new StringTokenizer(br.readLine(), ",");
		String fontFile = st.nextToken().toString();
		Color col = Color.decode("#" + st.nextToken().toString());
		String title = ScriptPath.getRealName(fontFile, st.nextToken()
				.toString());

		String fName = SharedUtils.HOST_MODULE + SharedUtils.DIR_FONTS
				+ fontFile;
		InputStream is = new BufferedInputStream(new FileInputStream(fName));
		Font font = Font.createFont(Font.TRUETYPE_FONT, is);
		font = font.deriveFont((float) 15);
		fg.setFont(font);

		fg.setColor(col);
		GlyphVector gv = fg.getFont().createGlyphVector(
				fg.getFontRenderContext(), title);
		Shape glyph = gv.getOutline();
		AffineTransform at = new AffineTransform();
		AffineTransform sc = AffineTransform.getScaleInstance(fontSc, fontSc);
		AffineTransform tr = AffineTransform.getTranslateInstance(x, y);

		at.concatenate(tr);
		at.concatenate(sc);
		Shape transformedGlyph = at.createTransformedShape(glyph);
		fg.fill(transformedGlyph);

	}
}
