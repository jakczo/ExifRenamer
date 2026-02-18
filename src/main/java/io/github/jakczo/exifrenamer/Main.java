package io.github.jakczo.exifrenamer;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
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

        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".jpeg")
        );

        if (files == null) return;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");

        for (File file : files) {
            try {
                Metadata metadata = ImageMetadataReader.readMetadata(file);
                ExifSubIFDDirectory directory =
                        metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

                if (directory == null) continue;

                Date date = directory.getDateOriginal();
                if (date == null) continue;

                String newName = "IMG_" + formatter.format(date) + ".jpg";

                Path source = file.toPath();
                Path target = source.resolveSibling(newName);

                Files.move(source, target);

                System.out.println("Renamed: " + file.getName() + " -> " + newName);

            } catch (Exception e) {
                System.out.println("Failed: " + file.getName());
            }
        }
    }
}
