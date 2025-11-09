import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class WebServer {
    
    private static final int PORT = 8080;
    
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        server.createContext("/", new StaticFileHandler());
        server.createContext("/generate", new GenerateCertificateHandler());
        server.createContext("/download", new DownloadHandler());
        server.createContext("/list", new ListCertificatesHandler());
        server.createContext("/revoke", new RevokeCertificateHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║   Certificate Generator Server Started    ║");
        System.out.println("╠════════════════════════════════════════════╣");
        System.out.println("║  🌐 Server running on port " + PORT + "           ║");
        System.out.println("║  🔗 Open: http://localhost:" + PORT + "          ║");
        System.out.println("║  📁 Certificates saved in: certificates/   ║");
        System.out.println("║  📊 Logs saved in: data/certificates.xml   ║");
        System.out.println("╚════════════════════════════════════════════╝");
    }
    
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            File file = new File("static" + path);
            
            if (file.exists() && !file.isDirectory()) {
                String contentType = getContentType(path);
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, file.length());
                
                OutputStream os = exchange.getResponseBody();
                Files.copy(file.toPath(), os);
                os.close();
            } else {
                String response = "404 - File Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            return "text/plain";
        }
    }
    
    static class GenerateCertificateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                    BufferedReader br = new BufferedReader(isr);
                    String query = br.readLine();
                    
                    Map<String, String> params = parseQuery(query);
                    
                    String name = params.getOrDefault("name", "");
                    String certType = params.getOrDefault("certType", "");
                    String courseName = params.getOrDefault("courseName", "");
                    String date = params.getOrDefault("date", "");
                    String instructor = params.getOrDefault("instructor", "");
                    String hours = params.getOrDefault("hours", "");
                    
                    CertificateGenerator generator = new CertificateGenerator();
                    String certId = generator.generateCertificate(name, certType, courseName, 
                                                                 date, instructor, hours);
                    
                    String jsonResponse = "{\"success\": true, \"certificateId\": \"" + certId + "\"}";
                    
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, jsonResponse.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(jsonResponse.getBytes());
                    os.close();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorResponse = "{\"success\": false, \"error\": \"" + 
                                         e.getMessage().replace("\"", "'") + "\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(500, errorResponse.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(errorResponse.getBytes());
                    os.close();
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
        
        private Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
            Map<String, String> result = new HashMap<>();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length > 1) {
                        result.put(URLDecoder.decode(pair[0], "UTF-8"), 
                                 URLDecoder.decode(pair[1], "UTF-8"));
                    } else {
                        result.put(URLDecoder.decode(pair[0], "UTF-8"), "");
                    }
                }
            }
            return result;
        }
    }
    
    static class DownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = new HashMap<>();
            
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length > 1) {
                        params.put(pair[0], URLDecoder.decode(pair[1], "UTF-8"));
                    }
                }
            }
            
            String certId = params.get("id");
            if (certId != null) {
                File file = new File("certificates/certificate_" + certId + ".pdf");
                
                if (file.exists()) {
                    exchange.getResponseHeaders().set("Content-Type", "application/pdf");
                    exchange.getResponseHeaders().set("Content-Disposition", 
                        "attachment; filename=\"certificate_" + certId + ".pdf\"");
                    exchange.sendResponseHeaders(200, file.length());
                    
                    OutputStream os = exchange.getResponseBody();
                    Files.copy(file.toPath(), os);
                    os.close();
                } else {
                    String response = "Certificate not found";
                    exchange.sendResponseHeaders(404, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else {
                String response = "Invalid request";
                exchange.sendResponseHeaders(400, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
    
    static class ListCertificatesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                File xmlFile = new File("data/certificates.xml");
                
                if (!xmlFile.exists()) {
                    String response = "{\"certificates\": []}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    return;
                }
                
                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append("{\"certificates\": [");
                
                BufferedReader reader = new BufferedReader(new FileReader(xmlFile));
                String line;
                boolean inCertificate = false;
                String id = "", name = "", type = "", course = "", date = "", instructor = "", hours = "", status = "";
                boolean firstCert = true;
                
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    
                    if (line.equals("<certificate>")) {
                        inCertificate = true;
                        id = name = type = course = date = instructor = hours = status = "";
                    } else if (line.equals("</certificate>") && inCertificate) {
                        if (!firstCert) {
                            jsonBuilder.append(",");
                        }
                        jsonBuilder.append("{");
                        jsonBuilder.append("\"id\":\"").append(escapeJson(id)).append("\",");
                        jsonBuilder.append("\"name\":\"").append(escapeJson(name)).append("\",");
                        jsonBuilder.append("\"type\":\"").append(escapeJson(type)).append("\",");
                        jsonBuilder.append("\"course\":\"").append(escapeJson(course)).append("\",");
                        jsonBuilder.append("\"date\":\"").append(escapeJson(date)).append("\",");
                        jsonBuilder.append("\"instructor\":\"").append(escapeJson(instructor)).append("\",");
                        jsonBuilder.append("\"hours\":\"").append(escapeJson(hours)).append("\",");
                        jsonBuilder.append("\"status\":\"").append(escapeJson(status.isEmpty() ? "Active" : status)).append("\"");
                        jsonBuilder.append("}");
                        firstCert = false;
                        inCertificate = false;
                    } else if (inCertificate) {
                        if (line.startsWith("<id>")) id = extractValue(line, "id");
                        else if (line.startsWith("<n>")) name = extractValue(line, "n");
                        else if (line.startsWith("<type>")) type = extractValue(line, "type");
                        else if (line.startsWith("<course>")) course = extractValue(line, "course");
                        else if (line.startsWith("<date>")) date = extractValue(line, "date");
                        else if (line.startsWith("<instructor>")) instructor = extractValue(line, "instructor");
                        else if (line.startsWith("<hours>")) hours = extractValue(line, "hours");
                        else if (line.startsWith("<status>")) status = extractValue(line, "status");
                    }
                }
                reader.close();
                
                jsonBuilder.append("]}");
                String response = jsonBuilder.toString();
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                
            } catch (Exception e) {
                e.printStackTrace();
                String response = "{\"certificates\": [], \"error\": \"" + e.getMessage() + "\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
        
        private String extractValue(String line, String tag) {
            String startTag = "<" + tag + ">";
            String endTag = "</" + tag + ">";
            int start = line.indexOf(startTag) + startTag.length();
            int end = line.indexOf(endTag);
            if (start > 0 && end > start) {
                return unescapeXml(line.substring(start, end));
            }
            return "";
        }
        
        private String escapeJson(String s) {
            return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        }
        
        private String unescapeXml(String s) {
            return s.replace("&amp;", "&").replace("&lt;", "<")
                    .replace("&gt;", ">").replace("&quot;", "\"")
                    .replace("&apos;", "'");
        }
    }
    
    static class RevokeCertificateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    String certId = null;
                    
                    if (query != null) {
                        for (String param : query.split("&")) {
                            String[] pair = param.split("=");
                            if (pair.length > 1 && pair[0].equals("id")) {
                                certId = URLDecoder.decode(pair[1], "UTF-8");
                            }
                        }
                    }
                    
                    if (certId == null) {
                        String response = "{\"success\": false, \"error\": \"Certificate ID not provided\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(400, response.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                        return;
                    }
                    
                    File xmlFile = new File("data/certificates.xml");
                    if (!xmlFile.exists()) {
                        String response = "{\"success\": false, \"error\": \"No certificates found\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(404, response.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                        return;
                    }
                    
                    // Read and update XML
                    StringBuilder xmlContent = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new FileReader(xmlFile));
                    String line;
                    boolean inTargetCert = false;
                    boolean foundCert = false;
                    
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("<certificate>")) {
                            inTargetCert = false;
                        }
                        
                        if (inTargetCert && line.contains("<status>")) {
                            // Skip existing status line
                            continue;
                        }
                        
                        if (line.contains("<id>" + certId + "</id>")) {
                            inTargetCert = true;
                            foundCert = true;
                        }
                        
                        xmlContent.append(line).append("\n");
                        
                        if (inTargetCert && line.contains("</generated>")) {
                            xmlContent.append("    <status>Revoked</status>\n");
                        }
                    }
                    reader.close();
                    
                    if (foundCert) {
                        FileWriter writer = new FileWriter(xmlFile);
                        writer.write(xmlContent.toString());
                        writer.close();
                        
                        String response = "{\"success\": true}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, response.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    } else {
                        String response = "{\"success\": false, \"error\": \"Certificate not found\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(404, response.length());
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    String response = "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(500, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
}