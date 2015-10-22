/**
      getResponse("comments?order=desc&min=10&sort=votes&site=askubuntu");
      getResponse("tags?order=desc&site=serverfault"); // serverfault.tags
      getResponse("questions?site=serverfault");

*/
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.io.IOException;
import org.apache.http.HttpResponse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.apache.http.HttpResponse;
public class process_sx {
   public static String[] _SITES = {
                        "stackoverflow",
                        "serverfault",
                        "superuser",
                        "askubuntu"
                       };
   public static String[] _QUERIES = {
                        "questions",
                        "answers",
                        "comments",
                        "posts" 
                       };
    public static String _QUERY = "";
    public static String _TAGS = "";
    public static String _SITE = "";
    public static int _MINUS_MONTHS = 0;
    public static StackXClient  sxClient = new StackXClient();

    public final static void main(String[] args) throws Exception {
     if (validate( args )) { 
      // long today = System.currentTimeMillis() / 1000;

      Calendar calendar = Calendar.getInstance();
      calendar.setTime( new Date());	// today
      long toDate = calendar.getTimeInMillis() / 1000;

      calendar.add(Calendar.DATE, -_MINUS_MONTHS*30);
      long fromDate = calendar.getTimeInMillis() / 1000;


      HttpResponse  response;
      // response = sxClient.getResponse("");
      // sxClient.showResults( response );

      String query = _QUERY + 
                     "?tagged=" + _TAGS + 
                     "&fromdate=" + fromDate + 
                     "&todate=" + toDate + 
                     "&sort=votes&order=desc" +
                     "&filter=withbody" +
                     "&pagesize=100" +
                     "&site=" + _SITE;
      response = sxClient.getResponse(query);
      String results = sxClient.getResults( response );

      String f = _SITE + "_" + _TAGS + ".json";
      String fname = f.replaceAll(";", "_");

      writeToFile(fname, query, results);
      JSONObject _obj = parseStackXJson( fname );

      fname = fname.replace(".json", "_Final.json");
      writeToFinalJSONFile( fname, _obj );
      
      sxClient.shutdown();
     } else usage();
    }

    private static void writeToFile(String fname, String query, String results) {
        BufferedWriter writer = null;
        try {
            File logFile = new File(fname);
            System.out.println("@@saving results to:\n" + logFile.getCanonicalPath());
            writer = new BufferedWriter(new FileWriter(logFile));

            // prepend some information first to results coming back ferom sx`
            String qt = "\"";
            writer.write("{\n");
            writer.write("  " + qt + "_query" +  qt + ": " + qt + StackXClient._BASE_URL + query + qt + ",\n");
            writer.write("  " + qt + "_site" +   qt + ": " + qt +_SITE + qt + ",\n");
            //
            writer.write(results.substring(2)); // Skip {\n from results
            writer.close();
        } catch (Exception e) {
            System.out.println("%%writeToFile error ");
            e.printStackTrace();
        } 
    }

    private static JSONObject parseStackXJson( String fname ) {
        JSONObject _obj =  new JSONObject();
        JSONParser parser = new JSONParser();
        try {
            Object fobj = parser.parse(new FileReader( fname ));
            JSONObject jsonObject = (JSONObject)fobj;

            JSONArray qlist = (JSONArray) jsonObject.get("items");
            JSONArray out_list = new JSONArray();
            for (int i = 0; i < qlist.size(); i++) {
                JSONObject iobj = (JSONObject) qlist.get(i);

                String title = (String)iobj.get("title");
                Long creation_date = (Long)iobj.get("creation_date");
                String link = (String)iobj.get("link");
                JSONArray tag_array = (JSONArray)iobj.get("tags");
                String tags = "";
                for (int j = 0; j < tag_array.size(); j++) {
                    tags += (String)tag_array.get(j) + ";";
                }
                String body = (String)iobj.get("body");
                Long question_id = (Long)iobj.get("question_id");
                Boolean is_answered = (Boolean)iobj.get("is_answered");
                Long accepted_answer_id = (Long)iobj.get("accepted_answer_id");

                // capture only answered questions
                if (is_answered && accepted_answer_id != null) {
                   JSONObject obj = new JSONObject();
                   obj.put("title", title);
                   obj.put("creation_date", creation_date);
                   obj.put("link", link);
                   obj.put("tags", tags);
                   obj.put("question_id", question_id);
                   obj.put("question", body.substring(0, 20) + " ... ");
                   obj.put("accepted_answer_id", accepted_answer_id);
                   String answer = getAnswerBody(accepted_answer_id);
                   obj.put("answer", answer.substring(0, 20) + " ... ");
                   
                   out_list.add( obj );
                   sleep(200);   // not to overwhelm stockexchange site
                } 
            } // for 
            _obj.put("cases", out_list);
        } catch (Exception e) {
            System.out.println("%%parseStackXJson error ");
            e.printStackTrace();
        }
        return _obj;
    }

    private static void writeToFinalJSONFile( String fname, JSONObject obj ) {
        BufferedWriter writer = null;
        try {
            File logFile = new File(fname);
            System.out.println("@@saving final results to:\n" + logFile.getCanonicalPath());
            writer = new BufferedWriter(new FileWriter(logFile));

            JSONWriter json_writer = new JSONWriter(); // this writer adds indentation
            obj.writeJSONString(json_writer);
            writer.write(json_writer.toString());
            writer.close();
        } catch (Exception e) {
            System.out.println("%%writeToFinalJSONFile error ");
            e.printStackTrace();
        } 
    }

    private static String getAnswerBody( long accepted_answer_id ) {
        String body = "";
        JSONParser parser = new JSONParser();
        try {
            String query = "answers/" + accepted_answer_id +  
                           "?filter=withbody&site=" + _SITE;
            HttpResponse response = sxClient.getResponse(query);
            String results = sxClient.getResults( response );

            JSONObject jsonObj = (JSONObject)parser.parse(results);
            JSONArray qlist = (JSONArray) jsonObj.get("items");
            jsonObj = (JSONObject) qlist.get(0);

            body = (String)jsonObj.get("body");
        } catch (Exception e) {
            System.out.println("%%parseStackXJson getAnswerBody ");
            e.printStackTrace();
        }
        return body;
    }

    private static void sleep(int msecs ) {
      try {
          Thread.sleep(msecs);           
      } catch(InterruptedException e) {
          e.printStackTrace();
      }
    }

    // there are better ways - 
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
