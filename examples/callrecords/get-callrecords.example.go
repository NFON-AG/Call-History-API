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
// 4. go run get-callrecords.example.go
//
// Requirements:
// - Go 1.20+

package main

import (
	"fmt"
	"io"
	"net/http"
	"os"
	"time"
)

const (
	// TODO: Change these values to match your application
	appName    = "NFON-GitHub-Example" // Replace with your application name
	appVersion = "1.0"                 // Replace with your application version
	baseURL    = "https://api.nfon.com/call-history"
)

var userAgent = appName + "/" + appVersion

func getCallRecords(accessToken string) (string, error) {
	client := &http.Client{Timeout: 20 * time.Second}

	req, err := http.NewRequest("GET", baseURL+"/records?limit=25", nil)
	if err != nil {
		return "", err
	}
	req.Header.Set("Accept", "application/json")
	req.Header.Set("Authorization", "Bearer "+accessToken)
	req.Header.Set("User-Agent", userAgent)

	resp, err := client.Do(req)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	bodyBytes, _ := io.ReadAll(resp.Body)
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return "", fmt.Errorf("failed to fetch call records: %d %s", resp.StatusCode, string(bodyBytes))
	}

	return string(bodyBytes), nil
}

func main() {
	accessToken := os.Getenv("ACCESS_TOKEN")
	if accessToken == "" {
		fmt.Fprintln(os.Stderr, "Missing ACCESS_TOKEN env var (OAuth2 access token from browser-based PKCE login).")
		os.Exit(1)
	}

	fmt.Println("Fetching call records...")
	callRecordsJSON, err := getCallRecords(accessToken)
	if err != nil {
		fmt.Fprintln(os.Stderr, "Error:", err)
		os.Exit(1)
	}

	fmt.Println("Call records:", callRecordsJSON)
}
