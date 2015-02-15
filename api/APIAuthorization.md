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