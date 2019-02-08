package fr.pantheonsorbonne.miage;

import java.io.IOException;
import java.util.logging.Level;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;
import org.glassfish.grizzly.http.io.NIOOutputStream;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.jersey.grizzly2.servlet.GrizzlyWebContainerFactory;
import com.itextpdf.text.log.Logger;
import com.google.common.io.ByteStreams;

/**
 * Main class.
 *
 */
public class Main {
	public static final String HOST = "localhost";
	public static final int PORT = 7000;
	private static final Logger logger = Logger.getLogger(Main.class.getName());
	private static StudentRepository studentRepo = StudentRepository.withDB("src/main/resources/students.db");

	public static void main(String[] args) throws IOException, URISyntaxException {

		HttpServer server = HttpServer.createSimpleServer();
		addRootPath(server, "/home");
		addDiplomaPath(server, "/diploma/*");

		try

		{
			server.start();
			String url = "http://localhost:8080/home";
			java.awt.Desktop.getDesktop().browse(new URI(url));
			logger.log(level.ALL,"Press any key to stop the server...");
			System.out.println("Press any key to stop the server...");
			System.in.read();
		} catch (Exception e) {
			System.err.println(e);
		}
	}
protected static Student getStudentData(StudentRepository repo) {

		for (int i = 0; i < students.size(); i++) {
			if (i == studentId -1) {
				return students.get(i);
			}
		}
		throw new NoSuchElementException();
	}
	protected static void handleResponse(Response response, int studentId) throws IOException {
		response.setContentType("application/pdf");
		Student student = getStudentData(studentId, studentRepo);
		DiplomaGenerator generator = new MiageDiplomaGenerator(student);
		try (InputStream is = generator.getContent()) {
			try (NIOOutputStream os = response.createOutputStream()) {
				ByteStreams.copy(is, os);
			}

		}
	}
	protected static void addDiplomaPath(HttpServer server, String path) {
		server.getServerConfiguration().addHttpHandler(new HttpHandler() {
			public void service(Request request, Response response) throws Exception {
				// get the id of the student
				int id = Integer.parseInt(request.getPathInfo().substring(1));

				handleResponse(response, id);
				response.setContentType("text/html; charset=utf-8");
				response.setStatus(404);
				response.getWriter().write("Erreur 404 : Le diplome n'existe pas pour cet utilisateur");
			}
		}, path);
	}
	protected static void addRootPath(HttpServer server, String path) {
		server.getServerConfiguration().addHttpHandler(new HttpHandler() {
			public void service(Request request, Response response) throws Exception {

				StringBuilder sb = new StringBuilder();
				sb.append("<!DOCTYPE html><head><meta charset='utf-8'></head><body><h1>Liste des diplômés</h1><ul>");
				for (Student stu : StudentRepository.withDB("src/main/resources/students.db")) {
					sb.append("<li>");
					sb.append(
							"<a href='/diploma/" + stu.getId() + "'>" + stu.getTitle() + ' ' + stu.getName() + "</a>");
					sb.append("</li>");
				}

				sb.append("</ul></body></html>");
				response.setContentType("text/html; charset=utf-8");
				response.setContentLength(sb.length());
				response.getWriter().write(sb.toString());
			}
		}, path);
	}
}
