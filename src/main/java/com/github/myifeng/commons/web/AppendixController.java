package com.github.myifeng.commons.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequestMapping(value = "/appendix")
@RestController
public class AppendixController {

	@Value("${appendix.upload-folder:./}")
	private String uploadFolder;

	@PostConstruct
	public void init() throws IOException {
		Path path = Paths.get(uploadFolder);
		if (!path.toFile().exists()) {
			Files.createDirectories(path);
		}
	}

    /**
     * Upload single file or multiple files.
     * Use RequestURI and UUID as subPath.
     * Return a collection of paths.
     *
     * @param request
     * @return return ["$RequestURI/$UUID/$filename"]
     * E.g : ["appendix/images/cf109c9e-7662-4c71-9a3d-27d3fd664206/hello.jpg", ...]
     * @throws UnsupportedEncodingException
     */
    @PostMapping(value = "/**")
    public List<String> fileUpload(HttpServletRequest request) throws UnsupportedEncodingException {
		Collection<List<MultipartFile>> multiFiles = ((MultipartHttpServletRequest) request).getMultiFileMap().values();

		String subPath = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8.name());
		Path relativePath = Paths.get(uploadFolder, subPath);
		if (!relativePath.toFile().exists()) {
			try {
				Files.createDirectories(relativePath);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("createDirectories " + relativePath + " fail");
			}
		}
		return multiFiles.stream()
				.flatMap(files ->
						files.stream().map(file -> {
							try {
								Path outputFilePath = Paths.get(subPath, UUID.randomUUID().toString(), file.getOriginalFilename());
								Path absolutePath = Paths.get(uploadFolder, outputFilePath.toString());
								File parent = absolutePath.toFile().getParentFile();
								if (!parent.mkdirs()) {
									throw new IOException(String.format("Couldn't create directory %s", parent.getAbsolutePath()));
								}

								InputStream inputStream = file.getInputStream();
								OutputStream outputStream = Files.newOutputStream(absolutePath);
								byte[] tempts = new byte[1024];
								int battered;
								while ((battered = inputStream.read(tempts)) != -1) {
									outputStream.write(tempts, 0, battered);
								}
								inputStream.close();
								outputStream.close();
								return outputFilePath.toString();
							} catch (IOException e) {
								e.printStackTrace();
							}
							return null;
						})
				).collect(Collectors.toList());
	}

	/**
	 * Download file
	 *
	 * @param request https://localhost:8080/appendix/images/cf109c9e-7662-4c71-9a3d-27d3fd664206/hello.jpg
	 * @return
	 * @throws IOException
	 */
	@GetMapping("/**")
	public ResponseEntity<InputStreamResource> getFile(HttpServletRequest request) throws IOException {
		Path path = Paths.get(uploadFolder, URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8.name()));
		File file = path.toFile();
		if (file.exists() && file.isFile()) {
			return ResponseEntity
					.ok()
					.header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
					.header(HttpHeaders.PRAGMA, "no-cache")
					.header(HttpHeaders.EXPIRES, "0")
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + new String(file.getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1) + "\"")
					.contentType(MediaTypeFactory.getMediaType(path.toString()).orElse(MediaType.APPLICATION_OCTET_STREAM))
					.contentLength(file.length())
					.body(new InputStreamResource(Files.newInputStream(path)));
		} else {
			return ResponseEntity.notFound().build();
		}
	}

}
