package org.main;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Servidor HTTP simple (tipo Apache) en Java puro.
 * - Sirve archivos estaticos (HTML, PNG) desde src/main/resources/public
 * - Despacha rutas REST a metodos @GetMapping via reflexion
 * - Soporta @RequestParam para inyectar parametros del query string
 * - Atiende multiples solicitudes NO concurrentes (secuencial)
 */
public class HttpServer {

    private final int port;
    private final Map<String, Method> routeMap;
    private final Map<String, Object> beanInstances;
    private static final String STATIC_DIR = "src/main/resources/public";

    public HttpServer(int port, Map<String, Method> routeMap, Map<String, Object> beanInstances) {
        this.port = port;
        this.routeMap = routeMap;
        this.beanInstances = beanInstances;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Servidor iniciado en http://localhost:" + port);

        while (true) {
            Socket client = serverSocket.accept();
            handleRequest(client);
        }
    }

    private void handleRequest(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             OutputStream out = client.getOutputStream()) {

            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;
            System.out.println(">>> " + requestLine);

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;

            String method = parts[0];
            String fullPath = parts[1];

            // Consumir headers
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {}

            if (!"GET".equalsIgnoreCase(method)) {
                sendResponse(out, 405, "text/plain", "405 Method Not Allowed".getBytes());
                return;
            }

            String path = fullPath;
            String queryString = "";
            if (fullPath.contains("?")) {
                path = fullPath.substring(0, fullPath.indexOf("?"));
                queryString = fullPath.substring(fullPath.indexOf("?") + 1);
            }

            Map<String, String> queryParams = parseQueryString(queryString);

            if (routeMap.containsKey(path)) {
                handleRestRoute(out, path, queryParams);
            } else {
                handleStaticFile(out, path);
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * Parsea un query string (ej: "name=Juan&age=30") en un Map.
     */
    private Map<String, String> parseQueryString(String query) throws Exception {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = URLDecoder.decode(kv[0], "UTF-8");
                String value = URLDecoder.decode(kv[1], "UTF-8");
                params.put(key, value);
            } else if (kv.length == 1) {
                params.put(URLDecoder.decode(kv[0], "UTF-8"), "");
            }
        }
        return params;
    }

    private void handleRestRoute(OutputStream out, String path, Map<String, String> queryParams) throws Exception {
        Method method = routeMap.get(path);

        if (!method.getReturnType().equals(String.class)) {
            sendResponse(out, 500, "text/plain", "500 Internal Server Error: el metodo no retorna String".getBytes());
            return;
        }

        Object bean = null;
        for (Object b : beanInstances.values()) {
            if (b.getClass() == method.getDeclaringClass()) {
                bean = b;
                break;
            }
        }

        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            RequestParam rp = parameters[i].getAnnotation(RequestParam.class);
            if (rp != null) {
                String paramName = rp.value();
                String defaultVal = rp.defaultValue();
                args[i] = queryParams.getOrDefault(paramName, defaultVal);
            } else {
                args[i] = null;
            }
        }

        String body = (String) (Modifier.isStatic(method.getModifiers())
                ? method.invoke(null, args)
                : method.invoke(bean, args));

        if (body == null) body = "";
        String contentType = body.trim().startsWith("<") ? "text/html" : "text/plain";
        sendResponse(out, 200, contentType, body.getBytes(StandardCharsets.UTF_8));
    }

    private void handleStaticFile(OutputStream out, String path) throws IOException {
        Path filePath = Paths.get(STATIC_DIR, path).normalize();
        File file = filePath.toFile();

        if (!file.exists() || !file.isFile()) {
            sendResponse(out, 404, "text/html",
                    "<html><body><h1>404 Not Found</h1></body></html>".getBytes());
            return;
        }

        String contentType = getContentType(file.getName());
        byte[] fileBytes = Files.readAllBytes(filePath);
        sendResponse(out, 200, contentType, fileBytes);
    }

    private void sendResponse(OutputStream out, int code, String contentType, byte[] body) throws IOException {
        String status = code == 200 ? "OK" : code == 404 ? "Not Found" : "Error";
        String header = "HTTP/1.1 " + code + " " + status + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "\r\n";
        out.write(header.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

    private String getContentType(String name) {
        if (name.endsWith(".html")) return "text/html";
        if (name.endsWith(".css")) return "text/css";
        if (name.endsWith(".js")) return "application/javascript";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }
}
