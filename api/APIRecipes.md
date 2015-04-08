# Recipes
## Create Recipe <br>
####Request Format
```
POST /households/:HOUSEHOLD_ID/recipes/create?token=SESSION_TOKEN

HTTP Headers
Content-Type : application/json

{
	"recipeName" : "Spaghetti",
	"recipeDescription" : "Jeanine's Grandmother's Spaghetti Recipe"
}
```
####Response Format
```
HTTP 201 CREATED

HTTP Headers
Content-Type : application/json
ETag : "190248237928"

{
	"recipeID" : 12345,
	"version" : 190248237928
}
```
##Update Recipe
Note that the version number must match the server side version for the request to be processed.<p>
The update request format requires that all un-modified portions of the recipe also be included in the request.  Omitting ingredients or instructions will remove them from the recipe.<p>
Setting quantity and fractional to 0 of an ingredient will remove the ingredient from the recipe.
####Request Format
```
POST /households/:HOUSEHOLD_ID/recipes/:RECIPE_ID/update?token=SESSION_TOKEN

HTTP Headers
Content-Type : application/json

{
	"version" : 190248237928,
	"recipeName" : "Pot O Stuff",
	"recipeDescription" : "Strange pot of stuff made of two things",
	"ingredients" : [
		{
			"UPC" : "029000071858",
			"quantity" : 1,
			"fractional" : 50
		},
		{
			"UPC" : "076183643570",
			"quantity" : 2,
			"fractional" : 0
		}
	],
	"instructions" : [
		"Put the first item in a jar.",
		"Put the second item in the same jar.",
		"Shake the jar.",
		"Serve."
	]
}
```
####Response Format
If the server version matches
```
HTTP 200 OK
Content-Type : application/json
ETag : "190248237928"

{
	"version" : 190248237928
}
```
##Get Recipe
The VERSION_ID below is the same version as described above in the update request specification.<p>
Setting VERSION_ID to 0 or not including the "If-None-Match" header in the request will force the request to return information.<p>
If the VERSION_ID matches the server version, information will not be returned. An HTTP status of 304 will be returned instead of 200.<p>
Note that fractional components range from 0 to 99 and signify a fractional quantity of package units used for the recipe.  For instance, setting quantity to 1 and fractional to 50 means that 1.5 packages of some UPC will be used in the recipe.
####Request Format
```
GET /households/:HOUSEHOLD_ID/recipes/:RECIPE_ID?token=SESSION_TOKEN

HTTP Headers
If-None-Match : "VERSION_ID"
```
####Response Format
If version does not match
```
HTTP 200 OK

HTTP Headers
ETag : "190248237928"
Content-Type : application/json
{
	"version" : 190248237928,
	"recipeName" : "Pot O Stuff",
	"recipeDescription" : "Strange pot of stuff made of two things",
	"ingredients" : [
		{
			"UPC" : "029000071858",
			"quantity" : 1,
			"fractional" : 50
		},
		{
			"UPC" : "076183643570",
			"quantity" : 2,
			"fractional" : 0
		}
	],
	"instructions" : [
		"Put the first item in a jar.",
		"Put the second item in the same jar.",
		"Shake the jar.",
		"Serve."
	]
}
```
If the version matches
```
HTTP 304 NOT MODIFIED

HTTP Headers
ETag : "190248237928"
```
##Remove Recipe
####Request Format
```
POST /households/:HOUSEHOLD_ID/recipes/:RECIPE_ID/remove?token=SESSION_TOKEN
```
####Response Format
```
HTTP 200 OK
```


