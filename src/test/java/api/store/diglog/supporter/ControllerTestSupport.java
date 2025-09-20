package api.store.diglog.supporter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import api.store.diglog.controller.FolderController;
import api.store.diglog.controller.NotificationController;
import api.store.diglog.controller.SubscriptionController;
import api.store.diglog.service.FolderService;
import api.store.diglog.service.SseEmitterService;
import api.store.diglog.service.SubscriptionService;
import api.store.diglog.service.notification.NotificationService;

@WebMvcTest(controllers = {
	FolderController.class,
	NotificationController.class,
	SubscriptionController.class
})
public abstract class ControllerTestSupport {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@MockitoBean
	protected FolderService folderService;

	@MockitoBean
	protected NotificationService notificationService;

	@MockitoBean
	protected SseEmitterService sseEmitterService;

	@MockitoBean
	protected SubscriptionService subscriptionService;

}


