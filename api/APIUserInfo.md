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