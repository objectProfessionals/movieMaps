package com.op.moviemaps.script.bak;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import com.op.moviemaps.script.PathLength;
import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;

public class ScriptPathBAK {
	public static ScriptPathBAK tester;
	protected static String scName = "ANH";
	protected int width = -1;
	protected int height = -1;
	protected int bWidth = -1;
	protected int bHeight = -1;
	protected int borderW = -1;
	protected int borderH = -1;
	protected double bWidthF = 0.05;
	protected double bHeightF = 0.1;
	protected double borderHF = 0.1;
	protected double borderWF = 0.0;
	protected double widthF = 1;
	protected double heightF = 0.25;
	protected double characterD = -1;
	protected double characterD2 = -1;
	protected double characterDF = 0.5;
	protected double characterD2F = 1.0;
	protected double birthDeathD = 10;
	protected static String opName = scName + "Sc_OUT.png";
	protected static String csvName = scName + "Sc.csv";
	protected static String dir = "scripts/" + scName + "/";
	protected String title = "";
	protected static final int ROW_TITLES = 0;
	protected static final int ROW_CODES = 1;
	protected static final int ROW_NAMES = 2;
	protected static final int ROW_COLORS = 3;
	protected static final int ROW_WEIGHTS = 4;
	protected static final int ROW_SCENE_NAMES = 5;
	protected static final int ROW_SCENES = 6;
	BufferedImage opImage;
	Graphics2D opG;
	double fontScFBrithDeath = 0.09;
	double fontScFScenes = 0.09; // 0.09;
	double fontScFLocs = 1.25;
	double fontScFLocNames = 0.085;
	ArrayList<Scene> scenes = new ArrayList<Scene>();
	ArrayList<Character> characters = new ArrayList<Character>();
	ArrayList<Location> locations = new ArrayList<Location>();
	protected boolean onlyShowOnScene = false;
	protected int allLocRows;
	double curveFr = 0.2;
	protected double numCurveSteps = 10.0;
	protected String fontName = "";
	protected Color titleColor;
	protected boolean descLoc = false;
	protected boolean plotLocationNames = true;
	protected boolean plotBirthDeaths = true;
	protected BufferedImage bgImage;
	protected boolean bg = true;
	protected boolean bgHalves = true;
	protected double bgHalvesMarginF = 0.1;
	private float textStrokeF = 0.05f;
	protected boolean titleOutline = false;
	protected static final int ALIGN_V_LEFT = 0;
	protected static final int ALIGN_V_RIGHT = 1;
	protected static final int ALIGN_V_CEN_Y = 2;
	protected static final int ALIGN_H_LEFT = 3;
	protected static final int ALIGN_H_LEFT_CEN_Y = 4;
	protected static final int ALIGN_H_RIGHT = 5;
	protected static final int ALIGN_H_CEN = 6;
	boolean only = false;
	boolean clip = false;
	private float strokeWidth;
	private static final int JOIN = BasicStroke.JOIN_ROUND;
	private static final int CAP = BasicStroke.CAP_BUTT;
	private float dashLF = 0.5f;
	private double weightPow = -1; // 1.5;
	private float sceneBlockColAlpha = 1f;
	private float sceneTextColAlpha = 0.5f; // 0.25f;
	private Color sceneTextColor = Color.LIGHT_GRAY;
	private float locationBlockColAlpha = 0.2f; // 0.2f;
	private float locationTextColAlpha = 0.3f; // 0.33f;
	private float characterPathColAlpha = 0.75f;
	private boolean useBlock = true;
	private boolean roundedHighlights = false;

	public static void main(String[] args) throws IOException,
			FontFormatException {
		tester = new ScriptPathBAK();
		tester.createImage();
	}

	protected void createImage() throws IOException, FontFormatException {
		createLocations();
		createRecords();
		// printScenes();
		plotRecords();
		saveImage();
		// saveSceneMaps();
	}

	protected void plotRecords() throws FontFormatException, IOException {
		width = scenes.get(scenes.size() - 1).end;
		height = (int) (width * heightF);
		bWidth = (int) (((double) width) * bWidthF);
		bHeight = (int) (((double) height) * bHeightF);
		borderH = (int) (((double) height) * borderHF);
		borderW = (int) (((double) width) * borderWF);
		int ww = width + (2 * bWidth) + (2 * borderW);
		int hh = height + (2 * bHeight) + (2 * borderH);
		opImage = RendererUtils.createAlphaBufferedImage(ww, hh);
		opG = (Graphics2D) opImage.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// GradientPaint gp = new GradientPaint(0, 0, Color.BLACK, ww, hh,
		// Color.WHITE);
		// opG.setPaint(gp);
		// opG.fillRect(0, 0, ww, hh);
		// opG.setColor(Color.BLACK);
		// opG.fillRect(0, 0, ww, hh);
		characterD = characterDF * height / (allLocRows);
		characterD2 = characterD * characterD2F;
		birthDeathD = characterD2;
		String fName = SharedUtils.HOST_MODULE + SharedUtils.DIR_FONTS
				+ fontName;
		InputStream is = new BufferedInputStream(new FileInputStream(fName));
		Font font = Font.createFont(Font.TRUETYPE_FONT, is);
		font = font.deriveFont((float) 15);
		opG.setFont(font);
		calculateLocationHeights();
		calculatePoints();
		updateBirthDeaths();
		if (bg) {
			if (bgHalves) {
				plotBackgroundHalves();
			} else {
				plotBackground();
			}
		}
		plotSceneNames();
		plotLocationNames();
		// plotOnscreenOutline();
		plotPointsByCharacter();
		// plotTitle();
		System.out.println("calculated");
	}

	private void updateNumScenesForCharacter(Character ch) {
		double c = 0;
		for (Scene sc : scenes) {
			for (SceneLocation sl : sc.sceneLocations) {
				if (sl.characters.contains(ch)) {
					c++;
				}
			}
		}
		ch.weighting = c;
	}

	protected void plotBackgroundHalves() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File inputFile = new File(src + "bg.jpg");
		bgImage = ImageIO.read(inputFile);
		opG.drawImage(bgImage, 0, 0, width + (bWidth + borderW) * 2, height
				+ (bHeight + borderH) * 2, null);
		src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		inputFile = new File(src + scName + "Paint.jpg");
		bgImage = ImageIO.read(inputFile);
		int dx1 = borderW + bWidth;
		int dw = width;
		double yBord = borderH + bHeight;
		double yBordMargin = yBord * bgHalvesMarginF;
		int dy1 = (int) (height + yBord + yBordMargin);
		int dh = (int) (yBord - 2 * yBordMargin);
		opG.drawImage(bgImage, dx1, dy1, dw, dh, null);
		dy1 = (int) (yBordMargin);
		opG.drawImage(bgImage, dx1, dy1, dw, dh, null);
		for (Scene sc : scenes) {
			int x1 = (int) (sc.start * widthF);
			int x2 = (int) (sc.end * widthF);
			int y1 = 0;
			int y2 = bgImage.getHeight();
			Color col = getRGB(bgImage, x1, x2, y1, y2);
			sc.col = col;
		}
	}

	private Color getRGB(BufferedImage image, int x1, int x2, int y1, int y2) {
		float rf = 0;
		float gf = 0;
		float bf = 0;
		for (int xx = x1; xx < x2; xx++) {
			for (int yy = y1; yy < y2; yy++) {
				int rgb = image.getRGB(xx, yy);
				int r = (rgb >>> 16) & 0x000000FF;
				int g = (rgb >>> 8) & 0x000000FF;
				int b = (rgb >>> 0) & 0x000000FF;
				rf = rf + ((float) r) / 255f;
				gf = gf + ((float) g) / 255f;
				bf = bf + ((float) b) / 255f;
			}
		}
		float xD = x2 - x1;
		float yD = y2 - y1;
		return new Color(rf / (xD * yD), gf / (xD * yD), bf / (xD * yD));
	}

	protected void plotBackgroundHalvesOld() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File inputFile = new File(src + scName + "Frames.jpg");
		bgImage = ImageIO.read(inputFile);
		int stOff = 0;
		for (int i = 0; i < scenes.get(scenes.size() - 1).end; i++) {
			int st = (int) (i / widthF) + stOff;
			int sx1 = (st * 64) % 3840;
			int sy1 = (st / (3840 / 64)) * 36;
			int sx2 = 64 + sx1;
			int sy2 = 36 + sy1;
			int dx1 = i + borderW + bWidth;
			int dx2 = i + 1 + borderW + bWidth;
			int dy1 = 0;
			int dy2 = borderH + bHeight;
			// opG.drawImage(bgImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
			// null);
			double yBord = borderH + bHeight;
			double yBordMargin = yBord * bgHalvesMarginF;
			dy1 = (int) (height + yBord + yBordMargin);
			dy2 = (int) (height + 2 * yBord - yBordMargin);
			opG.drawImage(bgImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
			// System.out.println(i + ":" + sx1 + "," + sy1);
		}
	}

	protected void plotBackground() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File inputFile = new File(src + scName + "Frames.jpg");
		bgImage = ImageIO.read(inputFile);
		int stOff = 0;
		for (int i = 0; i < scenes.get(scenes.size() - 1).end; i++) {
			int st = (int) (i / widthF) + stOff;
			int sx1 = (st * 64) % 3840;
			int sy1 = (st / (3840 / 64)) * 36;
			int sx2 = 64 + sx1;
			int sy2 = 36 + sy1;
			int dx1 = i + borderW + bWidth;
			int dx2 = i + 1 + borderW + bWidth;
			int dy1 = 0;
			int dy2 = height + 2 * borderH + 2 * bHeight;
			opG.drawImage(bgImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
		}
	}

	protected void saveSceneMaps() throws IOException {
		int w = scenes.size() + 1;
		int h = getMaxScene();
		opImage = RendererUtils.createAlphaBufferedImage(64 * w, 36 * h);
		opG = (Graphics2D) opImage.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		opG.setColor(Color.WHITE);
		opG.fillRect(0, 0, 64 * w, 36 * h);
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File inputFile = new File(src + scName + "Frames.jpg");
		bgImage = ImageIO.read(inputFile);
		int sccc = 0;
		for (int i = 0; i < scenes.size(); i++) {
			int scc = 0;
			for (double sci = scenes.get(i).start / widthF; sci < scenes.get(i).end
					/ widthF; sci++) {
				int sx1 = (int) (sci * 64) % 3840;
				int sy1 = (int) (sci / (3840 / 64)) * 36;
				int sx2 = 64 + sx1;
				int sy2 = 36 + sy1;
				int dx1 = (i * 64);
				int dx2 = dx1 + 64;
				int dy1 = (int) (scc) * 36;
				int dy2 = dy1 + 36;
				opG.drawImage(bgImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
						null);
				plotString(dx1, "" + sccc, dy1, ALIGN_H_LEFT, 1, false);
				scc++;
				sccc++;
			}
		}
		String src2 = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src2 + scName + "_OP.png");
		RendererUtils.savePNGFile(opImage, fFile1);
		opG.dispose();
		System.out.println("created " + fFile1);
	}

	protected void saveSceneMapsInBlocks() throws IOException {
		int w = 7345;
		int h = 1;
		int wl = 1000;
		int iw = 64;
		int ih = 36;
		int c = 0;
		for (int ww = 0; ww < w; ww = ww + wl) {
			BufferedImage opImage;
			opImage = new BufferedImage(wl * iw, h * ih,
					BufferedImage.TYPE_INT_RGB);
			opG = (Graphics2D) opImage.getGraphics();
			opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			opG.setColor(Color.WHITE);
			opG.fillRect(0, 0, iw * wl, ih * h);
			String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
					+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
			File inputFile = new File(src + scName + "Frames.jpg");
			bgImage = ImageIO.read(inputFile);
			for (int i = ww; i < ww + wl; i++) {
				int sx1 = (int) (i * iw) % 3840;
				int sy1 = (int) (i / (3840 / iw)) * ih;
				int sx2 = iw + sx1;
				int sy2 = ih + sy1;
				int dx1 = (i - ww) * iw;
				int dx2 = dx1 + iw;
				int dy1 = 0;
				int dy2 = dy1 + ih;
				opG.drawImage(bgImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
						null);
				plotString(dx1, "" + i, dy1, ALIGN_H_LEFT, 1, false);
			}
			String src2 = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
					+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
			File fFile1 = new File(src2 + scName + c + "_OP.png");
			RendererUtils.savePNGFile(opImage, fFile1);
			opG.dispose();
			System.out.println("created " + fFile1);
			c++;
		}
	}

	private int getMaxScene() {
		int max = 0;
		for (Scene sc : scenes) {
			if (sc.end - sc.start > max) {
				max = sc.end - sc.start;
			}
		}
		return max;
	}

	protected void plotSceneNames() {
		int num = 10;
		Shape clip = new Rectangle(borderW + bWidth, borderH + bHeight, width,
				height);
		opG.setClip(clip);
		for (Scene sc : scenes) {
			double x1 = sc.start;
			double x2 = sc.end;
			opG.setColor(getColorWithAlpha(sc.col, sceneBlockColAlpha));
			Shape scclip = new Rectangle((int) (borderW + bWidth + x1), borderH
					+ bHeight, (int) (x2 - x1), height);
			opG.fill(scclip);
			opG.setColor(getColorWithAlpha(sceneTextColor, sceneTextColAlpha));
			String tot = "";
			for (int i = 0; i < num; i++) {
				tot = tot + getRealName(sc.name) + " ";
			}
			if (!"".equals(tot.trim()) && x1 != x2) {
				plotString((int) x1, tot, borderH + bHeight, ALIGN_V_LEFT,
						fontScFScenes * (x2 - x1), false);
			}
		}
		opG.setClip(null);
	}

	protected void calculateLocationHeights() {
		for (Scene sc : scenes) {
			int clInd = 0;
			for (SceneLocation cl : sc.sceneLocations) {
				calculateLocationHeights(sc, clInd, cl);
			}
			clInd++;
		}
		normaliseMaxs();
		setLocationYPos();
	}

	protected void setLocationYPos() {
		double c = 0;
		for (Location loc : locations) {
			double maxH = loc.max;
			loc.y = (int) (c + (maxH) * 0.5);
			c = c + maxH;
		}
	}

	protected void normaliseMaxs() {
		double tot = 0;
		for (Location loc : locations) {
			tot = tot + loc.max;
		}
		double f = (double) (height) / tot;
		for (Location loc : locations) {
			loc.max = (int) (f * (double) loc.max);
		}
	}

	protected void calculateLocationHeights(Scene sc, int clInd,
			SceneLocation cl) {
		ArrayList<Character> chs = getScreenCharacters(cl);
		double chOff = 0;
		double chInd = 0;
		for (Character ch : chs) {
			boolean offSc = cl.offScene.get(ch);
			if (onlyShowOnScene && offSc) {
				continue;
			}
			boolean onn = !offSc;
			double chD = (onn ? characterD : characterD2);
			// chOff = chOff + (chD * ch.weighting);
			chOff = chOff + (chD);
			chInd++;
		}
		if (chOff > cl.location.max) {
			cl.location.max = chOff;
		}
	}

	protected String getRealName(String name) {
		String newW = name;
		if (fontName.equals("STARWARS.TTF")) {
			newW = name.replaceAll("I", "i");
			newW = newW.replaceAll("O", "o");
			newW = newW.replaceAll("Q", "q");
			newW = newW.replaceAll("U", "_");
			newW = newW.replaceAll("V", "`");
			newW = newW.replaceAll("X", "x");
			newW = newW.replaceAll("Z", "z");
			// newW = newW.replaceAll("S", "s");
			newW = replaceWithLowerCase(newW);
		}
		return newW;
	}

	private String replaceWithLowerCase(String newW) {
		String ret = "";
		for (int i = 0; i < newW.length(); i++) {
			String let = newW.substring(i, i + 1);
			if (let.equals(let.toUpperCase())) {
				ret = ret + let.toLowerCase();
			} else {
				ret = ret + let;
			}
		}
		return ret;
	}

	protected void calculatePoints() {
		for (Scene sc : scenes) {
			int x1 = sc.start;
			int x2 = sc.end;
			for (int i = 0; i < sc.sceneLocations.size(); i++) {
				SceneLocation cl = sc.sceneLocations.get(i);
				addLocationPoints(sc, x1, x2, cl);
			}
		}
	}

	protected double getCharacterOffLocationMaxHeight(int x1, int x2,
			SceneLocation cl) {
		double max = 0;
		int pc = 0;
		for (Character ch : cl.characters) {
			boolean off = cl.offScene.get(ch);
			// max = max + ((off) ? characterD2 : characterD) * ch.weighting;
			max = max + ((off) ? characterD2 : characterD);
			pc++;
		}
		return max;
	}

	protected double getCharacterOnLocationMaxHeight(int x1, int x2,
			SceneLocation cl) {
		double max = 0;
		int pc = 0;
		for (Character ch : cl.characters) {
			boolean off = cl.offScene.get(ch);
			// max = max + ((!off) ? characterD : characterD2) * ch.weighting;
			max = max + ((!off) ? characterD : characterD2);
			pc++;
		}
		return max;
	}

	protected void addLocationPoints(Scene sc, int x1, int x2, SceneLocation cl) {
		Location loc = cl.location;
		double max = getCharacterOnLocationMaxHeight(x1, x2, cl);
		double locOffset = (loc.max - max) / 2.0;
		double locSt = loc.y - (loc.max / 2) + locOffset;
		double chOff = locSt;
		ArrayList<Character> chs = getScreenCharacters(cl);
		for (Character ch : chs) {
			double x11 = x1;
			boolean offSc = cl.offScene.get(ch);
			if (onlyShowOnScene && offSc) {
				continue;
			}
			boolean onSc = !offSc;
			// double chD = (onSc ? characterD : characterD2) * ch.weighting;
			double chD = (onSc ? characterD : characterD2);
			double h1 = characterD;
			double h2 = chD;
			double y1 = (chOff + (h1 / 2));
			double y2 = ((h2 / 2) + chOff);
			double z1 = loc.z;
			double z2 = loc.z;
			// System.out.println(sc.toString());
			if (!ch.points.isEmpty()) {
				int ind = ch.points.size() - 1;
				PointData prev = ch.points.get(ind);
				x11 = prev.x2;
				prev.y2 = y1;
				z1 = prev.z2;
				h1 = prev.h2;
			}
			ch.addPoint(x11, y1, z1, h1, x2, y2, z2, h2, scenes.indexOf(sc),
					!onSc);
			chOff = chOff + (h2);
		}
	}

	protected ArrayList<Character> getScreenCharacters(SceneLocation cl) {
		ArrayList<Character> ret = new ArrayList<Character>();
		int i = 0;
		for (Character ch : cl.characters) {
			ret.add(ch);
			i++;
		}
		return ret;
	}

	protected void initCharacter(Character ch, int xOff, int yOff) {
		PointData pFirst = new PointData();
		pFirst.x1 = ch.birth.x1 - ch.birth.x2 + xOff;
		pFirst.y1 = 0;
		pFirst.x2 = ch.birth.x1 - ch.birth.x2 + xOff;
		pFirst.y2 = ch.birth.y1;
		pFirst.offSc = false;
		ch.points.add(0, pFirst);
		PointData pe = new PointData();
		PointData pLast = ch.points.get(ch.points.size() - 1);
		if (ch.death.x1 != 0) {
			pe.x1 = ch.death.x1 + ch.death.x2 + xOff;
			pe.y1 = ch.death.y1;
			pe.x2 = ch.death.x1 + ch.death.x2 + xOff;
			pe.y2 = height;
			pe.offSc = pLast.offSc;
			ch.points.add(pe);
		} else {
			pLast.x2 = scenes.get(scenes.size() - 1).end;
		}
	}

	protected void updateBirthDeaths() {
		int scC = 0;
		int scLast = scenes.size() - 2;
		for (Scene sc : scenes) {
			double birthOffx = 0;
			double birthOffy = 0;
			double deathOffx = 0;
			for (SceneLocation cl : sc.sceneLocations) {
				for (Character ch : cl.characters) {
					int c = 0;
					int last = ch.points.size() - 1;
					for (PointData p : ch.points) {
						int x1 = sc.start;
						int x2 = sc.end;
						if (x1 == p.x1 || x2 == p.x2) {
							if (c == 0) {
								ch.birth.x1 = p.x1;
								ch.birth.y1 = p.y1;
								ch.birth.z1 = p.z1;
								ch.birth.h1 = p.h1;
								ch.birth.x2 = (int) birthOffx;
								ch.birth.y2 = (int) birthOffy;
								ch.birth.z2 = p.z1;
								// birthOffx = birthOffx + birthDeathD
								// * ch.weighting;
								birthOffx = birthOffx + birthDeathD;
								birthOffy = 0;
							}
							if (c == last && scC != scLast) {
								ch.death.x1 = p.x2;
								ch.death.y1 = p.y2;
								ch.death.z1 = p.z2;
								ch.death.h1 = p.h2;
								ch.death.x2 = (int) deathOffx;
								ch.death.z1 = p.z2;
								// deathOffx = deathOffx + birthDeathD
								// * ch.weighting;
								deathOffx = deathOffx + birthDeathD;
							}
						}
						c++;
					}
				}
			}
			scC++;
		}
	}

	protected Color getCharacterColour(Character ch) {
		for (Character ch2 : characters) {
			if (ch2.name.equalsIgnoreCase(ch.name)) {
				return ch2.col;
			}
		}
		return null;
	}

	public void createLocations() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		BufferedReader br = new BufferedReader(new FileReader(src + csvName));
		String line;
		int row = 0;
		int firstDataRow = ROW_SCENES + 1;
		while ((line = br.readLine()) != null) {
			if (row == firstDataRow) {
				StringTokenizer st = new StringTokenizer(line, ",");
				String el = st.nextToken().toString();
				Location loc = new Location();
				loc.name = el;
				locations.add(loc);
				allLocRows++;
			} else if (row > firstDataRow) {
				StringTokenizer st = new StringTokenizer(line, ",");
				String el = st.nextToken().toString();
				Location loc = new Location();
				loc.name = el;
				locations.add(loc);
				allLocRows++;
			}
			row++;
		}
	}

	public void createRecords() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		BufferedReader br = new BufferedReader(new FileReader(src + csvName));
		String line;
		int row = 0;
		while ((line = br.readLine()) != null) {
			if (row == ROW_TITLES) {
				StringTokenizer st = new StringTokenizer(line, ",");
				fontName = st.nextToken().toString();
				titleColor = Color.decode("#" + st.nextToken().toString());
				title = st.nextToken().toString();
			} else if (row == ROW_CODES) {
				StringTokenizer st = new StringTokenizer(line, ",");
				while (st.hasMoreTokens()) {
					String el = st.nextToken().toString();
					Character ch = new Character();
					ch.name = el;
					characters.add(ch);
				}
			} else if (row == ROW_NAMES) {
				StringTokenizer st = new StringTokenizer(line, ",");
				int col = 0;
				while (st.hasMoreTokens()) {
					String el = st.nextToken().toString();
					characters.get(col).description = el;
					col++;
				}
			} else if (row == ROW_WEIGHTS) {
				StringTokenizer st = new StringTokenizer(line, ",");
				int col = 0;
				while (st.hasMoreTokens()) {
					String el = st.nextToken().toString();
					characters.get(col).weighting = getWeighting(Double
							.parseDouble(el));
					col++;
				}
			} else if (row == ROW_COLORS) {
				StringTokenizer st = new StringTokenizer(line, ",");
				int col = 0;
				while (st.hasMoreTokens()) {
					String el = st.nextToken().toString();
					characters.get(col).col = getColorFromData(el);
					col++;
				}
			} else if (row == ROW_SCENE_NAMES) {
				StringTokenizer st = new StringTokenizer(line, ",");
				while (st.hasMoreTokens()) {
					String el = st.nextToken().toString();
					Scene sc = new Scene();
					sc.name = el;
					scenes.add(sc);
				}
			} else if (row == ROW_SCENES) {
				StringTokenizer st = new StringTokenizer(line, ",");
				int col = 0;
				while (st.hasMoreTokens()) {
					String el = st.nextToken().toString();
					int t = Integer.parseInt(el);
					if (col == 0) {
						Scene sc = scenes.get(col);
						sc.start = 0;
					}
					if (col > 0) {
						Scene scB = scenes.get(col - 1);
						scB.end = (int) (t * widthF);
					}
					if (col > 0 && col < scenes.size()) {
						Scene sc = scenes.get(col);
						sc.start = (int) (t * widthF);
					}
					if (col == scenes.size() - 1) {
						Scene sc = scenes.get(col);
						Scene scB = scenes.get(col - 1);
						sc.end = sc.start + (scB.end - scB.start);
					}
					col++;
				}
			} else if (descLoc && row >= ROW_SCENES && row <= ROW_SCENES) {
				String[] details = line.split(",");
				int col = 0;
				Location loc = null;
				for (String el : details) {
					if (col == 0) {
						String locName = "";
						locName = el.substring(1, el.length() - 1);
						loc = getLocationForName(locName);
						// System.out.println(loc);
					} else {
						if (el.equals("")) {
							el = "0";
						}
						loc.scenePos.add(Integer.parseInt(el));
					}
					col++;
				}
			} else {
				String[] details = line.split(",");
				int col = 0;
				String locName = null;
				for (String el : details) {
					if (col == 0) {
						locName = getLocationForName(el).name;
					} else if (col == 1) {
						getLocationForName(locName).col = getColorFromData(el);
					} else {
						if (el != null && !"".equals(el)) {
							Scene sc = scenes.get(col - 2);
							addCharacterToScene(sc, el, locName);
						}
					}
					col++;
				}
			}
			row++;
		}
		scenes.remove(scenes.size() - 1);
		sortCharactersByWeight();
		br.close();
		System.out.println("data read");
	}

	private void sortCharactersByWeight() {
		for (Scene sc : scenes) {
			for (SceneLocation sl : sc.sceneLocations) {
				ArrayList<Character> chs = sl.characters;
				Collections.sort(chs, new Comparator<Character>() {
					@Override
					public int compare(Character c1, Character c2) {
						return new Double(c1.weighting).compareTo(new Double(
								c2.weighting));
					}
				});
			}
		}
	}

	private ArrayList<Character> reverseCharacters() {
		ArrayList<Character> chs = new ArrayList<Character>();
		for (Character ch : characters) {
			chs.add(ch);
		}
		Collections.sort(chs, new Comparator<Character>() {
			@Override
			public int compare(Character c1, Character c2) {
				return new Double(c1.weighting).compareTo(new Double(
						c2.weighting));
			}
		});
		ArrayList<Character> rev = new ArrayList<Character>();
		for (int i = chs.size() - 1; i >= 0; i--) {
			rev.add(chs.get(i));
		}
		return rev;
	}

	private double getWeighting(double we) {
		if (weightPow == -1) {
			return we;
		}
		double w2 = Math.pow(we, weightPow);
		return w2;
	}

	protected Color getColorFromData(String el) {
		float a = 0.75f;
		Color col2 = Color.decode("0x" + el);
		Color col = new Color(((float) col2.getRed()) / 255f,
				((float) col2.getGreen()) / 255f,
				((float) col2.getBlue()) / 255f, a);
		return col;
	}

	protected Color getColorWithAlpha(Color col2, float a) {
		Color col = new Color(((float) col2.getRed()) / 255f,
				((float) col2.getGreen()) / 255f,
				((float) col2.getBlue()) / 255f, a);
		return col;
	}

	protected void addCharacterToScene(Scene sc, String el, String locName) {
		SceneLocation cl = getCharacterLocationForScene(sc, locName);
		if (cl.location.name.equals(locName)) {
			Character ch = getCharacterForName(el, false);
			boolean offScene = false;
			if (ch == null) {
				ch = getCharacterForName(el, true);
				offScene = true;
			}
			cl.characters.add(ch);
			cl.offScene.put(ch, offScene);
		}
	}

	protected Character getCharacterForName(String name, boolean ignoreCase) {
		for (Character ch : characters) {
			if (ignoreCase && ch.name.equalsIgnoreCase(name)) {
				return ch;
			}
			if (!ignoreCase && ch.name.equals(name)) {
				return ch;
			}
		}
		return null;
	}

	protected SceneLocation getCharacterLocationForScene(Scene sc,
			String locName) {
		SceneLocation charLoc = null;
		Location loc = null;
		for (SceneLocation cl : sc.sceneLocations) {
			if (cl.location.name.equals(locName)) {
				loc = cl.location;
				charLoc = cl;
			}
		}
		if (loc == null) {
			charLoc = new SceneLocation();
			charLoc.location = getLocationForName(locName);
			sc.sceneLocations.add(charLoc);
		}
		return charLoc;
	}

	protected Location getLocationForName(String name) {
		for (Location loc : locations) {
			if (loc.name.equals(name)) {
				return loc;
			}
		}
		return null;
	}

	protected void addPoint(ArrayList<Point3D> allPoints, double x, double y,
			double z) {
		Point3D p = new Point3D();
		p.x = (int) x;
		p.y = (int) y;
		p.z = (int) z;
		allPoints.add(p);
	}

	protected double getTextWHForScale(boolean widthOrHeight, double scale) {
		GlyphVector gv = opG.getFont().createGlyphVector(
				opG.getFontRenderContext(), getRealName("H"));
		Shape glyph = gv.getOutline();
		AffineTransform at = new AffineTransform();
		AffineTransform sc = AffineTransform.getScaleInstance(scale, scale);
		at.concatenate(sc);
		Shape transformedGlyph = at.createTransformedShape(glyph);
		double ww = transformedGlyph.getBounds2D().getWidth();
		double hh = transformedGlyph.getBounds2D().getHeight();
		if (widthOrHeight) {
			return ww;
		}
		return hh;
	}

	protected int plotString(double x1, String name, double y, int align,
			double scale, boolean outline) {
		GlyphVector gv = opG.getFont().createGlyphVector(
				opG.getFontRenderContext(), name);
		Shape glyph = gv.getOutline();
		AffineTransform at = new AffineTransform();
		AffineTransform sc = AffineTransform.getScaleInstance(scale, scale);
		AffineTransform tr = AffineTransform.getTranslateInstance(borderW
				+ bWidth + x1, y);
		AffineTransform ro = AffineTransform.getRotateInstance(Math.PI / 2);
		if (align == ALIGN_V_RIGHT) {
			AffineTransform tr2 = AffineTransform.getTranslateInstance(0,
					borderH + bHeight - ((double) glyph.getBounds().width)
							* scale);
			at.concatenate(tr2);
		} else if (align == ALIGN_V_CEN_Y) {
			AffineTransform tr2 = AffineTransform.getTranslateInstance(0,
					-((double) glyph.getBounds().width) * scale * 0.5);
			at.concatenate(tr2);
		} else if (align == ALIGN_H_LEFT_CEN_Y) {
			AffineTransform tr2 = AffineTransform.getTranslateInstance(0,
					+((double) glyph.getBounds2D().getHeight()) * scale * 0.5);
			at.concatenate(tr2);
		} else if (align == ALIGN_H_CEN) {
			AffineTransform tr2 = AffineTransform.getTranslateInstance(
					-((double) glyph.getBounds2D().getWidth()) * scale * 0.5,
					+((double) glyph.getBounds2D().getHeight()) * scale * 0.5);
			at.concatenate(tr2);
		} else if (align == ALIGN_H_RIGHT) {
			AffineTransform tr2 = AffineTransform.getTranslateInstance(
					-((double) glyph.getBounds2D().getWidth()) * scale, 0);
			at.concatenate(tr2);
		}
		at.concatenate(tr);
		if (align == ALIGN_V_LEFT || align == ALIGN_V_RIGHT
				|| align == ALIGN_V_CEN_Y) {
			at.concatenate(ro);
		}
		at.concatenate(sc);
		Shape transformedGlyph = at.createTransformedShape(glyph);
		double ww = transformedGlyph.getBounds2D().getWidth();
		double hh = transformedGlyph.getBounds2D().getHeight();
		int ret = -1;
		if (align == ALIGN_V_LEFT || align == ALIGN_V_RIGHT) {
			ret = (int) (hh);
		} else if (align == ALIGN_V_CEN_Y) {
			ret = (int) (ww);
		}
		if (outline) {
			Stroke old = opG.getStroke();
			Stroke ns = new BasicStroke((float) hh * textStrokeF);
			opG.setStroke(ns);
			opG.draw(transformedGlyph);
			opG.setStroke(old);
		} else {
			opG.fill(transformedGlyph);
		}
		// opG.draw(transformedGlyph.getBounds().getBounds2D());
		return ret;
	}

	protected class Location {
		public String name;
		public Color col;
		public double max = 0;
		public double y = 0;
		public double z = 0;
		public ArrayList<Integer> scenePos = new ArrayList<Integer>();

		public String toString() {
			return "|" + name + "|";
		}
	}

	protected class SceneLocation {
		public Location location;
		public ArrayList<Character> characters = new ArrayList<Character>();
		public HashMap<Character, Boolean> offScene = new HashMap<Character, Boolean>();
		public double clOffset = 0;

		public String toString() {
			String ret = location.toString();
			for (Character ch : characters) {
				ret = ret + " " + ch.toString();
			}
			return ret;
		}
	}

	protected class Scene {
		public String name;
		public int start;
		public int end;
		public Color col;
		public ArrayList<SceneLocation> sceneLocations = new ArrayList<SceneLocation>();

		public String toString() {
			String ret = "[" + name + "]";
			for (SceneLocation cl : sceneLocations) {
				ret = ret + " " + cl.toString();
			}
			return ret;
		}
	}

	protected class Character {
		public String name;
		public String description;
		public Color col;
		public ArrayList<PointData> points = new ArrayList<PointData>();
		public ArrayList<Integer> scenes = new ArrayList<Integer>();
		public PointData birth = new PointData();
		public PointData death = new PointData();
		public double weighting = 1.0;

		public void addPoint(double x1, double y1, double z1, double h1,
				double x2, double y2, double z2, double h2, int scc, boolean off) {
			PointData p = new PointData();
			p.x1 = x1;
			p.y1 = y1;
			p.z1 = z1;
			p.h1 = h1;
			p.x2 = x2;
			p.y2 = y2;
			p.z2 = z2;
			p.h2 = h2;
			p.offSc = off;
			this.points.add(p);
			this.scenes.add(scc);
		}

		public String toString() {
			return "<" + name + ">";
		}
	}

	protected class PointData {
		double x1;
		double y1;
		double z1;
		double x2;
		double y2;
		double h1;
		double h2;
		double z2;
		boolean offSc = false;
		double cx1;
		double cx2;

		public String toString() {
			return x1 + "," + y1 + "," + h1 + " : " + x2 + "," + y2 + "," + h2;
		}
	}

	protected class Point3D {
		int x;
		int y;
		int z;
	}

	protected void plotPointsByCharacter() {
		strokeWidth = (float) characterD;
		birthDeathD = strokeWidth;
		updateBirthDeaths();
		// movePointsForBirthsDeaths();
		updateControlPoints();
		ArrayList<Character> rev = reverseCharacters();
		for (Character ch : rev) {
			if (isOnly(ch)) {
				continue;
			}
			// initCharacter(ch, 0, 0);
			createPath(ch);
		}
	}

	private ArrayList<PointData> getPointsForScene(Scene sc, SceneLocation sl,
			boolean up) {
		ArrayList<PointData> ps = new ArrayList<PointData>();
		double x1 = sc.start;
		double x2 = sc.end;
		for (Character ch : sl.characters) {
			for (PointData p : ch.points) {
				if (p.x1 == x1 && p.x2 == x2) {
					if (up && p.y1 > p.y2 && (p.y1 - p.y2 > characterD)) {
						ps.add(p);
					} else if (!up && p.y1 < p.y2 && (p.y2 - p.y1 > characterD)) {
						ps.add(p);
					}
				}
			}
		}
		return ps;
	}

	private boolean isOnly(Character ch) {
		String[] onlys = { "CH" };
		for (String o : onlys) {
			if (!o.equals(ch.name)) {
				return only;
			}
		}
		return false;
	}

	private void createPath(Character ch) {
		createPath(ch, 0, 0);
	}

	private void createPath(Character ch, double xOff, double yOff) {
		if (ch.points.isEmpty()) {
			return;
		}
		Color col = getColorWithAlpha(getCharacterColour(ch),
				characterPathColAlpha);
		Path2D.Double path = new Path2D.Double();
		Path2D.Double prevPath = new Path2D.Double();
		PointData pFirst = new PointData();
		pFirst.x1 = ch.birth.x1 - ch.birth.x2 + xOff;
		pFirst.y1 = 0;
		pFirst.x2 = ch.birth.x1 - ch.birth.x2 + xOff;
		pFirst.y2 = ch.birth.y1;
		pFirst.offSc = false;
		ch.points.add(0, pFirst);
		PointData pe = new PointData();
		PointData pLast = ch.points.get(ch.points.size() - 1);
		if (ch.death.x1 != 0) {
			pe.x1 = ch.death.x1 + ch.death.x2 + xOff;
			pe.y1 = ch.death.y1;
			pe.x2 = ch.death.x1 + ch.death.x2 + xOff;
			pe.y2 = height;
			pe.offSc = pLast.offSc;
			ch.points.add(pe);
		} else {
			pLast.x2 = scenes.get(scenes.size() - 1).end;
		}
		ArrayList<Float> dashesAL = new ArrayList<Float>();
		boolean lastOffSc = false;
		for (PointData p : ch.points) {
			p.y1 = p.y1 + yOff;
			p.y2 = p.y2 + yOff;
			if (path.getCurrentPoint() == null) {
				path.moveTo(p.x1 + borderW + bWidth, p.y1 + borderH + bHeight);
				prevPath = addCubicToPath(dashesAL, path, p, lastOffSc,
						prevPath, ch, false);
			} else {
				prevPath = addCubicToPath(dashesAL, path, p, lastOffSc,
						prevPath, ch, false);
			}
			lastOffSc = p.offSc;
		}
		addDashes(dashesAL, lastOffSc, prevPath, ch, true);
		PathLength pl = new PathLength(path);
		float len = pl.lengthOfPath();
		if (len == 0) {
			return;
		}
		float[] dashes = new float[dashesAL.size()];
		int i = 0;
		for (Float dash : dashesAL) {
			dashes[i] = dash;
			i++;
		}
		float[] dashes2 = null;
		if (dashes.length == 0) {
			dashes = dashes2;
		}
		opG.setColor(col);
		paintBirthsDeathsPoints(ch, col, pFirst, pe, pLast);

		opG.setStroke(new BasicStroke(((float) (strokeWidth)), CAP, JOIN, 1f,
				dashes, 0f));
		opG.draw(path);
		opG.setColor(Color.BLACK);
		float fontSc = strokeWidth * 0.075f;
		double fh = getTextWHForScale(false, fontSc);
		double xOffset = fh / 2.0;
		String name = getRealName(ch.description);
		int w = plotString(pFirst.x1 - xOffset, name, borderH + bHeight,
				ALIGN_V_LEFT, fontSc, false);
		if (ch.death.x1 != 0) {
			w = plotString(pe.x1 - xOffset, name, height, ALIGN_V_RIGHT,
					fontSc, false);
		} else {
			plotString(width, getRealName(ch.description), pLast.y2 + xOffset
					+ bHeight + borderH, ALIGN_H_RIGHT, fontSc, false);
		}
	}

	private void paintBirthsDeathsPoints(Character ch, Color col,
			PointData pFirst, PointData pe, PointData pLast) {
		opG.setColor(col);
		int x1 = (int) (pFirst.x1);
		int y1 = (int) (pFirst.y1);
		int dd = (int) characterD;
		int rr = dd / 2;
		opG.fillArc(borderW + bWidth + x1 - rr, borderH + bHeight + y1 - rr,
				dd, dd, 0, 180);

		if (ch.death.x1 != 0) {
			x1 = (int) (pe.x1);
			y1 = (int) (height);
			opG.fillArc(borderW + bWidth + x1 - rr, borderH + bHeight + y1 - rr
					- 1, dd, dd, 180, 180);
		} else {
			x1 = (int) (width);
			y1 = (int) (pLast.y2);
			opG.fillArc(borderW + bWidth + x1 - rr - 1, borderH + bHeight + y1
					- rr, dd, dd, -90, 180);
		}
	}

	private void updateControlPoints() {
		double off = characterD * 2;
		for (Scene sc : scenes) {
			double xD = sc.end - sc.start;
			for (SceneLocation sl : sc.sceneLocations) {
				ArrayList<PointData> points = getPointsForScene(sc, sl, true);
				if (points.size() * off > xD) {
					off = (xD / points.size());
				}
				double i = -((double) points.size()) * off * 0.5 + (off * 0.5);
				for (PointData p : points) {
					if (p.y1 > p.y2 && (p.y1 - p.y2 > characterD)) {
						p.cx1 = i;
						i = i + off;
					}
				}
				points = getPointsForScene(sc, sl, false);
				if (points.size() * off > xD) {
					off = (xD / points.size());
				}
				i = ((double) points.size()) * off * 0.5 + (off * 0.5);
				for (PointData p : points) {
					if (p.y1 < p.y2 && (p.y2 - p.y1 > characterD)) {
						p.cx1 = i;
						i = i - off;
					}
				}
			}
		}
	}

	private Path2D.Double addCubicToPath(ArrayList<Float> dashesAL,
			Path2D.Double path, PointData p, boolean lastOffSc,
			Path2D.Double prevPath, Character ch, boolean lastP) {
		double cxOff = p.cx1;
		double cx2Off = p.cx2;
		double x1 = p.x1 + borderW + bWidth;
		double y1 = p.y1 + borderH + bHeight;
		double h1 = p.h1;
		double x2 = p.x2 + borderW + bWidth;
		double y2 = p.y2 + borderH + bHeight;
		double h2 = p.h2;
		boolean offSc = p.offSc;
		CubicCurve2D c = new CubicCurve2D.Double();
		c.setCurve(x1, y1, x2 + cxOff, y1, x1 + cxOff, y2, x2, y2);
		path.append(c, true);
		// Path2D.Double c = getCubicCurve(p);
		// path.append(c, true);
		return addToDashes(c, dashesAL, p, lastOffSc, prevPath, ch, lastP);
		// addToDashes(c, dashesAL);
	}

	private void addToDashes(Shape c, ArrayList<Float> dashesAL) {
		Path2D.Double prevPath = new Path2D.Double();
		prevPath.append(c, true);
		PathLength plb = new PathLength(prevPath);
		float lenb = plb.lengthOfPath();
		dashesAL.add(lenb);
	}

	private Path2D.Double addToDashes(CubicCurve2D c,
			ArrayList<Float> dashesAL, PointData p, boolean lastOffSc,
			Path2D.Double prevPath, Character ch, boolean lastP) {
		boolean offSc = p.offSc;
		if (lastOffSc != offSc) {
			addDashes(dashesAL, lastOffSc, prevPath, ch, lastP);
			prevPath = new Path2D.Double();
			prevPath.append(c, true);
		} else {
			prevPath.append(c, true);
		}
		return prevPath;
	}

	private void addDashes(ArrayList<Float> dashesAL, boolean lastOffSc,
			Path2D.Double prevPath, Character ch, boolean lastP) {
		// float dashLen = (float) (characterD * dashLF * ch.weighting);
		float dashLen = (float) (characterD * dashLF);
		PathLength plb = new PathLength(prevPath);
		float lenb = plb.lengthOfPath();
		if (lastOffSc) {
			float n = 0;
			boolean space = true;
			while (n < lenb) {
				if (n + dashLen >= lenb) {
					if (!lastP && !space) {
						addToLast(dashesAL, lenb - n);
					} else if (lastP && space) {
						addToLast(dashesAL, lenb - n);
					} else {
						dashesAL.add(lenb - n);
					}
				} else {
					dashesAL.add(dashLen);
					space = !space;
				}
				n = n + dashLen;
			}
		} else {
			dashesAL.add(lenb);
		}
	}

	private void addToLast(ArrayList<Float> dashesAL, float lenb) {
		float lenbb = dashesAL.get(dashesAL.size() - 1);
		dashesAL.remove(dashesAL.size() - 1);
		dashesAL.add(lenb + lenbb);
	}

	protected ArrayList<PointData> getCurveUp(PointData p, double r) {
		double x1 = p.x1;
		double y1 = p.y1;
		double x2 = p.x2;
		double y2 = p.y2;
		double x1a = (x1 + r);
		double x2a = (x2 - r);
		ArrayList<PointData> allPoints = new ArrayList<PointData>();
		double xd = x2 - x1;
		addPointData(allPoints, x1, y1, x1, y1, 0);
		double lx = -1;
		double ly = -1;
		double a = Math.atan((y1 - y2) / (xd));
		double xR1 = Math.tan(a / 2) * r;
		double cX = (x1a - xR1);
		double cY = (y1 - r);
		for (double i = 0; i < a; i = i + a / numCurveSteps) {
			double ix = cX + r * Math.cos(Math.PI * 1.5 + i);
			double iy = cY - r * Math.sin(Math.PI * 1.5 + i);
			addPointData(allPoints, lx, ly, ix, iy, 0);
			lx = ix;
			ly = iy;
		}
		cX = (int) (x2a + xR1);
		cY = (int) (y2 + r);
		for (double i = a; i > 0; i = i - a / numCurveSteps) {
			double ix = cX + r * Math.cos(Math.PI * 0.5 + i);
			double iy = cY - r * Math.sin(Math.PI * 0.5 + i);
			addPointData(allPoints, lx, ly, ix, iy, 0);
			lx = ix;
			ly = iy;
		}
		return allPoints;
	}

	protected ArrayList<PointData> getCurveDown(PointData p, double r) {
		double x1 = p.x1;
		double y1 = p.y1;
		double x2 = p.x2;
		double y2 = p.y2;
		double x1a = (x1 + r);
		double x2a = (x2 - r);
		ArrayList<PointData> allPoints = new ArrayList<PointData>();
		double xd = x2 - x1;
		addPointData(allPoints, x1, y1, x1, y1, 0);
		double lx = -1;
		double ly = -1;
		double a = Math.atan((y2 - y1) / (xd));
		double xR1 = Math.tan(a / 2) * r;
		double cX = (x1a - xR1);
		double cY = (y1 + r);
		for (double i = 0; i < a; i = i + a / numCurveSteps) {
			double ix = cX + r * Math.cos(Math.PI * 0.5 - i);
			double iy = cY - r * Math.sin(Math.PI * 0.5 - i);
			addPointData(allPoints, lx, ly, ix, iy, 0);
			lx = ix;
			ly = iy;
		}
		cX = (int) (x2a + xR1);
		cY = (int) (y2 - r);
		for (double i = a; i > 0; i = i - a / numCurveSteps) {
			double ix = cX + r * Math.cos(Math.PI * 1.5 - i);
			double iy = cY - r * Math.sin(Math.PI * 1.5 - i);
			addPointData(allPoints, lx, ly, ix, iy, 0);
			lx = ix;
			ly = iy;
		}
		return allPoints;
	}

	private void addPointData(ArrayList<PointData> allPoints, double lx,
			double ly, double x1, double y1, double z1) {
		if (lx == -1 && ly == -1) {
			return;
		}
		PointData p = new PointData();
		p.x1 = lx;
		p.y1 = ly;
		p.x2 = x1;
		p.y2 = y1;
		allPoints.add(p);
	}

	public Path2D.Double getCubicCurve(PointData p) {
		double x1 = p.x1 + borderW + bWidth;
		double y1 = p.y1 + borderH + bHeight;
		double x2 = p.x2 + borderW + bWidth;
		double y2 = p.y2 + borderH + bHeight;
		Path2D.Double path = new Path2D.Double();
		double segments = 100;
		double step = 1 / segments;
		Point2D last = new Point2D.Double(x1, y1);
		path.moveTo(last.getX(), last.getY());
		// this loop draws each segment of the curve
		for (double t = step; t <= 1; t += step) {
			last = getCubicValue(t, p);
			path.lineTo(last.getX(), last.getY());
		}
		// As a final step, make sure the curve ends on the second anchor
		path.lineTo(x2, y2);
		return path;
	}

	public Point2D.Double getCubicValue(double t1, PointData p) {
		double x1 = p.x1 + borderW + bWidth;
		double y1 = p.y1 + borderH + bHeight;
		double x2 = p.x2 + borderW + bWidth;
		double y2 = p.y2 + borderH + bHeight;
		double xOff = 0;
		double cp1x = x2 - xOff;
		double cp1y = y1;
		double cp2x = x1 - xOff;
		double cp2y = y2;
		double t = Math.max(Math.min(t1, 1), 0);
		double tp = 1 - t;
		double t2 = t * t;
		double t3 = t2 * t;
		double tp2 = tp * tp;
		double tp3 = tp2 * tp;
		double x = (tp3 * x1) + (3 * tp2 * t * cp1x) + (3 * tp * t2 * cp2x)
				+ (t3 * x2);
		double y = (tp3 * y1) + (3 * tp2 * t * cp1y) + (3 * tp * t2 * cp2y)
				+ (t3 * y2);
		return new Point2D.Double(x, y);
	}

	protected void saveImage() throws IOException {
		if (clip) {
			saveImageClipped();
		} else {
			saveActualImage();
		}
	}

	protected void saveActualImage() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src + opName);
		RendererUtils.savePNGFile(opImage, fFile1);
		opG.dispose();
		System.out.println("created " + fFile1.getPath());
	}

	protected void saveImageClipped() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src + opName);
		int lx = 1000;
		int ly = height + 2 * bHeight + 2 * borderH;
		int xS = width + 2 * bWidth + 2 * borderW - lx;
		int yS = 0;
		BufferedImage sub = opImage.getSubimage(xS, yS, lx, ly);
		RendererUtils.savePNGFile(sub, fFile1);
		opG.dispose();
		System.out.println("created " + fFile1.getName());
	}

	protected void plotLocationNames() {
		for (Location loc : locations) {
			int y = (int) loc.y + bHeight + borderH;
			int h = (int) loc.max;
			if (h == 0) {
				continue;
			}
			int x1 = borderW;
			int x2 = borderW + bWidth + width + bWidth;
			Color col = null;
			PointData rect = getLocationBox(loc);
			double arc = characterD * 5;
			double x = bWidth + rect.x1 - arc;
			double yy = y - h / 2;
			double w = rect.x2 - rect.x1 + arc * 2;
			Rectangle2D.Double blockO = new Rectangle2D.Double(0, yy, width + 2
					* bWidth, h);
			if (useBlock) {
				col = getColorWithAlpha(loc.col, locationBlockColAlpha);
				opG.setColor(col);
				opG.fill(blockO);
			}
			if (roundedHighlights) {
				RoundRectangle2D.Double block = new RoundRectangle2D.Double(x,
						yy, w, h, arc, arc);
				col = getColorWithAlpha(loc.col, 0.375f);
				opG.setColor(col);
				opG.fill(block);
			}
			Shape clip = new Rectangle(borderW, borderH + bHeight, width + 2
					* bWidth, height);
			opG.setClip(clip);
			col = getColorWithAlpha(loc.col, locationTextColAlpha);
			opG.setColor(col);
			int num = 20;
			String tot = "";
			for (int i = 0; i < num; i++) {
				tot = tot + getRealName(loc.name) + " ";
			}
			plotString((int) (-bWidth), tot, y, ALIGN_H_LEFT_CEN_Y,
					fontScFLocNames * h, true);
			opG.setClip(null);
		}
	}

	private PointData getLocationBox(Location loc) {
		double x1 = width;
		double x2 = 0;
		for (Scene sc : scenes) {
			for (SceneLocation sl : sc.sceneLocations) {
				if (sl.location.equals(loc)) {
					if (sc.start < x1) {
						x1 = sc.start;
					}
					if (sc.end > x2) {
						x2 = sc.end;
					}
				}
			}
		}
		PointData p = new PointData();
		p.x1 = x1;
		p.x2 = x2;
		return p;
	}

	protected void plotTitle() {
		String nt = getRealName(title);
		opG.setColor(titleColor);
		plotString(width * 0.5, nt, (borderH + bHeight) * 0.5, ALIGN_H_CEN,
				bHeight * 0.08, titleOutline);
	}
}
