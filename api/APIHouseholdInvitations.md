# Household Management
##Create a new household Invitation<br>
####Request Format
```
POST /households/:HOUSEHOLD_ID/invite/:USER_ID/create?token=SESSION_TOKEN
```
####Response Format
#####Successful Creation
```
HTTP 201 CREATED

HTTP Headers
Content-Type : application/json

{
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
##Get Information About a Household Invite<br>
####Request Format
```
GET /households/:HOUSEHOLD_ID/invite/:USER_ID?token=SESSION_TOKEN
```
####Response Format
```
HTTP 200 OK

HTTP Headers
Content-Type : application/json
{
	"householdIDs": [
		1,
		2	
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