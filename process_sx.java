/**
      getResponse("comments?order=desc&min=10&sort=votes&site=askubuntu");
      getResponse("tags?order=desc&site=serverfault"); // serverfault.tags
      getResponse("questions?site=serverfault");

*/
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.StringTokenizer;
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
    public static Hashtable contextHash = new Hashtable();
    public static String _QUERY = "";
    public static String _TAGS = "";
    public static String _SITE = "";
    public static int _MINUS_MONTHS = 0;
    public static StackXClient  sxClient = new StackXClient();
    public static String CASE_TEMPLATE_FILE =  "./newprob.json";
    public static String PATTERN_TEMPLATE_FILE =  "./genericPattern.json";
    public static String SOLUTION_TEMPLATE_FILE =  "./genericSolution.json";
    public static JSONObject CASE_TEMPLATE_JSON =  new JSONObject();
    public static JSONObject PATTERN_TEMPLATE_JSON =  new JSONObject();
    public static JSONObject SOLUTION_TEMPLATE_JSON =  new JSONObject();

    public static String[] _CONTEXT = {
                             "Application",
                             "Database",
                             "Network",
                             "OS",
                             "Overall"
                           };

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

      setContextHash();
      setJsonTemplates();

      String f = _SITE + "_" + _TAGS + ".json";
      String fname = f.replaceAll(";", "_");

      writeToFile(fname, query, results);
      JSONObject _obj = parseStackXJson( fname );

      fname = fname.replace(".json", "_Final.json");
      writeToFinalJSONFile( fname, _obj );
      
      sxClient.shutdown();
     } else usage();
    }

    private static void setContextHash() {
      contextHash.put("someApp", "Application");
      contextHash.put("mongodb", "Database");
      contextHash.put("postgresql", "Database");
      contextHash.put("mysql", "Database");
      contextHash.put("oracle", "Database");
      contextHash.put("sql-server", "Database");
      contextHash.put("cisco", "Network");
      contextHash.put("dns", "Network");
      contextHash.put("router", "Network");
      contextHash.put("centos", "OS"); 
      contextHash.put("debian", "OS"); 
      contextHash.put("fedora", "OS"); 
      contextHash.put("linux", "OS"); 
      contextHash.put("osx", "OS"); 
      contextHash.put("unix", "OS"); 
      contextHash.put("redhat", "OS"); 
      contextHash.put("ubuntu", "OS"); 
      contextHash.put("windows", "OS"); 
      contextHash.put("firewall", "Overall"); 
      contextHash.put("performance", "Overall"); 
      contextHash.put("security", "Overall"); 
    }

    private static void setJsonTemplates() {
        JSONParser parser = new JSONParser();
        try {
            Object fobj = parser.parse(new FileReader( CASE_TEMPLATE_FILE ));
            CASE_TEMPLATE_JSON = (JSONObject)fobj;
            fobj = parser.parse(new FileReader( PATTERN_TEMPLATE_FILE ));
            PATTERN_TEMPLATE_JSON = (JSONObject)fobj;
            fobj = parser.parse(new FileReader( SOLUTION_TEMPLATE_FILE ));
            SOLUTION_TEMPLATE_JSON = (JSONObject)fobj;
        } catch (Exception e) {
            System.out.println("%%setJsonCaseTemplate error ");
            e.printStackTrace();
        }
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
            int objCount = 0;
            for (int i = 0; i < qlist.size(); i++) {
                JSONObject iobj = (JSONObject) qlist.get(i);

                String title = (String)iobj.get("title");
                Long creation_date = (Long)iobj.get("creation_date");
                String link = (String)iobj.get("link");
                link = link.replaceAll("\\/", "/");

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
                   JSONObject obj = (JSONObject)CASE_TEMPLATE_JSON.clone();
                   
                   obj.put("problemId", question_id);
                   obj.put("problemNumber", new Long(question_id).toString());
                   obj.put("problemType", "Reference");
                   obj.put("problemSource", "StackExchange");
                   obj.put("problemSourceType", "Public");
                   obj.put("problemSourceURL", link);
                   obj.put("problemTitle", title);
                   obj.put("problemTags", tag_array);
                   obj.put("problemDescription", body);
                   obj.put("priority", "P2");
                   obj.put("problemTime", creation_date);
                   obj.put("problemTimeString", getDateString(creation_date));
                   obj.put("problemStatus", "Closed");
                   
                   // JSONArray c_array = (JSONArray)obj.get("context");
                   // JSONObject c_obj = (JSONObject)c_array.get(0);
                   obj.put("context", getContextJSON( tag_array ));

                   StringTokenizer st = new StringTokenizer(body, "\n");
                   while (st.hasMoreElements()) {
                       String token = (String)st.nextElement();
                   }

                   // obj.put("accepted_answer_id", accepted_answer_id);
                   /* sp-11032015 Just replace actual answer with answer URL 
                   String answer = getAnswerBody(accepted_answer_id);
                   obj.put("answer", answer);
                   */
                   String answerURL = getAnswerURL(accepted_answer_id);

                   //((JSONObject)((JSONArray)(obj.get("solutions"))).get(0)).put("description", answerURL); // won't work
                   /*
                   JSONArray s_array = (JSONArray)(obj.get("solutions"));
                   JSONObject s_obj = (JSONObject)s_array.get(0);
                   s_obj.put("description", answerURL);
                   s_obj.put("suggestedBy", "SP"+objCount);

                   JSONArray ss_array = new JSONArray();
                   ss_array.add(s_obj);

                   obj.put("solutions", ss_array);
                   // Won't work either
                   */
                   JSONArray obj_a = new JSONArray();
                   obj_a.add( getSolutionsJSON( answerURL ));
                   obj.put("solutions", obj_a);
                   
                   out_list.add( obj );
                   ++objCount;
                   // sleep(200);   // not to overwhelm stockexchange site
                                    // sp-11032015 Not needed
                } 
            } // for 
            System.out.println("@@final object count: " + objCount);
            _obj.put("cases", out_list);
        } catch (Exception e) {
            System.out.println("%%parseStackXJson error ");
            e.printStackTrace();
        }
        return _obj;
    }

    private static String getDateString( long creation_date ) {
        Date d = new Date(creation_date*1000);
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss zzz");
        return df.format(d);
    }

    private static JSONArray getContextJSON( JSONArray tag_array ) { 
        JSONArray  obj_a = new JSONArray();
        JSONObject jo = new JSONObject();

        Hashtable tagHash = new Hashtable();
        for (int j = 0; j < tag_array.size(); j++) {
               String tag = (String)(tag_array.get(j));
               if (contextHash.containsKey(tag)) {
                   String val = (String)contextHash.get(tag);
                   if (tagHash.containsKey(val)) {
                     String prevTag = (String)tagHash.get(val);
                     String newTag = prevTag + ";" + tag;
                     tagHash.put(val, newTag);
                   } else tagHash.put(val, tag);
               }
            }

        for ( String context : _CONTEXT ) {
            String cv = "";
            if (tagHash.containsKey(context)) {
                   cv = (String)tagHash.get(context);
            }
            jo = new JSONObject();
            jo.put(context, cv);
            obj_a.add(jo);
        }
        return obj_a;
    }

    private static JSONObject getSolutionsJSON( String answerURL ) {
        JSONObject jo = (JSONObject)SOLUTION_TEMPLATE_JSON.clone();
        jo.put("description", answerURL );
        return jo;
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

    private static String getAnswerURL( long accepted_answer_id ) {
      return StackXClient._BASE_URL + "answers/" + accepted_answer_id +  
             "?filter=withbody&site=" + _SITE;
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
       System.out.println("       <site>: (st)acksoveflow");
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
       System.out.println("get all questions from site stackexchange tagged oracle&linux for last year");
       
       System.exit(0);
    }
}
