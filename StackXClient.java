/*
 * Source:  <http://www.apache.org/>
 * package org.apache.http.examples.client;
 *
 * A client to stackexchange api
 *  - request
 *  - response zipped to conserve bandwidth
 *  - unzip response 
 *  - decode response json 
 *
 * Max requests/day : 300 (10000 with app key )
 *
 * Use of protocol interceptors to transparently
 * modify properties of HTTP messages sent / received by the HTTP client.
 *
 * In this particular case HTTP client is made capable of transparent content
 * GZIP compression by adding two protocol interceptors: a request interceptor
 * that adds 'Accept-Encoding: gzip' header to all outgoing requests and
 * a response interceptor that automatically expands compressed response
 * entities by wrapping them with a uncompressing decorator class. The use of
 * protocol interceptors makes content compression completely transparent to
 * the consumer of the {@link org.apache.http.client.HttpClient HttpClient}
 * interface.
 *  sp-11152015
 */

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
public class StackXClient {
    public static String _BASE_URL = "https://api.stackexchange.com/2.2/";
    public static String _INFO = "info?site=stackoverflow";
    DefaultHttpClient httpclient;

    public StackXClient() {
        httpclient = new DefaultHttpClient();
        addInterceptors();
    }

    private void addInterceptors() {
        try {
            httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
                public void process(
                        final HttpRequest request,
                        final HttpContext context) throws HttpException, IOException {
                    if (!request.containsHeader("Accept-Encoding")) {
                        request.addHeader("Accept-Encoding", "gzip");
                    }
                }

            });
            httpclient.addResponseInterceptor(new HttpResponseInterceptor() {
                public void process(
                        final HttpResponse response,
                        final HttpContext context) throws HttpException, IOException {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        Header ceheader = entity.getContentEncoding();
                        if (ceheader != null) {
                            HeaderElement[] codecs = ceheader.getElements();
                            for (int i = 0; i < codecs.length; i++) {
                                if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                                    response.setEntity(
                                            new GzipDecompressingEntity(response.getEntity()));
                                    return;
                                }
                            }
                        }
                    }
                }

            });
        } catch (Exception e) {
            System.out.println("%%http Exception  " + e);
        }
    }

    public HttpResponse getResponse( String request ) {
        if (request.equals("")) request = _INFO;
        HttpResponse response = null;
        if (request.equals("")) return null;

        try {
          HttpGet httpget = new HttpGet( _BASE_URL + request );
          System.out.println("@@executing: \n" + httpget.getURI());
          response = httpclient.execute(httpget);
        } catch (IOException e) {
            System.out.println("%%http getResponse exception  " + e);
        }
        return response;
    }


    public void showResults(HttpResponse response) {
        try {
            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            System.out.println(response.getLastHeader("Content-Encoding"));
            System.out.println(response.getLastHeader("Content-Length"));
            System.out.println("----------------------------------------");

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String content = EntityUtils.toString(entity);
                // System.out.println(content.toString());
                System.out.println("----------------------------------------");
                System.out.println("Uncompressed size: "+ content.length());
                
                // encode as json 
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject)parser.parse(content);
                
                System.out.println("_quota     " + json.get("quota_max"));
                System.out.println("_remaining " + json.get("quota_remaining"));
                System.out.println("_pretty print json");

                JSONWriter writer = new JSONWriter(); // this writer adds indentation
                json.writeJSONString(writer);
                System.out.println(writer.toString());
            }
        } catch (IOException e) {
            System.out.println("%%IO Exception  " + e);
        } catch (ParseException e) {
            System.out.println("%%JSON Parsing Exception  " + e);
        }
    }


    public String getResults(HttpResponse response) {
        String result = null;
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String content = EntityUtils.toString(entity);
                
                // encode as json 
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject)parser.parse(content);
                
                JSONWriter writer = new JSONWriter(); 
                json.writeJSONString(writer);
                result = writer.toString();
            }
        } catch (IOException e) {
            System.out.println("%%IO Exception  " + e);
        } catch (ParseException e) {
            System.out.println("%%JSON Parsing Exception  " + e);
        }
        return result;
    }

    public void shutdown() {
          httpclient.getConnectionManager().shutdown();
    }
}
