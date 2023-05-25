package com.op.moviemaps.script;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
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
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;

/**
 * For Frame2 use scale 1230 x 523 For PC use scale 1123 x 889
 * 
 * @author sParekh
 * 
 */
public class ScriptPathMinimal extends ScriptPath {

	public static void main(String[] args) throws Exception, FontFormatException {
		tester = new ScriptPathMinimal();
		tester.opName = tester.scName + "ScMin_OUT.jpg";
		tester.bg = false;
		tester.createImage();
	}

	protected void plotSceneNames() {

	}

	protected void plotLocationNames() {
	}


}
