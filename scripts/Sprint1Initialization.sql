CREATE TABLE User (
	UserId INT NOT NULL AUTO_INCREMENT,
	Email VARCHAR(254) NOT NULL UNIQUE, 
	Password CHAR(64) NOT NULL,
	PasswordSalt CHAR(32) NOT NULL,
	FirstName VARCHAR(20) NOT NULL,
	LastName VARCHAR(20) NOT NULL,
	PRIMARY KEY ( UserId )
);
CREATE TABLE Household (
	HouseholdId INT NOT NULL AUTO_INCREMENT,
	Name VARCHAR(20) NOT NULL,
	Description VARCHAR(40),
	HeadOfHousehold INT NOT NULL,
	PRIMARY KEY ( HouseholdId ),
	FOREIGN KEY (HeadOfHousehold) REFERENCES User(UserId)
);
CREATE TABLE HouseholdPermissions (
	UserId INT NOT NULL,
	HouseholdId INT NOT NULL,
	PermissionLevel INT NOT NULL, 
	INDEX User(UserId),
	FOREIGN KEY (UserId)
	REFERENCES User ( UserId ),
	FOREIGN KEY ( HouseholdId )	REFERENCES Household ( HouseholdId )
);
CREATE TABLE UserContribution (
	ContributionId INT NOT NULL,
	UserId INT NOT NULL,
	HouseholdId INT NOT NULL,
	ContributionAmount FLOAT,
	DateAdded DATE,
	PRIMARY KEY ( ContributionId ),
	FOREIGN KEY (UserId) REFERENCES User ( UserId ),
	FOREIGN KEY ( HouseholdId )	REFERENCES Household ( HouseholdId )
);
CREATE TABLE MeasurementUnit (
	UnitId INT NOT NULL AUTO_INCREMENT,
	UnitName VARCHAR(20) NOT NULL UNIQUE,
	PRIMARY KEY (UnitId)
);
CREATE TABLE InventoryItem (
	ItemId Int NOT NULL AUTO_INCREMENT,
	UPC VARCHAR(13) NOT NULL,
	HouseholdId INT NOT NULL,
	Description VARCHAR(40),
	UnitQuantity FLOAT NOT NULL,
	UnitName VARCHAR(20) NOT NULL,
	Expiration DATE,
	DateAdded DATE,
	Hidden BOOLEAN,
	PRIMARY KEY ( ItemId ),
	UNIQUE ( UPC,HouseholdId ),
	FOREIGN KEY ( HouseholdId )	REFERENCES Household ( HouseholdId )
);
CREATE TABLE HouseholdShoppingList(
	ListId INT NOT NULL AUTO_INCREMENT,
	HouseholdId INT NOT NULL,
	Name VARCHAR(40) NOT NULL,
	UserId INT,
	AmountSpent FLOAT,
	DateCompleted DATE,
	Primary KEY (ListId),
	FOREIGN KEY (UserId) REFERENCES User ( UserId ),
	FOREIGN KEY ( HouseholdId )	REFERENCES Household ( HouseholdId )
);
CREATE TABLE ShoppingListItem (
	ListItemId INT NOT NULL AUTO_INCREMENT,
	ListId INT NOT NULL,
	UPC VARCHAR(13) NOT NULL,
	Name VARCHAR(40) NOT NULL,
	Quantity FLOAT,
	UnitId INT,
	PRIMARY KEY ( ListItemId ),
	FOREIGN KEY ( UPC ) REFERENCES InventoryItem ( UPC ),
	FOREIGN KEY ( UnitId ) REFERENCES MeasurementUnit ( UnitId )
);

	