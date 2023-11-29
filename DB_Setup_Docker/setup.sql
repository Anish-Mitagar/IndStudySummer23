CREATE USER anish;

CREATE DATABASE socialnetwork;

GRANT ALL PRIVILEGES ON DATABASE socialnetwork TO anish;

CREATE TABLE socialnetwork.tmptable (userID INT PRIMARY KEY, serverID INT, name VARCHAR(255));

CREATE TABLE socialnetwork.messages (userID INT, serverID INT, msg VARCHAR(1000));

-- Create the Message table with the "serverId" field
CREATE TABLE socialnetwork.message (
    creationDate timestamp with time zone NOT NULL,
    id bigint PRIMARY KEY,
    language varchar(80),
    content varchar(2000),
    imageFile varchar(80),
    locationIP varchar(80) NOT NULL,
    browserUsed varchar(80) NOT NULL,
    length int NOT NULL,
    CreatorPersonId bigint NOT NULL,
    ContainerForumId bigint,
    LocationCountryId bigint NOT NULL,
    ParentMessageId bigint,
    serverId int NOT NULL, -- Add the "serverId" field of type INT
    INDEX (LocationCountryId),
    INDEX (CreatorPersonId),
    INDEX (ContainerForumId),
    INDEX (ParentMessageId)
);

-- Create the Person table with the "serverId" field
CREATE TABLE socialnetwork.person (
    creationDate timestamp with time zone NOT NULL,
    id bigint PRIMARY KEY,
    firstName varchar(80) NOT NULL,
    lastName varchar(80) NOT NULL,
    gender varchar(80) NOT NULL,
    birthday date NOT NULL,
    locationIP varchar(80) NOT NULL,
    browserUsed varchar(80) NOT NULL,
    LocationCityId bigint NOT NULL,
    speaks varchar(640) NOT NULL,
    email varchar(8192) NOT NULL,
    serverId int NOT NULL, -- Add the "serverId" field of type INT
    INDEX (LocationCityId)
);

CREATE TABLE socialnetwork.forum (
    creationDate timestamp with time zone NOT NULL,
    id bigint PRIMARY KEY,
    title varchar(256) NOT NULL,
    ModeratorPersonId bigint, -- can be null as its cardinality is 0..1
    serverId int NOT NULL
);

INSERT INTO socialnetwork.tmptable VALUES (65, 1, 'Bob'), (66, 2, 'Charlie'), (67, 3, 'Duncan'), (68, 4, 'Emily'), (69, 5, 'Frank'), (70, 6, 'George');

INSERT INTO socialnetwork.messages VALUES (65, 1, 'Hello, I am your debt collector'), (65, 1, 'You cant escape me'), (66, 2, 'You still owe me $2000'), (66, 2, 'I know where you live');

-- Insert toy data into the Message table
INSERT INTO socialnetwork.message (creationDate, id, language, content, imageFile, locationIP, browserUsed, length, CreatorPersonId, ContainerForumId, LocationCountryId, ParentMessageId, serverId)
VALUES
    ('2010-01-03 20:20:36.28+00', 3, 'fa', 'About Wolfgang Amadeus Mozart, financial security. DuringAbout Thomas Jefferson, al slave trade, and adAbout Hen', NULL, '77.245.239.11', 'Firefox', 108, 14, 0, 80, NULL, 1),
    ('2010-02-27 20:05:54.095+00', 545, 'pl', 'About Shania Twain, ed 72nd on Billboard''s Artists of the decAbout Plato, Modern world. Along with his mentor, ', NULL, '31.182.127.125', 'Internet Explorer', 121, 27, 38, 92, NULL, 2),
    ('2010-02-02 13:34:05.094+00', 3612, 'ta', 'About Maria Theresa, Lorraine, Grand Duchess of Tuscany and Holy Roman Empress. She started her 40-year rAbout', NULL, '91.214.100.136', 'Internet Explorer', 247, 113, 187, 17, NULL, 3),
    ('2010-02-19 12:13:02.81+00', 4818, 'en', 'About Edvard Munch, f the main tenets of late 19th-century Symbolism and greatly influenced German Expression', NULL, '186.10.61.96', 'Firefox', 121, 137, 188, 69, NULL, 3),
    ('2010-02-11 09:32:45.423+00', 8316, 'es', 'About Franz Schubert, Peter Schubert (31 January 1797 –About Noël Coward, tribute to the late Sir', NULL, '24.53.139.223', 'Internet Explorer', 98, 218, 230, 57, NULL, 4),
    ('2010-02-18 22:03:33.634+00', 8852, 'en', 'About Josip Bronze Tito, from World War II until 1991. Despite being one of the fAbout Benny Goodman, Mexican jazz a', NULL, '91.198.217.1', 'Chrome', 111, 251, 232, 86, NULL, 2);

-- Insert toy person data into the Person table
INSERT INTO socialnetwork.person (creationDate, id, firstName, lastName, gender, birthday, locationIP, browserUsed, LocationCityId, speaks, email, serverId)
VALUES
    ('2010-01-03 15:10:31.499+00', 14, 'Hossein', 'Forouhar', 'male', '1984-03-11', '77.245.239.11', 'Firefox', 1166, 'fa;ku;en', 'Hossein14@hotmail.com', 2),
    ('2010-01-19 13:51:10.863+00', 27, 'Wojciech', 'Ciesla', 'male', '1985-12-07', '31.182.127.125', 'Internet Explorer', 1282, 'pl;en', 'Wojciech27@gmail.com;Wojciech27@yahoo.com;Wojciech27@gmx.com;Wojciech27@zoho.com', 3),
    ('2010-01-28 04:16:14.101+00', 113, 'Abhishek', 'Singh', 'male', '1982-12-07', '61.95.201.3', 'Internet Explorer', 161, 'hi;ta;en', 'Abhishek113@4-music-today.com', 4),
    ('2010-02-17 03:21:38.348+00', 137, 'Eduardo', 'Gonzalez', 'male', '1981-03-28', '186.10.61.96', 'Firefox', 1050, 'es;de;en', 'Eduardo137@yahoo.com;Eduardo137@gmail.com', 2),
    ('2010-01-17 08:54:00.3+00', 218, 'Mike', 'Wilson', 'female', '1984-05-23', '24.53.139.223', 'Internet Explorer', 883, 'en;es', 'Mike218@gmail.com;Mike218@yahoo.com;Mike218@yours.com;Mike218@gmx.com', 1),
    ('2010-02-01 08:24:22.124+00', 251, 'Ion', 'Bologan', 'female', '1983-11-21', '91.198.217.1', 'Chrome', 1223, 'ru;en', 'Ion251@gmx.com;Ion251@netfingers.com;Ion251@gmail.com', 4);

-- Insert data into the forum table
INSERT INTO socialnetwork.forum (creationDate, id, title, moderatorPersonId, serverId)
VALUES
    ('2010-01-03 15:10:41.499+00', 0, 'Wall of Hossein Forouhar', 14, 3),
    ('2010-01-19 13:51:20.863+00', 38, 'Wall of Wojciech Ciesla', 27, 4),
    ('2010-01-28 04:16:24.101+00', 187, 'Wall of Abhishek Singh', 113, 1),
    ('2010-02-17 03:21:48.348+00', 188, 'Wall of Eduardo Gonzalez', 137, 3),
    ('2010-01-17 08:54:10.3+00', 230, 'Wall of Mike Wilson', 218, 2),
    ('2010-02-01 08:24:32.124+00', 232, 'Wall of Ion Bologan', 251, 1),
    ('2010-02-17 21:34:44.561+00', 565, 'Wall of Vinod Kumar', 682, 4);


GRANT ALL PRIVILEGES ON TABLE socialnetwork.* TO anish;

SELECT * FROM socialnetwork.tmptable;

SELECT * FROM socialnetwork.messages;

SELECT * FROM socialnetwork.message;

SELECT * FROM socialnetwork.person;

SELECT * FROM socialnetwork.forum;