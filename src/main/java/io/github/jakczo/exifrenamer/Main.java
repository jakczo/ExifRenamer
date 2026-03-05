package io.github.jakczo.exifrenamer;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

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

        Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        // Formatter for EXIF timestamp
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");

        int counter = 1;

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

                String formattedDate = formatter.format(date);
                String newName = String.format("%05d_IMG_%s.jpg", counter, formattedDate);
                Path source = file.toPath();
                Path target = source.resolveSibling(newName);
                Files.move(source, target);
                System.out.println(counter + ": Renamed " + file.getName() + " -> " + newName);
            } catch (Exception e) {
                System.out.println(counter + ": Failed " + file.getName());
            }
            counter++;
        }
    }
}
