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
<hr>
### Authentication 
<hr>
####User Login<br>
<b>Request Format<b>
```
GET /users/login/

HTTP Headers
Authorization : Basic base64(emailAddress:sha256(password))
```
<b>Response Format</b><br>
Authentication Successful
```
HTTP 200 OK

HTTP Headers
Content-Type : application/json

{
  firstName : "John",
  lastName : "Doe",
  userID: 123456,
  token: "1284b68d36840496fbec62f34f40295de18e2bbffc3f2ec24cfab92fc734c9a9",
  token-expiration: 1423956647
}
```
Authentication Failure
```
HTTP 401 UNAUTHORIZED

HTTP Headers
WWW-Authenticate : Basic realm="api"
```
<hr>
####User Registration<br>
<b>Request Format<b>
```
POST /users/register/

HTTP Headers
Content-Type : application/json

{
  email : "emailAddress@domain.com",
  firstName : "First",
  lastName : "Last",
  password : sha256(password)
}
```
<b>Response Format</b><br>
Registration Successful
```
HTTP 201 CREATED
```
Registration Failure
```
HTTP 403 FORBIDDEN

HTTP Headers
Content-Type : application/json

{
  reason : "Email address is already in use."
}
```
<hr>
### User Information
<hr>
####Update User's Name<br>
<b>Request Format<b>
```
POST /users/:USERID/info?token=SESSION_TOKEN

HTTP Headers
Content-Type : application/json

{
  firstName : "newFirst",
  lastName : "newLast"
}
```
<b>Response Format</b><br>
```
HTTP 200 OK
```
<hr>
####Update User Password<br>
<b>Request Format<b>
```
POST /users/:USERID/password?token=SESSION_TOKEN

HTTP Headers
Content-Type : application/json

{
  oldpassword: sha256(oldpassword),
  newpassword: sha256(newpassword)
}
```
<b>Response Format</b><br>
Password Changed Successfully
```
HTTP 200 OK
```
Password Not Changed
```
HTTP 403 FORBIDDEN

Content-Type : application/json

{
	reason : "Password is incorrect./New Password does not meet requirements."
}
```
<hr>
####Get User Information<br>
<b>Request Format<b>
```
GET /users/:USERID/info?token=SESSION_TOKEN
```
<b>Response Format</b><br>
User ID valid
```
HTTP 200 OK

HTTP Headers
Content-Type : application/json

{
	userID : 123456,
	public : true,
	info : {
		firstName : "John",
		lastName : "Doe",
		email : "emailAddress@domain.com"
	}
	
}
```
User ID invalid
```
HTTP 400 BAD REQUEST
```
Note: The public field denotes whether or not user information is publicly viewable.  The <code>info</code> field may be null if information about a user is not available, but the user ID is valid.  An invalid user ID will invoke a <code>400 BAD REQUEST</code> response.
<hr>
### Household Management
<hr>
####Create a new household<br>
<b>Request Format<b>
```
POST /households/create?token=SESSION_TOKEN

HTTP Headers
Content-Type : application/json

{
  userID : 123456,
  householdName : "Stash",
  householdDescription : "For keeping everyone else's grubby hands off my stuff"
}
```
<b>Response Format</b><br>
```
HTTP 201 CREATED

HTTP Headers
Content-Type : application/json

{
  householdID : 543216,
  householdName : "Stash",
  householdDescription : "For keeping everyone else's grubby hands off my stuff",
  members : [
	  {
		  userID: 123456,
		  firstName : "First",
		  lastName : "Last"
	  }
  ],
  recipes : [],
  shoppingLists : [],
  headOfHousehold : 123456
}
```
<hr>
####Invite a user to a household<br>
<b>Request Format</b>
```
POST /households/:HOUSEHOLD_ID/invite?token=SESSION_TOKEN

HTTP Headers
Content-Type : application/json

{
	email : "emailAddress@domain.com",
	userID : 789101
}
```
Note : Only one of the above fields is required. If both are included the request will only process if the ID and emails belong to the same user.  If one or the other is included, make the other value null, or do not include.
<b>Response Format</b>
```
HTTP 200 OK
```
