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
	Version LONG NOT NULL,
	AvailableProduceID INT NOT NULL,
	PRIMARY KEY ( HouseholdId ),
	FOREIGN KEY (HeadOfHousehold) REFERENCES User(UserId)
);
CREATE TABLE HouseholdPermissions (
	UserId INT NOT NULL,
	HouseholdId INT NOT NULL,
	PermissionLevel INT NOT NULL, 
	PRIMARY KEY ( UserId, HouseholdId ),
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
	UnitAbbreviation VARCHAR(5) NOT NULL,
	PRIMARY KEY (UnitId)
);
INSERT INTO MeasurementUnit ( UnitName, UnitAbbreviation ) VALUES
	( 'pounds', 'lb' ),
	( 'ounces', 'oz' ),
	( 'grams', 'g' ),
	( 'milligrams', 'mg' ),
	( 'liters', 'L' ),
	( 'milliliters', 'mL' ),
	( 'gallons', 'gal' ),
	( 'quarts', 'qt' ),
	( 'pints', 'pt' ),
	( 'cups', 'c'),
	( 'teaspoons', 'tsp' ),
	( 'tablespoons', 'Tbsp' ),
	( 'fluid ounces', 'fl oz' ),
	( 'units', 'units' );
CREATE TABLE InventoryItem (
	ItemId Int NOT NULL AUTO_INCREMENT,
	UPC VARCHAR(13) NOT NULL,
	HouseholdId INT NOT NULL,
	Description VARCHAR(40),
	/*The number of PackageUnits in a package (the 14.5 in 14.5 oz./box)*/
	PackageQuantity FLOAT NOT NULL,
	/*Units of packaging for the package (the oz. in 14.5 oz./box)*/
	PackageUnits INT NOT NULL,
	/*Units describing the packaging itself (the box in 14.5 oz./box)*/
	PackageName VARCHAR(20) NOT NULL,
	/*The number of packages of this item in the inventory*/
	InventoryQuantity INT NOT NULL,
	Expiration DATE,
	DateAdded DATE,
	Hidden BOOLEAN NOT NULL,
	PRIMARY KEY ( ItemId ),
	UNIQUE ( UPC,HouseholdId ),
	FOREIGN KEY ( HouseholdId )	REFERENCES Household ( HouseholdId ),
	FOREIGN KEY ( PackageUnits ) REFERENCES MeasurementUnit ( UnitId )
);
CREATE TABLE HouseholdShoppingList(
	ListId INT NOT NULL AUTO_INCREMENT,
	HouseholdId INT NOT NULL,
	Name VARCHAR(40) NOT NULL,
	Timestamp LONG NOT NULL,
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
	ItemId INT NOT NULL,
	Quantity INT NOT NULL,
	UnitId INT,
	PRIMARY KEY ( ListItemId ),
	FOREIGN KEY (ListId) REFERENCES HouseholdShoppingList(ListId),
	FOREIGN KEY (ItemId) REFERENCES InventoryItem(ItemId),
	FOREIGN KEY ( UnitId ) REFERENCES MeasurementUnit ( UnitId ),
	UNIQUE (ItemId, ListId)
);
CREATE TABLE HouseholdRecipe (
	RecipeId INT NOT NULL AUTO_INCREMENT,
	HouseholdId INT NOT NULL,
	Name VARCHAR(40) NOT NULL,
	Description VARCHAR(40),
	Timestamp LONG NOT NULL,
	PRIMARY KEY ( RecipeId ),
	FOREIGN KEY ( HouseholdId ) REFERENCES Household ( HouseholdId )
);
CREATE TABLE RecipeItem (
	RecipeItemId INT NOT NULL AUTO_INCREMENT,
	RecipeId INT NOT NULL,
	ItemId INT NOT NULL,
	Quantity INT NOT NULL,
	PRIMARY KEY ( RecipeItemId ),
	FOREIGN KEY ( RecipeId ) REFERENCES HouseholdRecipe ( RecipeId ),
	FOREIGN KEY ( ItemId )   REFERENCES InventoryItem( ItemId )
);
CREATE TABLE RecipeInstruction(
	RecipeInstructionId INT NOT NULL AUTO_INCREMENT,
	RecipeId INT NOT NULL,
	SortOrder INT,
	Instruction VARCHAR(128),
	PRIMARY KEY ( RecipeInstructionId ),
	FOREIGN KEY ( RecipeId ) REFERENCES HouseholdRecipe ( RecipeId )
);
