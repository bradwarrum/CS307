# Inventory Management
##Link a UPC to a Household<br>
Linking a UPC to a household will set up the description and unit type for that UPC and household only.
If the UPC is not linked to the household a new record will be created.
If the UPC is already linked, the description and unit name will be updated in the database.<p>
Note that the packageName, packageUnits, and packageSize fields describe only the packaging units for the item.  For example, the following link call will describe the UPC as "12.0 oz. tin of Planter's Cocktail Peanuts".  Conversely, all inventory updates are in terms of package counts, not packaging units.
####Request Format
```
POST /households/:HOUSEHOLD_ID/items/:UPC/link?token=SESSION_TOKEN

HTTP Headers
Content-Type : application/json

{
	"description" : "Planter's Cocktail Peanuts",
	"packageName" : "tin",
	"packageUnits" : "oz.",
	"packageSize" : 12.0
}
```
####Response Format
#####Creation/Update Successful
```
HTTP 200 OK
```
#####Failures
Invalid Token, Insufficient Permissions
```
HTTP 403 FORBIDDEN
```
Household Not Found
```
HTTP 404 NOT FOUND
```
Any type of malformed input
```
HTTP 400 BAD REQUEST
```
Note: Barcodes currently accepted are UPC-A, EAN-13 and UPC-E/EAN-8.  UPC-A and EAN-13 have a checksum method that is carried out on the server.  A HTTP 400 BAD REQUEST response will occur if the checksum bit is invalid.<p>
EAN-8 and UPC-E checksums are not calculated because it is not possible to tell if the barcode is UPC-E or EAN-8 (same number of digits)
##Get UPC Description Suggestions<br>
####Request Format
```
GET /households/:HOUSEHOLD_ID/items/:UPC/suggestions?token=SESSION_TOKEN
```
####Response Format
```
HTTP 200
{
  "UPC": "04963406",
  "householdId": 24839,
  "currentDescription": "Can of Coke",
  "internalSuggestions": [
    {
      "householdID": 19283,
      "description": "Coca Cola, Can"
    }
  ],
  "externalSuggestions": [
    {
      "source": "www.upcdatabase.org",
      "description": "Coca Cola Classic"
    }
  ]
}
```
 - currentDescription is the description linked to the household :HOUSEHOLD_ID provided in the URL.
 - currentDescription is null if the household :HOUSEHOLD_ID provided in the URL does not have a linked description for :UPC.
 - internalSuggestions are all the descriptions for that :UPC from the user's other households. If they have no linked description for :UPC in another household, this field is an empty array.
 - externalSuggestions are all the descriptions for that :UPC from external APIs.  The source field designates which API produced the description.  If no APIs have a description for :UPC, this field is an empty array.
##Update Inventory Quantities<br>
The version below must match the version on the server for the update to succeed.
####Request Format
```
POST /households/:HOUSEHOLD_ID/items/update?token=SESSION_TOKEN

HTTP Headers
Content-Type : application/json

{
  "version": 1427604592834,
  "items": [
    {
      "UPC": "029000071858",
      "quantity": 1,
      "fractional": 50
    },
    {
      "UPC": "04963406",
      "quantity": 17,
      "fractional": 0
    },
    {
      "UPC": "040000231325",
      "quantity": 1,
      "fractional": 25
    }
  ]
}
```
####Response Format
If version matches
```
HTTP 200 OK

HTTP Headers
Content-Type : application/json
ETag : "1427607227384"

{
  "version": 1427607227384
}
```
##Get Household Inventory<br>
The VERSION_ID below is the same version as described above in the update request specification.<p>
Setting VERSION_ID to 0 or not including the "If-None-Match" header in the request will force the request to return information.
####Request Format
```
GET /households/:HOUSEHOLD_ID/items?token=SESSION_TOKEN

HTTP Headers
If-None-Match : "VERSION_ID"

```
####Response Format
If No version match
```
HTTP 200 OK

HTTP Headers
Content-Type : application/json
ETag : "1427607227384"

{
  "version": 1427607227384,
  "items": [
    {
      "UPC": "029000071858",
      "description": "Planters Cocktail Peanuts",
      "packageSize": 12.0,
      "packageUnits": "ounces",
      "packageName": "tins",
      "quantity": 2,
      "fractional": 50
    },
    {
      "UPC": "04963406",
      "description": "Coca Cola",
      "packageSize": 12.0,
      "packageUnits": "ounces",
      "packageName": "cans",
      "quantity": 15,
      "fractional": 0
    },
    {
      "UPC": "040000231325",
      "description": "Starburst FaveRed Jellybeans",
      "packageSize": 14.0,
      "packageUnits": "ounces",
      "packageName": "bags",
      "quantity": 1,
      "fractional": 75
    }
  ]
}
```
If version matches
```
HTTP 304 NOT MODIFIED

HTTP Headers
ETag : "1427607227384"
```
