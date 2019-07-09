package edu.brown.cs.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import edu.brown.cs.login.Login;
import edu.brown.cs.repl.REPL;
import freemarker.template.Configuration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import spark.ExceptionHandler;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.TemplateEngine;
import spark.template.freemarker.FreeMarkerEngine;
import edu.brown.cs.login.InfoWrapper;
import edu.brown.cs.login.Login;
import edu.brown.cs.repl.REPL;
import edu.brown.cs.websocket.WebSocket;
import freemarker.template.Configuration;
import spark.utils.IOUtils;

import static spark.Spark.*;
/**
 * The Main class of our project. This is where execution begins.
 *
 */
public final class Main {

  private static final int DEFAULT_PORT = 4567;

  private static REPL repl;

  /**
   * The initial method called when execution begins.
   *
   * @param args
   *          An array of command line arguments
   */
  public static void main(String[] args) {
    new Main(args).run();
  }

  private String[] args;

  private Main(String[] args) {
    this.args = args;

  }

  private void run() {

    // Parse command line arguments
    OptionParser parser = new OptionParser();
    parser.accepts("gui");
    parser.accepts("ec2");
    parser.accepts("port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_PORT);
    OptionSet options = parser.parse(args);

    Login login = new Login("data/final/login.sqlite3");
    runSparkServer(getHerokuAssignedPort());


    login.clear();

  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration();
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
          templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  private void runSparkServer(int port) {
    System.out.println(port);
    Spark.port(port);
    Spark.webSocket("/socket", WebSocket.class);
    Spark.externalStaticFileLocation("src/main/resources");
    Spark.exception(Exception.class, new ExceptionPrinter());
    get("/", (request, response) -> new ModelAndView(new HashMap<>(), "index.html"),
            new HTMLTemplateEngine());  }

  public class HTMLTemplateEngine extends TemplateEngine {

    @Override
    public String render(ModelAndView modelAndView) {
      try {
        // If you are using maven then your files
        // will be in a folder called resources.
        // getResource() gets that folder
        // and any files you specify.
        URL url = getClass().getResource("public/" +
                modelAndView.getViewName());

        // Return a String which has all
        // the contents of the file.
        Path path = Paths.get(url.toURI());
        return new String(Files.readAllBytes(path), Charset.defaultCharset());
      } catch (IOException | URISyntaxException e) {
        // Add your own exception handlers here.
      }
      return null;
    }
  }
  /**
   * Display an error page when an exception occurs in the server.
   */
  private static class ExceptionPrinter implements ExceptionHandler {
    @Override
    public void handle(Exception e, Request req, Response res) {
      res.status(500);
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }

  private static class ValidateHandler implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
      response.type("text");
      return "0F9C0BBA46ACD7633DFFA9500CD8EF65C9E9A20599D1AB17783B44C8F0EC60D5 comodoca.com 5cca340c631ce";
    }
  }

  static int getHerokuAssignedPort() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    if (processBuilder.environment().get("PORT") != null) {
      return Integer.parseInt(processBuilder.environment().get("PORT"));
    }
    return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
  }


}
