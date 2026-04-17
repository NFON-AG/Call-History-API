# NFON Call History API Usage Manual

- [Introduction](#introduction)
  - [What You Can Do With This API](#what-you-can-do-with-this-api)
  - [Terms of Use](#terms-of-use)
  - [Important Notes](#important-notes)
  - [Support \& Feedback](#support--feedback)
- [API Endpoints](#api-endpoints)
  - [Records](#records)
- [Authentication](#authentication)
  - [Authentication Flow](#authentication-flow)
- [User-Agent Header](#user-agent-header)
  - [Why is this required?](#why-is-this-required)
  - [Implementation](#implementation)
- [Examples](#examples)
  - [Get Call History Records](#get-call-history-records)
  - [Delete Call Records](#delete-call-records)
  - [Stream Call History Records (SSE)](#stream-call-history-records-sse)

## Introduction  

### What You Can Do With This API  

The NFON Call History API provides endpoints for retrieving, streaming, and deleting call history records for an authenticated NFON Cloud Telephony user.

> [!IMPORTANT]  
> 
> This API is currently offered as part of **[Login with NFON (Early Access)](https://www.nfon.com/en/integrations/login-with-nfon/)**. 
> 
> As an early access offering, the API is subject to change. Updates may occur on short notice, though we strive to provide advance notice where possible.

#### Example use cases include:  
- Retrieve call history records for an extension with filtering and search capabilities
- Stream real-time call history events as they occur using Server-Sent Events (SSE)
- Access call transcriptions and summaries for answered calls
- Delete call records individually or in bulk based on various criteria
- Integrate call history data into CRM systems or custom applications

---

### Terms of Use
By accessing and using the NFON Call History API, you agree to the [terms of use](https://www.nfon.com/en/legal/gtc-sla/).

---

### Important Notes

API endpoints may change: 
- Please subscribe to `API Breaking Changes` on the [NFON Status page](https://status.nfon.com) for updates.
- Please refer to the [latest API documentation](https://nfon-ag.github.io/Call-History-API/).

---

### Support & Feedback
NFON is committed to helping you integrate successfully with our APIs. Depending on the type of request or issue, please use the following channels:

#### Feature Requests
Got ideas to improve the API? Submit feature suggestions via Airfocus:
- [Submit in German language](https://nfon.airfocus.com/share/forms/a429ddf7abf54f6afed82d5a7327e030)
- [Submit in English language](https://nfon.airfocus.com/share/forms/648a1c17a224aa6e8937f8393f4bc21c)

#### Development Help
For best practices, implementation guidance, and community support:

- Join the [NFON Partner Portal](https://partners.nfon.com/)
- Contact your assigned sales representative  

#### Issues & Bugs
For technical issues or suspected bugs, please contact NFON Support directly.

## API Endpoints
Check the official [API documentation](https://nfon-ag.github.io/Call-History-API/) for latest endpoint references.
- **Base URL**: `https://api.nfon.com/call-history`
- **Architecture**: RESTful API
- **Data Format**: JSON

### Records

#### GET /records
Get, search, and stream call records with optional filtering.

The response format is controlled by the `Accept` request header:
- `application/json` (default): Returns a JSON array of call records
- `text/event-stream`: Streams call records as Server-Sent Events (SSE)

---

#### DELETE /records
Delete multiple call records based on filter criteria.

---

#### GET /records/{uuid}
Get details for a specific call record.

---

#### DELETE /records/{uuid}
Delete a single call record.

## Authentication

The NFON Call History API uses an OAuth 2.0 **`Access Token`** obtained via a browser-based login to NFON Cloud Telephony applications like the [Cloudya Web app](http://start.cloudya.com/). 

The `Access Token` must be included with every API request.

> 💡 As part of the **Login with NFON (Early Access)**, NFON provides partners with their own OAuth client, enabling them to integrate directly with the NFON login system using the standard OAuth 2.0 Authorization Code flow with PKCE.


**Authentication Header:**
```
Authorization: Bearer <your-access-token>
```

### Authentication Flow

```text
Access Token
     │
     ▼
GET /call-history/records
    (Authorization)
```

#### Example: Retrieve Call History Records
This example API call fetches call history entries.

```bash
curl 'https://api.nfon.com/call-history/records?limit=25' \
  -H 'accept: application/json' \
  -H 'authorization: Bearer <ACCESS_TOKEN>' \
  -H 'user-agent: my-crm-app/1.5.1'
```

---

## User-Agent Header

When integrating with the NFON APIs, please include a **User-Agent header** in all HTTP requests with the following format:

```
<productname>/<productversion>
```

**Example:**
```
my-crm-app/1.5.1
```

### Why is this required?

Including your application name and version in the User-Agent helps NFON's technical support team:

- **Quickly identify your application** in case of issues or unusual activity
- **Provide faster support** by understanding which integration is affected
- **Contact you proactively** if we detect any problems with your integration

### Implementation

All code examples in this repository include configurable constants at the top of each file:

```go
const (
    appName    = "NFON-GitHub-Example"  // Replace with your application name
    appVersion = "1.0"                  // Replace with your application version
)
```

Simply update these values to match your application before deploying to production.

---

## Examples

Below you'll find working examples for API operations using various programming languages. These are designed to help you get started quickly and understand how to authenticate and interact with the NFON Call History API.

> [!TIP]
> **Cannot find your programming language of choice?** We recommend you to use an **AI assistant** to rewrite the examples to other programming languages. 

### Get Call History Records
- [with Go (Golang)](./examples/callrecords/get-callrecords.example.go)
- [with Java](./examples/callrecords/GetCallRecordsExample.java)
- [with Node.js / Javascript](./examples/callrecords/get-callrecords.example.mjs)

### Delete Call Records
- [with Go (Golang)](./examples/delete-callrecords/delete-callrecords.example.go)
- [with Java](./examples/delete-callrecords/DeleteCallRecordsExample.java)
- [with Node.js / Javascript](./examples/delete-callrecords/delete-callrecords.example.mjs)

### Stream Call History Records (SSE)
- [with Go (Golang)](./examples/callrecords-events/stream-events.example.go)
- [with Java](./examples/callrecords-events/StreamEventsExample.java)
- [with Node.js / Javascript](./examples/callrecords-events/stream-events.example.mjs)
