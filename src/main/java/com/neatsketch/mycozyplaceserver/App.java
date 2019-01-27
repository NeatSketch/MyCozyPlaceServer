package com.neatsketch.mycozyplaceserver;

import fi.iki.elonen.NanoHTTPD;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class App extends NanoHTTPD  {

    //private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    public App() throws IOException {
        super(80);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
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

                JSONObject requestJSON = new JSONObject(requestData);

                if (requestJSON.getString("action").equals("login")) {
                    //JSONObject payload = requestJSON.getJSONObject("payload");
                    String username = requestJSON.getString("username");
                    String authToken = WorldMap.loginPlayer(username, "<INSERT PASSWORD HERE>");
                    if (authToken == null) {
                        return createErrorResponse(1, "Login failed!");
                    } else {
                        JSONObject loginResponse = new JSONObject();
                        loginResponse.put("authToken", authToken);
                        return createOkResponse(loginResponse);
                    }
                } else if (requestJSON.getString("action").equals("update")) {
                    boolean updProps = requestJSON.getBoolean("updProps");
                    String username = requestJSON.getString("username");
                    String authToken = requestJSON.getString("authToken");
                    float positionX = requestJSON.getFloat("positionX");
                    float positionZ = requestJSON.getFloat("positionZ");
                    float velocityX = requestJSON.getFloat("velocityX");
                    float velocityZ = requestJSON.getFloat("velocityZ");
                    String accessoryHead = requestJSON.getString("accHead");
                    String accessoryNeck = requestJSON.getString("accNeck");
                    String accessoryButt = requestJSON.getString("accButt");
                    String accessoryHead2 = requestJSON.getString("accHead2");
                    String accessoryEyes = requestJSON.getString("accEyes");
                    String accessoryMouth = requestJSON.getString("accMouth");

                    System.out.println(accessoryHead);

                    Player player = WorldMap.getPlayer(username);

                    if ((player == null) || !authToken.equals(player.authToken) || !player.isOnline()) {
                        return createErrorResponse(2, "Auth error!");
                    } else {
                        if (updProps) {
                            WorldMap.updatePlayer(
                                    player,
                                    positionX, positionZ,
                                    velocityX, velocityZ,
                                    accessoryHead, accessoryNeck, accessoryButt,
                                    accessoryHead2, accessoryEyes, accessoryMouth);
                        }

                        AbstractMap.SimpleImmutableEntry<Integer, Integer> chunk = new AbstractMap.SimpleImmutableEntry<>(player.chunkX, player.chunkZ);

                        JSONObject updateResponse = new JSONObject();
                        JSONArray layers = new JSONArray();

                        JSONObject layer = new JSONObject();
                        layer.put("id", 0); // Layer number

                        // Entities of this layer
                        ArrayList<LinkedList<WorldMapEntity>> entities = WorldMap.getEntitiesInChunkAndItsNeighbors(chunk);

                        JSONArray chunks = new JSONArray();

                        int chunkX = chunk.getKey();
                        int chunkZ = chunk.getValue();
                        int i = 0;
                        for (LinkedList<WorldMapEntity> entityList : entities) {
                            JSONObject chunkObject = new JSONObject();
                            JSONArray entitiesInChunk = new JSONArray();
                            for (WorldMapEntity entity : entityList) {
                                JSONObject entityObject = new JSONObject();
                                if (entity instanceof Player) {
                                    Player currentPlayer = (Player)entity;
                                    if (!player.username.equals(currentPlayer.username) && currentPlayer.isOnline()) {
                                        entityObject.put("id", "p:" + currentPlayer.username);
                                        entityObject.put("type", 0);
                                        entityObject.put("name", currentPlayer.username);
                                        entityObject.put("posX", currentPlayer.positionX);
                                        entityObject.put("posZ", currentPlayer.positionZ);
                                        entityObject.put("velX", currentPlayer.velocityX);
                                        entityObject.put("velZ", currentPlayer.velocityZ);
                                        entityObject.put("accHead", currentPlayer.accessoryHead);
                                        entityObject.put("accNeck", currentPlayer.accessoryNeck);
                                        entityObject.put("accButt", currentPlayer.accessoryButt);
                                        entityObject.put("accHead2", currentPlayer.accessoryHead2);
                                        entityObject.put("accEyes", currentPlayer.accessoryEyes);
                                        entityObject.put("accMouth", currentPlayer.accessoryMouth);
                                        entitiesInChunk.put(entityObject);
                                    }
                                } else if (entity instanceof Block) {
                                    Block currentBlock = (Block)entity;
                                    entityObject.put("id", String.format("b:%d:%d", currentBlock.positionX, currentBlock.positionZ));
                                    entityObject.put("type", 1);
                                    entityObject.put("blockType", currentBlock.blockType);
                                    entityObject.put("posX", currentBlock.positionX);
                                    entityObject.put("posZ", currentBlock.positionZ);
                                    entitiesInChunk.put(entityObject);
                                } else if (entity instanceof Furniture) {
                                    Furniture currentFurniture = (Furniture)entity;
                                    entityObject.put("id", String.format("f:%d:%d", currentFurniture.positionX, currentFurniture.positionZ));
                                    entityObject.put("type", 2);
                                    entityObject.put("furnitureType", currentFurniture.furnitureType);
                                    entityObject.put("rotation", currentFurniture.rotation);
                                    entityObject.put("posX", currentFurniture.positionX);
                                    entityObject.put("posZ", currentFurniture.positionZ);
                                    entitiesInChunk.put(entityObject);
                                }
                            }
                            chunkObject.put("entities", entitiesInChunk);
                            chunkObject.put("x", chunkX + (i % 3) - 1);
                            chunkObject.put("z", chunkZ + (i / 3) - 1);
                            chunks.put(chunkObject);
                            i++;
                        }

                        layer.put("chunks", chunks);

                        layers.put(layer);

                        updateResponse.put("accHead", player.accessoryHead);
                        updateResponse.put("accNeck", player.accessoryNeck);
                        updateResponse.put("accButt", player.accessoryButt);
                        updateResponse.put("accHead2", player.accessoryHead2);
                        updateResponse.put("accEyes", player.accessoryEyes);
                        updateResponse.put("accMouth", player.accessoryMouth);
                        updateResponse.put("posX", player.positionX);
                        updateResponse.put("posZ", player.positionZ);
                        updateResponse.put("layers", layers);

                        return createOkResponse(updateResponse);
                    }
                } else if (requestJSON.getString("action").equals("setBlock")) {
                    String username = requestJSON.getString("username");
                    String authToken = requestJSON.getString("authToken");
                    int positionX = requestJSON.getInt("positionX");
                    int positionZ = requestJSON.getInt("positionZ");
                    int blockType = requestJSON.getInt("blockType");

                    Player player = WorldMap.getPlayer(username);

                    if ((player == null) || !authToken.equals(player.authToken) || !player.isOnline()) {
                        return createErrorResponse(2, "Auth error!");
                    } else {
                        WorldMap.setBlock(positionX, positionZ, blockType);
                        //JSONObject okResponse = new JSONObject();
                        return createOkResponse(null /*okResponse*/);
                    }
                } else if (requestJSON.getString("action").equals("setFurniture")) {
                    String username = requestJSON.getString("username");
                    String authToken = requestJSON.getString("authToken");
                    int positionX = requestJSON.getInt("positionX");
                    int positionZ = requestJSON.getInt("positionZ");
                    int furnitureType = requestJSON.getInt("furnitureType");
                    int rotation = requestJSON.getInt("rotation");

                    Player player = WorldMap.getPlayer(username);

                    if ((player == null) || !authToken.equals(player.authToken) || !player.isOnline()) {
                        return createErrorResponse(2, "Auth error!");
                    } else {
                        WorldMap.setFurniture(positionX, positionZ, furnitureType, rotation);
                        //JSONObject okResponse = new JSONObject();
                        return createOkResponse(null /*okResponse*/);
                    }
                }

                return createOkResponse(null);
            /*} catch (IOException e) {
                System.out.println("IOException");
                e.printStackTrace();*/
            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();

                return createErrorResponse(0, "Something went wrong!");
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
            if (fileName.endsWith(".html")) {
                mimeType = "text/html";
            } else if (fileName.endsWith(".js")) {
                mimeType = "application/javascript";
            } else if (fileName.endsWith(".css")) {
                mimeType = "text/css";
            } else if (fileName.endsWith(".png")) {
                mimeType = "image/png";
            } else {
                mimeType = "application/octet-stream";
            }

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

    private Response createOkResponse(Object responseObject) {
        JSONObject obj = new JSONObject();

        obj.put("status", "ok");
        obj.put("payload", responseObject);

        System.out.println(responseObject);

        return newFixedLengthResponse(obj.toString());
    }

    private Response createErrorResponse(int errorCode, String errorText) {
        JSONObject obj = new JSONObject();

        obj.put("status", "error");
        obj.put("errorCode", errorCode);
        obj.put("errorText", errorText);

        return newFixedLengthResponse(obj.toString());
    }

    private class StringResponse {
        String payload;
    }
}
