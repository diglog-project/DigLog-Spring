package api.store.diglog.controller;

import api.store.diglog.config.CustomSecurityConfiguration;
import api.store.diglog.model.dto.post.PostRequest;
import api.store.diglog.service.PostService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = PostController.class,
        includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CustomSecurityConfiguration.class)})
public class PostControllerUnitTestExample2 {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    PostService postService;

    @Test

    void testSavePost() throws Exception {
        // given
        PostRequest dto = new PostRequest();
        dto.setTitle("test title");
        dto.setContent("test content");
        dto.setTagNames(new ArrayList<>());
        dto.setUrls(new ArrayList<>());

        // context 확인용
        SecurityContext context = SecurityContextHolder.getContext();

        // when
        MvcResult result = mockMvc.perform(post("/api/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn();
        MockHttpServletResponse response = result.getResponse();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString());

        // then
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
