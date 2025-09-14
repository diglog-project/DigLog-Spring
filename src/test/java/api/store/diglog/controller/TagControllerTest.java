package api.store.diglog.controller;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;

import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import api.store.diglog.model.entity.Tag;
import api.store.diglog.supporter.IntegrationTestSupport;

class TagControllerTest extends IntegrationTestSupport {

	@BeforeEach
	void beforeEach() {
		Member member = memberRepository.save(getMember());
		Tag tag3 = tagRepository.save(getTag("tag3"));
		Tag tag2 = tagRepository.save(getTag("tag2"));
		Tag tag1 = tagRepository.save(getTag("tag1"));
		postRepository.saveAll(List.of(
			getPost(member, List.of(tag1, tag2)),
			getPost(member, List.of(tag2)),
			getPost(member, List.of(tag1)),
			getPost(member, List.of())
		));
	}

	@AfterEach
	void afterEach() {
		postRepository.deleteAll();
		tagRepository.deleteAll();
		memberRepository.deleteAll();
	}

	@Test
	@DisplayName("멤버가 사용중인 태그가 name으로 정렬되어 조회된다.")
	void getMemberTags() throws Exception {
		// given
		String username = "test";

		// when
		MvcResult result = mockMvc.perform(get("/api/tag/" + username)).andReturn();
		MockHttpServletResponse response = result.getResponse();
		JsonNode data = objectMapper.readTree(response.getContentAsString());

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(data.size()).isEqualTo(2);
		assertThat(data.get(0).get("name").asText()).isEqualTo("tag1");
		assertThat(data.get(1).get("name").asText()).isEqualTo("tag2");
	}

	@Test
	@DisplayName("존재하지 않는 username으로 검색하면 0개가 조회된다.")
	void getMemberTags2() throws Exception {
		// given
		String username = "test222";

		// when
		MvcResult result = mockMvc.perform(get("/api/tag/" + username)).andReturn();
		MockHttpServletResponse response = result.getResponse();
		JsonNode data = objectMapper.readTree(response.getContentAsString());

		// then
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(data.size()).isEqualTo(0);
	}

	private Member getMember() {
		return Member.builder()
			.email("test@example.com")
			.username("test")
			.password("qwer1234")
			.roles(Set.of(Role.ROLE_USER))
			.platform(Platform.SERVER)
			.build();
	}

	private Tag getTag(String tagName) {
		return Tag.builder()
			.name(tagName)
			.build();
	}

	private Post getPost(Member member, List<Tag> tags) {
		return Post.builder()
			.title("test title")
			.content("test content")
			.member(member)
			.tags(tags)
			.build();
	}
}