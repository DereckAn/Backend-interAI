package dereck.angeles.controller;

import dereck.angeles.dto.TopicDto;
import dereck.angeles.model.Topic;
import dereck.angeles.repository.TopicRepository;
import dereck.angeles.service.TopicService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.stream.Collectors;

@Path("/topics")
public class TopicController {

	private final TopicService topicService;

	@Inject
	public TopicController(TopicService topicService) {
		;
		this.topicService = topicService;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<TopicDto> getTopics() {
		return topicService.getTopics();
	}
}