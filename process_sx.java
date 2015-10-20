/**
      getResponse("comments?order=desc&min=10&sort=votes&site=askubuntu");
      getResponse("tags?order=desc&site=serverfault"); // serverfault.tags
      getResponse("questions?site=serverfault");

*/
import java.util.Calendar;
import java.util.Date;
import java.io.IOException;
import org.apache.http.HttpResponse;
public class process_sx {
   public static String[] _SITES = {
                        "stackoverflow",
                        "serverfault",
                        "superuser",
                        "askubuntu"
                       };
   public static String[] _QUERIES = {
                        "questions/answered",
                        "answers",
                        "comments",
                        "posts" 
                       };
    public static String _BASE_URL = "https://api.stackexchange.com/2.2/";
    public static String _INFO = "info?site=stackoverflow";
    public static String _QUERY = "";
    public static String _TAGS = "";
    public static String _SITE = "";
    public static int _MINUS_MONTHS = 0;

    public final static void main(String[] args) throws Exception {
     if (validate( args )) { 
      // long today = System.currentTimeMillis() / 1000;

      Calendar calendar = Calendar.getInstance();
      calendar.setTime( new Date());	// today
      long toDate = calendar.getTimeInMillis() / 1000;

      calendar.add(Calendar.DATE, -_MINUS_MONTHS*30);
      long fromDate = calendar.getTimeInMillis() / 1000;

      StackXClient  sxClient = new StackXClient();

      HttpResponse  response;
      // response = sxClient.getResponse(_BASE_URL + _INFO);
      // sxClient.showResults( response );

      String query = _BASE_URL + 
                     _QUERY + 
                     "?tagged=" + _TAGS + 
                     "&fromdate=" + fromDate + 
                     "&todate=" + toDate + 
                     "&sort=votes&order=desc" +
                     "&filter=withbody" +
                     "&pagesize=100" +
                     "&site=" + _SITE;
      response = sxClient.getResponse(query);
      String results = sxClient.getResults( response );
      System.out.println(results);
      
      sxClient.shutdown();
     } else usage();
    }

    // I am sure there are better ways - 
    public static boolean validate( String[] args ) {
      if (args.length != 8) return false;
      if (!args[0].equals("-s") ||
          !args[2].equals("-t") ||
          !args[4].equals("-m") ||
          !args[6].equals("-q")) {
         System.out.println("%%Bad token ");
         return false; 
      }

      for ( String site : _SITES ) {
        if (site.startsWith(args[1])) _SITE = site;
      }
      if (_SITE.equals("")) {
        System.out.println("%%Bad site - st, se, su ");
        return false;
      }

      _TAGS = args[3];

      try {
         _MINUS_MONTHS = Integer.parseInt(args[5]);
      } catch (NumberFormatException e) {
        System.out.println("%%Bad months - int expected");
        return false;
      }

      for ( String query : _QUERIES ) {
        if (query.startsWith(args[7])) _QUERY = query;
      }
      if (_QUERY.equals("")) {
        System.out.println("%%Bad query - q, a, c, p ");
        return false;
      }

      return true;
    }

    public static void usage() {
       System.out.println("usage: java process_sx -s <site> -t <tag(s)> -m <months> -q <query>");
     
       System.out.println("       <site>: (st)ackexchange");
       System.out.println("               (se)rverfault");
       System.out.println("               (su)peruser");
       System.out.println("     <tag(s)>: \"tag1;tag2..\"");
       System.out.println("     <months>: go back m months");
       System.out.println("      <query>: (q)uestions");
       System.out.println("               (a)nswers");
       System.out.println("               (c)omments");
       System.out.println("               (p)osts");
       System.out.println("");
       System.out.println("   Start Date: today - 30*months days ");
       System.out.println("     End Date: today");
       System.out.println("ex: java process_sx -s st -t \"oracle;linux\" -m 12 -q q");
       System.out.println("get all questions tagged oracle&linux for last year");
       
       System.exit(0);
    }
}

