package com.op.moviemaps.services;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Logger;


public class RendererUtils {
	private static final Logger log = Logger.getLogger(RendererUtils.class
			.getName());

	public static void compressJpegFile(BufferedImage opImage, File outfile,
			float compressionQuality) throws IOException {
		try {
			// Find a jpeg writer
			ImageWriter writer = null;
			Iterator<ImageWriter> iter = ImageIO
					.getImageWritersByFormatName("jpg");
			if (iter.hasNext()) {
				writer = (ImageWriter) iter.next();
			}
			// Prepare output file
			ImageOutputStream ios = ImageIO.createImageOutputStream(outfile);
			writer.setOutput(ios);
			// Set the compression quality
			JPEGImageWriteParam iwparam = new JPEGImageWriteParam(
					Locale.getDefault());
			iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			iwparam.setCompressionQuality(compressionQuality);
			// Write the image
			writer.write(null, new IIOImage(opImage, null, null), iwparam);
			// Cleanup
			ios.flush();
			writer.dispose();
			ios.close();
			opImage = null;
			System.gc();
			log.info("saved jpg : " + outfile.getPath());
		} catch (IOException e) {
			log.info("compressJpegFile" + e.getMessage());
			throw e;
		}
	}

	public static void savePNGFile(BufferedImage opImage, File outfile)
			throws IOException {
		try {
			// Find a jpeg writer
			ImageWriter writer = null;
			Iterator<ImageWriter> iter = ImageIO
					.getImageWritersByFormatName("png");
			if (iter.hasNext()) {
				writer = (ImageWriter) iter.next();
			}
			// Prepare output file
			ImageOutputStream ios = ImageIO.createImageOutputStream(outfile);
			writer.setOutput(ios);
			// Write the image
			writer.write(null, new IIOImage(opImage, null, null), null);
			// Cleanup
			ios.flush();
			writer.dispose();
			ios.close();
			opImage = null;
			System.gc();
		} catch (IOException e) {
			log.info("compressJpegFile" + e.getMessage());
			throw e;
		}
	}

	public static void saveJPGFile(BufferedImage opImage, String filePath, double dpi, float quality) {
		try {
			File outfile = new File(filePath);
			// Find a jpeg writer
			ImageWriter writer = null;
			Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
			IIOMetadata metadata = null;
			if (iter.hasNext()) {
				writer = iter.next();
				ImageWriteParam writeParam = writer.getDefaultWriteParam();
				if (writeParam.canWriteCompressed()) {
					writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					writeParam.setCompressionQuality(0.05f);
				}
				ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier
						.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);
				metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
				if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
					// continue;
				}
				double dpmm = dpi / 25.4;
				IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
				horiz.setAttribute("value", Double.toString(dpmm));
				IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
				vert.setAttribute("value", Double.toString(dpmm));
				IIOMetadataNode dim = new IIOMetadataNode("Dimension");
				dim.appendChild(horiz);
				dim.appendChild(vert);
				IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
				root.appendChild(dim);
				metadata.mergeTree("javax_imageio_1.0", root);
			}
			// Prepare output file
			ImageOutputStream ios = ImageIO.createImageOutputStream(outfile);
			writer.setOutput(ios);
			// Write the image
			writer.write(null, new IIOImage(opImage, null, metadata), null);
			// Cleanup
			ios.flush();
			writer.dispose();
			ios.close();
			opImage = null;
			System.gc();
			System.out.println("Saved " + filePath);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

//	public static void saveJPGFile(BufferedImage opImage, File outfile,
//			double dpi) throws Exception {
//		try {
//			com.sun.image.codec.jpeg.JPEGEncodeParam param = com.sun.image.codec.jpeg.JPEGCodec
//					.getDefaultJPEGEncodeParam(opImage);
//			param.setXDensity((int) dpi);
//			param.setYDensity((int) dpi);
//			param.setDensityUnit(com.sun.image.codec.jpeg.JPEGDecodeParam.DENSITY_UNIT_DOTS_INCH);
//			// Prepare output file
//			FileOutputStream fos = new FileOutputStream(outfile);
//			JPEGImageEncoder enc = JPEGCodec.createJPEGEncoder(fos, param);
//			enc.encode(opImage);
//			opImage = null;
//			System.gc();
//		} catch (Exception e) {
//			log.info("compress jpg File " + e.getMessage());
//			throw e;
//		}
//	}

	public static void savePNGFile(BufferedImage opImage, File outfile,
			double dpi) throws Exception {
		try {
			// Find a jpeg writer
			ImageWriter writer = null;
			Iterator<ImageWriter> iter = ImageIO
					.getImageWritersByFormatName("png");
			IIOMetadata metadata = null;
			if (iter.hasNext()) {
				writer = (ImageWriter) iter.next();
				ImageWriteParam writeParam = writer.getDefaultWriteParam();
				ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier
						.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);
				metadata = writer.getDefaultImageMetadata(typeSpecifier,
						writeParam);
				if (metadata.isReadOnly()
						|| !metadata.isStandardMetadataFormatSupported()) {
					// continue;
				}
				double dpmm = dpi / 25.4;
				IIOMetadataNode horiz = new IIOMetadataNode(
						"HorizontalPixelSize");
				horiz.setAttribute("value", Double.toString(dpmm));
				IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
				vert.setAttribute("value", Double.toString(dpmm));
				IIOMetadataNode dim = new IIOMetadataNode("Dimension");
				dim.appendChild(horiz);
				dim.appendChild(vert);
				IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
				root.appendChild(dim);
				metadata.mergeTree("javax_imageio_1.0", root);
			}
			// Prepare output file
			ImageOutputStream ios = ImageIO.createImageOutputStream(outfile);
			writer.setOutput(ios);
			// Write the image
			writer.write(null, new IIOImage(opImage, null, metadata), null);
			// Cleanup
			ios.flush();
			writer.dispose();
			ios.close();
			opImage = null;
			System.gc();
		} catch (Exception e) {
			log.info("compress png File " + e.getMessage());
			throw e;
		}
	}

	public static void saveTIFFFile(BufferedImage opImage, File outfile,
			double dpi) throws Exception {
		try {
			// Find a jpeg writer
			ImageWriter writer = null;
			Iterator<ImageWriter> iter = ImageIO
					.getImageWritersByFormatName("tiff");
			IIOMetadata metadata = null;
			if (iter.hasNext()) {
				writer = (ImageWriter) iter.next();
				ImageWriteParam writeParam = writer.getDefaultWriteParam();
				ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier
						.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);
				metadata = writer.getDefaultImageMetadata(typeSpecifier,
						writeParam);
				if (metadata.isReadOnly()
						|| !metadata.isStandardMetadataFormatSupported()) {
					// continue;
				}
				double dpmm = dpi / 25.4;
				IIOMetadataNode horiz = new IIOMetadataNode(
						"HorizontalPixelSize");
				horiz.setAttribute("value", Double.toString(1 / dpmm));
				IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
				vert.setAttribute("value", Double.toString(1 / dpmm));
				IIOMetadataNode dim = new IIOMetadataNode("Dimension");
				dim.appendChild(horiz);
				dim.appendChild(vert);
				IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
				root.appendChild(dim);
				metadata.mergeTree("javax_imageio_1.0", root);
			}
			// Prepare output file
			ImageOutputStream ios = ImageIO.createImageOutputStream(outfile);
			writer.setOutput(ios);
			// Write the image
			writer.write(null, new IIOImage(opImage, null, metadata), null);
			// Cleanup
			ios.flush();
			writer.dispose();
			ios.close();
			opImage = null;
			System.gc();
		} catch (Exception e) {
			log.info("compress tiff File " + e.getMessage());
			throw e;
		}
	}

	public static BufferedImage createAlphaBufferedImage(int ww, int hh) {
		BufferedImage opImage;
		opImage = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_ARGB);
		return opImage;
	}

	public static BufferedImage createBufferedImage(int ww, int hh) {
		BufferedImage opImage;
		opImage = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_RGB);
		return opImage;
	}

}
