# Inventory Management
##Link a UPC to a Household<br>
Linking a UPC to a household will set up the description and unit type for that UPC and household only.<p>
If the UPC is not linked to the household a new record will be created.<p>
If the UPC is already linked, the description and unit name will be updated in the database.
####Request Format
```
POST /households/:HOUSEHOLD_ID/items/:UPC/link?token=SESSION_TOKEN

HTTP Headers
Content-Type : application/json

{
	"description" : "Planter's Cocktail Peanuts",
	"unitName" : "oz."
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