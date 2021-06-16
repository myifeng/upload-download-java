package com.myifeng.commons;

import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

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
        MvcResult result = mvc.perform(
                MockMvcRequestBuilders
                        .multipart("/single")
                        .file(mockFile)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn();

        var file = Paths.get(uploadFolder, new JSONArray(result.getResponse().getContentAsString()).getString(0)).toFile();

        Assert.isTrue(file.exists(), "File not found!");

        file.deleteOnExit();
    }

    @Test
    void testUploadMultipleFiles() throws Exception {
        MockMultipartFile mockFile1 = new MockMultipartFile("file1", "hello1.txt", MediaType.TEXT_PLAIN_VALUE, "Hello World 1!".getBytes());
        MockMultipartFile mockFile2 = new MockMultipartFile("file2", "hello2.txt", MediaType.TEXT_PLAIN_VALUE, "Hello World 2!".getBytes());
        MockMultipartFile mockFile3 = new MockMultipartFile("file2", "hello3.txt", MediaType.TEXT_PLAIN_VALUE, "Hello World 3!".getBytes());
        MvcResult result = mvc.perform(
                MockMvcRequestBuilders
                        .multipart("/multiple")
                        .file(mockFile1)
                        .file(mockFile2)
                        .file(mockFile3)
                        .characterEncoding("utf-8")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andReturn();

        var paths = new JSONArray(result.getResponse().getContentAsString());

        var file1 = Paths.get(uploadFolder, paths.getString(0)).toFile();
        Assert.isTrue(file1.exists(), "File not found!");

        var file2 = Paths.get(uploadFolder, paths.getString(1)).toFile();
        Assert.isTrue(file2.exists(), "File not found!");

        var file3 = Paths.get(uploadFolder, paths.getString(2)).toFile();
        Assert.isTrue(file3.exists(), "File not found!");

        file1.deleteOnExit();
        file2.deleteOnExit();
        file3.deleteOnExit();

    }

    @Test
    void downloadTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/test/notExists.txt"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}
