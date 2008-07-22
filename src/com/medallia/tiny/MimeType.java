/*
 * This file is part of the Spider Web Framework.
 * 
 * The Spider Web Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Spider Web Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Spider Web Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.medallia.tiny;

import javax.activation.MimetypesFileTypeMap;

/** Provider for mime types for various file name extensions. */
public class MimeType {
	private static MimetypesFileTypeMap mimeTypeMap = new MimetypesFileTypeMap();
	private static void addMimeType(String extensions, String mimeType) {
		mimeTypeMap.addMimeTypes(mimeType+" "+extensions+" "+extensions.toUpperCase());
	}
	static {
		// The default list of mime types is not very extensive. Therefore, we manually add the ones we'll need.
		addMimeType("gif", "image/gif");
		addMimeType("jpg jpeg", "image/jpeg");
		addMimeType("png", "image/png");
		addMimeType("css", "text/css");
		addMimeType("ico", "image/x-icon");
		addMimeType("js", "application/x-javascript");
		addMimeType("xls", "application/vnd.ms-excel");
		addMimeType("txt", "text/plain");
		addMimeType("htm html", "text/html");
		addMimeType("pgp gpg", "application/octet-stream");
		addMimeType("pdf", "application/pdf");
		 
		// audio types
		addMimeType("mp3", "audio/mpeg");
		addMimeType("wav", "audio/x-wav");
		 
		// video types
		addMimeType("asf", "video/x-ms-asf");
		addMimeType("avi", "video/x-msvideo");
		addMimeType("mpg", "video/mpeg");
	}
	
	/** @return The mime type of the given file extension. Defaults to "application/octet-stream" if the extension is missing or unknown. */
	public static String getMimeTypeForExtension(String extension) {
		return getMimeType("x."+extension);
	}
	
	/** @return The mime type of the given filename based on the file extension. Defaults to "application/octet-stream" if the extension is missing or unknown. */
	public static String getMimeType(String filename) {
		return mimeTypeMap.getContentType(filename);
	}

}
