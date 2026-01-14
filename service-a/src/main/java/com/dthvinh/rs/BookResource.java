import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.component.annotations.Component;

@Component(
  service = Servlet.class,
  property = {
    "osgi.http.whiteboard.servlet.pattern=/api/books/*"
  }
)
public class BooksServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // For /api/books/1 -> pathInfo is "/1"
    String pathInfo = req.getPathInfo(); // null or "/1" or "/1/chapters"
    // You can also use req.getRequestURI() if you prefer.

    String id = null;
    if (pathInfo != null && pathInfo.length() > 1) {
      id = pathInfo.substring(1); // "1" (you can split by "/" for more segments)
    }

    resp.setContentType("text/plain");
    resp.getWriter().write("book id = " + id);
  }
}
