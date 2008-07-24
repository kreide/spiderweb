package com.medallia.spider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.medallia.tiny.Empty;
import com.medallia.tiny.Implement;
import com.medallia.tiny.MimeType;

public class StaticResources {
	
	public interface StaticResourceLookup {
		StaticResource findStaticResource(String name);
	}
	
	public interface StaticResource {
		boolean exists();
		String getMimeType();
		void copyTo(OutputStream stream) throws IOException;
	}
	
	/** map from URI ending to resource path, i.e. package name. */
	private static final Map<String, String> resourceMap = Empty.hashMap();
	static {
		addResourceMapping("images", "gif", "jpg", "png");
		addResourceMapping("css", "css");
	}
	private static void addResourceMapping(String path, String... ending) {
		for (String s : ending) {
			String old = resourceMap.put(s, path);
			if (old != null)
				throw new AssertionError(s + " alreadys maps to " + old);
		}
	}
	
	public static StaticResourceLookup makeStaticResourceLookup(final Class<?> clazz) {
		return new StaticResourceLookup() {
			@Implement public StaticResource findStaticResource(String uri) {
				int k = uri.lastIndexOf('.');
				if (k > 0) {
					final String ext = uri.substring(k + 1);
					String path = resourceMap.get(ext);

					// do not allow extra path names in the resource path
					int lastSlashIndex = uri.lastIndexOf('/');
					String resourceName = uri.substring(lastSlashIndex < 0  ? 0 : lastSlashIndex + 1, uri.length());
					final InputStream in = path != null ? clazz.getResourceAsStream(path + "/" + resourceName) : null;
					
					return new StaticResource() {
						@Implement public boolean exists() {
							return in != null;
						}
						@Implement public String getMimeType() {
							return MimeType.getMimeTypeForExtension(ext);
						}
						@Implement public void copyTo(OutputStream stream) throws IOException {
							copy(in, stream);
						}
					};
				}
				return null;
			}
		};
	}
	
	private static final int COPY_BUFFER_SIZE = 1024 * 4;
	
	/** Copy input to output; neither stream is closed */
	public static int copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[COPY_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
	
}
