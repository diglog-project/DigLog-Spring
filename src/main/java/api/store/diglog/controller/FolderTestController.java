package api.store.diglog.controller;

import api.store.diglog.model.dto.folderTest.HandleFolderTestRequest;
import api.store.diglog.service.FolderTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/folder-test")
@RequiredArgsConstructor
public class FolderTestController {

    private final FolderTestService folderTestService;

    @PostMapping
    public ResponseEntity<Void> handleFolderTest(@RequestBody List<HandleFolderTestRequest> handleFolderTestRequestList) {
        folderTestService.handleFolderTest(handleFolderTestRequestList);

        return ResponseEntity.ok().build();
    }
}
