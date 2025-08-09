package dereck.angeles.controller;

import dereck.angeles.dto.FileUploadResponseDto;
import dereck.angeles.dto.InterviewDto;
import dereck.angeles.model.File;
import dereck.angeles.model.Interview;
import dereck.angeles.service.FileStorageService;
import dereck.angeles.service.InterviewService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import java.util.List;
import java.util.Map;

@Path("/interview")
public class InterviewController {

	@Inject
	InterviewService interviewService;

	@Inject
	FileStorageService fileStorageService;

	@POST
	@Path("/create")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response createInterview(MultipartFormDataInput input) {
		try {
			Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

			// Extract JSON data
			List<InputPart> dataParts = uploadForm.get("data");
			if (dataParts == null || dataParts.isEmpty()) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity("{\"error\": \"Missing data field\"}")
						.build();
			}

			String dataJson = dataParts.get(0).getBodyAsString();
			ObjectMapper mapper = new ObjectMapper();
			InterviewCreateRequest createRequest = mapper.readValue(dataJson, InterviewCreateRequest.class);

			// Handle resume file upload if present
			String resumeFileId = null;
			List<InputPart> resumeParts = uploadForm.get("resume");
			if (resumeParts != null && !resumeParts.isEmpty()) {
				InputPart resumePart = resumeParts.get(0);
				String filename = getFileName(resumePart);
				String contentType = resumePart.getMediaType().toString();
				
				try (InputStream inputStream = resumePart.getBody(InputStream.class, null)) {
					// Get file size (you might need to read the stream to calculate this)
					byte[] fileBytes = inputStream.readAllBytes();
					Long fileSize = (long) fileBytes.length;
					
					// Create new InputStream from bytes
					InputStream uploadStream = new java.io.ByteArrayInputStream(fileBytes);
					
					FileUploadResponseDto uploadResult = fileStorageService.uploadFile(
						uploadStream,
						filename,
						contentType,
						fileSize,
						File.FileType.RESUME,
						createRequest.userId
					);
					
					if (uploadResult.success()) {
						resumeFileId = uploadResult.fileDto().id();
					} else {
						return Response.status(Response.Status.BAD_REQUEST)
								.entity("{\"error\": \"Failed to upload resume: " + uploadResult.error() + "\"}")
								.build();
					}
				}
			}

			// Create InterviewDto
			InterviewDto interviewDto = new InterviewDto(
				null, // id will be generated
				createRequest.userId,
				createRequest.selectedTopic,
				createRequest.programmingLanguage,
				createRequest.difficultyLevel,
				createRequest.jobDescription,
				createRequest.yearsOfExperience,
				null, // startTime will be set in service
				null, // endTime
				null, // videoUrl
				null, // audioUrl
				"in_progress" // status
			);

			// Create the interview
			Interview interview = interviewService.createInterview(interviewDto);

			// Prepare response
			InterviewCreateResponse response = new InterviewCreateResponse(
				interview.getId().toString(),
				resumeFileId,
				"Interview created successfully"
			);

			return Response.ok(response).build();

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"Error processing request: " + e.getMessage() + "\"}")
					.build();
		}
	}

	private String getFileName(InputPart part) {
		String[] contentDispositionHeader = part.getHeaders().getFirst("Content-Disposition").split(";");
		for (String name : contentDispositionHeader) {
			if ((name.trim().startsWith("filename"))) {
				String[] tmp = name.split("=");
				String fileName = tmp[1].trim().replaceAll("\"", "");
				return fileName;
			}
		}
		return "unknown-file";
	}

	// Request DTO for parsing JSON data
	public static class InterviewCreateRequest {
		public String userId;
		public String selectedTopic;
		public String programmingLanguage;
		public String difficultyLevel;
		public String jobDescription;
		public Integer yearsOfExperience;
	}

	// Response DTO
	public static class InterviewCreateResponse {
		public String interviewId;
		public String resumeFileId;
		public String message;

		public InterviewCreateResponse(String interviewId, String resumeFileId, String message) {
			this.interviewId = interviewId;
			this.resumeFileId = resumeFileId;
			this.message = message;
		}
	}
}