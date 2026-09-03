// Copyright 2025 NFON AG
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//
// NFON Call History API SSE example: Stream call events and fetch transcription summaries
//
// What it does:
// 1. Uses an OAuth2 access token obtained via browser-based login (PKCE)
// 2. Connects to the /records SSE stream
// 3. For each answered call with transcription, fetches and prints the summary
// 
// Steps to run:
// 1. Login to your application using "Login with NFON" and open browser DevTools (F12)
// 2. Inspect any API request and copy the Bearer token from the Authorization header
// 3. Set environment variable:
//    Linux/macOS:        export ACCESS_TOKEN='<your-token>'
//    Windows CMD:        set ACCESS_TOKEN=<your-token>
//    Windows PowerShell: $env:ACCESS_TOKEN='<your-token>'
// 4. java StreamEventsExample.java
// 5. Make an actual call
// 6. Enable transcription and speak
// 7. Hang up to trigger the event
//
// Requirements:
// - Java 11+ (java.net.http.HttpClient)

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class StreamEventsExample {

  // TODO: Change these values to match your application
  private static final String APP_NAME = "NFON-GitHub-Example";  // Replace with your application name
  private static final String APP_VERSION = "1.0";               // Replace with your application version
  private static final String BASE_URL = "https://start.cloudya.com/api/callhistory";
  private static final String USER_AGENT = APP_NAME + "/" + APP_VERSION;

  public static void main(String[] args) {
    String accessToken = System.getenv("ACCESS_TOKEN");
    if (accessToken == null || accessToken.isBlank()) {
      System.err.println("Error: ACCESS_TOKEN environment variable is required");
      System.exit(1);
    }

    try {
      System.out.println("Connecting to event stream...");
      streamCallEvents(accessToken);

    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Step 1: Connect to SSE stream and process call events
   */
  private static void streamCallEvents(String accessToken) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(BASE_URL + "/records"))
      .header("Accept", "text/event-stream")
      .header("Authorization", "Bearer " + accessToken)
      .header("User-Agent", USER_AGENT)
      .GET()
      .build();

    HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new RuntimeException("Failed to connect to event stream: " + response.statusCode());
    }

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("data: ")) {
          String eventData = line.substring(6);
          handleCallEvent(eventData, accessToken);
        }
      }
    }
  }

  /**
   * Step 2: Process call event and print transcription summary for answered calls
   */
  private static void handleCallEvent(String eventData, String accessToken) {
    try {
      String status = extractJsonValue(eventData, "status");
      
      if ("answered".equals(status)) {
        String uuid = extractJsonValue(eventData, "uuid");
        System.out.println("\nAnswered call detected: " + uuid);

        if (eventData.contains("\"transcription\"")) {
          String details = getCallRecordDetails(accessToken, uuid);
          String summary = extractJsonValue(details, "summary");
          
          if (summary != null && !summary.isEmpty()) {
            System.out.println("Summary: " + summary);
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Error handling call event: " + e.getMessage());
    }
  }

  /**
   * Step 3: Fetch full call record details including transcription and summary
   */
  private static String getCallRecordDetails(String accessToken, String uuid) 
      throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(BASE_URL + "/records/" + uuid + "?complete=true"))
      .header("Accept", "application/json")
      .header("Authorization", "Bearer " + accessToken)
      .header("User-Agent", USER_AGENT)
      .GET()
      .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new RuntimeException("Failed to fetch call record: " + response.statusCode() + " " + response.body());
    }

    return response.body();
  }

  /**
   * Minimal JSON string value extraction without external dependencies.
   * Handles escaped quotes inside values.
   */
  private static String extractJsonValue(String json, String key) {
    String needle = "\"" + key + "\":\"";
    int start = json.indexOf(needle);
    if (start < 0) {
      return null;
    }
    start += needle.length();
    int end = start;
    while (end < json.length()) {
      if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') {
        break;
      }
      end++;
    }
    if (end >= json.length()) {
      return null;
    }
    return json.substring(start, end).replace("\\\"", "\"");
  }
}
