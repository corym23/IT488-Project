-- SQL Script to Create the Attendance Tracking System (ATS) Database Schema

-- ----------------------------------------------------------------------
-- 0. Create the ATS Schema
-- ----------------------------------------------------------------------
IF SCHEMA_ID(N'ATS') IS NULL
BEGIN
    EXEC('CREATE SCHEMA ATS');
END
GO

-- ----------------------------------------------------------------------
-- 1. Create the Roster Table 
--    This table holds the authoritative list of approved names and their class context.
-- ----------------------------------------------------------------------
CREATE TABLE Roster (
    -- Primary Key for the Roster entry
    RosterID INT IDENTITY(1,1) PRIMARY KEY,

    -- Class Context Fields
    ClassID INT NOT NULL,
    ClassName NVARCHAR(255) NOT NULL,
    TeacherName NVARCHAR(255) NOT NULL,

    -- The Student's Name, used for the strict validation check
    StudentName NVARCHAR(255) NOT NULL,

    -- Constraint: Ensures that no single class has the same student name listed twice
    CONSTRAINT UQ_Roster_ClassStudent UNIQUE (ClassID, StudentName)
);

-- ----------------------------------------------------------------------
-- 2. Create the SubmissionLog Table
--    This table stores every attempt to log attendance (success or failure).
-- ----------------------------------------------------------------------
CREATE TABLE SubmissionLog (
    -- Primary Key for the specific submission record
    SubmissionID INT IDENTITY(1,1) PRIMARY KEY,

    -- Audit Data
    SubmittedName NVARCHAR(255) NOT NULL,
    SubmissionTimestamp DATETIMEOFFSET NOT NULL, -- Stores time in UTC format for accuracy
    Status NVARCHAR(10) NOT NULL, -- 'Success' or 'Error'

    -- Class Context Data (Denormalized for complete audit trail)
    ClassID INT NOT NULL,
    ClassName NVARCHAR(255) NOT NULL,
    TeacherName NVARCHAR(255) NOT NULL,

    -- Foreign Key to link a successful submission back to the Roster entry.
    -- This field is NULLable because the Status can be 'Error' (meaning no RosterID exists for that name).
    RosterID_FK INT NULL,

    -- Define the Foreign Key Constraint
    CONSTRAINT FK_SubmissionLog_Roster
        FOREIGN KEY (RosterID_FK)
        REFERENCES Roster (RosterID)
        ON DELETE NO ACTION -- Prevents accidental deletion of roster data if a submission exists
);

-- ----------------------------------------------------------------------
-- 3. Create Indexes for Performance
-- ----------------------------------------------------------------------

-- Index for fast lookup by ClassID and StudentName
CREATE INDEX IX_Roster_Lookup
ON Roster (ClassID, StudentName);

-- Index for the Audit View
CREATE INDEX IX_SubmissionLog_Timestamp
ON SubmissionLog (SubmissionTimestamp DESC);