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

        // Show help when no args or when help flag is provided
        if (args.length == 0 || hasArg(args, "--help", "-h")) {
            printUsage();
            return;
        }

        // Dry-run mode: do not rename files, only print planned changes
        boolean dryRun = hasArg(args, "--dry-run", "-n");

        File folder = new File(args[0]);
        if (!folder.isDirectory()) {
            System.out.println("Provided path is not a folder.");
            return;
        }

        // List only JPG/JPEG files in the provided folder
        File[] files = folder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg");
        });

        if (files == null) return;

        // Timestamp format used in the new file name
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");

        for (File file : files) {
            try {
                // Read metadata from the image
                Metadata metadata = ImageMetadataReader.readMetadata(file);

                // Get the EXIF SubIFD directory (contains DateTimeOriginal)
                ExifSubIFDDirectory directory =
                        metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

                if (directory == null) {
                    System.out.println("Skipped (no EXIF): " + file.getName());
                    continue;
                }

                // Read the original capture date (DateTimeOriginal)
                Date date = directory.getDateOriginal();
                if (date == null) {
                    System.out.println("Skipped (no DateTimeOriginal): " + file.getName());
                    continue;
                }

                String timestamp = formatter.format(date);

                // Preserve the original extension (.jpg/.jpeg) to avoid unnecessary changes
                String ext = getExtensionOrDefaultJpg(file.getName());
                String base = "IMG_" + timestamp;

                Path source = file.toPath();

                // First try without a numeric suffix
                Path target = source.resolveSibling(base + ext);

                // If the target already exists, append _1, _2, _3, ... until a free name is found
                if (Files.exists(target)) {
                    int i = 1;
                    while (true) {
                        Path candidate = source.resolveSibling(base + "_" + i + ext);
                        if (!Files.exists(candidate)) {
                            target = candidate;
                            break;
                        }
                        i++;
                    }
                }

                // If the source name is already the desired target name, skip
                if (source.equals(target)) {
                    System.out.println("Skipped (already named): " + file.getName());
                    continue;
                }

                if (dryRun) {
                    // Print planned rename, but do not change anything
                    System.out.println("[DRY-RUN] Would rename: " + file.getName() + " -> " + target.getFileName());
                } else {
                    // Rename/move without overwriting existing files
                    Files.move(source, target);
                    System.out.println("Renamed: " + file.getName() + " -> " + target.getFileName());
                }

            } catch (Exception e) {
                System.out.println("Failed: " + file.getName() + " (" + e.getClass().getSimpleName() + ")");
            }
        }
    }

    private static boolean hasArg(String[] args, String... flags) {
        for (String a : args) {
            for (String f : flags) {
                if (a.equalsIgnoreCase(f)) return true;
            }
        }
        return false;
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java -jar exif-renamer.jar <folder_path> [--dry-run|-n]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --dry-run, -n   Do not rename anything, only print planned changes.");
        System.out.println("  --help, -h      Show this help.");
    }

    private static String getExtensionOrDefaultJpg(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpeg")) return ".jpeg";
        if (lower.endsWith(".jpg")) return ".jpg";
        return ".jpg";
    }
}