package dereck.angeles.controller;

import dereck.angeles.dto.TopicDto;
import dereck.angeles.service.TopicService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * The TopicController class handles HTTP requests related to Topic entities.
 * <p>
 * It delegates business logic to the TopicService and returns TopicDto objects in the
 * API responses.
 */
@Path("/topics")
public class TopicController {

	private final TopicService topicService;

	@Inject
	public TopicController(TopicService topicService) {
		this.topicService = topicService;
	}

	/**
	 * Retrieves all topics from the system.
	 * <p>
	 * This endpoint returns a list of all available topics as DTOs.
	 *
	 * @return a list of {@link TopicDto} objects representing all topics
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<TopicDto> getTopics() {
		return topicService.getTopics();
	}
}