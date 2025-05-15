package api.store.diglog.controller;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.store.diglog.model.dto.post.PostFolderUpdateRequest;
import api.store.diglog.model.dto.post.PostListMemberRequest;
import api.store.diglog.model.dto.post.PostListMemberTagRequest;
import api.store.diglog.model.dto.post.PostListSearchRequest;
import api.store.diglog.model.dto.post.PostRequest;
import api.store.diglog.model.dto.post.PostResponse;
import api.store.diglog.model.dto.post.PostUpdateRequest;
import api.store.diglog.model.dto.post.PostViewIncrementRequest;
import api.store.diglog.model.dto.post.PostViewResponse;
import api.store.diglog.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@PostMapping
	public ResponseEntity<Void> save(@RequestBody PostRequest postRequest) {
		postService.save(postRequest);

		return ResponseEntity.ok().build();
	}

	@PatchMapping
	public ResponseEntity<Void> update(@RequestBody PostUpdateRequest postUpdateRequest) {
		postService.update(postUpdateRequest);

		return ResponseEntity.ok().build();
	}

	@PatchMapping("/folder")
	public ResponseEntity<Void> updateFolder(@RequestBody PostFolderUpdateRequest postFolderUpdateRequest) {
		postService.updateFolder(postFolderUpdateRequest);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/{id}")
	public ResponseEntity<PostResponse> getPost(@PathVariable("id") UUID id) {
		PostResponse postResponse = postService.getPost(id);

		return ResponseEntity.ok().body(postResponse);
	}

	@GetMapping
	public ResponseEntity<Page<PostResponse>> getPosts(
		@ParameterObject @ModelAttribute PostListSearchRequest postListSearchRequest) {
		Page<PostResponse> postResponses = postService.getPosts(postListSearchRequest);

		return ResponseEntity.ok().body(postResponses);
	}

	@GetMapping("/member")
	public ResponseEntity<Page<PostResponse>> getMemberPosts(
		@ParameterObject @ModelAttribute PostListMemberRequest postListMemberRequest) {
		Page<PostResponse> postResponses = postService.getMemberPosts(postListMemberRequest);

		return ResponseEntity.ok().body(postResponses);
	}

	@GetMapping("/member/tag")
	public ResponseEntity<Page<PostResponse>> getMemberTagPosts(
		@ParameterObject @ModelAttribute PostListMemberTagRequest postListMemberTagRequest) {
		Page<PostResponse> postResponses = postService.getMemberTagPosts(postListMemberTagRequest);

		return ResponseEntity.ok().body(postResponses);
	}

	@GetMapping("/search")
	public ResponseEntity<Page<PostResponse>> searchPosts(
		@ParameterObject @ModelAttribute PostListSearchRequest postListSearchRequest) {
		Page<PostResponse> postResponses = postService.searchPosts(postListSearchRequest);

		return ResponseEntity.ok().body(postResponses);
	}

	@PatchMapping("/delete/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
		postService.delete(id);

		return ResponseEntity.ok().build();
	}

	@PostMapping("/view/increment")
	public ResponseEntity<Void> increasePostView(
		@RequestBody PostViewIncrementRequest postViewIncrementRequest,
		HttpServletRequest httpServletRequest
	) {
		String clientIp = httpServletRequest.getHeader("X-Forwarded-For");
		if (clientIp == null) {
			clientIp = httpServletRequest.getRemoteAddr();
		}

		postService.increaseView(postViewIncrementRequest, clientIp);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/view/{id}")
	public ResponseEntity<PostViewResponse> getPostView(@PathVariable(value = "id") UUID id) {
		PostViewResponse postViewResponse = postService.getViewCount(id);
		return ResponseEntity.ok().body(postViewResponse);
	}
}
