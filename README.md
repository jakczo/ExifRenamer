# ExifRenamer
A tool that replaces generic iPhone photo filenames (e.g., IMG_XXXX) with unique, timestamp-based names derived from the photo’s EXIF capture date, preventing filename collisions after import to a PC.

CLI workflow:
>java -jar exif-renamer.jar /Users/Admin/Photos


Expected result:

4135: Renamed IMG_6483.JPG -> 4135_IMG_20260209_112310.jpg

4136: Renamed IMG_8486.JPG -> 4136_IMG_20260210_193339.jpg

4137: Renamed IMG_8492.JPG -> 4137_IMG_20260210_193442.jpg

4138: Renamed IMG_7979.JPG -> 4138_IMG_20260210_172412.jpg

4139: Renamed IMG_5808.JPG -> 4139_IMG_20260208_121810.jpg

...
