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
The quantity is the whole number of units, and fractional is a number between 0 and 99 representing fractional units, if necessary.<p>
Quantity and/or fractional can be left out of any of the item declarations. It is assumed the value is 0 if it is missing. <p>
Setting a quantity and fraction to 0 will result in the item being removed from the shopping list.
```
POST /households/:HOUSEHOLD_ID/lists/:LIST_ID/update?token=SESSION_TOKEN

HTTP Headers
Content-Type : application/json

{
  "version": 1425107935671,
  "items": [
    {
      "UPC": "029000071858",
      "quantity": 3,
      "fractional": 0
    },
    {
      "UPC": "04963406",
      "quantity": 12,
      "fractional": 0
    },
    {
      "UPC": "036632001085",
      "quantity": 6,
      "fractional": 0
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
  "version": 1425107935796,
  "name": "Weekly Shopping",
  "items": [
    {
      "UPC": "029000071858",
      "description": "Planters Cocktail Peanuts",
      "quantity": 3,
      "fractional": 0,
      "unitName": "oz."
    },
    {
      "UPC": "04963406",
      "description": "Coca Cola, Can",
      "quantity": 12,
      "fractional": 0,
      "unitName": "oz."
    },
    {
      "UPC": "036632001085",
      "description": "Dannon Fruit on Bottom Blueberry",
      "quantity": 6,
      "fractional": 0,
      "unitName": "oz."
    }
  ]
}
```