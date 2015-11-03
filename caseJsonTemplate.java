import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.io.IOException;
public class caseJsonTemplate {
    public final static void main(String[] args) {
       wrap( null );
    }

    private static JSONObject wrap(JSONObject iobj) {
        JSONObject obj = new JSONObject();
        JSONArray  obj_a = new JSONArray();
        HashMap hash = new HashMap();

        obj.put("problemId", 0);
        obj.put("problemNumber", "0");
        obj.put("problemType", "Reference");
        obj.put("problemSource", "StackExchange");
        obj.put("problemSourceType", "Public");
        obj.put("problemSourceURL", "SOURCE_URL");
        obj.put("problemTitle", "PROBLEM_TITLE");
        obj_a = new JSONArray();
        obj.put("problemTags", obj_a); 
        obj.put("problemDescription", "PROBLEM_DESCRIPTION"); 
        obj.put("alerts", null);
        obj.put("external_alerts", null);
        obj.put("kpis", null);
        obj.put("hostName", null);
        obj.put("priority", "P2");
        obj.put("appComponent", null);
        obj.put("applicationID", null);
        obj.put("applicationName", null);
        obj.put("problemTime", null);
        obj.put("problemTimeString", null);
        obj.put("problemStatus", "CLOSED");
        obj.put("problemComments", null);
        hash = new HashMap();
        hash.put("Application", null);
        hash.put("Database", null);
        hash.put("Network", null);
        hash.put("OS", null);
        hash.put("Overall", null);
        obj.put("context", getJSONArray(hash));

        obj_a = new JSONArray();
        JSONObject jo = new JSONObject();
           jo.put("tierName", "");
           jo.put("tierType", "");
           jo.put("anomalyScore", 10);
           jo.put("metricAnomalies", null);
             JSONObject jo2 = new JSONObject();
             jo2.put("rarePatterns", null);
             jo2.put("unusualPatterns", null);
               JSONArray obj_a3 = new JSONArray();
               JSONObject jo3 = new JSONObject();
                 jo3.put("anomalyPatternID", 100);
                 jo3.put("upTick", false);
                 jo3.put("hostname", null);
                 jo3.put("displaypattern", "ERROR_MSG");
                 jo3.put("percentChange", 0);
                 jo3.put("hour", null);
                 jo3.put("pattern", "ERROR_MSG");
                 jo3.put("frequency", 1);
                 jo3.put("isClue", "true");
                   JSONObject jo4 = new JSONObject();
                   jo4.put("left", 10);
                   jo4.put("right", 10);
                 jo3.put("clueDisplayPosition", jo4);
                 jo3.put("clueConnections", null);
                 jo3.put("isFiltered", "false");
                 jo3.put("gobblerLogIdList", null);
               obj_a3.add(jo3);
             jo2.put("otherPatterns", obj_a3);
             jo2.put("rareEventsList", null);
             jo2.put("unusualEventsList", null);
             jo2.put("otherEventsList", null);
           jo.put("logAnomalies", jo2);
        obj_a.add(jo);
        obj.put("anomalies", obj_a);

        obj_a = new JSONArray();
        jo = new JSONObject();
         jo.put("description","DESCRIPTION");
         jo.put("suggestedBy","AUTHOR");
         jo.put("suggestedOn","TIME_STAMP");
           hash = new HashMap();
           hash.put("comment", "COMMENT");
           hash.put("commentBy", "AUTHOR");
           hash.put("commentOn", "TIME_STAMP");
           hash.put("rating", new Long(1));
         jo.put("reviewsAndRatings", getJSONArray(hash));
         jo.put("averageRating", 1);
         jo.put("numberOfRatings",  1);
         jo.put("tags", null);
         jo.put("annotations", null);
        obj_a.add(jo);
        obj.put("solutions", obj_a);

        hash = new HashMap();
        hash.put("cause", "CAUSE");
        obj.put("rootCauses", getJSONArray(hash));
        obj.put("matchingProblemIDList", null );
        obj.put("duplicateProblemIDList", null );
        try {
           JSONWriter writer = new JSONWriter(); // this writer adds indentation
           obj.writeJSONString(writer);
           System.out.println(writer.toString());
        } catch (IOException e) {
        }
        
        return null;
    }


    private static JSONArray getJSONArray( HashMap hash ) { 
        JSONArray  obj_a = new JSONArray();
        JSONObject jo = new JSONObject();
        Iterator iter = hash.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String key = (String)entry.getKey();
            jo.put(key, entry.getValue());
        }
        obj_a.add(jo);
        return obj_a;
    }
}
