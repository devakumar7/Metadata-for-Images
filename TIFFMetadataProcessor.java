package com.ctel.tiff;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import com.github.jaiimageio.plugins.tiff.BaselineTIFFTagSet;
import com.github.jaiimageio.plugins.tiff.TIFFDirectory;
import com.github.jaiimageio.plugins.tiff.TIFFField;
import com.github.jaiimageio.plugins.tiff.TIFFTag;

public class TIFFMetadataProcessor {

	public static void main(String[] args) throws IOException {
		BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_BYTE_GRAY);
		int resolution = 600; // Set your desired resolution (e.g., 300 DPI)

		// Create an ImageWriter for TIFF format
		ImageWriter writer = ImageIO.getImageWritersByFormatName("TIFF").next();

		// Create an ImageWriteParam (you can configure it as needed)
		ImageWriteParam writerParams = writer.getDefaultWriteParam();

		// Create TIFF metadata with resolution
		IIOMetadata tiffMetadata = createMetadata(writer, writerParams, resolution);

		// Create an IIOImage with the BufferedImage and TIFF metadata
		IIOImage iioImage = new IIOImage(image, null, tiffMetadata);

		// Specify the output file path
		String outputPath = "output300.tiff";

		// Create an ImageOutputStream for the output file
		ImageOutputStream outputStream = ImageIO.createImageOutputStream(new File(outputPath));

		// Set the output of the ImageWriter to the ImageOutputStream
		writer.setOutput(outputStream);

		// Write the IIOImage to the output file
		writer.write(null, iioImage, writerParams);

		// Close the ImageOutputStream and ImageWriter
		outputStream.close();
		writer.dispose();
	}

	private static IIOMetadata createMetadata(ImageWriter writer, ImageWriteParam writerParams, int resolution)
			throws IIOInvalidTreeException {
		// Get default metadata from writer
		ImageTypeSpecifier type = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_BYTE_GRAY);
		IIOMetadata meta = writer.getDefaultImageMetadata(type, writerParams);

		// Convert default metadata to TIFF metadata
		TIFFDirectory dir = TIFFDirectory.createFromMetadata(meta);

		// Get {X,Y} resolution tags
		BaselineTIFFTagSet base = BaselineTIFFTagSet.getInstance();
		TIFFTag tagXRes = base.getTag(BaselineTIFFTagSet.TAG_X_RESOLUTION);
		TIFFTag tagYRes = base.getTag(BaselineTIFFTagSet.TAG_Y_RESOLUTION);

		// Create {X,Y} resolution fields
		TIFFField fieldXRes = new TIFFField(tagXRes, TIFFTag.TIFF_RATIONAL, 1, new long[][] { { resolution, 1 } });
		TIFFField fieldYRes = new TIFFField(tagYRes, TIFFTag.TIFF_RATIONAL, 1, new long[][] { { resolution, 1 } });

		// Add {X,Y} resolution fields to TIFFDirectory
		dir.addTIFFField(fieldXRes);
		dir.addTIFFField(fieldYRes);

		// Add unit field to TIFFDirectory (change to RESOLUTION_UNIT_CENTIMETER if necessary)
		dir.addTIFFField(new TIFFField(base.getTag(BaselineTIFFTagSet.TAG_RESOLUTION_UNIT),
				BaselineTIFFTagSet.RESOLUTION_UNIT_INCH));

		// Return TIFF metadata so it can be picked up by the IIOImage
		return dir.getAsMetadata();
	}
}
