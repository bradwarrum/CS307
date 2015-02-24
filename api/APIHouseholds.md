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
	"householdID" : 46512
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
<hr>
####Invite a user to a household<br>
<b>Request Format</b>
```
POST /households/:HOUSEHOLD_ID/invite?token=SESSION_TOKEN

HTTP Headers
Content-Type : application/json

{
	email : "emailAddress@domain.com",
	userID : 789101
}
```
<b>Response Format</b>
```
HTTP 202 ACCEPTED
```
Note : Only one of the above fields is required. If both are included the request will only process if the ID and emails belong to the same user.  If one or the other is included, make the other value null, or do not include.
<hr>
####Get a user's active households<br>
<b>Request Format</b>
```
GET /users/:USERID/households?token=SESSION_TOKEN
```
<b>Response Format</b>
```
HTTP 200 OK
{
	userID : 123456
	households : [
		{
			householdID : 908546,
			householdName : "Stash",
			householdDescription : "Joe's Private Stash",
		} , 
		{
			householdID : 203458,
			householdName : "Apartment",
			householdDescription : "Joe and Tina's Shared Pantry"
		}
	]
}
```
<hr>
####Get Information about a Household<br>
<b>Request Format</b>
```
GET /households/:HOUSEHOLD_ID/info?token=SESSION_TOKEN
```
<b>Response Format</b>
```
HTTP 200 OK

HTTP Headers
Content-Type : application/json

{
  householdID : 908908,
  householdName : "Family",
  headOfHousehold : 678901,
  householdDescription : "Wilson family inventory",
  members : [
	  {
		  userID: 678901,
		  public:true,
		  info : {
			  firstName : "Billy",
			  lastName : "Wilson",
			  email : "emailAddress@domain.com"
		  }
	  },
	  {
		  userID: 345678,
		  public:false,
		  info : {
			  firstName : "Julia",
			  lastName : "Wilson",
			  email : "emailAddress@domain.com"
		  }
	  }
  ],
  recipes : [
	  {
		  recipeID: 756123,
		  recipeName: "Chicken Alfredo"
	  }
  ],
  shoppingLists : [
	  {
		  listID : 347089,
		  listName : "Groceries"
	  }
  ],
  pending : [
	  {
		  userID: 576123,
		  public: false,
		  info : null
	  }
  ]
}
```