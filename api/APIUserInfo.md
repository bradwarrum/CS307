# User Information
##Get Information About Oneself<br>
####Request Format
```
GET /users/me?token=SESSION_TOKEN
```
####Response Format
```
HTTP 200 OK

HTTP Headers
Content-Type : application/json

{
  "userID": 1829,
  "firstName": "John",
  "lastName": "Doe",
  "emailAddress": "email1@domain.com",
  "households": [
    {
      "householdID": 10192,
      "householdName": "Stash",
      "householdDescription": "Private Inventory"
    }
  ]
}
```