package api.store.diglog.service;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.dto.folderTest.HandleFolderTestRequest;
import api.store.diglog.model.entity.FolderTest;
import api.store.diglog.model.entity.Member;
import api.store.diglog.repository.FolderTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static api.store.diglog.common.exception.ErrorCode.FOLDER_OVER_FLOW_DEPTH;
import static api.store.diglog.common.exception.ErrorCode.POST_INVALID_SEARCH_OPTION;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FolderTestService {

    private final FolderTestRepository folderTestRepository;
    private final MemberService memberService;
    private static final int MAX_DEPTH = 3;

    @Transactional
    public void handleFolderTest(List<HandleFolderTestRequest> handleFolderTestRequestList) {
        Member member = memberService.getCurrentMember();

        deleteFolderTests(handleFolderTestRequestList, member);

        checkMaxDepth(handleFolderTestRequestList);

        List<FolderTest> folderTests = IntStream.range(0, handleFolderTestRequestList.size())
                .mapToObj(index -> handleFolderTestRequestList.get(index).toFolderTest(member, index))
                .toList();
        folderTestRepository.saveAll(folderTests);
    }

    private void deleteFolderTests(List<HandleFolderTestRequest> handleFolderTestRequestList, Member member) {
        List<UUID> notDeleteIds = handleFolderTestRequestList.stream()
                .map(HandleFolderTestRequest::getId)
                .toList();

        // post가 남아있는 경우는 코드로 처리 or try/catch, GlobalExceptionHandler에서 처리
        folderTestRepository.deleteAllByMemberAndIdNotIn(member, notDeleteIds);
    }

    private void checkMaxDepth(List<HandleFolderTestRequest> handleFolderTestRequestList) {
        int currentDepth = 0;

        List<HandleFolderTestRequest> folders = new ArrayList<>(handleFolderTestRequestList);
        List<UUID> parentIds = new ArrayList<>();

        while (currentDepth < MAX_DEPTH) {
            int depth = currentDepth;
            List<UUID> currentDepthIds = folders.stream()
                    .filter(request -> checkParentId(depth, request, parentIds))
                    .map(HandleFolderTestRequest::getId)
                    .toList();
            parentIds.addAll(currentDepthIds);

            folders.removeIf(request -> checkParentId(depth, request, parentIds));
            if (folders.isEmpty()) {
                return;
            }
            if (currentDepthIds.isEmpty()) {
                throw new CustomException(POST_INVALID_SEARCH_OPTION); // 잘못된 폴더 request 구조
            }

            currentDepth++;
        }

        throw new CustomException(FOLDER_OVER_FLOW_DEPTH, String.format(FOLDER_OVER_FLOW_DEPTH.getMessage(), MAX_DEPTH));
    }

    private boolean checkParentId(int depth, HandleFolderTestRequest request, List<UUID> parentIds) {
        if (depth == 0) {
            return request.getParentId() == null;
        }
        return parentIds.contains(request.getParentId());
    }
}
