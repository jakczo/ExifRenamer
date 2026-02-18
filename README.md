# ExifRenamer
A tool that replaces generic iPhone photo filenames (e.g., IMG_XXXX) with unique, timestamp-based names derived from the photo’s EXIF capture date, preventing filename collisions after import to a PC.

CLI workflow:
java -jar exif-renamer.jar /Users/Admin/Photos

Expected result:
Renamed: IMG_45544.jpg -> IMG_20260218_101530.jpg
Renamed: IMG_45545.jpg -> IMG_20260218_101532.jpg
...
