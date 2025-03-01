package api.store.diglog.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.store.diglog.model.dto.folder.FolderCreateRequest;
import api.store.diglog.model.dto.folder.FolderResponse;
import api.store.diglog.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

	private final FolderService folderService;

	@PutMapping
	public ResponseEntity<List<FolderResponse>> createAndUpdate(
		@RequestBody @Valid List<FolderCreateRequest> folderCreateRequests) {

		List<FolderResponse> folderResponses = folderService.createAndUpdateFolders(folderCreateRequests);
		return ResponseEntity.ok().body(folderResponses);
	}
}
