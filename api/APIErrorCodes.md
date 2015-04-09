#API Error Codes
All API requests that fail for any reason now return a normalized, enumerated format that can be used to find the source of the problem.  The table below shows all the error codes, their name, associated HTTP Status codes, and description.
<table>
	<tr>
	    <th>Error Code</th>
	    <th>Error Name</th>
	    <th>HTTP Status</th>
	    <th>Description</th>
	</tr>
	<tr>
	    <td>0-2</td>
	    <td colspan=3 align="center">Reserved for internal use</td>
	</tr>
	<tr>
	    <td>3</td>
	    <td>INTERNAL_ERROR</td>
	    <td>500</td>
	    <td>Generic Internal Server Error</td>
	</tr>
	    <td>4</td>
	    <td>TOKEN_EXPIRED</td>
	    <td>403</td>
	    <td>The user token is expired, renew the token.</td>
	</tr>
	<tr>
	    <td>5</td>
	    <td>INVALID_TOKEN</td>
	    <td>403</td>
	    <td>A user cannot be found for that token.</td>
	</tr>
	<tr>
	    <td>6</td>
	    <td>EMAIL_TAKEN</td>
	    <td>403</td>
	    <td>Cannot register a user for that email address, the email address is already taken</td>
	</tr>
	<tr>
	    <td>7</td>
	    <td>INVALID_PASSWORD</td>
	    <td>403</td>
	    <td>The user's password is incorrect.</td>
	</tr>
	<tr>
	    <td>8</td>
	    <td>INVALID_PAYLOAD</td>
	    <td>400</td>
	    <td>JSON request payload invalid.  Check field lengths and field names.</td>
	</tr>
	<tr>
	    <td>9</td>
	    <td>USER_NOT_FOUND</td>
	    <td>400</td>
	    <td>No user was found with that email address.</td>
	</tr>
	<tr>
	    <td>10</td>
	    <td>HOUSEHOLD_NOT_FOUND</td>
	    <td>400</td>
	    <td>User is not associated with that household.</td>
	</tr>
	<tr>
        <td>11</td>
        <td>LIST_NOT_FOUND</td>
        <td>400</td>
        <td>One or more shopping lists in the request cannot be found for that household.</td> 
    </tr>
    <tr>
        <td>12</td>
        <td>ITEM_NOT_FOUND</td>
        <td>400</td>
        <td>One or more UPC strings are not associated with the household, ensure that all UPCs are linked to the household.</td>
    </tr>
    <tr>
        <td>13</td>
        <td>UPC_FORMAT_NOT_SUPPORTED</td>
        <td>400</td>
        <td>Support does not exist for barcodes of that format.</td>
    </tr>
    <tr>
	    <td>14</td>
	    <td>UPC_CHECKSUM_INVALID</td>
	    <td>400</td>
	    <td>UPC-A or EAN-13 barcode has an invalid checksum, try again.</td>
	</tr>
	<tr>
	    <td>15</td>
	    <td>INSUFFICIENT_PERMISSIONS</td>
	    <td>403</td>
	    <td>The user has insufficient household permissions to perform this action.</td>
	</tr>
	<tr>
	    <td>16</td>
	    <td>OUTDATED_TIMESTAMP</td>
	    <td>409</td>
	    <td>The server version of this resource is different than the client's version.  Update the resource on the client and retry.</td>
    </tr>
    	<tr>
	    <td>17</td>
	    <td>RECIPE_NOT_FOUND</td>
	    <td>400</td>
	    <td>One or more of the recipes cannot be found for that household.</td>
    </tr>
</table>

###JSON Response Format Example
```
HTTP 403 FORBIDDEN

HTTP Headers
Content-Type : application/json

{
  "errorCode" : 6,
  "errorName" : "EMAIL_TAKEN",
  "httpStatus" : 403,
  "errorDescription" : "Cannot register a user for that email address, the email address is already taken"
}
```
