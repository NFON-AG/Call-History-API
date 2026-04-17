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
// NFON Call History API DELETE example: Delete the last call record
//
// What it does:
// 1. Uses an OAuth2 access token obtained via browser-based login (PKCE)
// 2. Fetches the most recent call record
// 3. Deletes that record by UUID
// 
// Steps to run:
// 1. Login to https://id.nfon.com and open browser DevTools (F12)
// 2. Inspect any API request and copy the Bearer token from the Authorization header
// 3. Set environment variable:
//    Linux/macOS:        export ACCESS_TOKEN='<your-token>'
//    Windows CMD:        set ACCESS_TOKEN=<your-token>
//    Windows PowerShell: $env:ACCESS_TOKEN='<your-token>'
// 4. java DeleteCallRecordsExample.java
//
// Requirements:
// - Java 11+ (java.net.http.HttpClient)

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DeleteCallRecordsExample {

  // TODO: Change these values to match your application
  private static final String APP_NAME = "NFON-GitHub-Example";  // Replace with your application name
  private static final String APP_VERSION = "1.0";               // Replace with your application version
  private static final String BASE_URL = "https://api.nfon.com/call-history";
  private static final String USER_AGENT = APP_NAME + "/" + APP_VERSION;

  public static void main(String[] args) {
    String accessToken = System.getenv("ACCESS_TOKEN");
    if (accessToken == null || accessToken.isBlank()) {
      System.err.println("Missing ACCESS_TOKEN env var (OAuth2 access token from browser-based PKCE login).");
      System.exit(1);
    }

    try {
      System.out.println("Fetching last call record...");
      String uuid = getLastCallRecord(accessToken);
      System.out.println("Found call record with UUID: " + uuid);

      System.out.println("Deleting call record...");
      deleteCallRecord(accessToken, uuid);

      System.out.println("Call record deleted successfully.");

    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(1);
    }
  }

  private static String getLastCallRecord(String accessToken) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(BASE_URL + "/records?limit=1"))
      .header("Accept", "application/json")
      .header("Authorization", "Bearer " + accessToken)
      .header("User-Agent", USER_AGENT)
      .GET()
      .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new RuntimeException("Failed to fetch call records: " + response.statusCode() + " " + response.body());
    }

    String body = response.body();
    if (body.equals("[]")) {
      throw new RuntimeException("No call records found");
    }

    String needle = "\"uuid\":\"";
    int start = body.indexOf(needle);
    if (start < 0) {
      throw new RuntimeException("uuid not present in call record response");
    }
    start += needle.length();
    int end = body.indexOf("\"", start);
    if (end < 0) {
      throw new RuntimeException("Failed to parse uuid from call record response");
    }

    return body.substring(start, end);
  }

  private static void deleteCallRecord(String accessToken, String uuid) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(BASE_URL + "/records/" + uuid))
      .header("Authorization", "Bearer " + accessToken)
      .header("User-Agent", USER_AGENT)
      .DELETE()
      .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 204) {
      throw new RuntimeException("Failed to delete call record: " + response.statusCode() + " " + response.body());
    }
  }
}
