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
// 1. Login to https://id.nfon.com and open browser DevTools (F12)
// 2. Inspect any API request and copy the Bearer token from the Authorization header
// 3. Set environment variable:
//    Linux/macOS:        export ACCESS_TOKEN='<your-token>'
//    Windows CMD:        set ACCESS_TOKEN=<your-token>
//    Windows PowerShell: $env:ACCESS_TOKEN='<your-token>'
// 4. node stream-events.example.mjs
// 5. Make an actual call
// 6. Enable transcription and speak
// 7. Hang up to trigger the event
//
// Requirements:
// - Node.js 18+ (native fetch support)

// TODO: Change these values to match your application
const APP_NAME = "NFON-GitHub-Example";  // Replace with your application name
const APP_VERSION = "1.0";               // Replace with your application version

const ACCESS_TOKEN = process.env.ACCESS_TOKEN; // obtained via browser-based OAuth2 PKCE login

const BASE_URL = "https://api.nfon.com/call-history";
const USER_AGENT = `${APP_NAME}/${APP_VERSION}`;

/**
 * Step 1: Connect to SSE stream and process call events
 */
async function streamCallEvents(accessToken) {
  const response = await fetch(
    `${BASE_URL}/records`,
    {
      method: "GET",
      headers: {
        "Accept": "text/event-stream",
        "Authorization": `Bearer ${accessToken}`,
        "User-Agent": USER_AGENT,
      },
    }
  );

  if (!response.ok) {
    throw new Error(
      `Failed to connect to event stream: ${response.status} ${response.statusText}`
    );
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    const chunk = decoder.decode(value);
    const lines = chunk.split("\n");

    for (const line of lines) {
      if (line.startsWith("data: ")) {
        const eventData = JSON.parse(line.slice(6));
        await handleCallEvent(eventData, accessToken);
      }
    }
  }
}

/**
 * Step 2: Process call event and print transcription summary for answered calls
 */
async function handleCallEvent(eventData, accessToken) {
  if (eventData.status === "answered") {
    console.log(`\nAnswered call detected: ${eventData.uuid}`);

    if (eventData.transcription) {
      const details = await getCallRecordDetails(
        accessToken,
        eventData.uuid
      );

      if (details.transcription?.summary) {
        console.log(`Summary: ${details.transcription.summary}`);
      }
    }
  } 
}

/**
 * Step 3: Fetch full call record details including transcription and summary
 */
async function getCallRecordDetails(accessToken, uuid) {
  const response = await fetch(
    `${BASE_URL}/records/${uuid}?complete=true`,
    {
      method: "GET",
      headers: {
        "Accept": "application/json",
        "Authorization": `Bearer ${accessToken}`,
        "User-Agent": USER_AGENT,
      },
    }
  );

  if (!response.ok) {
    throw new Error(
      `Failed to fetch call record: ${response.status} ${response.statusText}`
    );
  }

  return await response.json();
}

/**
 * Run the full flow
 */
(async () => {
  if (!ACCESS_TOKEN) {
    console.error("Error: ACCESS_TOKEN environment variable is required");
    process.exit(1);
  }

  try {
    console.log("Connecting to event stream...");
    await streamCallEvents(ACCESS_TOKEN);
  } catch (err) {
    console.error("Error:", err.message);
  }
})();
