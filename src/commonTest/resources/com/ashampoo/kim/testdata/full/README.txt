# Special photos

photo_8.jpg = Starts with IFD1 thumbnail data at offset 0
photo_9.jpg = Corrupted thumbnail data, but intact IFD1
photo_18.jpg = Corrupted IFD1 (thumbnail)
photo_19.jpg = F-Number is stored as double array
photo_20.jpg = EXIF DateTimeOriginal with all zeros
photo_21.jpg = Corrupted IFD1 (thumbnail)
photo_22.jpg = Non-corrupted version of photo_30.jpg
photo_23.jpg = Nothing Phone OOC-JPEG (50 MP)
photo_30.jpg = Multiple APP1
photo_41.jpg = Long APP1
photo_42.jpg = Makernote issue on rewrite
photo_43.jpg = Multiple EXIF offsets on rewrite
photo_44.jpg = Broken, but can be read by ExifTool (wrong segment size)
photo_45.jpg = Broken, but can be read by ExifTool (no SOFN = no image size)
photo_46.jpg = Broken, but can be read by ExifTool (ExifOffset field corrupt)
photo_47.jpg = Broken, but can be read by ExifTool (zero segment size)
photo_48.jpg = iPhone SE OOC-JPEG
photo_49.jpg = Canon 60D OOC-JPEG
photo_50.jpg = Fuji X-T4 OOC-JPEG

photo_60.heic = iPhone SE 3 OOC-HEIC
photo_73.heic = same as photo_60.heic, but added XMP & rotated using exiftool
photo_74.avif = photo_31.jpg converted to AVIF using ImageMagick
pnoto_75.heic = photo_31.jpg converted to HEIC using ImageMagick
pnoto_76.heic = photo_1.jpg converted to HEIC using Apple Preview
photo_77.heic = Samsung Galaxy S21 5G Ultra OOC-HEIC

photo_78.jxl = photo_6.jpg -> JXL using converted with darktable 4.6.0 from the JPG
photo_79.jxl = photo_6.jpg -> JXL using cjxl with container & without metadata compression
photo_80.jxl = photo_6.jpg -> JXL using cjxl with container & with metadata compression

photo_81.tif = GeoTiff using one tie-point and pixel scaling
photo_82.tif = GeoTiff using full affine transform matrix
