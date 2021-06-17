package com.myifeng.commons;

import org.json.JSONArray;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UploadDownloadApplicationTests {

    @Autowired
    protected WebApplicationContext context;

    @Value("${appendix.upload-folder:./appendix}")
    private String uploadFolder;

    private MockMvc mvc;

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testUploadSingleFile() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello World!".getBytes());
        mvc.perform(
                MockMvcRequestBuilders
                        .multipart("/appendix")
                        .file(mockFile)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andDo(res -> {
                    var path = new JSONArray(res.getResponse().getContentAsString()).getString(0);
                    var uploadFile = Paths.get(uploadFolder, path).toFile();
                    Assert.isTrue(uploadFile.exists(), "File not found!");
                    Assert.isTrue(uploadFile.length() == mockFile.getSize(), "Inconsistent file size!");
                });
    }

    @Test
    void testUploadMultipleFiles() throws Exception {
        MockMultipartFile mockFile1 = new MockMultipartFile("file1", "hello1.txt", MediaType.TEXT_PLAIN_VALUE, "Hello World 1!".getBytes());
        MockMultipartFile mockFile2 = new MockMultipartFile("file2", "hello2.txt", MediaType.TEXT_PLAIN_VALUE, "Hello World 2!".getBytes());
        MockMultipartFile mockFile3 = new MockMultipartFile("file2", "hello3.txt", MediaType.TEXT_PLAIN_VALUE, "Hello World 3!".getBytes());
        mvc.perform(
                MockMvcRequestBuilders
                        .multipart("/appendix/multiple")
                        .file(mockFile1)
                        .file(mockFile2)
                        .file(mockFile3)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andDo(result -> {
                    var paths = new JSONArray(result.getResponse().getContentAsString());

                    var file1 = Paths.get(uploadFolder, paths.getString(0)).toFile();
                    Assert.isTrue(file1.exists(), "File not found!");

                    var file2 = Paths.get(uploadFolder, paths.getString(1)).toFile();
                    Assert.isTrue(file2.exists(), "File not found!");

                    var file3 = Paths.get(uploadFolder, paths.getString(2)).toFile();
                    Assert.isTrue(file3.exists(), "File not found!");
                });

    }

    @Test
    void testDownloadNotExistFile() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/appendix/test/notExists.txt"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testDownloadExistingFile() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello World!".getBytes());
        mvc.perform(
                MockMvcRequestBuilders
                        .multipart("/appendix")
                        .file(mockFile)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andDo(res -> {
                    var path = new JSONArray(res.getResponse().getContentAsString()).getString(0);
                    var uploadFile = Paths.get(uploadFolder, path).toFile();
                    Assert.isTrue(uploadFile.exists(), "File not found!");
                    Assert.isTrue(uploadFile.length() == mockFile.getSize(), "Inconsistent file size!");

                    mvc.perform(MockMvcRequestBuilders.get(path.replaceAll("\\\\", "/")))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andDo(mvcResult -> {
                                var downloadFile = Paths.get(uploadFolder, UUID.randomUUID().toString(), mockFile.getOriginalFilename()).toFile();
                                downloadFile.getParentFile().mkdirs();
                                downloadFile.createNewFile();
                                var outputStream = new FileOutputStream(downloadFile);
                                var bin = new ByteArrayInputStream(mvcResult.getResponse().getContentAsByteArray());
                                StreamUtils.copy(bin, outputStream);
                                outputStream.close();

                                Assert.isTrue(downloadFile.exists(), "File not found!");
                                Assert.isTrue(downloadFile.length() == mockFile.getSize(), "Inconsistent file size!");
                            });
                });
    }

    @Test
    void testDownloadFilePathWithUTF_8() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", "你好.txt", MediaType.TEXT_PLAIN_VALUE, "你好，世界！".getBytes());
        mvc.perform(
                MockMvcRequestBuilders
                        .multipart("/appendix/你好")
                        .file(mockFile)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andDo(res -> {
                    var path = new JSONArray(new String(res.getResponse().getContentAsString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8)).getString(0);
                    var uploadFile = Paths.get(uploadFolder, path).toFile();
                    Assert.isTrue(uploadFile.exists(), "File not found!");
                    Assert.isTrue(uploadFile.length() == mockFile.getSize(), "Inconsistent file size!");

                    mvc.perform(MockMvcRequestBuilders.get(path.replaceAll("\\\\", "/")))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andDo(mvcResult -> {
                                var downloadFile = Paths.get(uploadFolder, UUID.randomUUID().toString(), mockFile.getOriginalFilename()).toFile();
                                downloadFile.getParentFile().mkdirs();
                                downloadFile.createNewFile();
                                var outputStream = new FileOutputStream(downloadFile);
                                var bin = new ByteArrayInputStream(mvcResult.getResponse().getContentAsByteArray());
                                StreamUtils.copy(bin, outputStream);
                                outputStream.close();

                                Assert.isTrue(downloadFile.exists(), "File not found!");
                                Assert.isTrue(downloadFile.length() == mockFile.getSize(), "Inconsistent file size!");
                            });
                });
    }

    @AfterEach
    void deleteAllTestFiles() {
        deleteFile(Paths.get(uploadFolder).toFile());
    }

    private boolean deleteFile(File dirFile) {
        if (!dirFile.exists()) {
            return false;
        }
        if (dirFile.isFile()) {
            return dirFile.delete();
        } else {
            for (var file : dirFile.listFiles()) {
                deleteFile(file);
            }
        }
        return dirFile.delete();
    }

}
