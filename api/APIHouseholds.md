# Household Management
##Create a new household<br>
####Request Format
```
POST /households/create?token=SESSION_TOKEN

HTTP Headers
Content-Type : application/json

{
  "householdName" : "Stash",
  "householdDescription" : "Personal Inventory"
}
```
####Response Format
#####Successful Creation
```
HTTP 201 CREATED

HTTP Headers
Content-Type : application/json

{
	"householdID" : 46512,
	"version" : 191223972942
}
```
#####Token Expired, Invalid or Not Present
```
HTTP 403 FORBIDDEN
```
#####Any type of malformed input
```
HTTP 400 BAD REQUEST
```
##Get Information About a Household<br>
####Request Format
```
GET /households/:HOUSEHOLD_ID?token=SESSION_TOKEN
```
####Response Format
```
HTTP 200 OK

HTTP Headers
Content-Type : application/json

{
  "householdID": 1843,
  "householdName": "Stash",
  "householdDescription": "Private Inventory",
  "headOfHousehold": 1234,
  "members": [
    {
      "userID": 1234,
      "firstName": "John",
      "lastName": "Doe",
      "emailAddress": "email1@domain.com"
    }
  ],
  "lists": [
    {
      "listID": 82983,
      "listName": "Weekly Shopping"
    }
  ]
}
```
#####Token Expired, Invalid or Not Present
```
HTTP 403 FORBIDDEN
```
#####Any type of malformed input
```
HTTP 400 BAD REQUEST
```