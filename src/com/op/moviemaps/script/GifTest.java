package com.op.moviemaps.script;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import com.op.moviemaps.services.SharedUtils;

public class GifTest {
	private static GifTest tester;
	protected static String dir = "scripts/MAT/";
	protected static String srcDir = dir + "src/";
	private String file = "scene";
	private ImageOutputStream output;
	private GifSequenceWriter writer;
	boolean ref = false;
	private GifSequenceWriter writerRef;
	private ImageOutputStream outputRef;
	int w = 640;
	int h = 360;
	int wr = 100;
	int hr = 1;
	int fps = 2;
	int step = 20;
	ArrayList<File> listOfFiles;

	public static void main(String[] args) throws Exception {
		tester = new GifTest();
		tester.create();
	}

	public void create() throws Exception {
		String path = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + srcDir;
		File folder = new File(path);
		File[] listOfFilesR = folder.listFiles();
		listOfFiles = sort(listOfFilesR);
		int len = listOfFiles.size();

		String out = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir + file
				+ ".gif";
		File fOut = new File(out);
		if (fOut.exists()) {
			fOut.delete();
		}

		output = new FileImageOutputStream(fOut);
		if (ref) {
			String outRef = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
					+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + dir + file
					+ "REF.gif";
			File fOutRef = new File(outRef);
			if (fOutRef.exists()) {
				fOutRef.delete();
			}
			outputRef = new FileImageOutputStream(fOutRef);
		}

		String src = SharedUtils.HOST_MODULE + SharedUtils.DIR_PROCESS
				+ SharedUtils.DIR_IMAGES + SharedUtils.DIR_OUT + srcDir + file
				+ "00001" + ".jpg";
		BufferedImage firstImage = ImageIO.read(new File(src));
		writer = new GifSequenceWriter(output, firstImage.getType(), "" + fps,
				true);
		writer.writeToSequence(createPix(0));

		if (ref) {
			writerRef = new GifSequenceWriter(outputRef, firstImage.getType(),
					"" + fps, true);
			writerRef.writeToSequence(createRef(0));
		}

		int num = len;
		for (int i = 1; i < num; i = i + step) {
			writer.writeToSequence(createPix(i));
			if (ref) {
				writerRef.writeToSequence(createRef(i));
			}
		}
		writer.close();
		output.close();
		if (ref) {
			writerRef.close();
			outputRef.close();
		}
		System.out.println("FINISHED");
	}

	private ArrayList<File> sort(File[] listOfFilesR) {
		ArrayList<File> files = new ArrayList<File>();
		for (File f : listOfFilesR) {
			if (f.getAbsoluteFile().toString().indexOf("jpg") > -1) {
				files.add(f);
			}
		}
		Collections.sort(files, new java.util.Comparator<File>() {
			@Override
			public int compare(File s1, File s2) {
				String name1 = s1.getName();
				int end = name1.indexOf("scene") + 5;
				if (name1.length() == 14) {
					name1 = "scene0" + name1.substring(end);
				}

				String name2 = s2.getName();
				int end2 = name2.indexOf("scene") + 5;
				if (name2.length() == 14) {
					name2 = "scene0" + name2.substring(end2);
				}
				return name1.compareTo(name2);
			}
		});
		return files;
	}

	public BufferedImage createPix(double i) throws IOException {
		File f = listOfFiles.get((int) i);
		BufferedImage im = ImageIO.read(f);
		double len = listOfFiles.size();

		int xs = 0;
		int ys = 5;
		int ws = im.getWidth();
		int hs = im.getHeight() - 10;
		BufferedImage resized = new BufferedImage(w, h, im.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		int xF = (int) (((double) w) * (i / len));
		// g.drawImage(im, xF, 0, xF + 1, h, xs, ys, ws, hs, null);
		g.drawImage(im, 0, 0, w, h, xs, ys, ws, hs, null);
		return resized;
	}

	private RenderedImage createRef(double i) {
		BufferedImage resized = new BufferedImage(wr, hr,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		double x = wr * i / ((double) (listOfFiles.size()));
		int xx = (int) x;
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, wr, hr);
		g.setColor(Color.RED);
		g.drawLine(xx, 0, xx, hr);
		g.dispose();
		return resized;
	}

}
