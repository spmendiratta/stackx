import java.io.FileReader;
import java.util.Iterator;
 
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
 
public class parseStackXJson {
 
    public static void main(String[] args) {
        if (args.length != 2) usage();
        if (!args[0].equals("-f")) usage();

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader( args[1]));
 
            JSONObject jsonObject = (JSONObject) obj;
            System.out.println(jsonObject.get("quota_max"));
            System.out.println(jsonObject.get("has_more"));
 
            JSONArray qlist = (JSONArray) jsonObject.get("items");
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
                if (is_answered && accepted_answer_id != null) {
                  System.out.println(title);
                  System.out.println(creation_date);
                  System.out.println(link);
                  System.out.println(tags);
                  System.out.println(question_id);
                  System.out.println(body);
                  System.out.println(accepted_answer_id);
                  System.out.println("accepted_answer_body");
                  System.out.println("____");
                }
            } 
        } catch (Exception e) {
            System.out.println("%%parseStackXJson error ");
            e.printStackTrace();
        }
    }

    public static void usage() {
       System.out.println("usage: java processStackXJson -f <fname>");
       System.exit(0);
    }
}
