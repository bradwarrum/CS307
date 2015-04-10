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
HTTP 201 CREATED

HTTP Headers
Content-Type : application/json

{
  "listID": 1,
  "version": 1425107935671
}
```
##Update Shopping List<br>
####Request Format
#####NOTE: Fractional components have been added. They are NOT required. Omitting fractional will simply default the value to zero.
The version below must match the version on the server for the update to succeed.<p>
The quantity is the whole number of packages, defined by packageName when linking the item.<p>
Setting a quantity to 0 will result in the item being removed from the shopping list.
```
POST /households/:HOUSEHOLD_ID/lists/:LIST_ID/update?token=SESSION_TOKEN

HTTP Headers
Content-Type : application/json

{
  "version": 1428626887626,
  "items": [
    {
      "UPC": "029000071858",
      "quantity": 3,
      "fractional": 0
    },
    {
      "UPC": "04963406",
      "quantity": 12,
      "fractional": 50
    },
    {
      "UPC": "040000231325",
      "quantity": 0,
      "fractional": 50
    },
    {
      "UPC": "00001",
      "quantity": 0,
      "fractional": 99
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
#####NOTE that this request now supports internally generated UPCs.
#####NOTE that this request now returns packaging information as well.
#####NOTE that this request now supports fractional quantities.
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
  "version": 1428626887762,
  "name": "Weekly Shopping",
  "items": [
    {
      "UPC": "029000071858",
      "isInternalUPC": false,
      "description": "Planters Cocktail Peanuts",
      "quantity": 3,
      "fractional": 0,
      "packaging": {
        "packageSize": 12.0,
        "unitID": 2,
        "unitName": "ounces",
        "unitAbbreviation": "oz",
        "packageName": "tins"
      }
    },
    {
      "UPC": "04963406",
      "isInternalUPC": false,
      "description": "Coca Cola",
      "quantity": 12,
      "fractional": 50,
      "packaging": {
        "packageSize": 12.0,
        "unitID": 2,
        "unitName": "ounces",
        "unitAbbreviation": "oz",
        "packageName": "cans"
      }
    },
    {
      "UPC": "040000231325",
      "isInternalUPC": false,
      "description": "Starburst FaveRed Jellybeans",
      "quantity": 0,
      "fractional": 50,
      "packaging": {
        "packageSize": 14.0,
        "unitID": 2,
        "unitName": "ounces",
        "unitAbbreviation": "oz",
        "packageName": "bags"
      }
    },
    {
      "UPC": "00001",
      "isInternalUPC": true,
      "description": "Apple",
      "quantity": 0,
      "fractional": 99,
      "packaging": {
        "packageSize": 1.0,
        "unitID": 14,
        "unitName": "units",
        "unitAbbreviation": "units",
        "packageName": "each"
      }
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
