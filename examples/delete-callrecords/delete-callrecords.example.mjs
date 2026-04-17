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
// 4. node delete-callrecords.example.mjs
//
// Requirements:
// - Node.js 18+ (native fetch support)

// TODO: Change these values to match your application
const APP_NAME = "NFON-GitHub-Example";  // Replace with your application name
const APP_VERSION = "1.0";               // Replace with your application version

const ACCESS_TOKEN = process.env.ACCESS_TOKEN;

const BASE_URL = "https://api.nfon.com/call-history";
const USER_AGENT = `${APP_NAME}/${APP_VERSION}`;

async function getLastCallRecord(accessToken) {
  const response = await fetch(
    `${BASE_URL}/records?limit=1`,
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
      `Failed to fetch call records: ${response.status} ${response.statusText}`
    );
  }

  const records = await response.json();

  if (records.length === 0) {
    throw new Error("No call records found");
  }

  return records[0].uuid;
}

async function deleteCallRecord(accessToken, uuid) {
  const response = await fetch(
    `${BASE_URL}/records/${uuid}`,
    {
      method: "DELETE",
      headers: {
        "Authorization": `Bearer ${accessToken}`,
        "User-Agent": USER_AGENT,
      },
    }
  );

  if (response.status !== 204) {
    throw new Error(
      `Failed to delete call record: ${response.status} ${response.statusText}`
    );
  }
}

(async () => {
  if (!ACCESS_TOKEN) {
    console.error("Error: ACCESS_TOKEN environment variable is required");
    process.exit(1);
  }

  try {
    console.log("Fetching last call record...");
    const uuid = await getLastCallRecord(ACCESS_TOKEN);
    console.log(`Found call record with UUID: ${uuid}`);

    console.log("Deleting call record...");
    await deleteCallRecord(ACCESS_TOKEN, uuid);

    console.log("Call record deleted successfully.");
  } catch (err) {
    console.error("Error:", err.message);
  }
})();
