# Authentication
##User Login<br>
####Request Format
The password should be hashed using SHA-256 and encoded into a hex string, as shown below
```
POST /users/login/

HTTP Headers
Content-Type : application/json

{
	"emailAddress" : "email@domain.com",
	"password" : "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"
}
```
####Response Format
#####Authentication Successful
The token field is a 24-byte value encoded in Base64-URLSafe.  This version of Base64 uses - and _ as encoding characters as they are not URL special characters, and does not use the = sign for padding.
```
HTTP 200 OK

HTTP Headers
Content-Type : application/json

{
	"firstName": "Jason",
	"lastName": "MacO'Dougal",
	"userID": 123456,
	"token": "0JKb0QNLMQVcMB0-fqx48fGch5zu9zOS"
}
```
#####Authentication Failures
Invalid Password
```
HTTP 403 FORBIDDEN
```
User Not Found
```
HTTP 404 NOT FOUND
```
Any type of malformed input
```
HTTP 400 BAD REQUEST
```
<hr>
##User Registration<br>
####Request Format
Maximum field lengths are as follows:
 - email : 254 characters
 - firstName : 20 characters
 - lastName : 20 characters
 - password : 64 characters (32 byte hash, hex encoded)

The sha256() function references the same hex-encoded SHA-256 function as described above.
```
POST /users/register/

HTTP Headers
Content-Type : application/json

{
  "email" : "emailAddress@domain.com",
  "firstName" : "First",
  "lastName" : "Last",
  "password" : sha256(password)
}
```
####Response Format####
#####Registration Successful
```
HTTP 201 CREATED
```
#####Registration Failures
Email address taken
```
HTTP 403 FORBIDDEN
```
Any type of malformed input
```
HTTP 400 BAD REQUEST
```
