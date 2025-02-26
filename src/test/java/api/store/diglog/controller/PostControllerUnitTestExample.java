package api.store.diglog.controller;

import api.store.diglog.config.TestSecurityConfig;
import api.store.diglog.model.dto.post.PostRequest;
import api.store.diglog.model.dto.post.PostResponse;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import api.store.diglog.service.PostService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = PostController.class)
@Import(TestSecurityConfig.class)
public class PostControllerUnitTestExample {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    PostService postService;

    private static final String EMAIL = "diglog@example.com";
    private static final String PASSWORD = "qwer1234";

    @Test
    void testGetPosts() throws Exception {
        // given
        UUID POST_ID = UUID.randomUUID();
        Post post = Post.builder()
                .id(POST_ID)
                .title("test title")
                .content("test content")
                .member(Member.builder().build())
                .isDeleted(false)
                .tags(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        PostResponse postResponse = new PostResponse(post);
        when(postService.getPost(POST_ID)).thenReturn(postResponse);

        // when
        MvcResult result = mockMvc.perform(get("/api/post/" + POST_ID))
                .andReturn();
        MockHttpServletResponse response = result.getResponse();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString());

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(data.get("title").asText()).isEqualTo("test title");
    }

    @Test
    @WithMockUser(username = EMAIL, password = PASSWORD)
    void testSavePost() throws Exception {
        // given
        PostRequest dto = new PostRequest();
        dto.setTitle("test title");
        dto.setContent("test content");
        dto.setTagNames(new ArrayList<>());
        dto.setUrls(new ArrayList<>());

        SecurityContext context = SecurityContextHolder.getContext();
        context.getAuthentication();

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
