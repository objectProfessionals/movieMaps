package com.op.moviemaps.bak;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import com.op.moviemaps.script.PathLength;
import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;

public class Script {
	public static Script tester;
	protected int width = -1;
	protected int height = -1;
	protected int bWidth = -1;
	protected int bHeight = -1;
	protected int borderW = -1;
	protected int borderH = -1;
	protected double widthF = 1;
	protected double heightF = 0.25;
	protected double characterD = -1;
	protected double characterD2 = -1;
	protected double characterDF = 1;
	protected double characterD2F = 0.25;
	protected double birthDeathD = 10;
	protected static String scName = "ANH";
	protected static String opName = scName + "Sc_OUT.png";
	protected static String csvName = scName + "Sc.csv";
	protected static String dir = "scripts/";
	protected String title = "";
	protected static final int ROW_TITLES = 0;
	protected static final int ROW_CODES = 1;
	protected static final int ROW_NAMES = 2;
	protected static final int ROW_COLORS = 3;
	protected static final int ROW_SCENE_NAMES = 4;
	protected static final int ROW_SCENES = 5;
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
	protected double numCurveSteps = 50.0;
	protected String fontName = "";
	protected Color titleColor;
	protected boolean descLoc = false;
	protected boolean plotLocationNames = true;
	protected boolean plotBirthDeaths = true;
	protected BufferedImage bgImage;
	protected boolean bg = true;
	protected boolean bgHalves = true;
	private float textStrokeF = 0.05f;
	protected boolean titleOutline = true;
	protected static final int ALIGN_V_LEFT = 0;
	protected static final int ALIGN_V_RIGHT = 1;
	protected static final int ALIGN_V_CEN_Y = 2;
	protected static final int ALIGN_H_LEFT = 3;
	protected static final int ALIGN_H_LEFT_CEN_Y = 4;
	protected static final int ALIGN_H_RIGHT = 5;
	protected static final int ALIGN_H_CEN = 6;

	public static void main(String[] args) throws IOException,
			FontFormatException {
		SharedUtils.HOST_MODULE = "warOFFLINE/";
		tester = new Script();
		tester.createImage();
	}

	protected void createImage() throws IOException, FontFormatException {
		createLocations();
		createRecords();
		// printScenes();
		// plotRecords();
		// saveImage();
		saveSceneMaps2();
	}

	protected void printScenes() {
		for (Scene sc : scenes) {
			System.out.println(sc.toString());
		}
	}

	protected void saveImage() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File fFile1 = new File(src + opName);
		RendererUtils.savePNGFile(opImage, fFile1);
		opG.dispose();
		System.out.println("created " + fFile1.getName());
	}

	protected void plotRecords() throws FontFormatException, IOException {
		width = scenes.get(scenes.size() - 1).end;
		height = (int) (width * heightF);
		bWidth = width / 20;
		bHeight = height / 10;
		borderH = height / 20;
		borderW = 0;
		int ww = width + (2 * bWidth) + (2 * borderW);
		int hh = height + (2 * bHeight) + (2 * borderH);
		opImage = RendererUtils.createAlphaBufferedImage(ww, hh);
		opG = (Graphics2D) opImage.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		opG.setColor(Color.BLACK);
		opG.fillRect(0, 0, ww, hh);
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
		plotTitle();
		System.out.println("calculated");
	}

	protected void plotTitle() {
	}

	protected void plotOnscreenOutline() {
		Color col = getColorWithAlpha(Color.WHITE, 0.25f);
		int scc = 0;
		int lastScc = scenes.size() - 2;
		int hoff = (int) characterD;
		PointData prev = null;
		boolean edgeOnly = false;
		for (Scene sc : scenes) {
			for (SceneLocation sl : sc.sceneLocations) {
				PointData p = getOnScreenPoint(sl, scc);
				if (p != null) {
					int x1 = sc.end;
					double y1 = sl.location.y;
					double h1 = sl.location.max;
					if (prev == null) {
						prev = new PointData();
						prev.x1 = sc.start - bWidth - hoff;
						prev.y1 = y1;
						prev.h1 = h1 - hoff;
						prev.x2 = sc.start - bWidth;
						prev.y2 = y1;
						prev.h2 = h1 + hoff;
						plotPointCurved(prev, col, edgeOnly);
					} else if (scc == lastScc) {
						plotPointCurved(prev, col, edgeOnly);
						prev.x2 = sc.end + bWidth;
						plotPointCurved(prev, col, edgeOnly);
					} else {
						plotPointCurved(prev, col, edgeOnly);
						prev.x1 = x1;
						prev.y1 = y1;
						prev.h1 = h1;
					}
				}
			}
			scc++;
		}
	}

	protected PointData getOnScreenPoint(SceneLocation sl, int scc) {
		int i = 0;
		PointData po = null;
		PointData prev = null;
		for (Boolean off : sl.offScene) {
			if (!off) {
				int scIndex = sl.characters.get(i).scenes.indexOf(scc);
				double x1 = sl.characters.get(i).points.get(scIndex).x1;
				double y1 = sl.characters.get(i).points.get(scIndex).y1;
				double h1 = sl.characters.get(i).points.get(scIndex).h1;
				double x2 = sl.characters.get(i).points.get(scIndex).x2;
				double y2 = sl.characters.get(i).points.get(scIndex).y2;
				double h2 = sl.characters.get(i).points.get(scIndex).h2;
				if (prev == null) {
					prev = new PointData();
					prev.x1 = x1;
					prev.y1 = y1;
					prev.h1 = h1;
					prev.x2 = x2;
					prev.y2 = y2;
					prev.h2 = h2;
					po = prev;
				} else {
					po.x1 = x1;
					po.y1 = (prev.y1 + y1) / 2;
					po.x2 = x2;
					po.y2 = (prev.y2 + y2) / 2;
					po.h1 = po.h1 + h1;
					po.h2 = po.h2 + h2;
				}
			}
			i++;
		}
		return po;
	}

	protected void plotBackgroundHalves() throws IOException {
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
			dy1 = height + borderH + bHeight;
			dy2 = height + 2 * borderH + 2 * bHeight;
			opG.drawImage(bgImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
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

	protected void saveSceneMaps2() throws IOException {
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
			opG.setColor(getColorWithAlpha(Color.LIGHT_GRAY, 0.25f));
			String tot = "";
			for (int i = 0; i < num; i++) {
				tot = tot + getRealName(sc.name) + " ";
			}
			int w = 0;
			if (!"".equals(tot.trim()) && x1 != x2) {
				w = plotString((int) x1, tot, borderH + bHeight, ALIGN_V_LEFT,
						fontScFScenes * (x2 - x1), false);
			}
		}
		opG.setClip(null);
	}

	protected void calculateLocationHeights() {
		for (Scene sc : scenes) {
			int clInd = 0;
			for (SceneLocation cl : sc.sceneLocations) {
				calculateLocationHeights(sc, clInd, cl, null);
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
			SceneLocation cl, Boolean on) {
		ArrayList<Character> chs = getScreenCharacters(cl, on);
		int chOff = 0;
		int chInd = 0;
		for (Character ch : chs) {
			boolean offSc = cl.offScene.get(chInd);
			if (onlyShowOnScene && offSc) {
				continue;
			}
			boolean onn = false;
			if (on == null) {
				onn = !offSc;
			} else {
				onn = !offSc;
			}
			int chD = (int) (onn ? characterD : characterD2);
			chOff = chOff + (chD);
			chInd++;
		}
		if (chOff > cl.location.max) {
			cl.location.max = chOff;
		}
	}

	protected void plotLocationNames() {
		boolean useBlock = true;
		for (Location loc : locations) {
			int y = (int) loc.y + bHeight + borderH;
			int h = (int) loc.max;
			if (h == 0) {
				continue;
			}
			Color col = getColorWithAlpha(loc.col, 0.5f);
			int x1 = borderW;
			int x2 = borderW + bWidth + width + bWidth;
			opG.setColor(col);
			Polygon3D block = new Polygon3D();
			block.addPoint(x1, y - h / 2, 0);
			block.addPoint(x2, y - h / 2, 0);
			block.addPoint(x2, y + h / 2, 0);
			block.addPoint(x1, y + h / 2, 0);
			block.addPoint(x1, y - h / 2, 0);
			if (useBlock) {
				opG.fill(block);
			}
			if (plotLocationNames) {
				Shape clip = new Rectangle(borderW, borderH + bHeight, width
						+ 2 * bWidth, height);
				opG.setClip(clip);
				col = getColorWithAlpha(loc.col, 0.5f);
				opG.setColor(col);
				int num = 20;
				String tot = "";
				for (int i = 0; i < num; i++) {
					tot = tot + getRealName(loc.name) + " ";
				}
				plotString((int) (-bWidth), tot, y, ALIGN_H_LEFT_CEN_Y,
						fontScFLocNames * h, true);
				opG.setClip(null);
			} else {
				double fontSc = heightF * fontScFLocs / characterD2F;
				opG.setColor(Color.BLACK);
				opG.drawLine(x1, y - h / 2, x2, y - h / 2);
				opG.drawLine(x1, y + h / 2, x2, y + h / 2);
				opG.setColor(col.brighter().brighter());
				String realName = getRealName(loc.name);
				GlyphVector gv = opG.getFont().createGlyphVector(
						opG.getFontRenderContext(), realName);
				Shape glyph = gv.getOutline();
				AffineTransform at = new AffineTransform();
				AffineTransform sc = AffineTransform.getScaleInstance(fontSc,
						fontSc);
				at.concatenate(sc);
				Shape transformedGlyph = at.createTransformedShape(glyph);
				double fact = 1;
				double hh = transformedGlyph.getBounds().getHeight() * fact;
				double ww = transformedGlyph.getBounds().getWidth() * fact;
				if (ww > h) {
					String real1 = realName.substring(0, realName.indexOf(" "));
					plotString((int) (-bWidth + 2 * hh), real1, y,
							ALIGN_V_CEN_Y, fontSc, false);
					plotString((int) (width + bWidth - 2 * hh), real1, y,
							ALIGN_V_CEN_Y, fontSc, false);
					String real2 = realName
							.substring(realName.indexOf(" ") + 1);
					plotString((int) (-bWidth + hh), real2, y, ALIGN_V_CEN_Y,
							fontSc, false);
					plotString((int) (width + bWidth - 3 * hh), real2, y,
							ALIGN_V_CEN_Y, fontSc, false);
				} else {
					plotString((int) (-bWidth + hh), realName, y,
							ALIGN_V_CEN_Y, fontSc, false);
					plotString((int) (width + bWidth - 2 * hh), realName, y,
							ALIGN_V_CEN_Y, fontSc, false);
				}
			}
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
		}
		return newW;
	}

	protected void calculatePoints() {
		for (Scene sc : scenes) {
			int x1 = sc.start;
			int x2 = sc.end;
			for (SceneLocation cl : sc.sceneLocations) {
				addLocationPoints(sc, x1, x2, cl, null);
				// addLocationPoints(sc, x1, x2, cl, false);
				// addLocationPoints(sc, x1, x2, cl, true);
			}
		}
	}

	protected double getCharacterOffLocationMaxHeight(int x1, int x2,
			SceneLocation cl) {
		double max = 0;
		int pc = 0;
		for (Character ch : cl.characters) {
			boolean off = cl.offScene.get(pc);
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
			boolean off = cl.offScene.get(pc);
			max = max + ((!off) ? characterD : characterD2);
			pc++;
		}
		return max;
	}

	protected void addLocationPoints(Scene sc, int x1, int x2,
			SceneLocation cl, Boolean on) {
		Location loc = cl.location;
		double max = getCharacterOnLocationMaxHeight(x1, x2, cl);
		double locOffset = (loc.max - max) / 2.0;
		double locSt = loc.y - (loc.max / 2) + locOffset;
		double chOff = locSt;
		boolean onn = on != null && !on;
		if (onn) {
			max = getCharacterOffLocationMaxHeight(x1, x2, cl);
			locOffset = (loc.max - max) / 2.0;
			locSt = loc.y - (loc.max / 2) + locOffset;
			chOff = locSt;
		}
		ArrayList<Character> chs = getScreenCharacters(cl, on);
		int chInd = 0;
		for (Character ch : chs) {
			double x11 = x1;
			boolean offSc = cl.offScene.get(chInd);
			if (onlyShowOnScene && offSc) {
				continue;
			}
			boolean onSc = !offSc;
			int chD = (int) (onSc ? characterD : characterD2);
			double h1 = characterD;
			double h2 = chD;
			double y1 = (chOff + (h1 / 2));
			double y2 = ((h2 / 2) + chOff);
			double z = (onSc ? characterD : -characterD);
			double z1 = loc.z;
			double z2 = loc.z;
			// System.out.println(sc.toString());
			if (!ch.points.isEmpty()) {
				int ind = ch.points.size() - 1;
				PointData prev = ch.points.get(ind);
				x11 = prev.x2;
				y1 = prev.y2;
				z1 = prev.z2;
				h1 = prev.h2;
			}
			ch.addPoint(x11, y1, z1, h1, x2, y2, z2, h2, scenes.indexOf(sc),
					!onSc);
			chOff = chOff + (h2);
			chInd++;
		}
	}

	protected ArrayList<Character> getScreenCharacters(SceneLocation cl,
			Boolean on) {
		ArrayList<Character> ret = new ArrayList<Character>();
		int i = 0;
		for (Character ch : cl.characters) {
			if (on == null) {
				ret.add(ch);
			} else if (on && !cl.offScene.get(i)) {
				ret.add(ch);
			} else if (!on && cl.offScene.get(i)) {
				ret.add(ch);
			}
			i++;
		}
		return ret;
	}

	protected void plotPointsByCharacter() {
		int lastToPrint = scenes.get(scenes.size() - 2).end;
		for (Character ch : characters) {
			initCharacter(ch);
			Color col = getColorWithAlpha(getCharacterColour(ch), 0.9f);
			int c = 0;
			int last = ch.points.size() - 1;
			for (PointData p : ch.points) {
				if (c == 0 && plotBirthDeaths) {
					double xx1 = ch.birth.x1;
					double yy1 = ch.birth.y1;
					double zz1 = ch.birth.z1;
					double hh1 = ch.birth.h1;
					double birthOffx = ch.birth.x2;
					double birthOffy = ch.birth.y2;
					plotPointBrith(xx1, yy1, zz1, hh1, birthOffx, birthOffy,
							ch, col);
				}
				if (c == last && p.x2 == lastToPrint) {
					// p.x2 = (int) (width + (bWidth / 2));
					p.x2 = (int) (width);
					double fontSc = fontScFBrithDeath * characterD2;
					opG.setColor(col);
					plotString(p.x2 + characterD2 * 0.5,
							getRealName(ch.description), p.y2 + bHeight
									+ borderH, ALIGN_H_LEFT_CEN_Y, fontSc,
							false);
				}
				plotPointCurved(p, col, false);
				if (plotBirthDeaths && c == last && p.x2 != lastToPrint
						&& ch.death.x1 != 0) {
					double xx1 = ch.death.x1;
					double yy1 = ch.death.y1;
					double zz1 = ch.death.z1;
					double hh1 = ch.death.h1;
					double deathOffx = ch.death.x2;
					double deathOffy = ch.death.y2;
					plotPointDeath(xx1, yy1, zz1, hh1, deathOffx, deathOffy,
							ch, col);
				}
				c++;
			}
		}
	}

	protected void plotPointsByCharacter1() {
		int lastToPrint = scenes.get(scenes.size() - 2).end;
		for (Character ch : characters) {
			initCharacter(ch);
			Color col = getColorWithAlpha(getCharacterColour(ch), 1f);
			int c = 0;
			int last = ch.points.size() - 1;
			Polygon3D pol = new Polygon3D();
			TreeMap<Float, Float> hm = new TreeMap<Float, Float>();
			hm.put(0f, (float) characterD);
			for (PointData p : ch.points) {
				plotPointPath(pol, p, col, false);
				PathLength pl = new PathLength(pol);
				float len = pl.lengthOfPath();
				hm.put(len, (float) p.h2);
			}
			Path2D.Double path = new Path2D.Double();
			for (int i = 0; i < pol.npoints; i++) {
				if (i == 0) {
					path.moveTo(pol.xpoints[i], pol.ypoints[i]);
				} else {
					path.lineTo(pol.xpoints[i], pol.ypoints[i]);
				}
			}
			// drawByPath(path, hm, col);
			opG.setColor(col);
			opG.setStroke(new BasicStroke((float) characterD2));
			opG.draw(path);
			c++;
		}
	}

	private void drawByPath(Path2D pol, TreeMap<Float, Float> hm, Color col) {
		opG.setColor(col);
		PathLength pl = new PathLength(pol);
		float len = pl.lengthOfPath();
		float tot = len;
		Point2D prev = null;
		float iSt = 0;
		float iEn = 0;
		for (float i = 0; i <= tot; i++) {
			float fr = i / tot;
			float pp = len * fr;
			iSt = (hm.lowerKey(pp) == null ? pp : hm.lowerKey(pp));
			iEn = (hm.higherKey(pp) == null ? pp : hm.higherKey(pp));
			float iFr = i / (iEn - iSt);
			float h1 = (hm.lowerEntry(pp) == null ? hm.get(pp) : hm.lowerEntry(
					pp).getValue());
			float h2 = (hm.higherEntry(pp) == null ? h1 : hm.higherEntry(pp)
					.getValue());
			float w = Math.abs(iFr * (h2 - h1));
			Point2D p = pl.pointAtLength(pp);
			if (prev == null) {
				prev = p;
			} else {
				opG.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND,
						BasicStroke.JOIN_BEVEL));
				int x1 = (int) prev.getX();
				int y1 = (int) prev.getY();
				int x2 = (int) p.getX();
				int y2 = (int) p.getY();
				opG.drawLine(x1, y1, x2, y2);
				prev = p;
			}
		}
	}

	protected void initCharacter(Character ch) {
		// TODO Auto-generated method stub
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
		int firstDataRow = 6;
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
		br.close();
		System.out.println("data read");
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
			cl.offScene.add(offScene);
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

	// protected void plotPointStaggered(int x1, int y1, int h1, int x2, int y2,
	// int h2, Color col) {
	// double fr = 0.25;
	// if (x1 < 0) {
	// return;
	// }
	// opG.setColor(col);
	// double x11 = (double) x1;
	// double x22 = (double) x2;
	// double xd = x22 - x11;
	// int x1a = (int) (x1 + xd * fr);
	// int x2a = (int) (x2 - xd * fr);
	//
	// Polygon3D pol = new Polygon3D();
	// addPoint(pol, x1, y1 - (h1 / 2));
	// addPoint(pol, x1a, y1 - (h1 / 2));
	// addPoint(pol, x2a, y2 - (h2 / 2));
	// addPoint(pol, x2, y2 - (h2 / 2));
	// addPoint(pol, x2, y2 + (h2 / 2));
	// addPoint(pol, x2a, y2 + (h2 / 2));
	// addPoint(pol, x1a, y1 + (h1 / 2));
	// addPoint(pol, x1, y1 + (h1 / 2));
	// addPoint(pol, x1, y1 - (h1 / 2));
	// opG.fill(pol);
	// }
	protected void plotPointCurved(PointData p, Color col, boolean edge) {
		Polygon3D pol = createPolygons(p, col);
		drawPolygons(edge, pol);
	}

	protected void plotPointPath(Polygon3D pol, PointData p, Color col,
			boolean edge) {
		double xd = p.x2 - p.x1;
		double r = xd * curveFr;
		if (p.y2 < p.y1) {
			plotCurvePartUp(p.x1, p.x2, p.y1, p.y1, p.y2, p.y2, 0, 0, pol, r,
					false);
		} else if (p.y2 > p.y1) {
			plotCurvePartDown(p.x1, p.x2, p.y1, p.y1, p.y2, p.y2, 0, 0, pol, r,
					false);
		} else {
			addPoint(pol, p.x1, p.y1, 0);
			addPoint(pol, p.x2, p.y2, 0);
		}
	}

	protected void drawPolygons(boolean edge, Polygon3D pol) {
		if (edge) {
			opG.setStroke(new BasicStroke((float) (characterD * 0.1)));
			opG.draw(pol);
		} else {
			opG.fill(pol);
		}
	}

	protected Polygon3D createPolygons(PointData p, Color col) {
		opG.setColor(col);
		double x11 = (double) p.x1;
		double x22 = (double) p.x2;
		double xd = x22 - x11;
		double flatFr = 1 - (3.0 * curveFr);
		int x1aa = (int) (p.x1 + xd * flatFr);
		double r = xd * curveFr;
		if (xd < characterD * 2) {
			flatFr = 1 - (3.0 * curveFr * 1.5);
			x1aa = (int) (p.x1 + xd * flatFr);
			r = xd * curveFr * 1.5;
		}
		double y1t = p.y1 - p.h1 / 2;
		double y1b = p.y1 + p.h1 / 2;
		double y2t = p.y2 - p.h2 / 2;
		double y2b = p.y2 + p.h2 / 2;
		// int pos = 20;
		// int neg = -20;
		// double z1 = (p.h1 == characterD ? pos : neg);
		// double z2 = (p.h2 == characterD ? pos : neg);
		double z1 = p.z1;
		double z2 = p.z2;
		Polygon3D pol = new Polygon3D();
		addPoint(pol, p.x1, y1t, z1);
		addPoint(pol, x1aa, y1t, z1);
		if (y2t < y1t) {
			plotCurvePartUp(x1aa, p.x2, y1t, y1b, y2t, y2b, z1, z2, pol, r,
					false);
		} else if (y1t < y2t) {
			plotCurvePartDown(x1aa, p.x2, y1t, y1b, y2t, y2b, z1, z2, pol, r,
					false);
		} else {
		}
		addPoint(pol, p.x2, y2t, z2);
		addPoint(pol, p.x2, y2b, z2);
		if (y2b < y1b) {
			plotCurvePartUp(x1aa, p.x2, y1t, y1b, y2t, y2b, z1, z2, pol, r,
					true);
		} else if (y1b < y2b) {
			plotCurvePartDown(x1aa, p.x2, y1t, y1b, y2t, y2b, z1, z2, pol, r,
					true);
		} else {
		}
		addPoint(pol, p.x1, y1b, z1);
		addPoint(pol, p.x1, y1t, z1);
		return pol;
	}

	protected void plotCurvePartUp(double x1, double x2, double y1t,
			double y1b, double y2t, double y2b, double z1, double z2,
			Polygon3D pol, double r, boolean reverse) {
		double xd = x2 - x1;
		double x1a = (x1 + r);
		double x2a = (x2 - r);
		ArrayList<Point3D> allPoints = new ArrayList<Point3D>();
		double y1 = y1t;
		double y2 = y2t;
		if (reverse) {
			y1 = y1b;
			y2 = y2b;
		}
		addPoint(allPoints, x1, y1, z1);
		double zd = z2 - z1;
		double a = Math.atan((y1 - y2) / (xd * 0.5));
		double xR1 = Math.tan(a / 2) * r;
		double cX = (x1a - xR1);
		double cY = (y1 - r);
		for (double i = 0; i < a; i = i + a / numCurveSteps) {
			double ix = cX + r * Math.cos(Math.PI * 1.5 + i);
			double iy = cY - r * Math.sin(Math.PI * 1.5 + i);
			double zi = z1 + ((zd * i / a) / 2);
			addPoint(allPoints, ix, iy, zi);
		}
		cX = (int) (x2a + xR1);
		cY = (int) (y2 + r);
		for (double i = a; i > 0; i = i - a / numCurveSteps) {
			double ix = cX + r * Math.cos(Math.PI * 0.5 + i);
			double iy = cY - r * Math.sin(Math.PI * 0.5 + i);
			double zi = z1 + (zd / 2) + ((zd * (a - i) / (a)) / 2);
			addPoint(allPoints, ix, iy, zi);
		}
		if (!reverse) {
			for (Point3D p : allPoints) {
				addPoint(pol, p.x, p.y, p.z);
			}
		} else {
			for (int i = allPoints.size() - 1; i > -1; i--) {
				Point3D p = allPoints.get(i);
				addPoint(pol, p.x, p.y, p.z);
			}
		}
	}

	protected void plotCurvePartDown(double x1, double x2, double y1t,
			double y1b, double y2t, double y2b, double z1, double z2,
			Polygon3D pol, double r, boolean reverse) {
		double x11 = (double) x1;
		double x22 = (double) x2;
		double xd = x22 - x11;
		double x1a = (x1 + r);
		double x2a = (x2 - r);
		ArrayList<Point3D> allPoints = new ArrayList<Point3D>();
		double y1 = y1t;
		double y2 = y2t;
		if (reverse) {
			y1 = y1b;
			y2 = y2b;
		}
		addPoint(allPoints, x1, y1, z1);
		double zd = z2 - z1;
		double a = Math.atan((y2 - y1) / (xd * 0.5));
		double xR1 = Math.tan(a / 2) * r;
		double cX = (x1a - xR1);
		double cY = (y1 + r);
		for (double i = 0; i < a; i = i + a / numCurveSteps) {
			double ix = cX + r * Math.cos(Math.PI * 0.5 - i);
			double iy = cY - r * Math.sin(Math.PI * 0.5 - i);
			double zi = z1 + ((zd * i / a) / 2);
			addPoint(allPoints, ix, iy, zi);
		}
		cX = (x2a + xR1);
		cY = (y2 - r);
		for (double i = a; i > 0; i = i - a / numCurveSteps) {
			double ix = cX + r * Math.cos(Math.PI * 1.5 - i);
			double iy = cY - r * Math.sin(Math.PI * 1.5 - i);
			double zi = z1 + (zd / 2) + ((zd * (a - i) / (a)) / 2);
			addPoint(allPoints, ix, iy, zi);
		}
		if (!reverse) {
			for (Point3D p : allPoints) {
				addPoint(pol, p.x, p.y, p.z);
			}
		} else {
			for (int i = allPoints.size() - 1; i > -1; i--) {
				Point3D p = allPoints.get(i);
				addPoint(pol, p.x, p.y, p.z);
			}
		}
	}

	protected void addPoint(Polygon3D pol, double x, double y, double z) {
		pol.addPoint(x + bWidth + borderW, y + bHeight + borderH, z);
	}

	protected void addPoint(ArrayList<Point3D> allPoints, double x, double y,
			double z) {
		Point3D p = new Point3D();
		p.x = (int) x;
		p.y = (int) y;
		p.z = (int) z;
		allPoints.add(p);
	}

	protected void plotPointBrith(double x1, double y1, double z1, double h1,
			double birthOffx, double birthOffy, Character ch, Color col) {
		double fontSc = fontScFBrithDeath * characterD2;
		opG.setColor(col);
		String name = getRealName(ch.description);
		int w = plotString(x1 - birthOffx, name, borderH, ALIGN_V_LEFT, fontSc,
				false);
		int top = w - bHeight;
		int d = (int) birthDeathD;
		if (x1 < 0) {
			return;
		}
		Polygon3D pol = new Polygon3D();
		addPoint(pol, x1 - birthOffx, top, h1);
		addPoint(pol, x1 - birthOffx + d, top, h1);
		addPoint(pol, x1 - birthOffx + d, y1 - (h1 / 2), h1);
		addPoint(pol, x1, y1 - (h1 / 2), h1);
		addPoint(pol, x1, y1 + (h1 / 2), h1);
		addPoint(pol, x1 - birthOffx + d, y1 + (h1 / 2), h1);
		int cX = (int) (x1 - birthOffx + d);
		int cY = (int) (y1 + (h1 / 2) - d);
		double a = Math.PI * 0.5;
		for (double i = Math.PI * 1.5; i >= Math.PI; i = i - a / numCurveSteps) {
			double ix = cX + d * Math.cos(i);
			double iy = cY - d * Math.sin(i);
			addPoint(pol, (int) ix, (int) iy, h1);
		}
		opG.fill(pol);
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

	protected void plotPointDeath(double x1, double y1, double z1, double h1,
			double deathOffx, double deathOffy, Character ch, Color col) {
		double fontSc = fontScFBrithDeath * characterD2;
		String name = getRealName(ch.description);
		opG.setColor(col);
		int d = (int) birthDeathD;
		if (x1 < 0) {
			return;
		}
		int w = plotString(x1 + deathOffx, name, height + bHeight,
				ALIGN_V_RIGHT, fontSc, false);
		int bot = height + bHeight - w;
		Polygon3D pol = new Polygon3D();
		addPoint(pol, x1, y1 - (h1 / 2), h1);
		addPoint(pol, x1 + deathOffx, y1 - (h1 / 2), h1);
		int cX = (int) (x1 + deathOffx);
		int cY = (int) (y1 - (h1 / 2) + d);
		double a = Math.PI * 0.5;
		for (double i = Math.PI * 0.5; i >= 0; i = i - a / numCurveSteps) {
			double ix = cX + d * Math.cos(i);
			double iy = cY - d * Math.sin(i);
			addPoint(pol, (int) ix, (int) iy, h1);
		}
		addPoint(pol, x1 + deathOffx + d, bot, h1);
		addPoint(pol, x1 + deathOffx, bot, h1);
		addPoint(pol, x1 + deathOffx, y1 + (h1 / 2), h1);
		addPoint(pol, x1, y1 + (h1 / 2), h1);
		opG.fill(pol);
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
		public ArrayList<Boolean> offScene = new ArrayList<Boolean>();
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

	protected class Polygon3D extends Polygon {
		ArrayList<Integer> zPoints = new ArrayList<Integer>();

		public void addPoint(double x, double y, double z) {
			super.addPoint((int) x, (int) y);
			this.zPoints.add((int) z);
		}
	}
}
