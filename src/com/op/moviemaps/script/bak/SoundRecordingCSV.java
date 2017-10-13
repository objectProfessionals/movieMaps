package com.op.moviemaps.script.bak;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import com.op.moviemaps.script.PathLength;
import com.op.moviemaps.services.RendererUtils;
import com.op.moviemaps.services.SharedUtils;

public class SoundRecordingCSV {
	protected static String scName = "ESB";
	protected static String dir = "scripts/" + scName + "/";
	private static SoundRecordingCSV recorder;
	private File csv;
	private BufferedWriter writer;
	private FileWriter fw;
	private int seconds = 100; // 60*120;
	Path2D.Double path = new Path2D.Double();

	public static void main(String[] args) throws IOException {
		recorder = new SoundRecordingCSV();
		recorder.initCapture();
		recorder.startCapture();
		recorder.saveCapture();

		// recorder.initCapture();
		// recorder.startCapture2();

		// recorder.readCapture();
	}

	public void initCapture() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		csv = new File(src + scName + "REC.csv");
		if (!csv.exists()) {
			csv.createNewFile();
		}
		fw = new FileWriter(csv);
		writer = new BufferedWriter(fw);
		path.moveTo(0, 0);
	}

	public void readCapture() throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		BufferedReader br = new BufferedReader(new FileReader(src + scName
				+ "REC.csv"));
		String line;
		long start = -1;
		System.out.println("reading... ");
		double mint = 0;
		double maxt = 0;
		double minv = 9999;
		double maxv = 0;
		TreeMap<Double, Double> tm = new TreeMap<Double, Double>();
		while ((line = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, ",");
			String el = st.nextToken().toString();
			Long xx = Long.parseLong(el);
			if (start == -1) {
				start = xx;
			}
			Double x = (xx - start) / 1000.0;
			el = st.nextToken().toString();
			Double y = Double.parseDouble(el);
			if (x > maxt) {
				maxt = x;
			}
			if (y < minv) {
				minv = y;
			}
			if (y > maxv) {
				maxv = y;
			}
			tm.put(x, y);
			maxt = x;
		}

		TreeMap<Integer, Float> tm2 = normalisePlot(mint, minv, maxv, tm);

		// BufferedImage opImage = plotHeightmap(maxt, tm2);
		BufferedImage opImage = getAttenuatedBackgroundMap(maxt, tm2);

		System.out.println("painting... ");
		File fFile1 = new File(src + scName + "_AUD2.png");
		RendererUtils.savePNGFile(opImage, fFile1);
		System.out.println("created " + fFile1.getName());
	}

	protected BufferedImage getAttenuatedBackgroundMap(double maxt,
			TreeMap<Integer, Float> tm2) throws IOException {
		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir;
		File inputFile = new File(src + scName + "Paint.png");
		BufferedImage bgImage = ImageIO.read(inputFile);

		int w = (int) maxt;
		int h = 2000;
		BufferedImage opImage = RendererUtils.createAlphaBufferedImage(w, h);
		Graphics2D opG = (Graphics2D) opImage.getGraphics();

		int hOff = (int) (((double) h) * 0.25);
		double hFac = 0.3;
		for (int x = 0; x < w; x++) {
			int sx1 = x;
			int sx2 = sx1 + 1;
			int sy1 = 0;
			int sy2 = bgImage.getHeight();
			int dx1 = x;
			int dx2 = dx1 + 1;
			int dcy = h / 2;
			int hf = (int) (tm2.get(x) * h * hFac);
			int dy1 = dcy - hOff - hf;
			int dy2 = dcy + hOff + hf;
			System.out.println("x=" + x + "," + hf);
			opG.drawImage(bgImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
		}

		opG.dispose();
		return opImage;

	}

	private BufferedImage plotHeightmap(double maxt, TreeMap<Integer, Float> tm2) {
		System.out.println("creating... ");
		int w = (int) maxt;
		int h = 2000;
		BufferedImage opImage = RendererUtils.createAlphaBufferedImage(w, h);
		Graphics2D opG = (Graphics2D) opImage.getGraphics();
		opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		opG.setColor(Color.WHITE);
		opG.fillRect(0, 0, w, h);
		for (int x : tm2.keySet()) {
			float c = tm2.get(x);
			Color cc = new Color(c, c, c);
			opG.setColor(cc);
			// System.out.println("xx=" + x + "," + c);
			opG.fillRect(x, 0, x + 1, h);
		}
		opG.dispose();
		return opImage;
	}

	private TreeMap<Integer, Float> normalisePlot(double mint, double minv,
			double maxv, TreeMap<Double, Double> tm) {
		TreeMap<Integer, Float> tm2 = new TreeMap<Integer, Float>();
		for (double t : tm.keySet()) {
			double v = tm.get(t);
			double xx = (t - mint);
			int x = (int) xx;
			float c = (float) ((v - minv) / (maxv - minv));
			c = (float) Math.pow(c, 0.5f);
			tm2.put(x, c);
		}
		return tm2;
	}

	public void saveCapture() throws IOException {
		PathLength pl = new PathLength(path);
		for (float l = 0; l <= seconds * 1000; l = l + 1000) {
			Point2D p = pl.pointAtLength(l);
			double y = p.getY();
			writer.write(y + ",");
			System.out.println(0 + ": " + y);
		}
		writer.flush();
		writer.close();
		fw.close();
	}

	public void startCapture() {
		ByteArrayOutputStream byteArrayOutputStream;
		TargetDataLine targetDataLine;
		int cnt;
		boolean stopCapture = false;
		int rate = 44100;
		float rateF = (float) rate;
		byte tempBuffer[] = new byte[rate];
		double count;
		short convert[] = new short[tempBuffer.length];
		long start = new Date().getTime();
		try {
			byteArrayOutputStream = new ByteArrayOutputStream();
			stopCapture = false;
			while (!stopCapture) {
				AudioFormat audioFormat = new AudioFormat(rateF, 16, 1, true,
						false);
				DataLine.Info dataLineInfo = new DataLine.Info(
						TargetDataLine.class, audioFormat);
				targetDataLine = (TargetDataLine) AudioSystem
						.getLine(dataLineInfo);
				targetDataLine.open(audioFormat);
				targetDataLine.start();
				cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
				byteArrayOutputStream.write(tempBuffer, 0, cnt);
				try {
					count = 0;
					for (int i = 0; i < tempBuffer.length; i++) {
						convert[i] = tempBuffer[i];
						count = count + convert[i];
					}
					double dAvg = count / tempBuffer.length;
					double sumMeanSquare = 0d;
					for (int j = 0; j < tempBuffer.length; j++)
						sumMeanSquare = sumMeanSquare
								+ Math.pow(tempBuffer[j] - dAvg, 2d);
					double averageMeanSquare = sumMeanSquare
							/ tempBuffer.length;
					double count2 = (Math.pow(averageMeanSquare, 0.5d) + 0.5);
					int count3 = (int) ((count2));
					long now = new Date().getTime();
					path.lineTo(now - start, count3);
					System.out.println("->" + count2);
					writer.write(now + "," + count2);
					writer.newLine();
					writer.flush();
				} catch (StringIndexOutOfBoundsException e) {
					System.out.println(e.getMessage());
				}
				targetDataLine.close();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void startCapture2() {
		ByteArrayOutputStream byteArrayOutputStream;
		SourceDataLine sourceDataLine;
		int cnt;
		boolean stopCapture = false;
		int rate = 44100;
		float rateF = (float) rate;
		byte tempBuffer[] = new byte[rate];
		double count;
		short convert[] = new short[tempBuffer.length];
		long start = new Date().getTime();
		try {
			byteArrayOutputStream = new ByteArrayOutputStream();
			stopCapture = false;
			while (!stopCapture) {
				AudioFormat audioFormat = new AudioFormat(rateF, 16, 2, true,
						false);
				DataLine.Info dataLineInfo = new DataLine.Info(
						SourceDataLine.class, audioFormat);
				sourceDataLine = (SourceDataLine) AudioSystem
						.getLine(dataLineInfo);
				sourceDataLine.open(audioFormat);
				sourceDataLine.start();
				try {
					System.out.println("->" + sourceDataLine.getLevel());
				} catch (StringIndexOutOfBoundsException e) {
					System.out.println(e.getMessage());
				}
				sourceDataLine.close();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
