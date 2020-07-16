
CREATE DATABASE IF NOT EXISTS segasesu;
USE segasesu;

DROP TABLE IF EXISTS evaluationCompleded;
DROP TABLE IF EXISTS evaluations;

DROP TABLE IF EXISTS formItems;
DROP TABLE IF EXISTS forms, flowers;
DROP TABLE IF EXISTS gameData;

DROP TABLE IF EXISTS access_codes;

CREATE TABLE IF NOT EXISTS gameData
(
  id INT UNSIGNED AUTO_INCREMENT NOT NULL,
  accessCode VARCHAR(16) UNIQUE NOT NULL,
  
  playerName TEXT NOT NULL,
  totalMoney INT NOT NULL,
  changedTimestamp TEXT NOT NULL,
  
  PRIMARY KEY(id),
  KEY(accessCode)

) ENGINE INNODB;

CREATE TABLE IF NOT EXISTS flowers
(
  id INT UNSIGNED NOT NULL,
  parent_id INT UNSIGNED NOT NULL,
  flowerType INTEGER UNSIGNED NOT NULL,
  `name` TEXT NOT NULL,
  waterLevel FLOAT NOT NULL,
  mineralsLevel FLOAT NOT NULL,
  fertilizerLevel FLOAT NOT NULL,
  state INTEGER UNSIGNED NOT NULL,
  expirience INTEGER UNSIGNED NOT NULL,
  lastDecay TEXT NOT NULL,
  
  PRIMARY KEY(id,parent_id),
  FOREIGN KEY (parent_id) REFERENCES gameData(id) ON UPDATE CASCADE ON DELETE CASCADE

) ENGINE INNODB;

CREATE TABLE IF NOT EXISTS forms
(
  id INT UNSIGNED AUTO_INCREMENT NOT NULL,
  parent_id INT UNSIGNED NOT NULL,
  `category` INT UNSIGNED NOT NULL,
  
  PRIMARY KEY(id),
  FOREIGN KEY (parent_id) REFERENCES gameData(id) ON UPDATE CASCADE ON DELETE CASCADE
)ENGINE INNODB;

CREATE TABLE IF NOT EXISTS formItems
(
  id INT UNSIGNED NOT NULL,
  parent_id INT UNSIGNED NOT NULL,
  `text` VARCHAR(1024) NOT NULL,
  `count` INT UNSIGNED NOT NULL,
  
  PRIMARY KEY(id,parent_id),
  FOREIGN KEY (parent_id) REFERENCES forms(id) ON UPDATE CASCADE ON DELETE CASCADE
)ENGINE INNODB;

CREATE TABLE IF NOT EXISTS access_codes
(
  accessCode VARCHAR(16) UNIQUE NOT NULL,
  PRIMARY KEY(accessCode)
)ENGINE INNODB;

CREATE TABLE IF NOT EXISTS evaluations
(
  id INT UNSIGNED AUTO_INCREMENT NOT NULL,
  yes INT UNSIGNED NOT NULL DEFAULT '0',
  `no` INT UNSIGNED NOT NULL DEFAULT '0',
  totalVotes INT UNSIGNED NOT NULL DEFAULT '0',
  owner_id INT UNSIGNED NOT NULL,
  category INT UNSIGNED NOT NULL,
  item_id INT UNSIGNED NOT NULL,
  lastVoter INT UNSIGNED DEFAULT NULL COMMENT 'Dummy column for passing voter id to trigger',
  
  PRIMARY KEY(id),
  FOREIGN KEY (owner_id) REFERENCES gameData(id) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (lastVoter) REFERENCES gameData(id) ON UPDATE CASCADE ON DELETE CASCADE

)ENGINE INNODB;

CREATE TABLE IF NOT EXISTS evaluationCompleded
(
  id INT UNSIGNED NOT NULL,
  player_id INT UNSIGNED NOT NULL,
  
  PRIMARY KEY (id, player_id),
  FOREIGN KEY (id) REFERENCES evaluations(id) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (player_id) REFERENCES gameData(id) ON UPDATE CASCADE ON DELETE CASCADE  
  
)ENGINE INNODB;

DELIMITER //
DROP TRIGGER IF EXISTS addToEvaluation//
CREATE TRIGGER `addToEvaluation` AFTER INSERT ON `formItems`
FOR EACH ROW
BEGIN
  INSERT INTO `evaluations` (`owner_id`, `category`, `item_id`) SELECT parent_id, category, NEW.id FROM forms WHERE id = NEW.parent_id;
END//

DROP FUNCTION IF EXISTS insertOrUpdateGameData//
CREATE FUNCTION insertOrUpdateGameData(in_id INT,in_accessCode VARCHAR(16), in_playerName TEXT, in_totalMoney INT, in_changedTimestamp TEXT)
RETURNS INT
BEGIN
  IF in_id IS NULL THEN
    SELECT id INTO in_id FROM gameData WHERE accessCode = in_accessCode;
  END IF;
  IF in_id IS NULL THEN
    INSERT INTO gameData (`id`,`accessCode`, `playerName`, `totalMoney`, `changedTimestamp`) VALUES(in_id,in_accessCode, in_playerName, in_totalMoney, in_changedTimestamp);
    SELECT `id` INTO in_id FROM gameData WHERE `accessCode` = in_accessCode;
  ELSE
     UPDATE gameData SET `playerName` = in_playerName, `totalMoney` = in_totalMoney, `changedTimestamp` = in_changedTimestamp WHERE `id` = in_id;
  END IF;
  RETURN in_id;
END//

DROP FUNCTION IF EXISTS insertOrUpdateForm//
CREATE FUNCTION insertOrUpdateForm(in_id INT,in_parent_id INT, in_category INT)
RETURNS INT
BEGIN
  IF in_id IS NULL THEN
    SELECT id INTO in_id FROM forms WHERE parent_id = in_parent_id AND category = in_category;
  END IF;
  IF in_id IS NULL THEN
    INSERT INTO forms (`id`,`parent_id`, `category`) VALUES(in_id,in_parent_id, in_category);
    SELECT `id` INTO in_id FROM forms WHERE parent_id = in_parent_id AND category = in_category;
  ELSE
     UPDATE forms SET `category` = in_category WHERE `id` = in_id AND parent_id = in_parent_id;
  END IF;
  RETURN in_id;
END//

DROP FUNCTION IF EXISTS insertOrUpdateFormItem//
CREATE FUNCTION insertOrUpdateFormItem(in_id INT,in_parent_id INT, in_text TEXT, in_count INT)
RETURNS INT
BEGIN
  DECLARE old_id INT;
  SELECT id INTO old_id FROM formItems WHERE parent_id = in_parent_id AND `id` = in_id;
  IF old_id IS NULL THEN
    INSERT INTO formItems (`id`,`parent_id`, `text`, `count`) VALUES(in_id,in_parent_id, in_text, in_count);
    SELECT `id` INTO in_id FROM formItems WHERE parent_id = in_parent_id AND `text` = in_text AND `count` = in_count;
  ELSE
     UPDATE formItems SET `text` = in_text, `count` = in_count WHERE `id` = in_id AND parent_id = in_parent_id;
  END IF;
  RETURN in_parent_id;
END//

DROP PROCEDURE IF EXISTS insertOrUpdateFlower//
CREATE PROCEDURE insertOrUpdateFlower(in_parent_id INT, in_id INT, in_type INT, in_name TEXT, in_water FLOAT, in_mineral FLOAT, in_poop FLOAT, in_state INT, in_experienc INT, in_lastDecay TEXT)
BEGIN
  DECLARE old_id INT;
  SELECT id INTO old_id FROM flowers WHERE parent_id = in_parent_id AND `id` = in_id;
  IF old_id IS NULL THEN
    INSERT INTO flowers (`id`,`parent_id`, `flowerType`, `name`, `waterLevel`, `mineralsLevel`, `fertilizerLevel`, `state`, `expirience`, `lastDecay`)
      VALUES(in_id, in_parent_id, in_type, in_name, in_water, in_mineral, in_poop, in_state, in_experienc, in_lastDecay);
  ELSE
     UPDATE flowers SET flowerType = in_type, `name` = in_name, `waterLevel` = in_water, `mineralsLevel` = in_mineral, `fertilizerLevel` = in_poop, `state` = in_state, `expirience` = in_experienc, `lastDecay` = in_lastDecay
      WHERE `id` = in_id AND parent_id = in_parent_id AND id = in_id;
  END IF;
END//


DROP TRIGGER IF EXISTS onEvalutionCompleted//
CREATE TRIGGER `onEvalutionCompleted` AFTER UPDATE ON `evaluations`
FOR EACH ROW
BEGIN
  INSERT INTO `evaluationCompleded` (`id`, `player_id`) VALUES (NEW.id, NEW.lastVoter);
END//

DELIMITER ;

INSERT INTO access_codes VALUES('test');
-- insert into formItems values(0, 1,"test", 1);

