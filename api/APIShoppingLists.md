# Shopping Lists
##Create Shopping List<br>
####Request Format
```
POST /households/:HOUSEHOLD_ID/lists/create?token=SESSION_TOKEN

HTTP Headers
Content-Type : application/json

{
  "listName": "Weekly Shopping"
}
```
####Response Format
```
HTTP 200 OK

HTTP Headers
Content-Type : application/json

{
  "listID": 1,
  "version": 1425107935671
}
```
##Update Shopping List<br>
####Request Format
The version below must match the version on the server for the update to succeed.<p>
The quantity is the whole number of packages, defined by packageName when linking the item.<p>
Setting a quantity to 0 will result in the item being removed from the shopping list.
```
POST /households/:HOUSEHOLD_ID/lists/:LIST_ID/update?token=SESSION_TOKEN

HTTP Headers
Content-Type : application/json

{
  "version": 1425107935671,
  "items": [
    {
      "UPC": "029000071858",
      "quantity": 3
    },
    {
      "UPC": "04963406",
      "quantity": 12
    },
    {
      "UPC": "036632001085",
      "quantity": 6
    }
  ]
}
```
####Response Format
If version matches and no errors
```
HTTP 200 OK

HTTP Headers
Content-Type : application/json

{
  "version": 1425107935796
}
```
##Get Shopping List<br>
####Request Format
The version number in the header shown below is the same format as the version shown in the update request/response. <p>
If the version matches the one on the server, no content is returned.<p>
If there is not a match, the normal response is returned. <p>
Setting the "If-None-Match" header to zero or not including it will force the request to return information.
```
GET /households/:HOUSEHOLD_ID/lists/:LIST_ID?token=SESSION_TOKEN

HTTP Headers
If-None-Match : ":VERSION_NUMBER"
```
####Response Format
If server does not match client
```
HTTP 200 OK

HTTP Headers
Content-Type : application/json
ETag : "1425107935796"

{
  "version": 1427601583081,
  "name": "Weekly Shopping",
  "items": [
    {
      "UPC": "029000071858",
      "description": "Planters Cocktail Peanuts",
      "quantity": 3,
      "packageName": "tins"
    },
    {
      "UPC": "04963406",
      "description": "Coca Cola",
      "quantity": 12,
      "packageName": "cans"
    },
    {
      "UPC": "040000231325",
      "description": "Starburst FaveRed Jellybeans",
      "quantity": 6,
      "packageName": "bags"
    }
  ]
}
```
##Remove Shopping List<br>
####Request Format
```
POST /households/:HOUSEHOLD_ID/lists/:LIST_ID/remove?token=SESSION_TOKEN
```
####Response Format
If no errors occurred
```
HTTP 200 OK
```