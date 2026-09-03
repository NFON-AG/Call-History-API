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
// 4. go run stream-events.example.go
// 5. Make an actual call
// 6. Enable transcription and speak
// 7. Hang up to trigger the event
//
// Requirements:
// - Go 1.20+

package main

import (
	"bufio"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
	"time"
)

const (
	// TODO: Change these values to match your application
	appName    = "NFON-GitHub-Example" // Replace with your application name
	appVersion = "1.0"                 // Replace with your application version
	baseURL    = "https://start.cloudya.com/api/callhistory"
)

var userAgent = appName + "/" + appVersion

type callRecord struct {
	UUID          string         `json:"uuid"`
	Status        string         `json:"status"`
	Transcription *transcription `json:"transcription,omitempty"`
}

type transcription struct {
	Available bool   `json:"available"`
	Summary   string `json:"summary,omitempty"`
}

// Step 1: Connect to SSE stream and process call events
func streamCallEvents(accessToken string) error {
	client := &http.Client{Timeout: 0}

	req, err := http.NewRequest("GET", baseURL+"/records", nil)
	if err != nil {
		return err
	}
	req.Header.Set("Accept", "text/event-stream")
	req.Header.Set("Authorization", "Bearer "+accessToken)
	req.Header.Set("User-Agent", userAgent)

	resp, err := client.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		bodyBytes, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("failed to connect to event stream: %d %s", resp.StatusCode, string(bodyBytes))
	}

	scanner := bufio.NewScanner(resp.Body)
	for scanner.Scan() {
		line := scanner.Text()

		if strings.HasPrefix(line, "data: ") {
			data := strings.TrimPrefix(line, "data: ")

			var event callRecord
			if err := json.Unmarshal([]byte(data), &event); err != nil {
				fmt.Fprintf(os.Stderr, "Failed to parse event data: %v\n", err)
				continue
			}

			handleCallEvent(&event, accessToken)
		}
	}

	return scanner.Err()
}

// Step 2: Process call event and print transcription summary for answered calls
func handleCallEvent(event *callRecord, accessToken string) {
	if event.Status == "answered" {
		fmt.Printf("\nAnswered call detected: %s\n", event.UUID)

		if event.Transcription != nil {
			details, err := getCallRecordDetails(accessToken, event.UUID)
			if err != nil {
				fmt.Fprintf(os.Stderr, "Error fetching call details: %v\n", err)
				return
			}

			if details.Transcription != nil && details.Transcription.Summary != "" {
				fmt.Printf("Summary: %s\n", details.Transcription.Summary)
			}
		}
	}
}

// Step 3: Fetch full call record details including transcription and summary
func getCallRecordDetails(accessToken, uuid string) (*callRecord, error) {
	client := &http.Client{Timeout: 20 * time.Second}

	req, err := http.NewRequest("GET", baseURL+"/records/"+uuid+"?complete=true", nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("Accept", "application/json")
	req.Header.Set("Authorization", "Bearer "+accessToken)
	req.Header.Set("User-Agent", userAgent)

	resp, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	bodyBytes, _ := io.ReadAll(resp.Body)
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return nil, fmt.Errorf("failed to fetch call record: %d %s", resp.StatusCode, string(bodyBytes))
	}

	var details callRecord
	if err := json.Unmarshal(bodyBytes, &details); err != nil {
		return nil, fmt.Errorf("failed to parse call record JSON: %w", err)
	}

	return &details, nil
}

func main() {
	accessToken := os.Getenv("ACCESS_TOKEN")
	if accessToken == "" {
		fmt.Fprintln(os.Stderr, "Error: ACCESS_TOKEN environment variable is required")
		os.Exit(1)
	}

	fmt.Println("Connecting to event stream...")
	if err := streamCallEvents(accessToken); err != nil {
		fmt.Fprintln(os.Stderr, "Error:", err)
		os.Exit(1)
	}
}
