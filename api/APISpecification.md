## Virtual Pantry API Specification
This document specifies all API requests that may be made against the VirtualPantry backend API.
<br>
URLs are in the form of */api/request/path/*, and are assumed to be appended to the base domain (IP or registered domain name)

<br>
All malformed requests will invoke a response of 
```
HTTP 400 BAD REQUEST
```
Some 400 class responses may include error information, in this form:
```
HTTP 400 BAD REQUEST

HTTP Headers
Content-Type : application/json

{
  reason: "The emailaddress used for registration is not valid."
}
```
Additionally, when requests require a SESSION_TOKEN as a URL parameter, the SESSION_TOKEN refers to the token received by a <b>GET /users/login/</b> request.  All requests requiring the session token may invoke a response of this type if the session token is malformed or expired:
```
HTTP 403 FORBIDDEN

HTTP Headers
Content-Type : application/json

{
	reason : "Token has expired."
}
```
###Table of Contents
[Authentication](APIAuthentication.md)<br>
[User Information](APIUserInfo.md)<br>
[Household Management](APIHouseholds.md)
