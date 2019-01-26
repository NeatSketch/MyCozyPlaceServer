package com.neatsketch;

import fi.iki.elonen.NanoHTTPD;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class App extends NanoHTTPD  {

    private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    public App() throws IOException {
        super(80);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
        System.out.println("\nRunning! Point your browsers to http://localhost:80/ \n");
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main( String[] args ) {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        try {
            new App();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        //String msg = "<html><body><h1>Hello server</h1>\n";
        //Map<String, String> parms = session.getParms();

        Map<String, String> body = new HashMap<String, String>();
        Method method = session.getMethod();

        if (method.equals(Method.POST)) {
            try {
                session.parseBody(body);
                String requestData = body.get("postData");

                System.out.println("Request: " + requestData);



                return createResponse("OK!");
            /*} catch (IOException e) {
                System.out.println("IOException");
                e.printStackTrace();*/
            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();
            }
        }

        String uri = session.getUri();

        String fileName = null;
        Response.Status status;
        String mimeType = null;

        if (uri.equals("/")) {
            fileName = "index.html";
            status = Response.Status.OK;
            mimeType = "text/html";
        } else if (uri.contains("..")) {
            fileName = "error.html";
            status = Response.Status.BAD_REQUEST;
            mimeType = "text/html";
        } else {
            fileName = uri.substring(1);
            status = Response.Status.OK;
            mimeType = "text/html";
        }

        FileInputStream inputStream = null;

        try {
            inputStream = new FileInputStream("./static/" + fileName);
            return newFixedLengthResponse(Response.Status.OK, mimeType, inputStream, inputStream.getChannel().size());
        } catch (FileNotFoundException e) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", "<html><head><title>Not Found</title></head><body><h1>Not Found</h1></body></html>");
        } catch (IOException e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html", "<html><head><title>Internal Error</title></head><body><h1>Internal Error</h1></body></html>");
        }
    }

    private Response createResponse(Object responseObject) {
        JSONObject obj = new JSONObject();
        if (responseObject instanceof String)
        {
            //StringResponse stringResponseObject = new StringResponse();
            //stringResponseObject.payload = (String)responseObject;
            //responseObject = stringResponseObject;

            obj.put("payload", responseObject);
        }
        return newFixedLengthResponse(obj.toString());
    }

    private class StringResponse {
        String payload;
    }
}
