//package dereck.angeles.controller;
//
//import dereck.angeles.dto.InterviewDto;
//import dereck.angeles.model.Interview;
//import dereck.angeles.service.InterviewService;
//import io.quarkus.security.Authenticated;
//import jakarta.inject.Inject;
//import jakarta.transaction.Transactional;
//import jakarta.ws.rs.*;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.Response;
//
//import java.io.InputStream;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@Path("/interview")
//@Authenticated
//public class InterviewController {
//
//	@Inject
//	InterviewService interviewService;
//
//	@POST
//	@Path("/create")
//	@Consumes(MediaType.MULTIPART_FORM_DATA)
//	@Produces(MediaType.APPLICATION_JSON)
//	@Transactional
//	public Response createInterview(
//				@FormDataParam("data") String dataJson,
//				@FormDataParam("resume") InputStream resumeInputStream,
//				@FormDataParam("resume") FormDataContentDisposition fileDetail
//																 ) {
//		try {
//			// Convertir el JSON a InterviewDto
//			ObjectMapper mapper = new ObjectMapper();
//			InterviewDto interviewDto = mapper.readValue(dataJson,
//																									 InterviewDto.class);
//
//			// Guardar el archivo del currículum y obtener la ruta
//			String filePath = saveResumeFile(resumeInputStream,
//																			 fileDetail.getFileName());
//
//			// Crear la entrevista en el servicio
//			Interview interview = interviewService.createInterview(interviewDto,
//																														 filePath);
//
//			return Response.ok(interview).build();
//		} catch (Exception e) {
//			return Response.status(Response.Status.BAD_REQUEST)
//										 .entity("Error al procesar los datos: " + e.getMessage())
//										 .build();
//		}
//	}
//
//	private String saveResumeFile(InputStream inputStream, String fileName) {
//		// Lógica para guardar el archivo en el sistema de archivos o un servicio en la nube
//		// Por ejemplo, guardar en una carpeta local como "/uploads/resumes/"
//		String filePath = "/uploads/resumes/" + System.currentTimeMillis() + "_" + fileName;
//		// Aquí implementarías la lógica real para guardar el archivo
//		return filePath;
//	}
//}