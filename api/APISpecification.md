## Virtual Pantry API Specification
This document specifies all API requests that may be made against the VirtualPantry backend API.
<br>
URLs are in the form of */api/request/path/*, and are assumed to be appended to the base domain (IP or registered domain name)
###Overview
 - :heavy_multiplication_x:  Not Implemented
 - :heavy_check_mark:  Implemented
 
<table>
	<tr><th colspan="3">
		<h5><a href="APIAuthorization.md">Authentication</a></h5>
	</th></tr>
	<tr>
		<td>:heavy_check_mark:</td>
		<td>User Login</td>
		<td><code>POST /users/login/</code></td>
	</tr>
	<tr>
		<td>:heavy_check_mark:</td>
		<td>User Registration</td>
		<td><code>POST /users/register/</code></td>
	</tr>
	
	<tr><th colspan="3">
		<h5><a href="APIUserInfo.md">User Information</a></h5>
	</th></tr>
	<tr>
		<td>:heavy_check_mark:</td>	
		<td>Get Information About Oneself</td>
		<td><code>GET /users/me?token=SESSION_TOKEN</code></td>
	</tr>
	<tr>
		<td>:heavy_multiplication_x:</td>	
		<td>Update User's Name</td>
		<td><code>POST /users/:USERID/info?token=SESSION_TOKEN</code></td>
	</tr>
	<tr>
		<td>:heavy_multiplication_x:</td>	
		<td>Update User Password</td>
		<td><code>POST /users/:USERID/password?token=SESSION_TOKEN</code></td>
	</tr>
	<tr>
		<td>:heavy_multiplication_x:</td>	
		<td>Get User Information</td>
		<td><code>GET /users/:USERID?token=SESSION_TOKEN</code></td>
	</tr>
	
	<tr><th colspan="3">
		<h5><a href="APIHouseholds.md">Household Management</a></h5>
	</th></tr>
	<tr>
		<td>:heavy_check_mark:</td>	
		<td>Create Household</td>
		<td><code>POST /households/create?token=SESSION_TOKEN</code></td>
	</tr>
	<tr>
		<td>:heavy_check_mark:</td>
		<td>Get Household Information</td>
		<td><code>GET /households/:HOUSEHOLD_ID?token=SESSION_TOKEN</code></td>
	</tr>	
	<tr>
		<td>:heavy_multiplication_x:</td>	
		<td>Invite User to Household</td>
		<td><code>POST /households/:HOUSEHOLD_ID/users/invite?token=SESSION_TOKEN</code></td>
	</tr>
	<tr>
		<td>:heavy_multiplication_x:</td>	
		<td>Update User Permissions</td>
		<td><code>POST /households/:HOUSEHOLD_ID/users/:USER_ID/permissions?token=SESSION_TOKEN</code></td>
	</tr>
	<tr>
		<td>:heavy_multiplication_x:</td>	
		<td>Promote User to Head</td>
		<td><code>POST /households/:HOUSEHOLD_ID/users/:USER_ID/promote?token=SESSION_TOKEN</code></td>
	</tr>
	<tr>
		<td>:heavy_multiplication_x:</td>
		<td>Remove Household</td>
		<td><code>POST /households/:HOUSEHOLD_ID/remove?token=SESSION_TOKEN</code></td>
	</tr>	
	
	<tr><th colspan="3">
		<h5><a href="APIInventory.md">Inventory Management</a></h5>
	</th></tr>

	<tr>
		<td>:heavy_check_mark:</td>
		<td>Link a UPC to a Household</td>
		<td><code>POST /households/:HOUSEHOLD_ID/items/:UPC/link?token=SESSION_TOKEN</code></td>
	</tr>
	<tr>
		<td>:heavy_multiplication_x:</td>
		<td>Get UPC Description Suggestions</td>
		<td><code>GET /suggest/:UPC?source=[ext,int,both]&token=SESSION_TOKEN</code></td>
	</tr>
	
	<tr><th colspan="3">
		<h5><a href="APIShoppingLists.md">Shopping Lists</a></h5>
	</th></tr>
	<tr>
		<td>:heavy_check_mark:</td>
		<td>Create Shopping List</td>
		<td><code>POST /households/:HOUSEHOLD_ID/lists/create?token=SESSION_TOKEN</code></td>
	</tr>
	<tr>
		<td>:heavy_check_mark:</td>
		<td>Update Shopping List</td>
		<td><code>POST /households/:HOUSEHOLD_ID/lists/:LIST_ID/update?token=SESSION_TOKEN</code></td>
	</tr>	
	<tr>
		<td>:heavy_check_mark:</td>
		<td>Get Shopping List</td>
		<td><code>GET /households/:HOUSEHOLD_ID/lists/:LIST_ID?token=SESSION_TOKEN</code></td>
	</tr>
	<tr>
		<td>:heavy_multiplication_x:</td>
		<td>Remove Shopping List</td>
		<td><code>POST /households/:HOUSEHOLD_ID/lists/:LIST_ID/remove?token=SESSION_TOKEN</code></td>
	</tr>
	
	<tr><th colspan="3">
		<h5><a href="APIRecipes.md">Recipes</a></h5>
	</th></tr>
	<tr>
		<td>:heavy_multiplication_x:</td>
		<td>Create Recipe</td>
		<td><code>POST /households/:HOUSEHOLD_ID/recipes?token=SESSION_TOKEN</code></td>
	</tr>
	<tr>
		<td>:heavy_multiplication_x:</td>
		<td>Update Recipe</td>
		<td><code>POST /households/:HOUSEHOLD_ID/recipes/:RECIPE_ID?token=SESSION_TOKEN</code></td>
	</tr>
	<tr>
		<td>:heavy_multiplication_x:</td>
		<td>Get Recipe</td>
		<td><code>GET /households/:HOUSEHOLD_ID/recipes/:RECIPE_ID?token=SESSION_TOKEN</code></td>
	</tr>	
	<tr>
		<td>:heavy_multiplication_x:</td>
		<td>Remove Recipe</td>
		<td><code>POST /households/:HOUSEHOLD_ID/recipes/:RECIPE_ID/remove?token=SESSION_TOKEN</code></td>
	</tr>
	

	
	
	<tr><th colspan="3">
		<h5><a href="APIContribution.md">Cost Tracking</a></h5>
	</th></tr>	
	
</table>

