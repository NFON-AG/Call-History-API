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
// NFON Call History API GET example: Retrieve call records
//
// What it does:
// 1. Uses an OAuth2 access token obtained via browser-based login (PKCE)
// 2. Fetches call records using the access token
//
// Steps to run:
// 1. Login to https://id.nfon.com and open browser DevTools (F12)
// 2. Inspect any API request and copy the Bearer token from the Authorization header
// 3. Set environment variable:
//    Linux/macOS:        export ACCESS_TOKEN='<your-token>'
//    Windows CMD:        set ACCESS_TOKEN=<your-token>
//    Windows PowerShell: $env:ACCESS_TOKEN='<your-token>'
// 4. node get-callrecords.example.mjs
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
 * Retrieve call history records
 */
async function getCallRecords(accessToken) {
  const response = await fetch(
    `${BASE_URL}/records?limit=25`,
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
    console.log("Fetching call records...");
    const callRecords = await getCallRecords(ACCESS_TOKEN);

    console.log(
      "Call records:",
      JSON.stringify(callRecords, null, 2)
    );
  } catch (err) {
    console.error("Error:", err.message);
  }
})();