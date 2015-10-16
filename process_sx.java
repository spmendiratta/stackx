/**
      getResponse("comments?order=desc&min=10&sort=votes&site=askubuntu");
      getResponse("tags?order=desc&site=serverfault"); // serverfault.tags
      getResponse("questions?site=serverfault");

*/
import java.io.IOException;
import org.apache.http.HttpResponse;
public class process_sx {
    public final static void main(String[] args) throws Exception {
      String[] SITES = {
                        "stackoverflow",
                        "serverfault",
                        "superuser",
                        "askubuntu"
                       };
      StackXClient  sxClient = new StackXClient();

      HttpResponse  response;
      response = sxClient.getResponse("");
      sxClient.showResults( response );

      String target = "questions";
      for ( String site : SITES ) {
          String query = target + "?site=" + site;
          response = sxClient.getResponse(query);
          String results = sxClient.getResults( response );
          System.out.println("--------------------------------");
          System.out.println(results);
      }
      
      sxClient.shutdown();
    }
}
