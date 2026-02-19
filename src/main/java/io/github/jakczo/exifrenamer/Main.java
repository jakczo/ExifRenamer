package io.github.jakczo.exifrenamer;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("Usage: java -jar exif-renamer.jar <folder_path>");
            return;
        }

        File folder = new File(args[0]);

        if (!folder.isDirectory()) {
            System.out.println("Provided path is not a folder.");
            return;
        }

        // Filter JPG/JPEG files in the folder
        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".jpeg")
        );

        if (files == null) return;

        // Formatter for EXIF timestamp
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");

        // Map to keep track of how many files share the same timestamp
        Map<String, Integer> timestampCounter = new HashMap<>();

        for (File file : files) {
            try {
                // Read metadata from image
                Metadata metadata = ImageMetadataReader.readMetadata(file);
                ExifSubIFDDirectory directory =
                        metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

                if (directory == null) continue;

                // Get the original capture date
                Date date = directory.getDateOriginal();
                if (date == null) continue;

                // Format the timestamp
                String formattedDate = formatter.format(date);

                // Base name for the new file
                String baseName = "IMG_" + formattedDate;

                // Get the counter for this timestamp
                int count = timestampCounter.getOrDefault(baseName, 0);

                // Create the final filename with zero-padded counter if needed
                String newFileName;
                if (count == 0) {
                    newFileName = baseName + ".jpg";
                } else {
                    // zero-pad to 2 digits (_01, _02, etc.)
                    newFileName = String.format("%s_%02d.jpg", baseName, count);
                }

                // Update the counter for this timestamp
                timestampCounter.put(baseName, count + 1);

                // Move/rename the file
                Path source = file.toPath();
                Path target = source.resolveSibling(newFileName);

                Files.move(source, target);

                System.out.println("Renamed: " + file.getName() + " -> " + newFileName);

            } catch (Exception e) {
                System.out.println("Failed: " + file.getName());
            }
        }
    }
}
